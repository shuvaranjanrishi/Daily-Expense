package com.therishideveloper.dailyexpense.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.therishideveloper.dailyexpense.data.dao.NoteDao
//import com.therishideveloper.dailyexpense.data.NoteTypeConverter
import com.therishideveloper.dailyexpense.data.dao.TransactionDao
import com.therishideveloper.dailyexpense.data.entity.Note
import com.therishideveloper.dailyexpense.data.entity.Transaction

@Database(
    entities = [Transaction::class, Note::class],
    version = 1,
    exportSchema = false
)
//@TypeConverters(NoteTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun noteDao(): NoteDao
}