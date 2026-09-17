package com.dhrashtax.network

import android.content.Context
import android.net.ConnectivityManager
import android.os.Process
import java.net.InetSocketAddress

class UidResolver(context: Context) {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    private val packageManager = context.packageManager

    fun resolve(packet: PacketMetadata, outbound: Boolean): String {
        val protocol = when (packet.protocol) {
            TransportProtocol.TCP -> 6
            TransportProtocol.UDP -> 17
            TransportProtocol.OTHER -> return UNKNOWN_PACKAGE
        }
        val sourcePort = packet.sourcePort ?: return UNKNOWN_PACKAGE
        val destinationPort = packet.destinationPort ?: return UNKNOWN_PACKAGE
        val local = if (outbound) {
            InetSocketAddress(packet.sourceAddress, sourcePort)
        } else {
            InetSocketAddress(packet.destinationAddress, destinationPort)
        }
        val remote = if (outbound) {
            InetSocketAddress(packet.destinationAddress, destinationPort)
        } else {
            InetSocketAddress(packet.sourceAddress, sourcePort)
        }
        val uid = runCatching { connectivityManager.getConnectionOwnerUid(protocol, local, remote) }
            .getOrDefault(Process.INVALID_UID)
        if (uid == Process.INVALID_UID) return UNKNOWN_PACKAGE
        return runCatching { packageManager.getPackagesForUid(uid)?.sorted()?.firstOrNull() }
            .getOrNull()
            ?: "uid:$uid"
    }

    private companion object { const val UNKNOWN_PACKAGE = "unknown.uid" }
}
