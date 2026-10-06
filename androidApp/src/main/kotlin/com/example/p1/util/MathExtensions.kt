package com.example.p1.util

import kotlin.math.PI

/** Conversión de grados a radianes (Kotlin-first, sin depender de java.lang.Math). */
fun Double.degreesToRadians(): Double = this * PI / 180.0

/** Conversión de radianes a grados. */
fun Double.radiansToDegrees(): Double = this * 180.0 / PI

