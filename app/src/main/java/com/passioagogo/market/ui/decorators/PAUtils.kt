package com.passioagogo.market.ui.decorators

import androidx.core.text.isDigitsOnly

fun Double?.orZero(): Double {
    return this ?: 0.0
}

fun Int?.orZero(): Int {
    return this ?: 0
}

fun Long?.orZero(): Long {
    return this ?: 0L
}

fun String?.toDoubleSafe() : Double{
    return this?.let {
        if(it.isNotEmpty() && it.isDigitsOnly()){
            it.toDouble()
        } else {
            0.0
        }
    } ?: run {
        0.0
    }
}

fun String?.toIntSafe() : Int{
    return this?.let {
        if(it.isNotEmpty() && it.isDigitsOnly()){
            it.toInt()
        } else {
            0
        }
    } ?: run {
        0
    }
}
