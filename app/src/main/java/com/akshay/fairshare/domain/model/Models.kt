package com.akshay.fairshare.domain.model

data class Member(
    val id: String,
    val name: String,
)

data class Group(
    val id: String,
    val name: String,
    val members: List<Member>,
)

/** One person's portion of a single expense. */
data class Share(
    val memberId: String,
    val amount: Money,
)

data class Expense(
    val id: String,
    val groupId: String,
    val description: String,
    val total: Money,
    val paidBy: String,
    val shares: List<Share>,
    val createdAt: Long,
) {
    init {
        val sum = shares.map { it.amount }.sum()
        require(sum == total) {
            "Shares (${sum.format()}) must sum to the total (${total.format()})"
        }
    }
}

/** Net position of one member: positive means the group owes them. */
data class Balance(
    val memberId: String,
    val net: Money,
)

/** A single suggested payment that moves the group toward settled. */
data class Settlement(
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Money,
)

enum class SplitMode { EVENLY, BY_WEIGHT, EXACT }
