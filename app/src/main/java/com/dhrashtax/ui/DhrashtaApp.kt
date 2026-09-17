package com.dhrashtax.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dhrashtax.data.AlertEntity
import com.dhrashtax.vpn.MonitorStatus
import java.text.DateFormat
import java.util.Date

private object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val ALERT = "alert/{id}"
    const val SETTINGS = "settings"
    const val PRIVACY = "privacy"
    const val SCANNER = "scanner"
}

@Composable
fun DhrashtaApp(
    onStartObserver: () -> Unit,
    onStopObserver: () -> Unit,
    viewModel: AppViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val startRoute = if (state.settings.onboardingComplete) Routes.DASHBOARD else Routes.ONBOARDING

    NavHost(navController = navController, startDestination = startRoute) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen {
                viewModel.completeOnboarding()
                navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
            }
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                state = state,
                onStart = onStartObserver,
                onStop = onStopObserver,
                onDemo = viewModel::runSafeDemo,
                onAlert = { navController.navigate("alert/$it") },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onPrivacy = { navController.navigate(Routes.PRIVACY) },
            )
        }
        composable(Routes.ALERT, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            AlertDetailScreen(
                alert = state.alerts.firstOrNull { it.id == id },
                onBack = navController::popBackStack,
                onAcknowledge = { viewModel.acknowledge(id) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                state = state,
                onBack = navController::popBackStack,
                onVoiceChanged = viewModel::setVoiceAlerts,
                onClear = viewModel::clearAlerts,
                onTrust = viewModel::trustPackage,
                onRemoveTrusted = viewModel::removeTrustedPackage,
                onScan = { navController.navigate(Routes.SCANNER) },
            )
        }
        composable(Routes.PRIVACY) {
            PrivacyScreen(state.monitor.ownEgressBytes, navController::popBackStack)
        }
        composable(Routes.SCANNER) {
            QrScannerScreen(
                onPackageScanned = {
                    viewModel.trustPackage(it)
                    navController.popBackStack()
                },
                onBack = navController::popBackStack,
            )
        }
    }
}

@Composable
private fun OnboardingScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("DHRASHTA-X", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("On-device mobile threat defense", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(28.dp))
        Text("Private by design", style = MaterialTheme.typography.titleLarge)
        Text("Alerts and evidence stay in the local database. No analytics or telemetry SDK is included.")
        Spacer(Modifier.height(18.dp))
        Text("Heuristic edition", style = MaterialTheme.typography.titleLarge)
        Text("The ML model is intentionally disabled. Current detections come only from explicit, inspectable rules.")
        Spacer(Modifier.height(18.dp))
        Text("Safe observer", style = MaterialTheme.typography.titleLarge)
        Text("The VPN creates no default route until a production packet forwarder is integrated, so it cannot block normal traffic.")
        Spacer(Modifier.height(32.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("I understand — continue") }
    }
}

@Composable
private fun DashboardScreen(
    state: AppUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDemo: () -> Unit,
    onAlert: (Long) -> Unit,
    onSettings: () -> Unit,
    onPrivacy: () -> Unit,
) {
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("DHRASHTA-X", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Rule-based protection", color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = onSettings) { Text("Settings") }
                }
            }
            item { MonitorCard(state, onStart, onStop) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDemo, modifier = Modifier.weight(1f)) { Text("Run safe demo") }
                    OutlinedButton(onClick = onPrivacy, modifier = Modifier.weight(1f)) { Text("Privacy proof") }
                }
            }
            item { Text("Recent alerts", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp)) }
            if (state.alerts.isEmpty()) {
                item { EmptyAlerts() }
            } else {
                items(state.alerts, key = { it.id }) { alert -> AlertRow(alert) { onAlert(alert.id) } }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MonitorCard(state: AppUiState, onStart: () -> Unit, onStop: () -> Unit) {
    val active = state.monitor.status == MonitorStatus.OBSERVER_ACTIVE
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.background(if (active) Color(0xFF22C55E) else Color(0xFF64748B), RoundedCornerShape(50))
                        .width(12.dp).height(12.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(if (active) "Observer active" else "Observer stopped", style = MaterialTheme.typography.titleMedium)
            }
            Text(state.monitor.message, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
            Text("ML model: not installed", color = Color(0xFFF59E0B), modifier = Modifier.padding(top = 8.dp))
            Text("Full packet capture: disabled", color = Color(0xFFF59E0B))
            Spacer(Modifier.height(14.dp))
            Button(onClick = if (active) onStop else onStart, modifier = Modifier.fillMaxWidth()) {
                Text(if (active) "Stop observer" else "Start safe observer")
            }
        }
    }
}

