package com.dhrashtax.domain

object SafeDemo {
    fun snapshots(now: Long = System.currentTimeMillis()): List<Pair<TrafficSnapshot, DeviceRiskContext>> = listOf(
        TrafficSnapshot(
            timestamp = now,
            packageName = "demo.beacon.client",
            uploadBytes = 24_000,
            downloadBytes = 9_000,
            connectionCount = 12,
            uniqueDestinations = 1,
            uniqueDestinationPorts = 1,
            beaconIntervalMeanSeconds = 15.0,
            beaconIntervalCv = 0.04,
        ) to DeviceRiskContext(),
        TrafficSnapshot(
            timestamp = now + 1,
            packageName = "demo.sideloaded.app",
            uploadBytes = 2_400_000,
            downloadBytes = 120_000,
            connectionCount = 5,
            uniqueDestinations = 3,
        ) to DeviceRiskContext(sideloaded = true, accessibilityEnabled = true),
        TrafficSnapshot(
            timestamp = now + 2,
            packageName = "demo.dns.client",
            dnsQueryCount = 14,
            dnsUniqueQueries = 14,
            dnsMaxNameLength = 61,
            dnsMaxEntropy = 4.2,
        ) to DeviceRiskContext(),
    )
}

