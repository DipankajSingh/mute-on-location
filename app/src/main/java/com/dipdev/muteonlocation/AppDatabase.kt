package com.dipdev.muteonlocation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.concurrent.Volatile

@Database(entities = [LocationEntity::class, ContactEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mutedLocationDao(): MutedLocationDAO
    abstract fun contactDao(): ContactDAO

    companion object {
        @Volatile
        private var INSTANCE:AppDatabase?=null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Define SQL statement to create the new table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `muted_contact` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `phoneNumber` TEXT NOT NULL, `name` TEXT NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context):AppDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext,AppDatabase::class.java,"app_database")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE=instance
                instance
            }
        }
    }


}