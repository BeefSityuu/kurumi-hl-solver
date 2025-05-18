package com.sityuu.kurumiHlSolver

import kotlin.math.pow
import kotlin.math.round

object RandUtil {
    fun round(value: Double, decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces)
        return round(value * factor) / factor
    }
}