package com.example.capstonetest.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SummaryDao {
    @Query("SELECT * FROM SummaryEntity")
    fun getAll():List<SummaryEntity>

    @Insert
    fun insertSummary(todo: SummaryEntity)

    @Delete
    fun deleteSummary(todo: SummaryEntity)

}