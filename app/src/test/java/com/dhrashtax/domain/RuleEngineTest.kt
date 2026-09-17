package com.dhrashtax.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {
    private val engine = RuleEngine()

    @Test
    fun benignSnapshotProducesNoVerdict() {
        val result = engine.evaluate(
            TrafficSnapshot(
                packageName = "com.example.browser",
                uploadBytes = 40_000,
                downloadBytes = 800_000,
                connectionCount = 4,
                uniqueDestinations = 4,
                uniqueDestinationPorts = 2,
            ),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun uploadAsymmetryProducesExfiltrationEvidence() {
        val result = engine.evaluate(
            TrafficSnapshot(
                packageName = "com.example.suspicious",
                uploadBytes = 2_000_000,
                downloadBytes = 100_000,
            ),
        )
        assertEquals(ThreatClass.EXFILTRATION, result.single().threatClass)
        assertTrue(result.single().evidence.any { it.contains("ratio") })
    }

    @Test
    fun periodicConnectionsProduceBeaconVerdict() {
        val result = engine.evaluate(
            TrafficSnapshot(
                packageName = "com.example.beacon",
                connectionCount = 10,
                uniqueDestinations = 1,
                beaconIntervalMeanSeconds = 15.0,
                beaconIntervalCv = 0.05,
            ),
        )
        assertEquals(ThreatClass.PERIODIC_BEACON, result.single().threatClass)
    }

    @Test
    fun sideloadedAccessibilityAppProducesExplicitRuleVerdict() {
        val result = engine.evaluate(
            TrafficSnapshot(packageName = "com.example.side", uploadBytes = 200_000),
            DeviceRiskContext(sideloaded = true, accessibilityEnabled = true),
        )
        assertEquals(ThreatClass.SPYWARE_SUSPECT, result.single().threatClass)
        assertEquals(0.95f, result.single().confidence)
    }
}

