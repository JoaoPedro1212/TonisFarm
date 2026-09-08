package com.example.tonisfarm.ui.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMonthDetail: (String) -> Unit = {},
    viewModel: FinanceHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico Financeiro") },
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
        } else if (uiState.monthlySummaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma transação registrada",
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
                items(uiState.monthlySummaries) { summary ->
                    HistoryMonthCard(
                        summary = summary,
                        currencyFormat = currencyFormat,
                        onClick = { onNavigateToMonthDetail(summary.month) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryMonthCard(
    summary: MonthlySummary,
    currencyFormat: NumberFormat,
    onClick: () -> Unit = {}
) {
    val profitColor = when {
        summary.lucro > 0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
        summary.lucro < 0 -> androidx.compose.ui.graphics.Color(0xFFF44336) // Vermelho
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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

