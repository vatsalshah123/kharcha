package com.kharcha.app.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kharcha.app.data.ExpenseRepository
import com.kharcha.app.model.Expense
import com.kharcha.app.model.currencyByCode
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repo: ExpenseRepository,
    onAddExpense: () -> Unit,
    onEditExpense: (Expense) -> Unit
) {
    var filterCurrency by remember { mutableStateOf("All") }
    var pendingDelete by remember { mutableStateOf<Expense?>(null) }

    val currenciesUsed = remember(repo.expenses.size) {
        repo.expenses.map { it.currencyCode }.distinct().sorted()
    }
    val visible = remember(repo.expenses.size, filterCurrency) {
        if (filterCurrency == "All") repo.expenses
        else repo.expenses.filter { it.currencyCode == filterCurrency }
    }
    val totalInr = remember(repo.expenses.size, filterCurrency) {
        visible.sumOf { it.inrAmount }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kharcha") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (filterCurrency == "All") "Total spent" else "Total spent ($filterCurrency)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "₹ " + formatAmount(totalInr),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${visible.size} expense${if (visible.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (currenciesUsed.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterCurrency == "All",
                            onClick = { filterCurrency = "All" },
                            label = { Text("All") }
                        )
                    }
                    items(currenciesUsed) { code ->
                        FilterChip(
                            selected = filterCurrency == code,
                            onClick = { filterCurrency = code },
                            label = { Text(code) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (visible.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No expenses yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Tap + to log your first expense",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visible, key = { it.id }) { expense ->
                        ExpenseRow(
                            expense = expense,
                            onClick = { onEditExpense(expense) },
                            onDelete = { pendingDelete = expense }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    pendingDelete?.let { toDelete ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete expense?") },
            text = { Text("This will remove \"${toDelete.note.ifBlank { toDelete.category }}\" permanently.") },
            confirmButton = {
                TextButton(onClick = {
                    repo.deleteExpense(toDelete.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseRow(expense: Expense, onClick: () -> Unit, onDelete: () -> Unit) {
    val currency = currencyByCode(expense.currencyCode)
    val dateStr = remember(expense.dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(expense.dateMillis)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onDelete)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.note.ifBlank { expense.category },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${expense.category} · $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Rate: 1 ${currency.code} = ₹${formatRate(expense.rateToInr)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${currency.symbol}${formatAmount(expense.foreignAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹ ${formatAmount(expense.inrAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

internal fun formatAmount(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        "%,d".format(value.toLong())
    } else {
        "%,.2f".format(value)
    }
}

internal fun formatRate(value: Double): String {
    return if (value >= 1) "%,.4f".format(value).trimEnd('0').trimEnd('.')
    else "%.6f".format(value).trimEnd('0').trimEnd('.')
}
