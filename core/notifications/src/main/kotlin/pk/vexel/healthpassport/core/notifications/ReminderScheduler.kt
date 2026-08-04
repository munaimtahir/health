package pk.vexel.healthpassport.core.notifications

interface ReminderScheduler {
    suspend fun schedule(id: String, dueAtEpochMillis: Long, recurrence: String)
    suspend fun cancel(id: String)
    suspend fun reconcile()
}
