package com.vexel.passport.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.database.HealthEventEntity
import com.vexel.passport.core.files.SecureFileStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val database: HealthDatabase,
    private val secureFileStore: SecureFileStore,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _selectedKind = MutableStateFlow<String?>(null)
    val selectedKind: StateFlow<String?> = _selectedKind

    private val _limit = MutableStateFlow(50)
    val limit: StateFlow<Int> = _limit

    private val _archivedEvents = MutableSharedFlow<HealthEventEntity>(extraBufferCapacity = 1)
    val archivedEvents = _archivedEvents.asSharedFlow()

    // Expose all unarchived events (for count/filters computation if needed, but wait: we want distinct kinds!)
    val allEvents = database.healthEventDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val events = kotlinx.coroutines.flow.combine(_query, _selectedKind, _limit) { q, k, l ->
        Triple(q, k, l)
    }.flatMapLatest { (q, k, l) ->
        database.healthEventDao().observeFiltered(q.trim(), k, l)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) {
        _query.value = q
        // Reset limit on search to avoid loading too many elements initially
        _limit.value = 50
    }

    fun setSelectedKind(kind: String?) {
        _selectedKind.value = kind
        _limit.value = 50
    }

    fun loadMore() {
        _limit.value = _limit.value + 50
    }

    fun addEvent(kind: String, title: String, details: String, severity: Int?) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        database.healthEventDao().insert(
            HealthEventEntity(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                details = details.trim(),
                kind = kind,
                effectiveAtEpochMillis = now,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                status = "ACTIVE",
                severity = severity
            )
        )
    }

    fun archive(event: HealthEventEntity) = viewModelScope.launch {
        database.healthEventDao().archive(event.id, System.currentTimeMillis())
        _archivedEvents.tryEmit(event)
    }

    fun unarchive(event: HealthEventEntity) = viewModelScope.launch {
        database.healthEventDao().unarchive(event.id, System.currentTimeMillis())
    }

    fun delete(event: HealthEventEntity) = viewModelScope.launch {
        event.imageAttachmentId?.let {
            secureFileStore.delete(it)
            database.documentDao().delete(it)
        }
        database.healthEventDao().delete(event.id)
    }
}
