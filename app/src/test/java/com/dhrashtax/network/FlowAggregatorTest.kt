package com.dhrashtax.network

import org.junit.Assert.assertEquals
import org.junit.Test

class FlowAggregatorTest {
    @Test
    fun computesStableBeaconIntervalsAndResetsWindow() {
        val aggregator = FlowAggregator()
        repeat(8) { index ->
            aggregator.record(
                packageName = "com.example.beacon",
                packet = PacketMetadata(
                    timestamp = index * 10_000L,
                    ipVersion = 4,
                    protocol = TransportProtocol.TCP,
                    sourceAddress = "10.0.0.1",
                    destinationAddress = "203.0.113.1",
                    sourcePort = 50_000 + index,
                    destinationPort = 443,
                    length = 60,
                    tcpFlags = 0x02,
                ),
                outbound = true,
            )
        }

        val snapshot = aggregator.flush().single()
        assertEquals(8, snapshot.connectionCount)
        assertEquals(1, snapshot.uniqueDestinations)
        assertEquals(10.0, snapshot.beaconIntervalMeanSeconds, 0.0001)
        assertEquals(0.0, snapshot.beaconIntervalCv, 0.0001)
        assertEquals(0, aggregator.flush().size)
    }
}

