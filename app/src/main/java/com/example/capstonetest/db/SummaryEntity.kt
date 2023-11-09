package com.example.capstonetest.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SummaryEntity (
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    @ColumnInfo(name = "title") val title:String,
    @ColumnInfo(name = "summary") val summary:String,
    @ColumnInfo(name = "date") val date:String
)