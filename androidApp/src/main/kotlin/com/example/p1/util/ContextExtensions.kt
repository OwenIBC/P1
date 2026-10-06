package com.example.p1.util

import android.content.Context
import androidx.annotation.StringRes
import com.example.p1.R
import com.example.p1.model.ValidationError

/** "símbolo = valor unidad" totalmente localizado (p. ej. "F = 12,5 N"). */
fun Context.formatQuantity(
    @StringRes symbolRes: Int,
    value: Double,
    @StringRes unitRes: Int?,
): String = getString(
    R.string.quantity_format,
    getString(symbolRes),
    NumberFormatter.format(value),
    unitRes?.let(::getString).orEmpty(),
).trim()

/** Resuelve un [ValidationError] a su mensaje localizado. */
fun Context.resolve(error: ValidationError): String =
    getString(error.messageRes, *error.formatArgs.toTypedArray())

