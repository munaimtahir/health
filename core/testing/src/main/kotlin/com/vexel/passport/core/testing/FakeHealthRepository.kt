package com.vexel.passport.core.testing

import com.vexel.passport.core.domain.HealthRepository
import com.vexel.passport.core.model.HealthSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHealthRepository : HealthRepository {
    private val _snapshot = MutableStateFlow(HealthSnapshot())

    override fun observeSnapshot(): Flow<HealthSnapshot> = _snapshot

    fun emit(snapshot: HealthSnapshot) {
        _snapshot.value = snapshot
    }
}
