package com.akshay.fairshare.data.mapper

import com.akshay.fairshare.data.local.ExpenseEntity
import com.akshay.fairshare.data.local.ExpenseWithShares
import com.akshay.fairshare.data.local.GroupWithMembers
import com.akshay.fairshare.data.local.MemberEntity
import com.akshay.fairshare.data.local.ShareEntity
import com.akshay.fairshare.data.remote.ExpenseDto
import com.akshay.fairshare.data.remote.ShareDto
import com.akshay.fairshare.domain.model.Expense
import com.akshay.fairshare.domain.model.Group
import com.akshay.fairshare.domain.model.Member
import com.akshay.fairshare.domain.model.Money
import com.akshay.fairshare.domain.model.Share

fun GroupWithMembers.toDomain() = Group(
    id = group.id,
    name = group.name,
    members = members.map { Member(it.id, it.name) },
)

fun ExpenseWithShares.toDomain() = Expense(
    id = expense.id,
    groupId = expense.groupId,
    description = expense.description,
    total = Money(expense.totalCents),
    paidBy = expense.paidBy,
    shares = shares.map { Share(it.memberId, Money(it.amountCents)) },
    createdAt = expense.createdAt,
)

fun Expense.toEntity(pendingSync: Boolean) = ExpenseEntity(
    id = id,
    groupId = groupId,
    description = description,
    totalCents = total.cents,
    paidBy = paidBy,
    createdAt = createdAt,
    pendingSync = pendingSync,
)

fun Expense.toShareEntities() = shares.map { ShareEntity(id, it.memberId, it.amount.cents) }

fun Expense.toDto() = ExpenseDto(
    id = id,
    groupId = groupId,
    description = description,
    totalCents = total.cents,
    paidBy = paidBy,
    shares = shares.map { ShareDto(it.memberId, it.amount.cents) },
    createdAt = createdAt,
)

fun ExpenseDto.toDomain() = Expense(
    id = id,
    groupId = groupId,
    description = description,
    total = Money(totalCents),
    paidBy = paidBy,
    shares = shares.map { Share(it.memberId, Money(it.amountCents)) },
    createdAt = createdAt,
)

fun Member.toEntity(groupId: String) = MemberEntity(id, groupId, name)
