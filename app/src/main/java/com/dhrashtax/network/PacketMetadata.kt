package com.dhrashtax.network

data class PacketMetadata(
    val timestamp: Long,
    val ipVersion: Int,
    val protocol: TransportProtocol,
    val sourceAddress: String,
    val destinationAddress: String,
    val sourcePort: Int?,
    val destinationPort: Int?,
    val length: Int,
    val tcpFlags: Int = 0,
    val dnsQuery: String? = null,
) {
    val isTcpSyn: Boolean get() = protocol == TransportProtocol.TCP && tcpFlags and 0x02 != 0
}

enum class TransportProtocol { TCP, UDP, OTHER }

