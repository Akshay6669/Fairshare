package com.akshay.fairshare.domain

import com.akshay.fairshare.domain.model.Money
import com.akshay.fairshare.domain.model.Share
import com.akshay.fairshare.domain.model.sum
import com.akshay.fairshare.domain.split.SplitCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitCalculatorTest {

    private val members = listOf("alex", "blair", "casey")

    @Test
    fun `divides evenly when the amount divides cleanly`() {
        val shares = SplitCalculator.splitEvenly(Money(900), members)
        assertTrue(shares.all { it.amount == Money(300) })
    }

    @Test
    fun `assigns the leftover cent rather than dropping it`() {
        val shares = SplitCalculator.splitEvenly(Money(1000), members)
        assertEquals(Money(1000), shares.map { it.amount }.sum())
        assertEquals(listOf(334L, 333L, 333L), shares.map { it.amount.cents }.sortedDescending())
    }

    @Test
    fun `never loses or invents a cent for any amount`() {
        for (cents in 0L..2000L) {
            for (people in 1..7) {
                val ids = (1..people).map { "member$it" }
                val shares = SplitCalculator.splitEvenly(Money(cents), ids)
                assertEquals(
                    "Total changed splitting $cents between $people",
                    Money(cents),
                    shares.map { it.amount }.sum(),
                )
            }
        }
    }

    @Test
    fun `share sizes never differ by more than one cent`() {
        for (cents in 0L..500L) {
            val shares = SplitCalculator.splitEvenly(Money(cents), members)
            val spread = shares.maxOf { it.amount.cents } - shares.minOf { it.amount.cents }
            assertTrue("Uneven split of $cents", spread <= 1L)
        }
    }

    @Test
    fun `weighted split is proportional and still sums to the total`() {
        val shares = SplitCalculator.splitByWeights(
            Money(1000),
            mapOf("alex" to 2L, "blair" to 1L),
        ).associate { it.memberId to it.amount.cents }

        assertEquals(667L, shares.getValue("alex"))
        assertEquals(333L, shares.getValue("blair"))
        assertEquals(1000L, shares.values.sum())
    }

    @Test
    fun `split is deterministic across repeated runs`() {
        val first = SplitCalculator.splitEvenly(Money(1000), members)
        repeat(50) {
            assertEquals(first, SplitCalculator.splitEvenly(Money(1000), members.shuffled()))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects an empty member list`() {
        SplitCalculator.splitEvenly(Money(1000), emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects a duplicated member`() {
        SplitCalculator.splitEvenly(Money(1000), listOf("alex", "alex"))
    }

    @Test
    fun `exact split validation reports the shortfall`() {
        val result = SplitCalculator.validateExact(
            Money(1000),
            listOf(Share("alex", Money(400)), Share("blair", Money(500))),
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("1.00"))
    }

    @Test
    fun `exact split validation accepts a balanced split`() {
        val shares = listOf(Share("alex", Money(400)), Share("blair", Money(600)))
        assertEquals(shares, SplitCalculator.validateExact(Money(1000), shares).getOrThrow())
    }
}
