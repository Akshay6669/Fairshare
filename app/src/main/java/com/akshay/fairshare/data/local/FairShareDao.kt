package com.akshay.fairshare.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class ExpenseWithShares(
    @Embedded val expense: ExpenseEntity,
    @Relation(parentColumn = "id", entityColumn = "expenseId")
    val shares: List<ShareEntity>,
)

data class GroupWithMembers(
    @Embedded val group: GroupEntity,
    @Relation(parentColumn = "id", entityColumn = "groupId")
    val members: List<MemberEntity>,
)

@Dao
interface FairShareDao {

    @Transaction
    @Query("SELECT * FROM groups ORDER BY updatedAt DESC")
    fun observeGroups(): Flow<List<GroupWithMembers>>

    @Transaction
    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun observeGroup(groupId: String): Flow<GroupWithMembers?>

    @Query("SELECT COUNT(*) FROM groups")
    suspend fun groupCount(): Int

    @Transaction
    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun observeExpenses(groupId: String): Flow<List<ExpenseWithShares>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMembers(members: List<MemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShares(shares: List<ShareEntity>)

    @Query("DELETE FROM shares WHERE expenseId = :expenseId")
    suspend fun deleteSharesFor(expenseId: String)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: String)

    /** Writes an expense and its shares atomically so a crash can't leave a partial split. */
    @Transaction
    suspend fun saveExpense(expense: ExpenseEntity, shares: List<ShareEntity>) {
        upsertExpense(expense)
        deleteSharesFor(expense.id)
        upsertShares(shares)
    }

    @Transaction
    @Query("SELECT * FROM expenses WHERE pendingSync = 1 AND groupId = :groupId")
    suspend fun pendingExpenses(groupId: String): List<ExpenseWithShares>

    @Query("UPDATE expenses SET pendingSync = 0 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)
}
