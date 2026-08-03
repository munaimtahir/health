package pk.vexel.healthpassport.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    fun create(context: Context): HealthDatabase =
        Room.databaseBuilder(context, HealthDatabase::class.java, DatabaseConstants.NAME)
            .addMigrations(MIGRATION_1_2)
            .build()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE health_events ADD COLUMN details TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE health_events ADD COLUMN kind TEXT NOT NULL DEFAULT 'OTHER'")
            db.execSQL("ALTER TABLE health_events ADD COLUMN effectiveAtEpochMillis INTEGER")
            db.execSQL("ALTER TABLE health_events ADD COLUMN updatedAtEpochMillis INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE health_events ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
            db.execSQL("ALTER TABLE health_events ADD COLUMN severity INTEGER")
            db.execSQL("ALTER TABLE health_events ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE health_events SET updatedAtEpochMillis = createdAtEpochMillis, effectiveAtEpochMillis = createdAtEpochMillis")
            db.execSQL("CREATE TABLE IF NOT EXISTS profile (id INTEGER NOT NULL, name TEXT NOT NULL, dateOfBirth TEXT NOT NULL, bloodGroup TEXT NOT NULL, allergies TEXT NOT NULL, conditions TEXT NOT NULL, emergencyContact TEXT NOT NULL, updatedAtEpochMillis INTEGER NOT NULL, PRIMARY KEY(id))")
        }
    }
}
