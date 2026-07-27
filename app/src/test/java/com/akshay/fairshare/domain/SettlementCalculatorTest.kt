package com.akshay.fairshare.domain

import com.akshay.fairshare.domain.model.Balance
import com.akshay.fairshare.domain.model.Money
import com.akshay.fairshare.domain.model.Settlement
import com.akshay.fairshare.domain.settle.SettlementCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SettlementCalculatorTest {

    private fun balances(vararg pairs: Pair<String, Long>) =
        pairs.map { (id, cents) -> Balance(id, Money(cents)) }

    /** Replays the payments and asserts every member ends at zero. */
    private fun assertResolves(input: List<Balance>, payments: List<Settlement>) {
        val net = input.associate { it.memberId to it.net.cents }.toMutableMap()
        for (p in payments) {
            net[p.fromMemberId] = net.getValue(p.fromMemberId) + p.amount.cents
            net[p.toMemberId] = net.getValue(p.toMemberId) - p.amount.cents
        }
        assertTrue("Unsettled after replay: $net", net.values.all { it == 0L })
    }

    @Test
    fun `one creditor and two debtors settles in two payments`() {
        val input = balances("alex" to 1000, "blair" to -600, "casey" to -400)
        val payments = SettlementCalculator.settlements(input)

        assertEquals(2, payments.size)
        assertResolves(input, payments)
    }

    @Test
    fun `an already settled group needs no payments`() {
        val payments = SettlementCalculator.settlements(
            balances("alex" to 0, "blair" to 0, "casey" to 0),
        )
        assertTrue(payments.isEmpty())
    }

    @Test
    fun `nobody pays more than they owe`() {
        val input = balances("alex" to 1500, "blair" to -1000, "casey" to -500)
        val payments = SettlementCalculator.settlements(input)

        val paid = payments.groupBy { it.fromMemberId }
            .mapValues { entry -> entry.value.sumOf { it.amount.cents } }

        assertEquals(1000L, paid.getValue("blair"))
        assertEquals(500L, paid.getValue("casey"))
    }

    @Test
    fun `never produces more than n minus one payments`() {
        val random = Random(20260727)
        repeat(500) {
            val n = random.nextInt(2, 9)
            val raw = (1 until n).map { random.nextLong(-5000, 5000) }
            val input = (raw + (-raw.sum())).mapIndexed { i, cents -> Balance("m$i", Money(cents)) }

            val payments = SettlementCalculator.settlements(input)
            assertTrue("Too many payments for $n members", payments.size <= n - 1)
            assertResolves(input, payments)
        }
    }

    @Test
    fun `all payment amounts are positive`() {
        val input = balances("alex" to 733, "blair" to -1, "casey" to -732)
        assertTrue(SettlementCalculator.settlements(input).all { it.amount.isPositive })
    }
}
