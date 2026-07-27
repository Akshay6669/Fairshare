package com.akshay.fairshare.domain.balance

import com.akshay.fairshare.domain.model.Balance
import com.akshay.fairshare.domain.model.Expense
import com.akshay.fairshare.domain.model.Money
import com.akshay.fairshare.domain.model.sum

/**
 * Reduces a list of expenses to one net figure per member.
 *
 * Paying for something credits you the full amount; being included in the split debits you
 * your share. Because every expense's shares sum to its total, the resulting balances across
 * the whole group always sum to zero - which the caller can rely on as an invariant.
 */
object BalanceCalculator {

    fun balances(memberIds: List<String>, expenses: List<Expense>): List<Balance> {
        val net = memberIds.associateWith { 0L }.toMutableMap()

        for (expense in expenses) {
            net[expense.paidBy] = (net[expense.paidBy] ?: 0L) + expense.total.cents
            for (share in expense.shares) {
                net[share.memberId] = (net[share.memberId] ?: 0L) - share.amount.cents
            }
        }

        val result = memberIds.map { Balance(it, Money(net.getValue(it))) }
        check(result.map { it.net }.sum().isZero) {
            "Balances must sum to zero; got ${result.map { it.net }.sum().format()}"
        }
        return result
    }

    /** Convenience view for the "you are owed / you owe" header. */
    fun netFor(memberId: String, balances: List<Balance>): Money =
        balances.firstOrNull { it.memberId == memberId }?.net ?: Money.ZERO
}
