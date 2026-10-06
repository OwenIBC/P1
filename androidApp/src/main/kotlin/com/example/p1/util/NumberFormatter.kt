package com.example.p1.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Formateo decimal de resultados respetando la configuración regional.
 *
 * - Valores "normales": hasta 4 decimales con separador de miles (`1.234,5678`).
 * - Valores muy pequeños o muy grandes: notación científica (`1,6E-19`), para que
 *   magnitudes como la carga del electrón no se muestren como `0`.
 */
object NumberFormatter {

    private const val PATTERN_DECIMAL = "#,##0.####"
    private const val PATTERN_SCIENTIFIC = "0.####E0"
    private const val SCIENTIFIC_LOWER_BOUND = 1e-3
    private const val SCIENTIFIC_UPPER_BOUND = 1e7

    fun format(value: Double, locale: Locale = Locale.getDefault()): String {
        val normalized = value + 0.0 // convierte -0.0 en 0.0
        val magnitude = abs(normalized)
        val useScientific = magnitude != 0.0 &&
            (magnitude < SCIENTIFIC_LOWER_BOUND || magnitude >= SCIENTIFIC_UPPER_BOUND)
        val pattern = if (useScientific) PATTERN_SCIENTIFIC else PATTERN_DECIMAL
        // DecimalFormat no es thread-safe: se crea una instancia por llamada.
        return DecimalFormat(pattern, DecimalFormatSymbols.getInstance(locale)).format(normalized)
    }
}

