package com.therishideveloper.dailyexpense.domain.repository

import com.therishideveloper.dailyexpense.data.dao.NoteDao
import com.therishideveloper.dailyexpense.data.entity.Note
import com.therishideveloper.dailyexpense.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {
    override fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()

    override suspend fun insertNote(note: Note) {
        dao.insertNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        dao.deleteNote(note)
    }

    override suspend fun getAllNotesList(): List<Note> {
        return dao.getAllNotesList()
    }

    override suspend fun deleteAllNotes() {
        dao.deleteAllNotes()
    }
}