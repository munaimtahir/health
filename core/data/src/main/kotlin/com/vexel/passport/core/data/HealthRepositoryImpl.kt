package com.vexel.passport.core.data

import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.domain.HealthRepository
import com.vexel.passport.core.model.HealthSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthRepositoryImpl(
    private val database: HealthDatabase
) : HealthRepository {

    override fun observeSnapshot(): Flow<HealthSnapshot> {
        val remindersFlow = database.reminderDao().observeAll()
        val eventsFlow = database.healthEventDao().observeAll()
        val medicationsFlow = database.medicationDao().observeAll()
        val documentsFlow = database.documentDao().observeAll()

        return combine(remindersFlow, eventsFlow, medicationsFlow, documentsFlow) { reminders, events, medications, documents ->
            val now = System.currentTimeMillis()
            
            // 1. Next follow-up/scheduled reminder
            val nextReminder = reminders
                .filter { it.status == "SCHEDULED" && it.dueAtEpochMillis >= now }
                .minByOrNull { it.dueAtEpochMillis }
            val nextFollowUpText = nextReminder?.let {
                val date = Date(it.dueAtEpochMillis)
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                "${it.title} (${format.format(date)})"
            }

            // 2. Count of active/ongoing symptoms
            val activeSymptomsCount = events
                .filter { it.kind == "SYMPTOM" && !it.archived && (it.ongoing || it.endAtEpochMillis == null) }
                .size

            // 3. Most recent report/document
            val recentDoc = documents
                .filter { !it.archived }
                .maxByOrNull { it.createdAtEpochMillis }
            val recentDocText = recentDoc?.let {
                if (it.documentDate.isNotBlank()) "${it.title} (${it.documentDate})" else it.title
            }

            // 4. Count of current medications
            val currentMedsCount = medications
                .filter { it.status == "CURRENT" }
                .size

            HealthSnapshot(
                nextFollowUp = nextFollowUpText,
                activeSymptoms = activeSymptomsCount,
                recentReport = recentDocText,
                currentMedications = currentMedsCount
            )
        }
    }
}
