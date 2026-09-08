package com.example.tonisfarm.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailScreen(
    monthKey: String,
    onNavigateBack: () -> Unit,
    viewModel: MonthDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    var transactionToEdit by remember { mutableStateOf<com.example.tonisfarm.domain.model.FinancialTransaction?>(null) }
    var transactionToDelete by remember { mutableStateOf<com.example.tonisfarm.domain.model.FinancialTransaction?>(null) }

    LaunchedEffect(monthKey) {
        viewModel.loadMonthDetail(monthKey)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes - $monthKey") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.incomes.isEmpty() && uiState.expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma receita ou despesa cadastrada para este mês",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Seção de Receitas
                if (uiState.incomes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Receitas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.incomes) { income ->
                        IncomeCard(
                            income = income,
                            currencyFormat = currencyFormat,
                            onEdit = { transactionToEdit = income },
                            onDelete = { transactionToDelete = income }
                        )
                    }
                }

                // Seção de Despesas
                if (uiState.expenses.isNotEmpty()) {
                    item {
                        Text(
                            text = "Despesas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.expenses) { expense ->
                        ExpenseCard(
                            expense = expense,
                            currencyFormat = currencyFormat,
                            onEdit = { transactionToEdit = expense },
                            onDelete = { transactionToDelete = expense }
                        )
                    }
                }
            }
        }
    }

    // Diálogos de edição
    transactionToEdit?.let { transaction ->
        when (transaction.tipo) {
            com.example.tonisfarm.domain.model.TipoTransacao.RECEITA -> {
                AddIncomeDialog(
                    onDismiss = { transactionToEdit = null },
                    onConfirm = { updatedTransaction ->
                        viewModel.updateTransaction(updatedTransaction)
                        transactionToEdit = null
                    },
                    transaction = transaction
                )
            }
            com.example.tonisfarm.domain.model.TipoTransacao.DESPESA -> {
                AddExpenseDialog(
                    onDismiss = { transactionToEdit = null },
                    onConfirm = { updatedTransaction ->
                        viewModel.updateTransaction(updatedTransaction)
                        transactionToEdit = null
                    },
                    transaction = transaction
                )
            }
        }
    }

    // Diálogo de exclusão
    transactionToDelete?.let { transaction ->
        val transactionType = when (transaction.tipo) {
            com.example.tonisfarm.domain.model.TipoTransacao.RECEITA -> "Receita"
            com.example.tonisfarm.domain.model.TipoTransacao.DESPESA -> "Despesa"
        }
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Excluir $transactionType") },
            text = { Text("Tem certeza que deseja excluir esta $transactionType?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction)
                        transactionToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

