package com.akshay.fairshare.domain.settle

import com.akshay.fairshare.domain.model.Balance
import com.akshay.fairshare.domain.model.Money
import com.akshay.fairshare.domain.model.Settlement

/**
 * Turns a set of balances into a short list of payments that settles the group.
 *
 * Naively, everyone who owes pays everyone they owe, which produces O(n^2) transfers. This
 * instead repeatedly matches the largest debtor against the largest creditor, which clears at
 * least one person from the books on every pass and therefore always finishes in at most
 * n - 1 payments. That is not provably the global minimum (the exact problem is NP-hard),
 * but it is the standard heuristic and is optimal whenever no subset of members happens to
 * balance out among themselves.
 */
object SettlementCalculator {

    fun settlements(balances: List<Balance>): List<Settlement> {
        val creditors = balances.filter { it.net.isPositive }
            .sortedWith(compareByDescending<Balance> { it.net.cents }.thenBy { it.memberId })
            .map { it.memberId to it.net.cents }
            .toMutableList()

        val debtors = balances.filter { it.net.isNegative }
            .sortedWith(compareBy<Balance> { it.net.cents }.thenBy { it.memberId })
            .map { it.memberId to -it.net.cents }
            .toMutableList()

        val payments = mutableListOf<Settlement>()

        var c = 0
        var d = 0
        while (c < creditors.size && d < debtors.size) {
            val (creditorId, credit) = creditors[c]
            val (debtorId, debt) = debtors[d]
            val amount = minOf(credit, debt)

            if (amount > 0) {
                payments += Settlement(debtorId, creditorId, Money(amount))
            }

            creditors[c] = creditorId to (credit - amount)
            debtors[d] = debtorId to (debt - amount)

            if (creditors[c].second == 0L) c++
            if (debtors[d].second == 0L) d++
        }

        return payments
    }
}
