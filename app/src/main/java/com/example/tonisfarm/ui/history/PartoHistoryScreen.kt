@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tonisfarm.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tonisfarm.ui.cowform.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PartoHistoryScreen(
    cowId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (Long, Long?) -> Unit,
    viewModel: PartoHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var partoToDelete by remember { mutableStateOf<com.example.tonisfarm.domain.model.Parto?>(null) }

    LaunchedEffect(cowId) {
        viewModel.init(cowId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Partos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(cowId, null) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Parto")
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filtros
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Filtro por data
                    DateFilterRow(
                        filterType = uiState.dateFilterType,
                        dateValue = uiState.dateFilterValue,
                        onFilterTypeChanged = viewModel::updateDateFilterType,
                        onDateValueChanged = viewModel::updateDateFilterValue
                    )

                    // Filtro por quantidade de bezerros
                    if (uiState.partos.isNotEmpty()) {
                        CalvesCountFilterRow(
                            selectedCount = uiState.calvesCountFilter,
                            onCountSelected = viewModel::updateCalvesCountFilter,
                            allCounts = uiState.partos.map { it.calvesCount }.distinct().sorted()
                        )
                    }
                }

                if (uiState.filteredPartos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.partos.isEmpty()) 
                                "Nenhum parto cadastrado" 
                            else 
                                "Nenhum parto encontrado com os filtros aplicados",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredPartos) { parto ->
                            PartoListItem(
                                parto = parto,
                                onEdit = { onNavigateToForm(cowId, parto.id) },
                                onDelete = { partoToDelete = parto }
                            )
                        }
                    }
                }
            }
        }
    }

    partoToDelete?.let { parto ->
        AlertDialog(
            onDismissRequest = { partoToDelete = null },
            title = { Text("Excluir Parto") },
            text = { Text("Tem certeza que deseja excluir este parto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteParto(parto)
                        partoToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { partoToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PartoListItem(
    parto: com.example.tonisfarm.domain.model.Parto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${parto.displayCalvesCount} bezerro${if (parto.isCalvesCountInformed && parto.calvesCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Data: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parto.date)}",
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

@Composable
fun CalvesCountFilterRow(
    selectedCount: String?,
    onCountSelected: (String?) -> Unit,
    allCounts: List<Int>
) {
    var expanded by remember { mutableStateOf(false) }
    val counts = listOf("Todas" to null) + allCounts.map { it.toString() to it.toString() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCount ?: "Todas",
            onValueChange = {},
            readOnly = true,
            label = { Text("Filtrar por Quantidade de Bezerros") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            counts.forEach { (label, count) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onCountSelected(count)
                        expanded = false
                    }
                )
            }
        }
    }
}

