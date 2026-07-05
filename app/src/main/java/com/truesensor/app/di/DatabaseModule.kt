package com.truesensor.app.di

import android.content.Context
import androidx.room.Room
import com.truesensor.app.data.recording.RecordingDao
import com.truesensor.app.data.recording.TrueSensorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TrueSensorDatabase =
        Room.databaseBuilder(context, TrueSensorDatabase::class.java, "truesensor.db").build()

    @Provides
    fun provideRecordingDao(database: TrueSensorDatabase): RecordingDao = database.recordingDao()
}
