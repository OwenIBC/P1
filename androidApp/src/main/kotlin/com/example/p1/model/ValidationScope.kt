package com.example.p1.model

import androidx.annotation.StringRes

/**
 * Pequeño DSL para declarar reglas de validación de forma legible.
 *
 * Sólo se registra el **primer** error de cada campo, de modo que los mensajes
 * más básicos (p. ej. "debe ser > 0") tienen prioridad sobre las reglas cruzadas.
 */
class ValidationScope(private val inputs: FormulaInputs) {

    private val errors = linkedMapOf<InputField, ValidationError>()

    /** Valor numérico del campo (NaN si faltara). */
    fun valueOf(field: InputField): Double = inputs[field]

    /** Registra [messageRes] en [field] si el valor no cumple [predicate]. */
    fun ensure(field: InputField, @StringRes messageRes: Int, predicate: (Double) -> Boolean) {
        if (!predicate(inputs[field])) fail(field, ValidationError(messageRes))
    }

    /** Registra un error explícito en [field] (si aún no tenía uno). */
    fun fail(field: InputField, error: ValidationError) {
        errors.putIfAbsent(field, error)
    }

    /** `true` si ninguno de los campos indicados tiene errores todavía. */
    fun allValid(vararg fields: InputField): Boolean = fields.none(errors::containsKey)

    fun errors(): Map<InputField, ValidationError> = errors.toMap()
}

/** Ejecuta [rules] y devuelve el mapa de errores resultante (vacío si todo es válido). */
inline fun validationOf(
    inputs: FormulaInputs,
    rules: ValidationScope.() -> Unit,
): Map<InputField, ValidationError> = ValidationScope(inputs).apply(rules).errors()
