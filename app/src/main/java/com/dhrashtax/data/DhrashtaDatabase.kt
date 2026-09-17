package com.dhrashtax.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlertEntity::class], version = 1, exportSchema = true)
abstract class DhrashtaDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao

    companion object {
        fun create(context: Context): DhrashtaDatabase = Room.databaseBuilder(
            context.applicationContext,
            DhrashtaDatabase::class.java,
            "dhrashta.db",
        ).build()
    }
}

