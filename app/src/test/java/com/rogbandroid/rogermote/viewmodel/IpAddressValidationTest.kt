package com.rogbandroid.rogermote.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IpAddressValidationTest {
    @Test
    fun acceptsValidIpv4Address() {
        assertTrue("192.168.1.50".isValidIpv4Address())
        assertTrue("10.0.0.1".isValidIpv4Address())
    }

    @Test
    fun rejectsMalformedOrOutOfRangeAddress() {
        assertFalse("192.168.1".isValidIpv4Address())
        assertFalse("192.168.1.256".isValidIpv4Address())
        assertFalse("samsung.local".isValidIpv4Address())
        assertFalse("192.168..50".isValidIpv4Address())
    }
}
