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
import com.example.tonisfarm.util.WeightFilterType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PesagemHistoryScreen(
    cowId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (Long, Long?) -> Unit,
    viewModel: PesagemHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var pesagemToDelete by remember { mutableStateOf<com.example.tonisfarm.domain.model.Pesagem?>(null) }

    LaunchedEffect(cowId) {
        viewModel.init(cowId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Pesagem") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(cowId, null) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Pesagem")
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

                    // Filtro por peso
                    WeightFilterRow(
                        filterType = uiState.weightFilterType,
                        weightValue = uiState.weightValue,
                        onFilterTypeChanged = viewModel::updateWeightFilterType,
                        onWeightValueChanged = viewModel::updateWeightValue
                    )
                }

                if (uiState.filteredPesagens.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.pesagens.isEmpty()) 
                                "Nenhuma pesagem cadastrada" 
                            else 
                                "Nenhuma pesagem encontrada com os filtros aplicados",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredPesagens) { pesagem ->
                            PesagemListItem(
                                pesagem = pesagem,
                                onEdit = { onNavigateToForm(cowId, pesagem.id) },
                                onDelete = { pesagemToDelete = pesagem }
                            )
                        }
                    }
                }
            }
        }
    }

    pesagemToDelete?.let { pesagem ->
        AlertDialog(
            onDismissRequest = { pesagemToDelete = null },
            title = { Text("Excluir Pesagem") },
            text = { Text("Tem certeza que deseja excluir esta pesagem?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePesagem(pesagem)
                        pesagemToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pesagemToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PesagemListItem(
    pesagem: com.example.tonisfarm.domain.model.Pesagem,
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
                    text = "${pesagem.displayWeight} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Data: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(pesagem.date)}",
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
fun WeightFilterRow(
    filterType: String?,
    weightValue: String,
    onFilterTypeChanged: (String?) -> Unit,
    onWeightValueChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filterTypes = listOf(
        "Sem filtro" to null,
        "Mais de" to "MAIS_DE",
        "Menos de" to "MENOS_DE",
        "Igual a" to "IGUAL_A"
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
                        "MAIS_DE" -> "Mais de"
                        "MENOS_DE" -> "Menos de"
                        "IGUAL_A" -> "Igual a"
                        else -> "Sem filtro"
                    }
                } ?: "Sem filtro",
                onValueChange = {},
                readOnly = true,
                label = { Text("Filtro de Peso") },
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
            OutlinedTextField(
                value = weightValue,
                onValueChange = onWeightValueChanged,
                label = { Text("Peso (kg)") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

