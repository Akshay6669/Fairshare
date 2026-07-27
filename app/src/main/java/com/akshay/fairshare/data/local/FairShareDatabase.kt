package com.akshay.fairshare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GroupEntity::class,
        MemberEntity::class,
        ExpenseEntity::class,
        ShareEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class FairShareDatabase : RoomDatabase() {
    abstract fun dao(): FairShareDao

    companion object {
        const val NAME = "fairshare.db"
    }
}