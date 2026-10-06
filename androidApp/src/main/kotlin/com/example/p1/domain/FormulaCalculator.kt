package com.example.p1.domain

import com.example.p1.R
import com.example.p1.model.CalculationOutcome
import com.example.p1.model.Formula
import com.example.p1.model.FormulaInputs
import com.example.p1.model.InputField
import com.example.p1.model.ValidationError

/**
 * Punto de entrada único de la lógica de negocio: recibe el texto crudo del formulario
 * y devuelve un [CalculationOutcome]. Es Kotlin puro (sin Android) y, por tanto,
 * testeable con JUnit en la JVM.
 *
 * Flujo:
 *  1. Campos vacíos → `error_required`; texto no numérico → `error_invalid_number`.
 *  2. Reglas de dominio de la fórmula ([Formula.validate]).
 *  3. Sólo si todo es válido se ejecuta [Formula.compute].
 */
object FormulaCalculator {

    fun evaluate(formula: Formula, rawInputs: Map<InputField, String>): CalculationOutcome {
        // --- 1. Parseo y campos obligatorios ---------------------------------------------
        val parseErrors = linkedMapOf<InputField, ValidationError>()
        val parsed = linkedMapOf<InputField, Double>()

        formula.fields.forEach { field ->
            val raw = rawInputs[field].orEmpty().trim()
            val value = parseNumber(raw)
            when {
                raw.isEmpty() -> parseErrors[field] = ValidationError(R.string.error_required)
                value == null -> parseErrors[field] = ValidationError(R.string.error_invalid_number)
                else -> parsed[field] = value
            }
        }
        if (parseErrors.isNotEmpty()) return CalculationOutcome.Invalid(parseErrors)

        // --- 2. Reglas del dominio físico ------------------------------------------------
        val inputs = FormulaInputs(parsed)
        val domainErrors = formula.validate(inputs)
        if (domainErrors.isNotEmpty()) return CalculationOutcome.Invalid(domainErrors)

        // --- 3. Cálculo -------------------------------------------------------------------
        val result = formula.compute(inputs)
        return if (result.isFinite()) {
            CalculationOutcome.Success(formula, parsed.toMap(), result)
        } else {
            // Desbordamiento (p. ej. valores astronómicos): se trata como dato inválido.
            CalculationOutcome.Invalid(
                formula.fields.associateWith { ValidationError(R.string.error_result_out_of_range) },
            )
        }
    }

    /**
     * Acepta tanto "1.5" como "1,5" (teclados en español) y notación científica ("1.6e-19").
     * Rechaza NaN/Infinity para que nunca lleguen al cálculo.
     */
    internal fun parseNumber(raw: String): Double? =
        raw.replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf { it.isFinite() }
}

