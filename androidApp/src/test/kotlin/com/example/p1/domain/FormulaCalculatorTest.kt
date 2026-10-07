package com.example.p1.domain

import com.example.p1.R
import com.example.p1.model.CalculationOutcome
import com.example.p1.model.Formula
import com.example.p1.model.InputField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormulaCalculatorTest {

    private fun evaluate(formula: Formula, vararg inputs: Pair<InputField, String>) =
        FormulaCalculator.evaluate(formula, inputs.toMap())

    private fun CalculationOutcome.resultOrFail(): Double =
        (this as? CalculationOutcome.Success)?.result ?: error("Se esperaba Success y llegó $this")

    private fun CalculationOutcome.errorsOrFail() =
        (this as? CalculationOutcome.Invalid)?.errors ?: error("Se esperaba Invalid y llegó $this")

    // --- Comunes ------------------------------------------------------------------------

    @Test
    fun `campos vacios marcan error_required y no calculan`() {
        val errors = evaluate(Formula.KINETIC_ENERGY, InputField.MASS to " ").errorsOrFail()
        assertEquals(R.string.error_required, errors[InputField.MASS]?.messageRes)
        assertEquals(R.string.error_required, errors[InputField.VELOCITY]?.messageRes)
    }

    @Test
    fun `texto no numerico marca error_invalid_number`() {
        val errors = evaluate(Formula.KINETIC_ENERGY, InputField.MASS to "abc", InputField.VELOCITY to "2").errorsOrFail()
        assertEquals(R.string.error_invalid_number, errors[InputField.MASS]?.messageRes)
        assertEquals(1, errors.size)
    }

    @Test
    fun `acepta coma decimal y notacion cientifica`() {
        assertEquals(1.5, FormulaCalculator.parseNumber("1,5") ?: Double.NaN, 0.0)
        assertEquals(1.6e-19, FormulaCalculator.parseNumber("1.6e-19") ?: Double.NaN, 0.0)
        assertEquals(null, FormulaCalculator.parseNumber("NaN"))
        assertEquals(null, FormulaCalculator.parseNumber("Infinity"))
    }

    // --- Fuerza magnética ---------------------------------------------------------------

    @Test
    fun `fuerza magnetica usa valor absoluto de q`() {
        val result = evaluate(
            Formula.MAGNETIC_FORCE,
            InputField.CHARGE to "-2",
            InputField.VELOCITY to "3",
            InputField.MAGNETIC_FIELD to "4",
            InputField.THETA_MAGNETIC to "90",
        ).resultOrFail()
        assertEquals(24.0, result, 1e-9)
    }

    @Test
    fun `fuerza magnetica a 180 grados es exactamente cero`() {
        val result = evaluate(
            Formula.MAGNETIC_FORCE,
            InputField.CHARGE to "1",
            InputField.VELOCITY to "1",
            InputField.MAGNETIC_FIELD to "1",
            InputField.THETA_MAGNETIC to "180",
        ).resultOrFail()
        assertEquals(0.0, result, 0.0)
    }

    @Test
    fun `fuerza magnetica rechaza angulo fuera de rango y velocidad negativa`() {
        val errors = evaluate(
            Formula.MAGNETIC_FORCE,
            InputField.CHARGE to "1",
            InputField.VELOCITY to "-1",
            InputField.MAGNETIC_FIELD to "1",
            InputField.THETA_MAGNETIC to "200",
        ).errorsOrFail()
        assertEquals(R.string.error_must_be_non_negative, errors[InputField.VELOCITY]?.messageRes)
        assertEquals(R.string.error_angle_range_180, errors[InputField.THETA_MAGNETIC]?.messageRes)
    }

    // --- Conducción de calor ------------------------------------------------------------

    @Test
    fun `fourier calcula el flujo de calor`() {
        val result = evaluate(
            Formula.HEAT_CONDUCTION,
            InputField.THERMAL_CONDUCTIVITY to "200",
            InputField.AREA to "0.5",
            InputField.TEMPERATURE_HOT to "100",
            InputField.TEMPERATURE_COLD to "20",
            InputField.THICKNESS to "0.1",
        ).resultOrFail()
        assertEquals(80_000.0, result, 1e-6)
    }

    @Test
    fun `fourier exige T1 mayor que T2`() {
        val errors = evaluate(
            Formula.HEAT_CONDUCTION,
            InputField.THERMAL_CONDUCTIVITY to "1",
            InputField.AREA to "1",
            InputField.TEMPERATURE_HOT to "20",
            InputField.TEMPERATURE_COLD to "20",
            InputField.THICKNESS to "1",
        ).errorsOrFail()
        assertEquals(R.string.error_t1_not_greater, errors[InputField.TEMPERATURE_HOT]?.messageRes)
        assertEquals(R.string.error_t2_not_lower, errors[InputField.TEMPERATURE_COLD]?.messageRes)
    }

    @Test
    fun `fourier rechaza temperaturas bajo el cero absoluto`() {
        val errors = evaluate(
            Formula.HEAT_CONDUCTION,
            InputField.THERMAL_CONDUCTIVITY to "1",
            InputField.AREA to "1",
            InputField.TEMPERATURE_HOT to "10",
            InputField.TEMPERATURE_COLD to "-300",
            InputField.THICKNESS to "1",
        ).errorsOrFail()
        assertEquals(R.string.error_below_absolute_zero, errors[InputField.TEMPERATURE_COLD]?.messageRes)
        assertEquals(1, errors.size)
    }

    // --- Energía cinética ---------------------------------------------------------------

    @Test
    fun `energia cinetica correcta`() {
        val result = evaluate(Formula.KINETIC_ENERGY, InputField.MASS to "2", InputField.VELOCITY to "3").resultOrFail()
        assertEquals(9.0, result, 1e-9)
    }

    @Test
    fun `energia cinetica exige masa positiva`() {
        val errors = evaluate(Formula.KINETIC_ENERGY, InputField.MASS to "0", InputField.VELOCITY to "3").errorsOrFail()
        assertEquals(R.string.error_must_be_positive, errors[InputField.MASS]?.messageRes)
    }

    // --- Ley de Snell -------------------------------------------------------------------

    @Test
    fun `snell aire a agua`() {
        val result = evaluate(
            Formula.SNELL_REFRACTION,
            InputField.REFRACTIVE_INDEX_1 to "1",
            InputField.REFRACTIVE_INDEX_2 to "1.33",
            InputField.THETA_INCIDENCE to "30",
        ).resultOrFail()
        assertEquals(22.0824, result, 1e-3)
    }

    @Test
    fun `snell detecta reflexion total interna`() {
        val errors = evaluate(
            Formula.SNELL_REFRACTION,
            InputField.REFRACTIVE_INDEX_1 to "1.5",
            InputField.REFRACTIVE_INDEX_2 to "1",
            InputField.THETA_INCIDENCE to "60",
        ).errorsOrFail()
        val error = errors[InputField.THETA_INCIDENCE]
        assertEquals(R.string.error_total_internal_reflection, error?.messageRes)
        assertTrue(error?.formatArgs.orEmpty().isNotEmpty())
    }

    @Test
    fun `snell exige indices mayores o iguales que uno`() {
        val errors = evaluate(
            Formula.SNELL_REFRACTION,
            InputField.REFRACTIVE_INDEX_1 to "0.9",
            InputField.REFRACTIVE_INDEX_2 to "1",
            InputField.THETA_INCIDENCE to "10",
        ).errorsOrFail()
        assertEquals(R.string.error_refractive_index, errors[InputField.REFRACTIVE_INDEX_1]?.messageRes)
    }
}