@Composable
private fun EmptyAlerts() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No alerts recorded", style = MaterialTheme.typography.titleMedium)
            Text(
                "Use the safe demo to verify the local rule → database → notification pipeline.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun AlertRow(alert: AlertEntity, onClick: () -> Unit) {
    val severityColor = when (alert.severity) {
        "CRITICAL" -> Color(0xFFEF4444)
        "HIGH" -> Color(0xFFF97316)
        else -> Color(0xFFEAB308)
    }
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.background(severityColor, RoundedCornerShape(4.dp)).width(5.dp).height(56.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(alert.threatClass.replace('_', ' '), fontWeight = FontWeight.Bold)
                Text(alert.packageName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(alert.timestamp)),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Text("${(alert.confidence * 100).toInt()}%", color = severityColor)
        }
    }
}

@Composable
private fun AlertDetailScreen(alert: AlertEntity?, onBack: () -> Unit, onAcknowledge: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        if (alert == null) {
            Text("Alert not found")
            return@Column
        }
        Text(alert.threatClass.replace('_', ' '), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(alert.packageName, color = MaterialTheme.colorScheme.primary)
        Text("${alert.severity} · ${(alert.confidence * 100).toInt()}% rule confidence", modifier = Modifier.padding(vertical = 12.dp))
        HorizontalDivider()
        Text(alert.summary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 18.dp))
        Text("Evidence", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 22.dp))
        alert.evidence.lines().filter { it.isNotBlank() }.forEach { Text("• $it", modifier = Modifier.padding(top = 6.dp)) }
        Spacer(Modifier.height(22.dp))
        Text("Detector: ${alert.detector}. This is not an ML verdict.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        Spacer(Modifier.height(18.dp))
        Button(onClick = onAcknowledge, enabled = !alert.acknowledged) {
            Text(if (alert.acknowledged) "Acknowledged" else "Acknowledge")
        }
    }
}

@Composable
private fun SettingsScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onVoiceChanged: (Boolean) -> Unit,
    onClear: () -> Unit,
    onTrust: (String) -> Unit,
    onRemoveTrusted: (String) -> Unit,
    onScan: () -> Unit,
) {
    var packageName by remember { mutableStateOf("") }
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onClick = onBack) { Text("← Back") } }
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Voice alerts", style = MaterialTheme.typography.titleMedium)
                    Text("Speak the threat type after a rule fires")
                }
                Switch(checked = state.settings.voiceAlerts, onCheckedChange = onVoiceChanged)
            }
        }
        item {
            OutlinedButton(
                onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Open usage-access settings") }
        }
        item { HorizontalDivider() }
        item { Text("Trusted packages", style = MaterialTheme.typography.titleLarge) }
        item {
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("com.example.app") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { onTrust(packageName); packageName = "" }, modifier = Modifier.weight(1f)) { Text("Add") }
                OutlinedButton(onClick = onScan, modifier = Modifier.weight(1f)) { Text("Scan QR") }
            }
        }
        items(state.settings.trustedPackages.sorted()) { packageItem ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(packageItem, modifier = Modifier.weight(1f))
                TextButton(onClick = { onRemoveTrusted(packageItem) }) { Text("Remove") }
            }
        }
        item { HorizontalDivider() }
        item { OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Clear local alert history") } }
        item { Text("Model integration is disabled until verified artifacts are supplied.", color = Color(0xFFF59E0B)) }
    }
}

@Composable
private fun PrivacyScreen(ownEgressBytes: Long, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Privacy proof", style = MaterialTheme.typography.headlineMedium)
        Text("App egress since observer start", modifier = Modifier.padding(top = 30.dp))
        Text("$ownEgressBytes bytes", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "This counter measures bytes attributed to DHRASHTA-X itself. Android and other apps are not included.",
            modifier = Modifier.padding(top = 12.dp),
        )
        Spacer(Modifier.height(24.dp))
        Text("Local data", style = MaterialTheme.typography.titleLarge)
        Text("Only alert metadata, rule evidence, preferences and trusted package names are stored. Packet payloads are never persisted.")
        Spacer(Modifier.height(18.dp))
        Text("No telemetry", style = MaterialTheme.typography.titleLarge)
        Text("The application contains no analytics client or configured backend endpoint.")
    }
}
