package pk.vexel.healthpassport.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HealthEventEntity::class, ProfileEntity::class, MedicationEntity::class, DocumentEntity::class], version = DatabaseConstants.VERSION, exportSchema = false)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthEventDao(): HealthEventDao
    abstract fun profileDao(): ProfileDao
    abstract fun medicationDao(): MedicationDao
    abstract fun documentDao(): DocumentDao
}
