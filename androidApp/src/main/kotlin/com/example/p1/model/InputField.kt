package com.example.p1.model

import android.text.InputType
import androidx.annotation.StringRes
import com.example.p1.R

/**
 * Catálogo de todas las magnitudes de entrada que pueden aparecer en un formulario.
 *
 * Cada campo describe cómo debe presentarse en la UI (etiqueta, pista, unidad, símbolo)
 * sin contener texto "hardcodeado": todo se resuelve desde `strings.xml`.
 *
 * @property labelRes  Etiqueta flotante del [com.google.android.material.textfield.TextInputLayout].
 * @property hintRes   Pista mostrada como helper text bajo el campo.
 * @property symbolRes Símbolo corto usado en el historial (p. ej. "q", "T₁").
 * @property unitRes   Unidad mostrada como sufijo; `null` si la magnitud es adimensional.
 * @property acceptsNegative Indica si el teclado debe permitir el signo menos.
 */
enum class InputField(
    @param:StringRes val labelRes: Int,
    @param:StringRes val hintRes: Int,
    @param:StringRes val symbolRes: Int,
    @param:StringRes val unitRes: Int?,
    val acceptsNegative: Boolean = false,
) {
    // --- Fuerza magnética ---
    CHARGE(R.string.label_charge, R.string.hint_charge, R.string.symbol_charge, R.string.unit_coulomb, acceptsNegative = true),
    VELOCITY(R.string.label_velocity, R.string.hint_velocity, R.string.symbol_velocity, R.string.unit_meters_per_second),
    MAGNETIC_FIELD(R.string.label_magnetic_field, R.string.hint_magnetic_field, R.string.symbol_magnetic_field, R.string.unit_tesla),
    THETA_MAGNETIC(R.string.label_theta_magnetic, R.string.hint_theta_magnetic, R.string.symbol_theta, R.string.unit_degrees),

    // --- Conducción de calor ---
    THERMAL_CONDUCTIVITY(R.string.label_conductivity, R.string.hint_conductivity, R.string.symbol_conductivity, R.string.unit_conductivity),
    AREA(R.string.label_area, R.string.hint_area, R.string.symbol_area, R.string.unit_square_meters),
    TEMPERATURE_HOT(R.string.label_temp_hot, R.string.hint_temp_hot, R.string.symbol_temp_hot, R.string.unit_celsius, acceptsNegative = true),
    TEMPERATURE_COLD(R.string.label_temp_cold, R.string.hint_temp_cold, R.string.symbol_temp_cold, R.string.unit_celsius, acceptsNegative = true),
    THICKNESS(R.string.label_thickness, R.string.hint_thickness, R.string.symbol_thickness, R.string.unit_meters),

    // --- Energía cinética ---
    MASS(R.string.label_mass, R.string.hint_mass, R.string.symbol_mass, R.string.unit_kilograms),

    // --- Ley de Snell ---
    REFRACTIVE_INDEX_1(R.string.label_n1, R.string.hint_n1, R.string.symbol_n1, unitRes = null),
    REFRACTIVE_INDEX_2(R.string.label_n2, R.string.hint_n2, R.string.symbol_n2, unitRes = null),
    THETA_INCIDENCE(R.string.label_theta_incidence, R.string.hint_theta_incidence, R.string.symbol_theta_incidence, R.string.unit_degrees);

    /** Tipo de teclado numérico adecuado para el campo. */
    val inputType: Int
        get() = InputType.TYPE_CLASS_NUMBER or
            InputType.TYPE_NUMBER_FLAG_DECIMAL or
            (if (acceptsNegative) InputType.TYPE_NUMBER_FLAG_SIGNED else 0)
}

