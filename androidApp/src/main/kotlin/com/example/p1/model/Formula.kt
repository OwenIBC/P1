package com.example.p1.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.p1.R
import com.example.p1.util.NumberFormatter
import com.example.p1.util.degreesToRadians
import com.example.p1.util.radiansToDegrees
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sin

/**
 * Las cuatro fórmulas de la calculadora.
 *
 * Cada constante encapsula **todo** lo que la define:
 *  - presentación (título, expresión, imagen, símbolo y unidad del resultado),
 *  - campos que necesita (en el orden en que se muestran),
 *  - reglas de validación del dominio físico ([validate]),
 *  - el cálculo puro ([compute]), que sólo se invoca con datos ya validados.
 *
 * Añadir una nueva fórmula = añadir una constante; la UI se adapta sola.
 */
enum class Formula(
    @param:StringRes val titleRes: Int,
    @param:StringRes val expressionRes: Int,
    @param:DrawableRes val imageRes: Int,
    @param:StringRes val resultSymbolRes: Int,
    @param:StringRes val resultUnitRes: Int,
    val fields: List<InputField>,
) {

    /** F = |q| · v · B · sin(θ) */
    MAGNETIC_FORCE(
        titleRes = R.string.formula_magnetic_title,
        expressionRes = R.string.formula_magnetic_expression,
        imageRes = R.drawable.formula_fuerza,
        resultSymbolRes = R.string.symbol_force,
        resultUnitRes = R.string.unit_newton,
        fields = listOf(InputField.CHARGE, InputField.VELOCITY, InputField.MAGNETIC_FIELD, InputField.THETA_MAGNETIC),
    ) {
        override fun validate(inputs: FormulaInputs) = validationOf(inputs) {
            // q admite cualquier signo (se usa |q|); sólo debe ser numérico.
            ensure(InputField.VELOCITY, R.string.error_must_be_non_negative) { it >= 0.0 }
            ensure(InputField.MAGNETIC_FIELD, R.string.error_must_be_non_negative) { it >= 0.0 }
            ensure(InputField.THETA_MAGNETIC, R.string.error_angle_range_180) { it in 0.0..180.0 }
        }

        override fun compute(inputs: FormulaInputs): Double {
            val q = inputs[InputField.CHARGE]
            val v = inputs[InputField.VELOCITY]
            val b = inputs[InputField.MAGNETIC_FIELD]
            val sine = sin(inputs[InputField.THETA_MAGNETIC].degreesToRadians()).cleanFloatingNoise()
            return abs(q) * v * b * sine
        }
    },

    /** Q = k · A · (T₁ − T₂) / L */
    HEAT_CONDUCTION(
        titleRes = R.string.formula_heat_title,
        expressionRes = R.string.formula_heat_expression,
        imageRes = R.drawable.formula_calor,
        resultSymbolRes = R.string.symbol_heat_rate,
        resultUnitRes = R.string.unit_watt,
        fields = listOf(
            InputField.THERMAL_CONDUCTIVITY,
            InputField.AREA,
            InputField.TEMPERATURE_HOT,
            InputField.TEMPERATURE_COLD,
            InputField.THICKNESS,
        ),
    ) {
        override fun validate(inputs: FormulaInputs) = validationOf(inputs) {
            ensure(InputField.THERMAL_CONDUCTIVITY, R.string.error_must_be_positive) { it > 0.0 }
            ensure(InputField.AREA, R.string.error_must_be_positive) { it > 0.0 }
            ensure(InputField.THICKNESS, R.string.error_must_be_positive) { it > 0.0 }
            ensure(InputField.TEMPERATURE_HOT, R.string.error_below_absolute_zero) { it >= ABSOLUTE_ZERO_CELSIUS }
            ensure(InputField.TEMPERATURE_COLD, R.string.error_below_absolute_zero) { it >= ABSOLUTE_ZERO_CELSIUS }

            // Regla cruzada: sólo tiene sentido si ambas temperaturas son válidas por separado.
            val hot = InputField.TEMPERATURE_HOT
            val cold = InputField.TEMPERATURE_COLD
            if (allValid(hot, cold) && valueOf(hot) <= valueOf(cold)) {
                fail(hot, ValidationError(R.string.error_t1_not_greater))
                fail(cold, ValidationError(R.string.error_t2_not_lower))
            }
        }

        override fun compute(inputs: FormulaInputs): Double {
            val k = inputs[InputField.THERMAL_CONDUCTIVITY]
            val a = inputs[InputField.AREA]
            val deltaT = inputs[InputField.TEMPERATURE_HOT] - inputs[InputField.TEMPERATURE_COLD]
            val l = inputs[InputField.THICKNESS]
            return (k * a * deltaT) / l
        }
    },

    /** Ec = ½ · m · v² */
    KINETIC_ENERGY(
        titleRes = R.string.formula_kinetic_title,
        expressionRes = R.string.formula_kinetic_expression,
        imageRes = R.drawable.formula_cinetica,
        resultSymbolRes = R.string.symbol_kinetic_energy,
        resultUnitRes = R.string.unit_joule,
        fields = listOf(InputField.MASS, InputField.VELOCITY),
    ) {
        override fun validate(inputs: FormulaInputs) = validationOf(inputs) {
            ensure(InputField.MASS, R.string.error_must_be_positive) { it > 0.0 }
            ensure(InputField.VELOCITY, R.string.error_must_be_non_negative) { it >= 0.0 }
        }

        override fun compute(inputs: FormulaInputs): Double {
            val m = inputs[InputField.MASS]
            val v = inputs[InputField.VELOCITY]
            return 0.5 * m * v * v
        }
    },

    /** θ₂ = arcsin((n₁ · sin θ₁) / n₂) */
    SNELL_REFRACTION(
        titleRes = R.string.formula_snell_title,
        expressionRes = R.string.formula_snell_expression,
        imageRes = R.drawable.formula_snell,
        resultSymbolRes = R.string.symbol_refraction_angle,
        resultUnitRes = R.string.unit_degrees,
        fields = listOf(InputField.REFRACTIVE_INDEX_1, InputField.REFRACTIVE_INDEX_2, InputField.THETA_INCIDENCE),
    ) {
        override fun validate(inputs: FormulaInputs) = validationOf(inputs) {
            val n1Field = InputField.REFRACTIVE_INDEX_1
            val n2Field = InputField.REFRACTIVE_INDEX_2
            val thetaField = InputField.THETA_INCIDENCE

            ensure(n1Field, R.string.error_refractive_index) { it >= MIN_REFRACTIVE_INDEX }
            ensure(n2Field, R.string.error_refractive_index) { it >= MIN_REFRACTIVE_INDEX }
            ensure(thetaField, R.string.error_angle_range_90) { it in 0.0..90.0 }

            // Reflexión total interna: n₁·sin θ₁ / n₂ > 1 ⇒ arcsin indefinido.
            if (allValid(n1Field, n2Field, thetaField)) {
                val n1 = valueOf(n1Field)
                val n2 = valueOf(n2Field)
                val ratio = n1 * sin(valueOf(thetaField).degreesToRadians()) / n2
                if (ratio > 1.0) {
                    // Sólo puede ocurrir si n₁ > n₂, por lo que n₂/n₁ ∈ [0, 1).
                    val criticalAngle = asin(n2 / n1).radiansToDegrees()
                    fail(
                        thetaField,
                        ValidationError(
                            R.string.error_total_internal_reflection,
                            listOf(NumberFormatter.format(criticalAngle)),
                        ),
                    )
                }
            }
        }

        override fun compute(inputs: FormulaInputs): Double {
            val n1 = inputs[InputField.REFRACTIVE_INDEX_1]
            val n2 = inputs[InputField.REFRACTIVE_INDEX_2]
            val theta1 = inputs[InputField.THETA_INCIDENCE].degreesToRadians()
            // coerceIn protege frente a errores de redondeo en el límite (ratio ≈ 1).
            val ratio = (n1 * sin(theta1) / n2).coerceIn(-1.0, 1.0)
            return asin(ratio).radiansToDegrees()
        }
    };

    /** Reglas del dominio físico. Devuelve un mapa vacío si los datos son válidos. */
    abstract fun validate(inputs: FormulaInputs): Map<InputField, ValidationError>

    /** Cálculo puro. **Precondición:** [validate] devolvió un mapa vacío. */
    abstract fun compute(inputs: FormulaInputs): Double

    companion object {
        const val ABSOLUTE_ZERO_CELSIUS = -273.15
        const val MIN_REFRACTIVE_INDEX = 1.0
        private const val FLOATING_NOISE_EPSILON = 1e-12

        /** Fórmula seleccionada al abrir la app por primera vez. */
        val DEFAULT = MAGNETIC_FORCE

        /** Elimina residuos de coma flotante, p. ej. sin(180°) ≈ 1.2e-16 → 0. */
        private fun Double.cleanFloatingNoise(): Double =
            if (abs(this) < FLOATING_NOISE_EPSILON) 0.0 else this
    }
}
