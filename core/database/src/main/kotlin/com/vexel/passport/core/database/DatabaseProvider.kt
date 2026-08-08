package com.vexel.passport.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    fun create(context: Context): HealthDatabase =
        Room.databaseBuilder(context, HealthDatabase::class.java, DatabaseConstants.NAME)
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .addMigrations(MIGRATION_6_7)
            .addMigrations(MIGRATION_7_8)
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

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS medications (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                genericName TEXT NOT NULL DEFAULT '',
                strength TEXT NOT NULL DEFAULT '',
                dose TEXT NOT NULL DEFAULT '',
                unit TEXT NOT NULL DEFAULT '',
                route TEXT NOT NULL DEFAULT '',
                frequency TEXT NOT NULL DEFAULT '',
                startDate TEXT NOT NULL DEFAULT '',
                stopDate TEXT NOT NULL DEFAULT '',
                status TEXT NOT NULL DEFAULT 'CURRENT',
                indication TEXT NOT NULL DEFAULT '',
                physician TEXT NOT NULL DEFAULT '',
                notes TEXT NOT NULL DEFAULT '',
                createdAtEpochMillis INTEGER NOT NULL,
                updatedAtEpochMillis INTEGER NOT NULL
            )""")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS documents (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT 'OTHER',
                documentDate TEXT NOT NULL DEFAULT '',
                notes TEXT NOT NULL DEFAULT '',
                originalFileName TEXT NOT NULL,
                mimeType TEXT NOT NULL,
                byteCount INTEGER NOT NULL,
                sha256 TEXT NOT NULL,
                createdAtEpochMillis INTEGER NOT NULL,
                archived INTEGER NOT NULL DEFAULT 0
            )""")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS reminders (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                type TEXT NOT NULL DEFAULT 'CUSTOM',
                notes TEXT NOT NULL DEFAULT '',
                dueAtEpochMillis INTEGER NOT NULL,
                recurrence TEXT NOT NULL DEFAULT 'ONCE',
                status TEXT NOT NULL DEFAULT 'SCHEDULED',
                snoozeUntilEpochMillis INTEGER,
                createdAtEpochMillis INTEGER NOT NULL,
                updatedAtEpochMillis INTEGER NOT NULL
            )""")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE health_events ADD COLUMN durationMinutes INTEGER")
            db.execSQL("ALTER TABLE health_events ADD COLUMN startAtEpochMillis INTEGER")
            db.execSQL("ALTER TABLE health_events ADD COLUMN endAtEpochMillis INTEGER")
            db.execSQL("ALTER TABLE health_events ADD COLUMN ongoing INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE health_events ADD COLUMN bodyLocation TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE health_events ADD COLUMN associatedSymptoms TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE health_events ADD COLUMN possibleTrigger TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE health_events ADD COLUMN relatedMedication TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS medication_changes (
                id TEXT NOT NULL PRIMARY KEY,
                medicationId TEXT NOT NULL,
                changedAtEpochMillis INTEGER NOT NULL,
                changeType TEXT NOT NULL,
                strength TEXT NOT NULL DEFAULT '',
                dose TEXT NOT NULL DEFAULT '',
                unit TEXT NOT NULL DEFAULT '',
                frequency TEXT NOT NULL DEFAULT '',
                status TEXT NOT NULL DEFAULT 'CURRENT',
                notes TEXT NOT NULL DEFAULT ''
            )""")
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE health_events ADD COLUMN imageAttachmentId TEXT")
            db.execSQL("ALTER TABLE health_events ADD COLUMN episodeId TEXT")
        }
    }
}
