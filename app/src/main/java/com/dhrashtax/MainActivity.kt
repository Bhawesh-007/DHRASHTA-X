package com.dhrashtax

import android.Manifest
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.dhrashtax.ui.DhrashtaApp
import com.dhrashtax.ui.theme.DhrashtaTheme
import com.dhrashtax.vpn.DhrashtaVpnService

class MainActivity : ComponentActivity() {
    private val vpnPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) startObserverService()
    }

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            DhrashtaTheme {
                DhrashtaApp(
                    onStartObserver = ::requestVpnAndStart,
                    onStopObserver = ::stopObserverService,
                )
            }
        }
    }

    private fun requestVpnAndStart() {
        val permissionIntent = VpnService.prepare(this)
        if (permissionIntent == null) startObserverService() else vpnPermission.launch(permissionIntent)
    }

    private fun startObserverService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, DhrashtaVpnService::class.java).setAction(DhrashtaVpnService.ACTION_START),
        )
    }

    private fun stopObserverService() {
        startService(Intent(this, DhrashtaVpnService::class.java).setAction(DhrashtaVpnService.ACTION_STOP))
    }
}

