package mohaamadreza.saemipour.no.vazheh.ui.util

fun Int.toPersianDigits(): String = toString().map { digit ->
    when (digit) {
        '0' -> '۰'
        '1' -> '۱'
        '2' -> '۲'
        '3' -> '۳'
        '4' -> '۴'
        '5' -> '۵'
        '6' -> '۶'
        '7' -> '۷'
        '8' -> '۸'
        '9' -> '۹'
        else -> digit
    }
}.joinToString("")
