package com.dhrashtax.domain

import kotlin.math.max

class RuleEngine {
    fun evaluate(
        snapshot: TrafficSnapshot,
        context: DeviceRiskContext = DeviceRiskContext(),
    ): List<RuleVerdict> {
        val verdicts = mutableListOf<RuleVerdict>()
        val downloadFloor = max(snapshot.downloadBytes, 1L)
        val uploadRatio = snapshot.uploadBytes.toDouble() / downloadFloor

        if (snapshot.uploadBytes >= 1_000_000 && uploadRatio >= 4.0) {
            verdicts += verdict(
                snapshot,
                ThreatClass.EXFILTRATION,
                Severity.CRITICAL,
                0.90f,
                "Unusually asymmetric outbound transfer",
                "Uploaded ${snapshot.uploadBytes} bytes",
                "Upload/download ratio ${"%.1f".format(uploadRatio)}x",
            )
        }

        if (
            snapshot.connectionCount >= 8 &&
            snapshot.uniqueDestinations <= 2 &&
            snapshot.beaconIntervalMeanSeconds in 2.0..120.0 &&
            snapshot.beaconIntervalCv <= 0.15
        ) {
            verdicts += verdict(
                snapshot,
                ThreatClass.PERIODIC_BEACON,
                Severity.HIGH,
                0.88f,
                "Highly regular connection pattern",
                "${snapshot.connectionCount} connections to ${snapshot.uniqueDestinations} destination(s)",
                "Mean interval ${"%.1f".format(snapshot.beaconIntervalMeanSeconds)} s",
                "Interval variation ${"%.3f".format(snapshot.beaconIntervalCv)}",
            )
        }

        if (
            snapshot.dnsQueryCount >= 8 &&
            snapshot.dnsUniqueQueries >= 8 &&
            snapshot.dnsMaxNameLength >= 45 &&
            snapshot.dnsMaxEntropy >= 3.5
        ) {
            verdicts += verdict(
                snapshot,
                ThreatClass.DNS_TUNNEL,
                Severity.HIGH,
                0.86f,
                "DNS traffic resembles encoded data",
                "${snapshot.dnsUniqueQueries} unique queries",
                "Longest query ${snapshot.dnsMaxNameLength} characters",
                "Maximum entropy ${"%.2f".format(snapshot.dnsMaxEntropy)} bits",
            )
        }

        if (snapshot.uniqueDestinations >= 20 || snapshot.uniqueDestinationPorts >= 15) {
            verdicts += verdict(
                snapshot,
                ThreatClass.NETWORK_SCANNER,
                Severity.MEDIUM,
                0.82f,
                "Rapid network enumeration detected",
                "${snapshot.uniqueDestinations} destinations",
                "${snapshot.uniqueDestinationPorts} destination ports",
            )
        }

        if (
            context.sideloaded &&
            (context.accessibilityEnabled || context.notificationListenerEnabled) &&
            snapshot.uploadBytes >= 100_000
        ) {
            verdicts += verdict(
                snapshot,
                ThreatClass.SPYWARE_SUSPECT,
                Severity.CRITICAL,
                0.95f,
                "Risky app privileges combined with outbound activity",
                "Application was installed outside the trusted app store",
                "Sensitive service access is enabled",
                "Uploaded ${snapshot.uploadBytes} bytes",
            )
        }

        return verdicts
    }

    private fun verdict(
        snapshot: TrafficSnapshot,
        threatClass: ThreatClass,
        severity: Severity,
        confidence: Float,
        summary: String,
        vararg evidence: String,
    ) = RuleVerdict(
        timestamp = snapshot.timestamp,
        packageName = snapshot.packageName,
        threatClass = threatClass,
        severity = severity,
        confidence = confidence,
        summary = summary,
        evidence = evidence.toList(),
    )
}

