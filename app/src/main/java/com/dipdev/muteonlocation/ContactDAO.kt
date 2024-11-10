package com.dipdev.muteonlocation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactDAO {
    @Insert
    suspend fun insert(contact: ContactEntity)

    @Query("SELECT * FROM muted_contact")
    suspend fun getAllContacts(): List<ContactEntity>

    @Query("SELECT * FROM muted_contact WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun findContactByPhoneNumber(phoneNumber: String): ContactEntity?

    @Query("DELETE FROM muted_contact WHERE phoneNumber = :phoneNumber")
    suspend fun deleteContactByPhoneNumber(phoneNumber: String)
}
