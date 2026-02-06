package com.therishideveloper.dailyexpense.domain.repository

import com.therishideveloper.dailyexpense.data.entity.Note
import com.therishideveloper.dailyexpense.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun insertNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun getAllNotesList(): List<Note>
    suspend fun deleteAllNotes()

}