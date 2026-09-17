package com.dhrashtax.domain

enum class ThreatClass {
    PERIODIC_BEACON,
    EXFILTRATION,
    DNS_TUNNEL,
    NETWORK_SCANNER,
    SPYWARE_SUSPECT,
}

enum class Severity { MEDIUM, HIGH, CRITICAL }

data class DeviceRiskContext(
    val sideloaded: Boolean = false,
    val accessibilityEnabled: Boolean = false,
    val notificationListenerEnabled: Boolean = false,
    val deviceAdminEnabled: Boolean = false,
    val overlayAllowed: Boolean = false,
)

data class TrafficSnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String,
    val uploadBytes: Long = 0,
    val downloadBytes: Long = 0,
    val connectionCount: Int = 0,
    val uniqueDestinations: Int = 0,
    val uniqueDestinationPorts: Int = 0,
    val beaconIntervalMeanSeconds: Double = 0.0,
    val beaconIntervalCv: Double = 1.0,
    val dnsQueryCount: Int = 0,
    val dnsUniqueQueries: Int = 0,
    val dnsMaxNameLength: Int = 0,
    val dnsMaxEntropy: Double = 0.0,
)

data class RuleVerdict(
    val timestamp: Long,
    val packageName: String,
    val threatClass: ThreatClass,
    val severity: Severity,
    val confidence: Float,
    val summary: String,
    val evidence: List<String>,
)

