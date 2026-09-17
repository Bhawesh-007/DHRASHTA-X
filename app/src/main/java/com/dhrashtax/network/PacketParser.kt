package com.dhrashtax.network

import java.net.InetAddress
import kotlin.math.ln

object PacketParser {
    fun parse(data: ByteArray, size: Int = data.size, timestamp: Long = System.currentTimeMillis()): PacketMetadata? {
        if (size <= 0 || size > data.size) return null
        return when (unsigned(data[0]) ushr 4) {
            4 -> parseIpv4(data, size, timestamp)
            6 -> parseIpv6(data, size, timestamp)
            else -> null
        }
    }

    fun entropy(value: String): Double {
        if (value.isEmpty()) return 0.0
        return value.groupingBy { it }.eachCount().values.sumOf { count ->
            val probability = count.toDouble() / value.length
            -probability * (ln(probability) / ln(2.0))
        }
    }

    private fun parseIpv4(data: ByteArray, size: Int, timestamp: Long): PacketMetadata? {
        if (size < 20) return null
        val headerLength = (unsigned(data[0]) and 0x0F) * 4
        if (headerLength < 20 || size < headerLength) return null
        val totalLength = u16(data, 2).coerceAtMost(size)
        if (totalLength < headerLength) return null
        val protocolNumber = unsigned(data[9])
        val source = InetAddress.getByAddress(data.copyOfRange(12, 16)).hostAddress ?: return null
        val destination = InetAddress.getByAddress(data.copyOfRange(16, 20)).hostAddress ?: return null
        return parseTransport(data, totalLength, headerLength, 4, protocolNumber, source, destination, timestamp)
    }

    private fun parseIpv6(data: ByteArray, size: Int, timestamp: Long): PacketMetadata? {
        if (size < 40) return null
        val payloadLength = u16(data, 4)
        val totalLength = (40 + payloadLength).coerceAtMost(size)
        val protocolNumber = unsigned(data[6])
        val source = InetAddress.getByAddress(data.copyOfRange(8, 24)).hostAddress ?: return null
        val destination = InetAddress.getByAddress(data.copyOfRange(24, 40)).hostAddress ?: return null
        return parseTransport(data, totalLength, 40, 6, protocolNumber, source, destination, timestamp)
    }

    private fun parseTransport(
        data: ByteArray,
        totalLength: Int,
        offset: Int,
        ipVersion: Int,
        protocolNumber: Int,
        source: String,
        destination: String,
        timestamp: Long,
    ): PacketMetadata? {
        return when (protocolNumber) {
            6 -> {
                if (totalLength < offset + 20) return null
                PacketMetadata(
                timestamp = timestamp,
                ipVersion = ipVersion,
                protocol = TransportProtocol.TCP,
                sourceAddress = source,
                destinationAddress = destination,
                sourcePort = u16(data, offset),
                destinationPort = u16(data, offset + 2),
                length = totalLength,
                tcpFlags = unsigned(data[offset + 13]),
            )
        }
            17 -> {
                if (totalLength < offset + 8) return null
                val sourcePort = u16(data, offset)
                val destinationPort = u16(data, offset + 2)
                val dnsQuery = if (sourcePort == 53 || destinationPort == 53) {
                    parseDnsQuery(data, offset + 8, totalLength)
                } else null
                PacketMetadata(
                timestamp = timestamp,
                ipVersion = ipVersion,
                protocol = TransportProtocol.UDP,
                sourceAddress = source,
                destinationAddress = destination,
                sourcePort = sourcePort,
                destinationPort = destinationPort,
                length = totalLength,
                dnsQuery = dnsQuery,
            )
        }
            else -> PacketMetadata(
                timestamp = timestamp,
                ipVersion = ipVersion,
                protocol = TransportProtocol.OTHER,
                sourceAddress = source,
                destinationAddress = destination,
                sourcePort = null,
                destinationPort = null,
                length = totalLength,
            )
        }
    }

    private fun parseDnsQuery(data: ByteArray, offset: Int, limit: Int): String? {
        if (limit < offset + 13) return null
        var cursor = offset + 12
        val labels = mutableListOf<String>()
        var totalNameLength = 0
        while (cursor < limit) {
            val labelLength = unsigned(data[cursor++])
            if (labelLength == 0) break
            if (labelLength and 0xC0 != 0 || labelLength > 63 || cursor + labelLength > limit) return null
            totalNameLength += labelLength
            if (totalNameLength > 253) return null
            val label = data.copyOfRange(cursor, cursor + labelLength).toString(Charsets.US_ASCII)
            if (label.any { !it.isLetterOrDigit() && it != '-' && it != '_' }) return null
            labels += label
            cursor += labelLength
        }
        return labels.takeIf { it.isNotEmpty() }?.joinToString(".")
    }

    private fun unsigned(value: Byte): Int = value.toInt() and 0xFF
    private fun u16(data: ByteArray, offset: Int): Int = unsigned(data[offset]) shl 8 or unsigned(data[offset + 1])
}
