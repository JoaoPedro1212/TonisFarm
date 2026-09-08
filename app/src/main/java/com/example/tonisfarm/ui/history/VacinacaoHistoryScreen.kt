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
fun VacinacaoHistoryScreen(
    cowId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (Long, Long?) -> Unit,
    viewModel: VacinacaoHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var vacinacaoToDelete by remember { mutableStateOf<com.example.tonisfarm.domain.model.Vacinacao?>(null) }

    LaunchedEffect(cowId) {
        viewModel.init(cowId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Vacinação") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(cowId, null) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Vacinação")
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
                    // Filtro por vacina
                    VaccineFilterDropdown(
                        selectedVaccine = uiState.selectedVaccineName,
                        onVaccineSelected = viewModel::updateVaccineFilter,
                        allVaccines = uiState.vacinacoes.map { it.vaccineName }.distinct()
                    )

                    // Filtro por data
                    DateFilterRow(
                        filterType = uiState.dateFilterType,
                        dateValue = uiState.dateFilterValue,
                        onFilterTypeChanged = viewModel::updateDateFilterType,
                        onDateValueChanged = viewModel::updateDateFilterValue
                    )
                }

                if (uiState.filteredVacinacoes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.vacinacoes.isEmpty()) 
                                "Nenhuma vacinação cadastrada" 
                            else 
                                "Nenhuma vacinação encontrada com os filtros aplicados",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredVacinacoes) { vacinacao ->
                            VacinacaoListItem(
                                vacinacao = vacinacao,
                                onEdit = { onNavigateToForm(cowId, vacinacao.id) },
                                onDelete = { vacinacaoToDelete = vacinacao }
                            )
                        }
                    }
                }
            }
        }
    }

    vacinacaoToDelete?.let { vacinacao ->
        AlertDialog(
            onDismissRequest = { vacinacaoToDelete = null },
            title = { Text("Excluir Vacinação") },
            text = { Text("Tem certeza que deseja excluir esta vacinação?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVacinacao(vacinacao)
                        vacinacaoToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { vacinacaoToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun VacinacaoListItem(
    vacinacao: com.example.tonisfarm.domain.model.Vacinacao,
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
                    text = vacinacao.vaccineName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Data: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(vacinacao.date)}",
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
fun VaccineFilterDropdown(
    selectedVaccine: String,
    onVaccineSelected: (String) -> Unit,
    allVaccines: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    val vaccines = listOf("Todas") + allVaccines.sorted()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedVaccine,
            onValueChange = {},
            readOnly = true,
            label = { Text("Filtrar por Vacina") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            vaccines.forEach { vaccine ->
                DropdownMenuItem(
                    text = { Text(vaccine) },
                    onClick = {
                        onVaccineSelected(vaccine)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DateFilterRow(
    filterType: String?,
    dateValue: Date?,
    onFilterTypeChanged: (String?) -> Unit,
    onDateValueChanged: (Date?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val filterTypes = listOf(
        "Sem filtro" to null,
        "Antes de" to "ANTES_DE",
        "Depois de" to "DEPOIS_DE",
        "No dia" to "NO_DIA"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = filterType?.let { type ->
                    when (type) {
                        "ANTES_DE" -> "Antes de"
                        "DEPOIS_DE" -> "Depois de"
                        "NO_DIA" -> "No dia"
                        else -> "Sem filtro"
                    }
                } ?: "Sem filtro",
                onValueChange = {},
                readOnly = true,
                label = { Text("Filtro de Data") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filterTypes.forEach { (label, type) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onFilterTypeChanged(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (filterType != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = dateValue?.let { 
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) 
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (dateValue != null) {
                            IconButton(
                                onClick = { onDateValueChanged(null) }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Limpar"
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateValue?.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onConfirm = {
                datePickerState.selectedDateMillis?.let {
                    onDateValueChanged(Date(it))
                }
                showDatePicker = false
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

