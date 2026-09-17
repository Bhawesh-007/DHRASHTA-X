package com.dhrashtax.network

import com.dhrashtax.domain.TrafficSnapshot
import kotlin.math.sqrt

class FlowAggregator {
    private data class Accumulator(
        var uploadBytes: Long = 0,
        var downloadBytes: Long = 0,
        var connectionCount: Int = 0,
        val destinations: MutableSet<String> = linkedSetOf(),
        val destinationPorts: MutableSet<Int> = linkedSetOf(),
        val connectionTimes: MutableList<Long> = mutableListOf(),
        val dnsQueries: MutableSet<String> = linkedSetOf(),
        var dnsQueryCount: Int = 0,
        var dnsMaxNameLength: Int = 0,
        var dnsMaxEntropy: Double = 0.0,
    )

    private val packages = linkedMapOf<String, Accumulator>()

    @Synchronized
    fun record(packageName: String, packet: PacketMetadata, outbound: Boolean) {
        val accumulator = packages.getOrPut(packageName) { Accumulator() }
        if (outbound) {
            accumulator.uploadBytes += packet.length
            accumulator.destinations.addBounded(packet.destinationAddress)
            packet.destinationPort?.let { accumulator.destinationPorts.addBounded(it) }
        } else {
            accumulator.downloadBytes += packet.length
        }
        if (packet.isTcpSyn && outbound) {
            accumulator.connectionCount++
            accumulator.connectionTimes.addBounded(packet.timestamp)
        }
        packet.dnsQuery?.let { query ->
            accumulator.dnsQueryCount++
            accumulator.dnsQueries.addBounded(query)
            accumulator.dnsMaxNameLength = maxOf(accumulator.dnsMaxNameLength, query.length)
            accumulator.dnsMaxEntropy = maxOf(accumulator.dnsMaxEntropy, PacketParser.entropy(query))
        }
    }

    @Synchronized
    fun flush(timestamp: Long = System.currentTimeMillis()): List<TrafficSnapshot> {
        val snapshots = packages.map { (packageName, value) ->
            val intervals = value.connectionTimes.zipWithNext { first, second -> (second - first) / 1_000.0 }
            val mean = intervals.averageOrZero()
            val cv = if (mean > 0.0) intervals.standardDeviation(mean) / mean else 1.0
            TrafficSnapshot(
                timestamp = timestamp,
                packageName = packageName,
                uploadBytes = value.uploadBytes,
                downloadBytes = value.downloadBytes,
                connectionCount = value.connectionCount,
                uniqueDestinations = value.destinations.size,
                uniqueDestinationPorts = value.destinationPorts.size,
                beaconIntervalMeanSeconds = mean,
                beaconIntervalCv = cv,
                dnsQueryCount = value.dnsQueryCount,
                dnsUniqueQueries = value.dnsQueries.size,
                dnsMaxNameLength = value.dnsMaxNameLength,
                dnsMaxEntropy = value.dnsMaxEntropy,
            )
        }
        packages.clear()
        return snapshots
    }

    private fun Collection<Double>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()
    private fun Collection<Double>.standardDeviation(mean: Double): Double =
        if (isEmpty()) 0.0 else sqrt(sumOf { (it - mean) * (it - mean) } / size)

    private fun <T> MutableCollection<T>.addBounded(value: T) {
        if (size < MAX_CARDINALITY) add(value)
    }

    private companion object { const val MAX_CARDINALITY = 256 }
}
