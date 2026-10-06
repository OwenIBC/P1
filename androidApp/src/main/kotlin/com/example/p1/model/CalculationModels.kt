package com.example.p1.model

import androidx.annotation.StringRes

/**
 * Error de validación asociado a un campo. Se guarda como referencia a recurso
 * (y argumentos de formato ya formateados) para que la capa de modelo no dependa
 * de un [android.content.Context].
 */
data class ValidationError(
    @param:StringRes val messageRes: Int,
    val formatArgs: List<String> = emptyList(),
)

/** Valores numéricos ya parseados. Un campo ausente devuelve `NaN` (nunca lanza excepción). */
@JvmInline
value class FormulaInputs(private val values: Map<InputField, Double>) {
    operator fun get(field: InputField): Double = values[field] ?: Double.NaN
}

/** Resultado de evaluar una fórmula: o bien un valor, o bien errores por campo. */
sealed interface CalculationOutcome {

    /** Cálculo correcto. [inputs] contiene sólo los campos de [formula]. */
    data class Success(
        val formula: Formula,
        val inputs: Map<InputField, Double>,
        val result: Double,
    ) : CalculationOutcome

    /** Datos inválidos o ausentes: no se ha realizado ningún cálculo. */
    data class Invalid(
        val errors: Map<InputField, ValidationError>,
    ) : CalculationOutcome
}

/** Entrada inmutable del historial de la sesión. */
data class HistoryEntry(
    val id: Long,
    val formula: Formula,
    val inputs: Map<InputField, Double>,
    val result: Double,
    val timestampMillis: Long,
)

