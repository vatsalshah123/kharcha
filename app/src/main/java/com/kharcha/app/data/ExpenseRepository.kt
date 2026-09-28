package com.kharcha.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.kharcha.app.model.Expense
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persists expenses and per-currency "last used rate" memory to a small JSON file in
 * the app's private storage. No network, no database dependency — everything the app
 * needs works fully offline, and the data never leaves the phone.
 */
class ExpenseRepository(context: Context) {

    private val file = File(context.filesDir, "kharcha_data.json")

    /** Backing list observed directly by Compose UI. Newest first. */
    val expenses = mutableStateListOf<Expense>()

    /** currencyCode -> last rate the user entered for it (INR per 1 unit). */
    val lastRates = mutableStateMapOf<String, Double>()

    init {
        load()
    }

    fun addExpense(expense: Expense) {
        expenses.add(0, expense)
        lastRates[expense.currencyCode.uppercase()] = expense.rateToInr
        save()
    }

    fun updateExpense(expense: Expense) {
        val idx = expenses.indexOfFirst { it.id == expense.id }
        if (idx >= 0) {
            expenses[idx] = expense
            lastRates[expense.currencyCode.uppercase()] = expense.rateToInr
            save()
        }
    }

    fun deleteExpense(id: String) {
        expenses.removeAll { it.id == id }
        save()
    }

    fun setLastRate(currencyCode: String, rate: Double) {
        lastRates[currencyCode.uppercase()] = rate
        save()
    }

    private fun load() {
        if (!file.exists()) return
        runCatching {
            val root = JSONObject(file.readText())

            val arr = root.optJSONArray("expenses") ?: JSONArray()
            val loaded = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Expense(
                    id = o.getString("id"),
                    dateMillis = o.getLong("dateMillis"),
                    category = o.getString("category"),
                    note = o.optString("note", ""),
                    currencyCode = o.getString("currencyCode"),
                    foreignAmount = o.getDouble("foreignAmount"),
                    rateToInr = o.getDouble("rateToInr"),
                    inrAmount = o.getDouble("inrAmount")
                )
            }
            expenses.clear()
            expenses.addAll(loaded.sortedByDescending { it.dateMillis })

            val rates = root.optJSONObject("lastRates") ?: JSONObject()
            lastRates.clear()
            rates.keys().forEach { key -> lastRates[key] = rates.getDouble(key) }
        }
    }

    private fun save() {
        runCatching {
            val arr = JSONArray()
            expenses.forEach { e ->
                val o = JSONObject()
                o.put("id", e.id)
                o.put("dateMillis", e.dateMillis)
                o.put("category", e.category)
                o.put("note", e.note)
                o.put("currencyCode", e.currencyCode)
                o.put("foreignAmount", e.foreignAmount)
                o.put("rateToInr", e.rateToInr)
                o.put("inrAmount", e.inrAmount)
                arr.put(o)
            }
            val rates = JSONObject()
            lastRates.forEach { (k, v) -> rates.put(k, v) }

            val root = JSONObject()
            root.put("expenses", arr)
            root.put("lastRates", rates)
            file.writeText(root.toString())
        }
    }
}
