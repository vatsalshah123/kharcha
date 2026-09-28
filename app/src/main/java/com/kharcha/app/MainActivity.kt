package com.kharcha.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kharcha.app.data.ExpenseRepository
import com.kharcha.app.model.Expense
import com.kharcha.app.ui.screens.AddEditExpenseScreen
import com.kharcha.app.ui.screens.HomeScreen
import com.kharcha.app.ui.theme.KharchaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = ExpenseRepository(applicationContext)

        setContent {
            KharchaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KharchaApp(repo)
                }
            }
        }
    }
}

private sealed class Screen {
    data object Home : Screen()
    data class AddEdit(val existing: Expense?) : Screen()
}

@Composable
private fun KharchaApp(repo: ExpenseRepository) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    when (val s = screen) {
        is Screen.Home -> HomeScreen(
            repo = repo,
            onAddExpense = { screen = Screen.AddEdit(null) },
            onEditExpense = { screen = Screen.AddEdit(it) }
        )
        is Screen.AddEdit -> AddEditExpenseScreen(
            repo = repo,
            existing = s.existing,
            onDone = { screen = Screen.Home }
        )
    }
}
