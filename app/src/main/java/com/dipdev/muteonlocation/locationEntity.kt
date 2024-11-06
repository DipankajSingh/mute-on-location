package com.dipdev.muteonlocation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muted_locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val address: String
)
