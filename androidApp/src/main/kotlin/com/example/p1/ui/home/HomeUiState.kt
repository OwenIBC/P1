package com.example.p1.ui.home

import com.example.p1.model.Formula
import com.example.p1.model.InputField
import com.example.p1.model.ValidationError

/** Estado inmutable que la pantalla de Inicio sabe renderizar. */
data class HomeUiState(
    val formula: Formula,
    val fieldErrors: Map<InputField, ValidationError> = emptyMap(),
    val feedback: Feedback = Feedback.Idle,
)

/** Retroalimentación tras pulsar "Calcular" (controla meme y tarjeta de resultado). */
sealed interface Feedback {
    /** Aún no se ha calculado nada para la fórmula actual. */
    data object Idle : Feedback

    /** Datos inválidos → meme1. */
    data object Failure : Feedback

    /** Cálculo correcto → meme2 + valor. */
    data class Success(val result: Double) : Feedback
}

