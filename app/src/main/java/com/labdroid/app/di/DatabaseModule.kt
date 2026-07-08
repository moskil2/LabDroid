package com.labdroid.app.di

import android.content.Context
import androidx.room.Room
import com.labdroid.app.data.recording.RecordingDao
import com.labdroid.app.data.recording.LabDroidDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): LabDroidDatabase =
        Room.databaseBuilder(context, LabDroidDatabase::class.java, "labdroid.db").build()

    @Provides
    fun provideRecordingDao(database: LabDroidDatabase): RecordingDao = database.recordingDao()
}
