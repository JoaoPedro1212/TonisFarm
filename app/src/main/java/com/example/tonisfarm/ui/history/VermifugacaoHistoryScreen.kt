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
fun VermifugacaoHistoryScreen(
    cowId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (Long, Long?) -> Unit,
    viewModel: VermifugacaoHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var vermifugacaoToDelete by remember { mutableStateOf<com.example.tonisfarm.domain.model.Vermifugacao?>(null) }

    LaunchedEffect(cowId) {
        viewModel.init(cowId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Vermifugação") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(cowId, null) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Vermifugação")
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
                    // Filtro por vermífugo
                    DrugFilterDropdown(
                        selectedDrug = uiState.selectedDrugName,
                        onDrugSelected = viewModel::updateDrugFilter,
                        allDrugs = uiState.vermifugacoes.map { it.drugName }.distinct()
                    )

                    // Filtro por data
                    DateFilterRow(
                        filterType = uiState.dateFilterType,
                        dateValue = uiState.dateFilterValue,
                        onFilterTypeChanged = viewModel::updateDateFilterType,
                        onDateValueChanged = viewModel::updateDateFilterValue
                    )
                }

                if (uiState.filteredVermifugacoes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.vermifugacoes.isEmpty()) 
                                "Nenhuma vermifugação cadastrada" 
                            else 
                                "Nenhuma vermifugação encontrada com os filtros aplicados",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredVermifugacoes) { vermifugacao ->
                            VermifugacaoListItem(
                                vermifugacao = vermifugacao,
                                onEdit = { onNavigateToForm(cowId, vermifugacao.id) },
                                onDelete = { vermifugacaoToDelete = vermifugacao }
                            )
                        }
                    }
                }
            }
        }
    }

    vermifugacaoToDelete?.let { vermifugacao ->
        AlertDialog(
            onDismissRequest = { vermifugacaoToDelete = null },
            title = { Text("Excluir Vermifugação") },
            text = { Text("Tem certeza que deseja excluir esta vermifugação?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVermifugacao(vermifugacao)
                        vermifugacaoToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { vermifugacaoToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun VermifugacaoListItem(
    vermifugacao: com.example.tonisfarm.domain.model.Vermifugacao,
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
                    text = vermifugacao.drugName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Data: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(vermifugacao.date)}",
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
fun DrugFilterDropdown(
    selectedDrug: String,
    onDrugSelected: (String) -> Unit,
    allDrugs: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    val drugs = listOf("Todas") + allDrugs.sorted()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedDrug,
            onValueChange = {},
            readOnly = true,
            label = { Text("Filtrar por Vermífugo") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            drugs.forEach { drug ->
                DropdownMenuItem(
                    text = { Text(drug) },
                    onClick = {
                        onDrugSelected(drug)
                        expanded = false
                    }
                )
            }
        }
    }
}

