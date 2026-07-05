package com.truesensor.app.data.recording

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordingDao {
    @Insert
    suspend fun insertSession(session: RecordingSessionEntity): Long

    @Update
    suspend fun updateSession(session: RecordingSessionEntity)

    @Query("SELECT * FROM RecordingSessionEntity WHERE id = :sessionId")
    suspend fun getSession(sessionId: Long): RecordingSessionEntity?

    @Insert
    suspend fun insertSensorSample(sample: SensorSampleEntity)

    @Query("SELECT * FROM SensorSampleEntity WHERE sessionId = :sessionId ORDER BY timestampMillis")
    suspend fun getSensorSamples(sessionId: Long): List<SensorSampleEntity>

    @Insert
    suspend fun insertLocationSample(sample: LocationSampleEntity)

    @Query("SELECT * FROM LocationSampleEntity WHERE sessionId = :sessionId ORDER BY timestampMillis")
    suspend fun getLocationSamples(sessionId: Long): List<LocationSampleEntity>
}
