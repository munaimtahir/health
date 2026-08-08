package com.vexel.passport.core.model

data class HealthSnapshot(
    val nextFollowUp: String? = null,
    val activeSymptoms: Int = 0,
    val recentReport: String? = null,
    val currentMedications: Int = 0,
)

