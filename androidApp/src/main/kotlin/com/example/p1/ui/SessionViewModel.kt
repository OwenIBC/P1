package com.example.p1.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.p1.data.SettingsRepository
import com.example.p1.model.CalculationOutcome
import com.example.p1.model.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

/**
 * Estado compartido entre las tres pestañas (con alcance de Activity):
 *  - Historial de la sesión (en memoria: se pierde al cerrar la app, tal como se pide).
 *  - Preferencias de usuario (persistidas en [SettingsRepository]).
 */
class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val idGenerator = AtomicLong(0L)

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    /** Cálculos válidos de la sesión, del más reciente al más antiguo. */
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    private val _soundEnabled = MutableStateFlow(settingsRepository.isSoundEnabled())
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    fun record(outcome: CalculationOutcome.Success) {
        val entry = HistoryEntry(
            id = idGenerator.incrementAndGet(),
            formula = outcome.formula,
            inputs = outcome.inputs,
            result = outcome.result,
            timestampMillis = System.currentTimeMillis(),
        )
        _history.update { current -> listOf(entry) + current }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    fun setSoundEnabled(enabled: Boolean) {
        settingsRepository.setSoundEnabled(enabled)
        _soundEnabled.value = enabled
    }
}

