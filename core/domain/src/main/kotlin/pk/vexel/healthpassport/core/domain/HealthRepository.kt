package pk.vexel.healthpassport.core.domain

import kotlinx.coroutines.flow.Flow
import pk.vexel.healthpassport.core.model.HealthSnapshot

interface HealthRepository {
    fun observeSnapshot(): Flow<HealthSnapshot>
}

