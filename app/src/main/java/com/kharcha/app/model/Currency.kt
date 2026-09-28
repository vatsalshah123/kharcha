package com.kharcha.app.model

/**
 * A currency the user can log expenses in. [code] is the ISO-ish code shown
 * throughout the app and used as the key for the "last used rate" memory.
 */
data class Currency(
    val code: String,
    val name: String,
    val symbol: String
)

/** A starter list of currencies common for Indian travellers. Users can add a custom one too. */
val commonCurrencies: List<Currency> = listOf(
    Currency("INR", "Indian Rupee", "₹"),
    Currency("USD", "US Dollar", "$"),
    Currency("EUR", "Euro", "€"),
    Currency("GBP", "British Pound", "£"),
    Currency("VND", "Vietnamese Dong", "₫"),
    Currency("THB", "Thai Baht", "฿"),
    Currency("SGD", "Singapore Dollar", "S$"),
    Currency("MYR", "Malaysian Ringgit", "RM"),
    Currency("IDR", "Indonesian Rupiah", "Rp"),
    Currency("AED", "UAE Dirham", "AED"),
    Currency("JPY", "Japanese Yen", "¥"),
    Currency("AUD", "Australian Dollar", "A$"),
    Currency("NPR", "Nepalese Rupee", "Rs"),
    Currency("LKR", "Sri Lankan Rupee", "Rs")
)

fun currencyByCode(code: String): Currency =
    commonCurrencies.find { it.code.equals(code, ignoreCase = true) }
        ?: Currency(code.uppercase(), code.uppercase(), code.uppercase())

/** Default categories shown as quick-pick chips; user can also type a custom one. */
val defaultCategories: List<String> = listOf(
    "Food", "Transport", "Stay", "Shopping", "Activities", "Sim/Data", "Visa/Fees", "Other"
)
