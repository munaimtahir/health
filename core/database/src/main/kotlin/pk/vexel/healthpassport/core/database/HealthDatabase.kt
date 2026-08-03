package pk.vexel.healthpassport.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HealthEventEntity::class], version = DatabaseConstants.VERSION, exportSchema = false)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthEventDao(): HealthEventDao
}

