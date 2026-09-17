package com.dhrashtax.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.os.Process
import android.net.TrafficStats
import androidx.core.app.NotificationCompat
import com.dhrashtax.DhrashtaApplication
import com.dhrashtax.MainActivity
import com.dhrashtax.R
import com.dhrashtax.alerts.AlertNotifier
import com.dhrashtax.context.ContextCollector
import com.dhrashtax.domain.ThreatClass
import com.dhrashtax.network.FlowAggregator
import com.dhrashtax.network.PacketParser
import com.dhrashtax.network.UidResolver
import java.io.FileInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DhrashtaVpnService : VpnService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var interfaceDescriptor: ParcelFileDescriptor? = null
    private var packetJob: Job? = null
    private var egressJob: Job? = null
    private var analysisJob: Job? = null
    private val flowAggregator = FlowAggregator()
    private val lastAlertAt = mutableMapOf<Pair<String, ThreatClass>, Long>()
    private lateinit var uidResolver: UidResolver
    private lateinit var contextCollector: ContextCollector
    private lateinit var notifier: AlertNotifier

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        uidResolver = UidResolver(this)
        contextCollector = ContextCollector(this)
        notifier = AlertNotifier(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopObserver()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, foregroundNotification())
        startObserver()
        return START_STICKY
    }

    override fun onRevoke() {
        stopObserver()
        super.onRevoke()
    }

    override fun onDestroy() {
        packetJob?.cancel()
        egressJob?.cancel()
        analysisJob?.cancel()
        interfaceDescriptor?.close()
        notifier.close()
        serviceScope.cancel()
        MonitorStateStore.reset()
        super.onDestroy()
    }

    private fun startObserver() {
        if (interfaceDescriptor != null) return
        MonitorStateStore.update {
            it.copy(status = MonitorStatus.STARTING, message = "Starting safe observer")
        }
        interfaceDescriptor = runCatching {
            Builder()
                .setSession(getString(R.string.app_name))
                .setMtu(1500)
                .setBlocking(false)
                .addAddress("10.111.0.1", 32)
                .addAddress("fd00:111::1", 128)
                .addDisallowedApplication(packageName)
                // Deliberately no default route: traffic is not intercepted until a
                // production user-space forwarder is integrated and tested.
                .establish()
        }.getOrElse { error ->
            MonitorStateStore.update {
                it.copy(status = MonitorStatus.ERROR, message = error.message ?: "Could not start VPN observer")
            }
            stopSelf()
            return
        }
        val descriptor = interfaceDescriptor ?: return
        val initialTx = TrafficStats.getUidTxBytes(Process.myUid()).coerceAtLeast(0)
        MonitorStateStore.update {
            MonitorState(
                status = MonitorStatus.OBSERVER_ACTIVE,
                startedAt = System.currentTimeMillis(),
                captureOperational = false,
                message = "Observer active; full traffic routing is intentionally disabled",
            )
        }
        packetJob = serviceScope.launch {
            val input = FileInputStream(descriptor.fileDescriptor)
            val buffer = ByteArray(32_767)
            while (isActive) {
                val count = runCatching { input.read(buffer) }.getOrDefault(0)
                val packet = if (count > 0) PacketParser.parse(buffer, count) else null
                if (packet != null) {
                    val outbound = packet.sourceAddress == "10.111.0.1" || packet.sourceAddress == "fd00:111:0:0:0:0:0:1"
                    val packageName = uidResolver.resolve(packet, outbound)
                    flowAggregator.record(packageName, packet, outbound)
                    MonitorStateStore.update { it.copy(packetsObserved = it.packetsObserved + 1) }
                } else {
                    delay(100)
                }
            }
        }
        egressJob = serviceScope.launch {
            while (isActive) {
                val current = TrafficStats.getUidTxBytes(Process.myUid()).coerceAtLeast(0)
                MonitorStateStore.update { it.copy(ownEgressBytes = (current - initialTx).coerceAtLeast(0)) }
                delay(2_000)
            }
        }
        analysisJob = serviceScope.launch {
            val container = (application as DhrashtaApplication).container
            while (isActive) {
                delay(WINDOW_MILLIS)
                val settings = container.settings.settings.value
                flowAggregator.flush().forEach { snapshot ->
                    if (snapshot.packageName in settings.trustedPackages) return@forEach
                    val context = runCatching { contextCollector.collect(snapshot.packageName) }.getOrDefault(
                        com.dhrashtax.domain.DeviceRiskContext(),
                    )
                    container.ruleEngine.evaluate(snapshot, context).forEach { verdict ->
                        val key = verdict.packageName to verdict.threatClass
                        val last = lastAlertAt[key] ?: 0L
                        if (verdict.timestamp - last >= ALERT_COOLDOWN_MILLIS) {
                            lastAlertAt[key] = verdict.timestamp
                            val id = container.alerts.save(verdict)
                            runCatching { notifier.notify(id, verdict, settings.voiceAlerts) }
                        }
                    }
                }
            }
        }
    }

    private fun stopObserver() {
        packetJob?.cancel()
        egressJob?.cancel()
        analysisJob?.cancel()
        interfaceDescriptor?.close()
        interfaceDescriptor = null
        MonitorStateStore.reset()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                MONITOR_CHANNEL,
                getString(R.string.monitor_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    private fun foregroundNotification() = NotificationCompat.Builder(this, MONITOR_CHANNEL)
        .setSmallIcon(android.R.drawable.ic_lock_lock)
        .setContentTitle(getString(R.string.monitor_notification_title))
        .setContentText("Safe observer mode; no default route is installed")
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
        .addAction(
            0,
            "Stop",
            PendingIntent.getService(
                this,
                1,
                Intent(this, DhrashtaVpnService::class.java).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
        .build()

    companion object {
        const val ACTION_START = "com.dhrashtax.action.START"
        const val ACTION_STOP = "com.dhrashtax.action.STOP"
        private const val MONITOR_CHANNEL = "monitor"
        private const val NOTIFICATION_ID = 1001
        private const val WINDOW_MILLIS = 5_000L
        private const val ALERT_COOLDOWN_MILLIS = 60_000L
    }
}
