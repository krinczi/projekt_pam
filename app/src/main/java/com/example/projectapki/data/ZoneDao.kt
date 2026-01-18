package com.example.projectapki.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {

    @Insert
    suspend fun insert(zone: Zone)

    @Query("SELECT * FROM zones ORDER BY name ASC")
    fun observeAll(): Flow<List<Zone>>

    @Query("SELECT * FROM zones WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Zone?

    @Query("DELETE FROM zones")
    suspend fun deleteAll()
}
