package com.akshay.fairshare.domain

import com.akshay.fairshare.domain.balance.BalanceCalculator
import com.akshay.fairshare.domain.model.Expense
import com.akshay.fairshare.domain.model.Money
import com.akshay.fairshare.domain.model.sum
import com.akshay.fairshare.domain.split.SplitCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceCalculatorTest {

    private val members = listOf("alex", "blair", "casey")

    private fun expense(id: String, payer: String, cents: Long, among: List<String> = members) =
        Expense(
            id = id,
            groupId = "trip",
            description = id,
            total = Money(cents),
            paidBy = payer,
            shares = SplitCalculator.splitEvenly(Money(cents), among),
            createdAt = 0L,
        )

    @Test
    fun `payer is credited the full amount and debited only their share`() {
        val balances = BalanceCalculator.balances(members, listOf(expense("dinner", "alex", 900)))
            .associate { it.memberId to it.net.cents }

        assertEquals(600L, balances.getValue("alex"))
        assertEquals(-300L, balances.getValue("blair"))
        assertEquals(-300L, balances.getValue("casey"))
    }

    @Test
    fun `balances always sum to zero`() {
        val expenses = listOf(
            expense("dinner", "alex", 1000),
            expense("taxi", "blair", 733),
            expense("tickets", "casey", 4501),
            expense("coffee", "alex", 7, listOf("alex", "blair")),
        )
        val balances = BalanceCalculator.balances(members, expenses)
        assertTrue(balances.map { it.net }.sum().isZero)
    }

    @Test
    fun `a member excluded from a split is not charged for it`() {
        val balances = BalanceCalculator.balances(
            members,
            listOf(expense("drinks", "alex", 1000, listOf("alex", "blair"))),
        ).associate { it.memberId to it.net.cents }

        assertEquals(0L, balances.getValue("casey"))
        assertEquals(500L, balances.getValue("alex"))
        assertEquals(-500L, balances.getValue("blair"))
    }

    @Test
    fun `reciprocal expenses cancel out`() {
        val balances = BalanceCalculator.balances(
            members,
            listOf(expense("lunch", "alex", 1200), expense("dinner", "blair", 1200)),
        ).associate { it.memberId to it.net.cents }

        assertEquals(400L, balances.getValue("alex"))
        assertEquals(400L, balances.getValue("blair"))
        assertEquals(-800L, balances.getValue("casey"))
    }

    @Test
    fun `a group with no expenses is settled`() {
        assertTrue(BalanceCalculator.balances(members, emptyList()).all { it.net.isZero })
    }
}
