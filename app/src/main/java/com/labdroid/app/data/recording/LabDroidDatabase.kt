package com.labdroid.app.data.recording

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecordingSessionEntity::class, SensorSampleEntity::class, LocationSampleEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class LabDroidDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao
}
