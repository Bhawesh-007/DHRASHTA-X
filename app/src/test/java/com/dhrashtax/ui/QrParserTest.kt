package com.dhrashtax.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrParserTest {
    @Test
    fun acceptsRawPackageName() {
        assertEquals("com.example.app", packageNameFromQr("com.example.app"))
    }

    @Test
    fun acceptsDhrashtaTrustUri() {
        assertEquals(
            "com.example.app",
            packageNameFromQr("dhrashta://trust?package=com.example.app"),
        )
    }

    @Test
    fun rejectsArbitraryQrText() {
        assertNull(packageNameFromQr("https://example.com"))
    }
}

