package com.akshay.fairshare.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Amounts are persisted as Long cents. The domain's Money value class is deliberately kept
 * out of the schema so the storage format never depends on a domain type that might change.
 */

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val updatedAt: Long,
    val pendingSync: Boolean = false,
)

@Entity(
    tableName = "members",
    indices = [Index("groupId")],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MemberEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val name: String,
)

@Entity(
    tableName = "expenses",
    indices = [Index("groupId")],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val description: String,
    val totalCents: Long,
    val paidBy: String,
    val createdAt: Long,
    val pendingSync: Boolean = false,
)

@Entity(
    tableName = "shares",
    primaryKeys = ["expenseId", "memberId"],
    indices = [Index("expenseId")],
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ShareEntity(
    val expenseId: String,
    val memberId: String,
    val amountCents: Long,
)
