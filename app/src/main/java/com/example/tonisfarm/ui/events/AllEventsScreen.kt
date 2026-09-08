@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tonisfarm.ui.events

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tonisfarm.domain.model.TipoEvento
import com.example.tonisfarm.domain.model.TipoEventoExtensions.getDisplayName
import com.example.tonisfarm.ui.cowform.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AllEventsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEventForm: (Long?, Long?) -> Unit,
    onNavigateToEventDetail: (Long) -> Unit = {},
    viewModel: AllEventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var eventoToDelete by remember { mutableStateOf<com.example.tonisfarm.domain.model.Evento?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAllEventos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todos os Eventos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEventForm(null, null) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Evento")
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
                    // Filtro por Tipo
                    TipoEventoFilterDropdown(
                        selectedTipo = uiState.selectedTipo,
                        onTipoSelected = viewModel::updateTipoFilter
                    )

                    // Filtro por Data
                    DateFilterRow(
                        filterType = uiState.dateFilterType,
                        dateValue = uiState.dateFilterValue,
                        onFilterTypeChanged = viewModel::updateDateFilterType,
                        onDateValueChanged = viewModel::updateDateFilterValue
                    )
                }

                if (uiState.filteredEventos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.eventos.isEmpty()) 
                                "Nenhum evento cadastrado" 
                            else 
                                "Nenhum evento encontrado com os filtros aplicados",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredEventos) { evento ->
                            EventListItem(
                                evento = evento,
                                onClick = {
                                    onNavigateToEventDetail(evento.id)
                                },
                                onEdit = { eventoId ->
                                    onNavigateToEventForm(null, eventoId)
                                },
                                onDelete = { eventoToDelete = evento }
                            )
                        }
                    }
                }
            }
        }
    }

    eventoToDelete?.let { evento ->
        AlertDialog(
            onDismissRequest = { eventoToDelete = null },
            title = { Text("Excluir Evento") },
            text = { Text("Tem certeza que deseja excluir este evento?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvento(evento)
                        eventoToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventoToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EventListItem(
    evento: com.example.tonisfarm.domain.model.Evento,
    onClick: () -> Unit = {},
    onEdit: (Long) -> Unit,
    onDelete: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.tipoEvento.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (evento.descricao != null && evento.descricao.isNotBlank()) {
                    Text(
                        text = evento.descricao,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                // Data/hora do evento
                val dataEventoFormat = if (evento.horaEvento != null) {
                    // Combinar data do evento com hora do evento
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = evento.dataEvento
                    val horaCalendar = java.util.Calendar.getInstance()
                    horaCalendar.time = evento.horaEvento
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, horaCalendar.get(java.util.Calendar.HOUR_OF_DAY))
                    calendar.set(java.util.Calendar.MINUTE, horaCalendar.get(java.util.Calendar.MINUTE))
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                } else {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(evento.dataEvento)
                }
                Text(
                    text = "Data do evento: $dataEventoFormat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Tipo de vacina/vermífugo (se aplicável)
                if (evento.vaccineType != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Vacina: ${evento.vaccineType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (evento.dewormingType != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Vermífugo: ${evento.dewormingType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // Data/hora do alerta (se existir)
                if (evento.dataAlerta != null) {
                    Text(
                        text = "Alerta: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(evento.dataAlerta)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row {
                IconButton(onClick = { onEdit(evento.id) }) {
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
fun TipoEventoFilterDropdown(
    selectedTipo: String,
    onTipoSelected: (String) -> Unit
) {
    val tipos = listOf("Todos", "PESAGEM", "PARTO", "VACINACAO", "VERMIFUGACAO", "OUTRO")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedTipo,
            onValueChange = {},
            readOnly = true,
            label = { Text("Filtrar por Tipo") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            tipos.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(if (tipo == "Todos") tipo else TipoEvento.valueOf(tipo).getDisplayName()) },
                    onClick = {
                        onTipoSelected(tipo)
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

