package com.iptvplayer.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iptvplayer.app.core.database.entity.EpgProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgProgramEntity>)

    @Query("""
        SELECT * FROM epg_programs 
        WHERE channelTvgId = :channelTvgId AND end >= :fromTime AND start <= :toTime 
        ORDER BY start ASC
    """)
    fun getSchedule(channelTvgId: String, fromTime: Long, toTime: Long): Flow<List<EpgProgramEntity>>

    @Query("""
        SELECT * FROM epg_programs 
        WHERE channelTvgId = :channelTvgId AND start <= :now AND end > :now 
        LIMIT 1
    """)
    fun getCurrentProgram(channelTvgId: String, now: Long): Flow<EpgProgramEntity?>

    @Query("""
        SELECT * FROM epg_programs 
        WHERE channelTvgId = :channelTvgId AND start >= :now 
        ORDER BY start ASC 
        LIMIT 1
    """)
    fun getNextProgram(channelTvgId: String, now: Long): Flow<EpgProgramEntity?>

    @Query("DELETE FROM epg_programs WHERE end < :cutoffTime")
    suspend fun deleteExpiredPrograms(cutoffTime: Long)

    @Query("DELETE FROM epg_programs")
    suspend fun deleteAll()
}
