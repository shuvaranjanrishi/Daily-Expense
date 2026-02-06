package com.therishideveloper.dailyexpense.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.therishideveloper.dailyexpense.data.database.AppDatabase
import com.therishideveloper.dailyexpense.data.dao.NoteDao // Dao import করুন
import com.therishideveloper.dailyexpense.data.dao.TransactionDao
import com.therishideveloper.dailyexpense.domain.repository.NoteRepository
import com.therishideveloper.dailyexpense.domain.repository.NoteRepositoryImpl
import com.therishideveloper.dailyexpense.domain.repository.TransactionRepository
import com.therishideveloper.dailyexpense.domain.repository.TransactionRepositoryImpl
import com.therishideveloper.dailyexpense.util.BackupHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "daily_expense_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository {
        return TransactionRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun provideNoteRepository(dao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideBackupHelper(
        @ApplicationContext context: Context
    ): BackupHelper {
        return BackupHelper(context)
    }
}