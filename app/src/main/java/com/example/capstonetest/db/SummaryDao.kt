package com.example.capstonetest.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.*

@Dao
interface SummaryDao {
    @Query("SELECT * FROM SummaryEntity")
    fun getAll():List<SummaryEntity>

    @Insert
    fun insertHabit(todo: SummaryEntity)

    @Delete
    fun deleteHabit(todo: SummaryEntity)

    @Update
    fun updateHabit(todo: SummaryEntity)
}