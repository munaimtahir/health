package com.vexel.passport.core.domain

import kotlinx.coroutines.flow.Flow
import com.vexel.passport.core.model.HealthSnapshot

interface HealthRepository {
    fun observeSnapshot(): Flow<HealthSnapshot>
}

