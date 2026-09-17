package com.dhrashtax.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PacketParserTest {
    @Test
    fun parsesIpv4TcpSyn() {
        val packet = ByteArray(40)
        packet[0] = 0x45
        put16(packet, 2, packet.size)
        packet[9] = 6
        putIpv4(packet, 12, 10, 0, 0, 1)
        putIpv4(packet, 16, 8, 8, 8, 8)
        put16(packet, 20, 12_345)
        put16(packet, 22, 443)
        packet[32] = 0x50
        packet[33] = 0x02

        val parsed = PacketParser.parse(packet)!!

        assertEquals(TransportProtocol.TCP, parsed.protocol)
        assertEquals("10.0.0.1", parsed.sourceAddress)
        assertEquals("8.8.8.8", parsed.destinationAddress)
        assertEquals(12_345, parsed.sourcePort)
        assertEquals(443, parsed.destinationPort)
        assertTrue(parsed.isTcpSyn)
    }

    @Test
    fun parsesDnsQueryWithoutRetainingPayload() {
        val name = listOf("abc123", "example", "com")
        val encodedNameSize = name.sumOf { it.length + 1 } + 1
        val packet = ByteArray(20 + 8 + 12 + encodedNameSize + 4)
        packet[0] = 0x45
        put16(packet, 2, packet.size)
        packet[9] = 17
        putIpv4(packet, 12, 10, 0, 0, 2)
        putIpv4(packet, 16, 1, 1, 1, 1)
        put16(packet, 20, 53_000)
        put16(packet, 22, 53)
        put16(packet, 24, packet.size - 20)
        var cursor = 20 + 8 + 12
        name.forEach { label ->
            packet[cursor++] = label.length.toByte()
            label.toByteArray().copyInto(packet, cursor)
            cursor += label.length
        }
        packet[cursor] = 0

        val parsed = PacketParser.parse(packet)!!

        assertEquals(TransportProtocol.UDP, parsed.protocol)
        assertEquals("abc123.example.com", parsed.dnsQuery)
    }

    @Test
    fun rejectsTruncatedPackets() {
        assertNull(PacketParser.parse(byteArrayOf(0x45)))
        assertNull(PacketParser.parse(ByteArray(20).also { it[0] = 0x65 }))
    }

    @Test
    fun entropyIsZeroForRepeatedCharacters() {
        assertEquals(0.0, PacketParser.entropy("aaaaaaaa"), 0.00001)
        assertTrue(PacketParser.entropy("a1b2c3d4") > 2.5)
    }

    private fun put16(target: ByteArray, offset: Int, value: Int) {
        target[offset] = (value ushr 8).toByte()
        target[offset + 1] = value.toByte()
    }

    private fun putIpv4(target: ByteArray, offset: Int, a: Int, b: Int, c: Int, d: Int) {
        target[offset] = a.toByte()
        target[offset + 1] = b.toByte()
        target[offset + 2] = c.toByte()
        target[offset + 3] = d.toByte()
    }
}

