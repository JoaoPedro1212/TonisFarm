package com.example.tonisfarm.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDespesas: () -> Unit = {},
    onNavigateToReceitas: () -> Unit = {},
    onNavigateToMonthDetail: (String) -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCharts: () -> Unit = {},
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financeiro") },
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Mês Atual",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FinanceCard(
                            title = "Receitas",
                            value = currencyFormat.format(uiState.currentMonthReceitas),
                            modifier = Modifier.weight(1f),
                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50), // Verde
                            onClick = onNavigateToReceitas
                        )
                        FinanceCard(
                            title = "Despesas",
                            value = currencyFormat.format(uiState.currentMonthDespesas),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.errorContainer,
                            onClick = onNavigateToDespesas
                        )
                    }
                }

                item {
                    val situationColor = when {
                        uiState.currentMonthLucro > 0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
                        uiState.currentMonthLucro < 0 -> androidx.compose.ui.graphics.Color(0xFFF44336) // Vermelho
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.currentMonthLucro >= 0)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Situação do Mês",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = currencyFormat.format(uiState.currentMonthLucro),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = situationColor
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FinanceActionCard(
                            title = "Histórico Financeiro",
                            icon = Icons.Filled.History,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToHistory
                        )
                        FinanceActionCard(
                            title = "Gráficos",
                            icon = Icons.Filled.ShowChart,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCharts
                        )
                    }
                }

                item {
                    Text(
                        text = "Registros do Mês",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Mostrar transações do mês atual
                if (uiState.currentMonthTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Nenhuma transação registrada no mês atual",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(uiState.currentMonthTransactions) { transaction ->
                        TransactionCard(transaction, currencyFormat)
                    }
                }

            }
        }
    }
}

@Composable
fun FinanceCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HistoricalMonthCard(
    summary: MonthlySummary,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    val profitColor = when {
        summary.lucro > 0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
        summary.lucro < 0 -> androidx.compose.ui.graphics.Color(0xFFF44336) // Vermelho
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = summary.month,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currencyFormat.format(summary.lucro),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = profitColor
            )
        }
    }
}

@Composable
fun FinanceActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: com.example.tonisfarm.domain.model.FinancialTransaction,
    currencyFormat: NumberFormat
) {
    val transactionColor = when (transaction.tipo) {
        com.example.tonisfarm.domain.model.TipoTransacao.RECEITA -> 
            androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
        com.example.tonisfarm.domain.model.TipoTransacao.DESPESA -> 
            androidx.compose.ui.graphics.Color(0xFFF44336) // Vermelho
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.tipo.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (transaction.descricao != null && transaction.descricao.isNotBlank()) {
                    Text(
                        text = transaction.descricao,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(transaction.data),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = currencyFormat.format(transaction.valor),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = transactionColor
            )
        }
    }
}

