package com.dhrashtax.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhrashtax.DhrashtaApplication
import com.dhrashtax.alerts.AlertNotifier
import com.dhrashtax.data.AlertEntity
import com.dhrashtax.data.AppSettings
import com.dhrashtax.domain.SafeDemo
import com.dhrashtax.vpn.MonitorState
import com.dhrashtax.vpn.MonitorStateStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(
    val alerts: List<AlertEntity> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val monitor: MonitorState = MonitorState(),
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as DhrashtaApplication).container
    private val notifier = AlertNotifier(application)

    val uiState: StateFlow<AppUiState> = combine(
        container.alerts.alerts,
        container.settings.settings,
        MonitorStateStore.state,
    ) { alerts, settings, monitor ->
        AppUiState(alerts = alerts, settings = settings, monitor = monitor)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppUiState(
            settings = container.settings.settings.value,
            monitor = MonitorStateStore.state.value,
        ),
    )

    fun completeOnboarding() = container.settings.completeOnboarding()

    fun runSafeDemo() {
        viewModelScope.launch {
            val settings = container.settings.settings.value
            SafeDemo.snapshots().forEach { (snapshot, context) ->
                if (snapshot.packageName !in settings.trustedPackages) {
                    container.ruleEngine.evaluate(snapshot, context).forEach { verdict ->
                        val id = container.alerts.save(verdict)
                        runCatching { notifier.notify(id, verdict, settings.voiceAlerts) }
                    }
                }
            }
        }
    }

    fun acknowledge(id: Long) = viewModelScope.launch { container.alerts.acknowledge(id) }
    fun clearAlerts() = viewModelScope.launch { container.alerts.clear() }
    fun setVoiceAlerts(enabled: Boolean) = container.settings.setVoiceAlerts(enabled)
    fun trustPackage(packageName: String) = container.settings.trustPackage(packageName)
    fun removeTrustedPackage(packageName: String) = container.settings.removeTrustedPackage(packageName)

    override fun onCleared() {
        notifier.close()
        super.onCleared()
    }
}
