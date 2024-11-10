package com.dipdev.muteonlocation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muted_contact")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val phoneNumber: String
)
