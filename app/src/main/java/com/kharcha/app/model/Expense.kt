package com.kharcha.app.model

import java.util.UUID

/**
 * One expense entry. The original amount and currency are kept exactly as paid,
 * alongside the INR value computed at the exchange rate that applied *at the time*.
 * [rateToInr] and [inrAmount] are frozen at save time so later rate changes never
 * silently rewrite past entries.
 */
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val dateMillis: Long,
    val category: String,
    val note: String,
    val currencyCode: String,
    val foreignAmount: Double,
    /** How many INR one unit of [currencyCode] was worth when this was recorded. */
    val rateToInr: Double,
    /** foreignAmount * rateToInr, stored explicitly so it never drifts. */
    val inrAmount: Double
)
