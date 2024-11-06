package com.dipdev.muteonlocation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlin.concurrent.Volatile

@Database(entities = [LocationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mutedLocationDao(): MutedLocationDAO

    companion object {
        @Volatile
        private var INSTANCE:AppDatabase?=null

        fun getDatabase(context: Context):AppDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext,AppDatabase::class.java,"app_database").build()
                INSTANCE=instance
                instance
            }
        }
    }

}

//val db = AppDatabase.getDatabase(applicationContext)
//val locationDao = db.locationDao()
//
//val newLocation = Location(name = "Home", latitude = 12.9716, longitude = 77.5946, address = "Your Address")
//CoroutineScope(Dispatchers.IO).launch {
//    locationDao.insertLocation(newLocation)
//}

//CoroutineScope(Dispatchers.IO).launch {
//    val locations = locationDao.getAllLocations()
//    // Handle locations here (e.g., update UI)
//}