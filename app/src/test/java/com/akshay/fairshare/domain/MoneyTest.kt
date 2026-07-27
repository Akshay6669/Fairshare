package com.akshay.fairshare.domain

import com.akshay.fairshare.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun `parses whole amounts`() {
        assertEquals(Money(1200), Money.parse("12"))
    }

    @Test
    fun `parses one and two decimal places`() {
        assertEquals(Money(1230), Money.parse("12.3"))
        assertEquals(Money(1234), Money.parse("12.34"))
    }

    @Test
    fun `parses a trailing decimal point as whole units`() {
        assertEquals(Money(1200), Money.parse("12."))
    }

    @Test
    fun `rejects malformed input instead of throwing`() {
        assertNull(Money.parse(""))
        assertNull(Money.parse("abc"))
        assertNull(Money.parse("12.345"))
        assertNull(Money.parse("1,200"))
        assertNull(Money.parse("$12"))
    }

    @Test
    fun `formats with two decimal places always`() {
        assertEquals("12.00", Money(1200).format())
        assertEquals("0.05", Money(5).format())
        assertEquals("-0.05", Money(-5).format())
        assertEquals("1234.56", Money(123456).format())
    }

    @Test
    fun `arithmetic is exact where floating point would not be`() {
        val tenth = Money(10)
        val fifth = Money(20)
        assertEquals(Money(30), tenth + fifth)
        assertEquals(Money(-10), tenth - fifth)
    }
}
