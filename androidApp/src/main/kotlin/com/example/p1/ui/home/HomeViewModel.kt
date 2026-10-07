package com.example.p1.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.p1.domain.FormulaCalculator
import com.example.p1.model.CalculationOutcome
import com.example.p1.model.Formula
import com.example.p1.model.InputField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel de la pantalla de Inicio.
 *
 * La fórmula seleccionada y el texto de cada campo se guardan en [SavedStateHandle],
 * de modo que sobreviven a rotaciones, cambios de pestaña e incluso a la muerte del proceso.
 */
class HomeViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(formula = restoreFormula()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Cambia de fórmula limpiando entradas, errores y resultado previos. */
    fun selectFormula(formula: Formula) {
        if (formula == _uiState.value.formula) return
        savedStateHandle[KEY_FORMULA] = formula.name
        InputField.entries.forEach { field -> savedStateHandle.remove<String>(inputKey(field)) }
        _uiState.value = HomeUiState(formula = formula)
    }

    /** Texto actual de un campo (vacío si nunca se escribió). */
    fun inputText(field: InputField): String = savedStateHandle[inputKey(field)] ?: ""

    /** Guarda lo que escribe el usuario y retira el error de ese campo, si lo había. */
    fun onInputChanged(field: InputField, text: String) {
        savedStateHandle[inputKey(field)] = text
        _uiState.update { state ->
            if (field in state.fieldErrors) state.copy(fieldErrors = state.fieldErrors - field) else state
        }
    }

    /**
     * Valida y calcula. Actualiza el estado (errores / resultado) y devuelve el
     * [CalculationOutcome] para que la UI dispare los efectos (audio, historial).
     */
    fun calculate(): CalculationOutcome {
        val state = _uiState.value
        val rawInputs = state.formula.fields.associateWith(::inputText)
        val outcome = FormulaCalculator.evaluate(state.formula, rawInputs)

        _uiState.value = when (outcome) {
            is CalculationOutcome.Success -> state.copy(
                fieldErrors = emptyMap(),
                feedback = Feedback.Success(outcome.result),
            )
            is CalculationOutcome.Invalid -> state.copy(
                fieldErrors = outcome.errors,
                feedback = Feedback.Failure,
            )
        }
        return outcome
    }

    private fun restoreFormula(): Formula =
        savedStateHandle.get<String>(KEY_FORMULA)
            ?.let { name -> Formula.entries.firstOrNull { it.name == name } }
            ?: Formula.DEFAULT

    private companion object {
        const val KEY_FORMULA = "selected_formula"
        fun inputKey(field: InputField) = "input_${field.name}"
    }
}

