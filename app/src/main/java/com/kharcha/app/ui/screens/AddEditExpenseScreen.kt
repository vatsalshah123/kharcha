package com.kharcha.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kharcha.app.data.ExpenseRepository
import com.kharcha.app.model.Expense
import com.kharcha.app.model.commonCurrencies
import com.kharcha.app.model.currencyByCode
import com.kharcha.app.model.defaultCategories
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    repo: ExpenseRepository,
    existing: Expense?,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val isEdit = existing != null

    var currencyCode by remember { mutableStateOf(existing?.currencyCode ?: "VND") }
    var currencyMenuExpanded by remember { mutableStateOf(false) }
    var customCurrencyMode by remember {
        mutableStateOf(existing != null && commonCurrencies.none { it.code == existing.currencyCode })
    }

    var amountText by remember {
        mutableStateOf(existing?.foreignAmount?.let { formatPlain(it) } ?: "")
    }
    var rateText by remember {
        mutableStateOf(
            existing?.rateToInr?.let { formatPlain(it) }
                ?: repo.lastRates[currencyCode]?.let { formatPlain(it) }
                ?: ""
        )
    }
    var category by remember { mutableStateOf(existing?.category ?: defaultCategories.first()) }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var dateMillis by remember { mutableStateOf(existing?.dateMillis ?: System.currentTimeMillis()) }

    val amount = amountText.toDoubleOrNull()
    val rate = rateText.toDoubleOrNull()
    val inrPreview = if (amount != null && rate != null) amount * rate else null

    val dateStr = remember(dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dateMillis)
    }

    fun pickDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val c = Calendar.getInstance()
                c.set(year, month, day, 12, 0)
                dateMillis = c.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun canSave(): Boolean = amount != null && amount > 0 && rate != null && rate > 0 && currencyCode.isNotBlank()

    fun save() {
        val amt = amount ?: return
        val rt = rate ?: return
        val code = currencyCode.trim().uppercase()
        val e = Expense(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            dateMillis = dateMillis,
            category = category,
            note = note.trim(),
            currencyCode = code,
            foreignAmount = amt,
            rateToInr = rt,
            inrAmount = amt * rt
        )
        if (isEdit) repo.updateExpense(e) else repo.addExpense(e)
        onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit expense" else "Add expense") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Currency picker
            if (!customCurrencyMode) {
                ExposedDropdownMenuBox(
                    expanded = currencyMenuExpanded,
                    onExpandedChange = { currencyMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${currencyByCode(currencyCode).code} — ${currencyByCode(currencyCode).name}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency paid in") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    androidx.compose.material3.ExposedDropdownMenu(
                        expanded = currencyMenuExpanded,
                        onDismissRequest = { currencyMenuExpanded = false }
                    ) {
                        commonCurrencies.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.code} — ${c.name}") },
                                onClick = {
                                    currencyCode = c.code
                                    currencyMenuExpanded = false
                                    repo.lastRates[c.code]?.let { rateText = formatPlain(it) }
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Other currency…") },
                            onClick = {
                                customCurrencyMode = true
                                currencyMenuExpanded = false
                            }
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = currencyCode,
                    onValueChange = { currencyCode = it.uppercase().take(6) },
                    label = { Text("Currency code (e.g. KRW)") },
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.material3.TextButton(onClick = { customCurrencyMode = false }) {
                    Text("Pick from list instead")
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount paid (in $currencyCode)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rateText,
                onValueChange = { rateText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Rate: 1 $currencyCode = ? INR") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    repo.lastRates[currencyCode.uppercase()]?.let { saved ->
                        Text("Last used rate for $currencyCode: ${formatPlain(saved)}")
                    }
                }
            )
            repo.lastRates[currencyCode.uppercase()]?.let { saved ->
                if (formatPlain(saved) != rateText) {
                    androidx.compose.material3.OutlinedButton(onClick = { rateText = formatPlain(saved) }) {
                        Text("Use last rate (${formatPlain(saved)})")
                    }
                }
            }

            if (inrPreview != null) {
                Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Equivalent in INR", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "₹ ${formatAmount(inrPreview)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column {
                Text("Category", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    defaultCategories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (e.g. street food in Hanoi)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dateStr,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    androidx.compose.material3.TextButton(onClick = { pickDate() }) { Text("Change") }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { save() },
                enabled = canSave(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Save changes" else "Add expense")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

internal fun formatPlain(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString()
    else value.toString().trimEnd('0').trimEnd('.')
}
