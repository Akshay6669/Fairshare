package com.akshay.fairshare.data.repository

import com.akshay.fairshare.data.local.FairShareDao
import com.akshay.fairshare.data.local.GroupEntity
import com.akshay.fairshare.data.local.MemberEntity
import com.akshay.fairshare.data.mapper.toDomain
import com.akshay.fairshare.data.mapper.toDto
import com.akshay.fairshare.data.mapper.toEntity
import com.akshay.fairshare.data.mapper.toShareEntities
import com.akshay.fairshare.data.remote.FairShareApi
import com.akshay.fairshare.domain.model.Expense
import com.akshay.fairshare.domain.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first: Room is the single source of truth and the only thing the UI observes.
 *
 * Writes land locally first and are flagged pendingSync, so adding an expense on the tube
 * works exactly like adding one on wifi. The network is a background reconciler, never
 * something the user waits on, and a failed sync leaves the local write untouched.
 */
@Singleton
class ExpenseRepository @Inject constructor(
    private val dao: FairShareDao,
    private val api: FairShareApi,
) {

    fun observeGroup(groupId: String): Flow<Group?> =
        dao.observeGroup(groupId).map { it?.toDomain() }

    fun observeExpenses(groupId: String): Flow<List<Expense>> =
        dao.observeExpenses(groupId).map { rows -> rows.map { it.toDomain() } }

    /** Seeds a demo group the first time the app runs on a device; a no-op after that. */
    suspend fun seedDemoGroupIfEmpty() {
        if (dao.groupCount() > 0) return
        dao.upsertGroup(GroupEntity(id = DEMO_GROUP_ID, name = "Trip", updatedAt = System.currentTimeMillis()))
        dao.upsertMembers(
            listOf(
                MemberEntity(id = "demo-alex", groupId = DEMO_GROUP_ID, name = "Alex"),
                MemberEntity(id = "demo-blair", groupId = DEMO_GROUP_ID, name = "Blair"),
                MemberEntity(id = "demo-casey", groupId = DEMO_GROUP_ID, name = "Casey"),
            ),
        )
    }

    /** Persists immediately, then attempts to push. The local write is never rolled back. */
    suspend fun addExpense(expense: Expense): Result<Unit> {
        dao.saveExpense(expense.toEntity(pendingSync = true), expense.toShareEntities())
        return syncPending(expense.groupId)
    }

    suspend fun syncPending(groupId: String): Result<Unit> = runCatching {
        val pending = dao.pendingExpenses(groupId).map { it.toDomain() }
        if (pending.isEmpty()) return@runCatching

        api.pushExpenses(groupId, pending.map { it.toDto() })
        dao.markSynced(pending.map { it.id })
    }

    /** Pulls the server's view and reconciles it into Room. */
    suspend fun refresh(groupId: String): Result<Unit> = runCatching {
        val remote = api.expenses(groupId).map { it.toDomain() }
        for (expense in remote) {
            dao.saveExpense(expense.toEntity(pendingSync = false), expense.toShareEntities())
        }
    }

    companion object {
        const val DEMO_GROUP_ID = "demo-group"
    }
}
