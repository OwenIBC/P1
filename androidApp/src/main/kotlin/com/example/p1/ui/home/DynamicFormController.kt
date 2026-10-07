package com.example.p1.ui.home

import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged
import com.example.p1.databinding.ItemFormInputBinding
import com.example.p1.model.Formula
import com.example.p1.model.InputField
import com.example.p1.model.ValidationError

/**
 * Construye y mantiene los campos del formulario para la fórmula activa.
 *
 * Sólo se infla un [ItemFormInputBinding] por campo pertinente, de modo que el
 * formulario muestra exclusivamente las entradas que la fórmula necesita.
 * Su ciclo de vida es el de la vista del fragmento (se crea en `onViewCreated`).
 */
class DynamicFormController(
    private val container: LinearLayout,
    private val inflater: LayoutInflater,
) {
    private val rows = linkedMapOf<InputField, ItemFormInputBinding>()

    // `var` necesario: recuerda qué fórmula está inflada para no reconstruir en cada emisión.
    private var renderedFormula: Formula? = null

    /**
     * Reconstruye los campos si la fórmula cambió.
     * @return `true` si se reconstruyó (útil para actualizar imagen/expresión sólo entonces).
     */
    fun bind(
        formula: Formula,
        initialText: (InputField) -> String,
        onTextChanged: (InputField, String) -> Unit,
    ): Boolean {
        if (formula == renderedFormula) return false
        renderedFormula = formula

        container.removeAllViews()
        rows.clear()

        val lastIndex = formula.fields.lastIndex
        formula.fields.forEachIndexed { index, field ->
            val row = ItemFormInputBinding.inflate(inflater, container, true)
            val context = row.root.context

            row.root.apply {
                hint = context.getString(field.labelRes)
                helperText = context.getString(field.hintRes)
                suffixText = field.unitRes?.let(context::getString)
            }
            row.inputEditText.apply {
                inputType = field.inputType
                imeOptions = if (index == lastIndex) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_NEXT
                // Se fija el texto ANTES de añadir el listener para no borrar errores restaurados.
                setText(initialText(field))
                doAfterTextChanged { editable -> onTextChanged(field, editable?.toString().orEmpty()) }
            }
            rows[field] = row
        }
        return true
    }

    /** Muestra (o limpia) el error de cada campo mediante `TextInputLayout.error`. */
    fun showErrors(errors: Map<InputField, ValidationError>, resolve: (ValidationError) -> String) {
        rows.forEach { (field, row) ->
            val message = errors[field]?.let(resolve)
            // Evita re-animar el mismo error en cada emisión de estado.
            if (row.root.error?.toString() != message) row.root.error = message
        }
    }

    /** Lleva el foco al primer campo con error (accesibilidad y UX). */
    fun focusFirstError(errors: Map<InputField, ValidationError>) {
        rows.entries.firstOrNull { (field, _) -> field in errors }?.value?.inputEditText?.requestFocus()
    }

    /** Asigna una acción al botón "Hecho" del teclado en el último campo. */
    fun setOnDoneAction(action: () -> Unit) {
        rows.values.lastOrNull()?.inputEditText?.setOnEditorActionListener { _, actionId, _ ->
            (actionId == EditorInfo.IME_ACTION_DONE).also { isDone -> if (isDone) action() }
        }
    }
}

