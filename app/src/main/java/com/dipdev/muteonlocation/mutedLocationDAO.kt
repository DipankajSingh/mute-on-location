package com.dipdev.muteonlocation

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MutedLocationDAO {
    @Insert
    suspend fun insertMutedLocation(location: LocationEntity)
    @Query("SELECT * FROM muted_locations")
    suspend fun getAllMutedLocations(): List<LocationEntity>
    @Delete
    suspend fun deleteMutedLocation(location: LocationEntity)
}