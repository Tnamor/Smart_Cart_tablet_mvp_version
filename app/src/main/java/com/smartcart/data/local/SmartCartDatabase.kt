package com.smartcart.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "last_session")
data class LastSessionEntity(
    @PrimaryKey val id: Int = 0,
    val json: String,
    val updatedAt: Long,
)

@Dao
interface LastSessionDao {
    @Query("SELECT * FROM last_session WHERE id = 0 LIMIT 1")
    suspend fun getLastSession(): LastSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LastSessionEntity)

    @Query("DELETE FROM last_session")
    suspend fun clear()

}

@Database(
    entities = [LastSessionEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SmartCartDatabase : RoomDatabase() {
    abstract fun lastSessionDao(): LastSessionDao
}

