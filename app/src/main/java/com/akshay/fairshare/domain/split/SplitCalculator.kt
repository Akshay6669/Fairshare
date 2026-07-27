package com.akshay.fairshare.domain.split

import com.akshay.fairshare.domain.model.Money
import com.akshay.fairshare.domain.model.Share
import com.akshay.fairshare.domain.model.sum

/**
 * Divides an amount between people without losing or inventing cents.
 *
 * The obvious implementation - total / n for everyone - loses money whenever the division
 * isn't exact: 10.00 split three ways gives 3.33 x 3 = 9.99, and the missing cent has to
 * land on somebody. This uses the largest remainder method, which assigns every leftover
 * cent deterministically so the parts always add back up to the whole.
 */
object SplitCalculator {

    fun splitEvenly(total: Money, memberIds: List<String>): List<Share> {
        require(memberIds.isNotEmpty()) { "Cannot split between zero people" }
        require(memberIds.toSet().size == memberIds.size) { "Duplicate member in split" }
        return splitByWeights(total, memberIds.associateWith { 1L })
    }

    /**
     * Splits proportionally to weights - useful for "Alex was only there two of the three
     * nights" style splits. Leftover cents go to the largest discarded fractions first,
     * with member id as a tie-break so results are stable across runs and devices.
     */
    fun splitByWeights(total: Money, weights: Map<String, Long>): List<Share> {
        require(weights.isNotEmpty()) { "Cannot split between zero people" }
        require(weights.values.all { it > 0 }) { "Weights must be positive" }

        val totalWeight = weights.values.sum()
        val entries = weights.entries.sortedBy { it.key }

        val base = entries.map { (id, weight) ->
            id to Math.floorDiv(total.cents * weight, totalWeight)
        }

        var remainder = total.cents - base.sumOf { it.second }

        val ranked = entries.sortedWith(
            compareByDescending<Map.Entry<String, Long>> {
                Math.floorMod(total.cents * it.value, totalWeight)
            }.thenBy { it.key }
        ).map { it.key }

        val extra = mutableMapOf<String, Long>()
        val step = if (remainder >= 0) 1L else -1L
        var index = 0
        while (remainder != 0L) {
            val id = ranked[index % ranked.size]
            extra[id] = (extra[id] ?: 0L) + step
            remainder -= step
            index++
        }

        val shares = base.map { (id, cents) -> Share(id, Money(cents + (extra[id] ?: 0L))) }
        check(shares.map { it.amount }.sum() == total) { "Split did not preserve the total" }
        return shares
    }

    /** Validates a manually entered split before it is allowed to become an Expense. */
    fun validateExact(total: Money, shares: List<Share>): Result<List<Share>> {
        val sum = shares.map { it.amount }.sum()
        return if (sum == total) {
            Result.success(shares)
        } else {
            val diff = total - sum
            Result.failure(
                IllegalArgumentException(
                    "Split is off by ${diff.format()} - shares total ${sum.format()}, " +
                        "expense is ${total.format()}"
                )
            )
        }
    }
}
