package com.vexel.passport.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HealthEventEntity::class, ProfileEntity::class, MedicationEntity::class, MedicationChangeEntity::class, DocumentEntity::class, ReminderEntity::class, ConditionEntity::class, AllergyEntity::class, MeasurementEntity::class], version = DatabaseConstants.VERSION, exportSchema = true)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthEventDao(): HealthEventDao
    abstract fun profileDao(): ProfileDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationChangeDao(): MedicationChangeDao
    abstract fun documentDao(): DocumentDao
    abstract fun reminderDao(): ReminderDao
    abstract fun conditionDao(): ConditionDao
    abstract fun allergyDao(): AllergyDao
    abstract fun measurementDao(): MeasurementDao
}
