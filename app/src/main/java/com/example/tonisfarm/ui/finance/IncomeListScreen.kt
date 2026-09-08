package com.example.tonisfarm.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.tonisfarm.domain.model.RecurrenceType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddIncome: () -> Unit,
    viewModel: IncomeListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<com.example.tonisfarm.domain.model.FinancialTransaction?>(null) }
    var transactionToDelete by remember { mutableStateOf<com.example.tonisfarm.domain.model.FinancialTransaction?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadCurrentMonthIncomes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receitas do Mês") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Receita")
            }
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
        } else if (uiState.incomes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma receita registrada no mês atual",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.incomes) { income ->
                    IncomeCard(
                        income = income,
                        currencyFormat = currencyFormat,
                        onEdit = { transactionToEdit = income },
                        onDelete = { transactionToDelete = income }
                    )
                }
            }
        }
    }

    if (showAddDialog || transactionToEdit != null) {
        AddIncomeDialog(
            onDismiss = { 
                showAddDialog = false
                transactionToEdit = null
            },
            onConfirm = { transaction ->
                viewModel.addIncome(transaction)
                showAddDialog = false
                transactionToEdit = null
            },
            transaction = transactionToEdit
        )
    }

    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Excluir Receita") },
            text = { Text("Tem certeza que deseja excluir esta receita?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteIncome(transaction)
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

@Composable
fun IncomeCard(
    income: com.example.tonisfarm.domain.model.FinancialTransaction,
    currencyFormat: NumberFormat,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Rótulo de recorrência no canto superior direito
            if (income.recurrenceType != RecurrenceType.NONE) {
                RecurrenceBadge(
                    recurrenceType = income.recurrenceType,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currencyFormat.format(income.valor),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
                    )
                    if (income.descricao != null && income.descricao.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = income.descricao,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(income.data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

