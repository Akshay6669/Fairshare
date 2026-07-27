package com.akshay.fairshare.domain.model

/**
 * Money is stored as an integral number of minor units (cents).
 *
 * Floating point is never used for monetary values: 0.1 + 0.2 != 0.3 in IEEE-754, and
 * repeated splitting of a Double amount accumulates error that eventually surfaces as a
 * group balance which refuses to reach zero. Every amount in this app is a Long.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money) = Money(cents + other.cents)
    operator fun minus(other: Money) = Money(cents - other.cents)
    operator fun unaryMinus() = Money(-cents)

    val isZero: Boolean get() = cents == 0L
    val isPositive: Boolean get() = cents > 0L
    val isNegative: Boolean get() = cents < 0L
    val absolute: Money get() = Money(kotlin.math.abs(cents))

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    /** Renders as a plain decimal string: 1234 -> "12.34", -5 -> "-0.05". */
    fun format(): String {
        val sign = if (cents < 0) "-" else ""
        val abs = kotlin.math.abs(cents)
        return "%s%d.%02d".format(sign, abs / 100, abs % 100)
    }

    companion object {
        val ZERO = Money(0)

        /**
         * Parses user input such as "12", "12.3", "12.34".
         * Returns null rather than throwing, because this runs on every keystroke.
         */
        fun parse(input: String): Money? {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return null
            val negative = trimmed.startsWith("-")
            val body = trimmed.removePrefix("-").removePrefix("+")
            if (!body.matches(Regex("""\d+(\.\d{0,2})?"""))) return null
            val parts = body.split(".")
            val whole = parts[0].toLongOrNull() ?: return null
            val fraction = parts.getOrNull(1).orEmpty().padEnd(2, '0').toLong()
            val total = whole * 100 + fraction
            return Money(if (negative) -total else total)
        }
    }
}

fun Iterable<Money>.sum(): Money = Money(sumOf { it.cents })
