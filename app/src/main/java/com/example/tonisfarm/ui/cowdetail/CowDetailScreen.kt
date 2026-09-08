package com.example.tonisfarm.ui.cowdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tonisfarm.domain.model.TipoEventoExtensions.getDisplayName
import com.example.tonisfarm.ui.cowform.DatePickerDialog
import com.example.tonisfarm.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CowDetailScreen(
    cowId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToEventForm: (Long?) -> Unit,
    onNavigateToVacinacaoHistory: (Long) -> Unit = {},
    onNavigateToVermifugacaoHistory: (Long) -> Unit = {},
    onNavigateToPesagemHistory: (Long) -> Unit = {},
    onNavigateToPartoHistory: (Long) -> Unit = {},
    viewModel: CowDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cowId) {
        viewModel.loadCow(cowId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Vaca") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEventForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Evento")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.cow == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Vaca não encontrada")
            }
        } else {
            val cow = uiState.cow ?: return@Scaffold
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = cow.identificacao,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (cow.nome != null) {
                                Text(
                                    text = cow.nome,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow("Raça", cow.raca)
                            DetailRow(
                                "Data de Nascimento",
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cow.dataNascimento)
                            )
                            DetailRow("Idade", DateUtils.calculateAge(cow.dataNascimento))
                            DetailRow("Peso", "${String.format("%.1f", cow.pesoKg)} kg")
                            if (cow.estaPrenha) {
                                val mesesAtual = cow.getMesesPrenhezAtual() ?: 0
                                DetailRow(
                                    "Status",
                                    "Prenha (${mesesAtual} meses)",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (cow.observacoes != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Observações",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = cow.observacoes,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Histórico",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Vacinação
                            ClickableHistoryRow(
                                label = "Vacinação",
                                date = uiState.lastVacinacao,
                                onClick = { onNavigateToVacinacaoHistory(cowId) }
                            )
                            
                            // Vermifugação
                            ClickableHistoryRow(
                                label = "Vermifugação",
                                date = uiState.lastVermifugacao,
                                onClick = { onNavigateToVermifugacaoHistory(cowId) }
                            )
                            
                            // Pesagem
                            val pesagemText = if (uiState.lastPesagem != null) {
                                val dateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(uiState.lastPesagem)
                                val weightText = uiState.lastPesagemWeight?.let { weight ->
                                    if (weight >= 0) {
                                        "${String.format("%.1f", weight)} kg"
                                    } else {
                                        "Não informado"
                                    }
                                } ?: "Não informado"
                                "$weightText em $dateText"
                            } else {
                                "-/-/-"
                            }
                            ClickableHistoryRow(
                                label = "Pesagem",
                                dateText = pesagemText,
                                onClick = { onNavigateToPesagemHistory(cowId) }
                            )
                            
                            // Parto
                            val partoText = if (uiState.lastParto != null) {
                                val dateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(uiState.lastParto)
                                val calvesText = uiState.lastPartoCalvesCount?.let { count ->
                                    if (count >= 0) {
                                        " – $count bezerro${if (count != 1) "s" else ""}"
                                    } else {
                                        " – Não informado"
                                    }
                                } ?: " – Não informado"
                                "$dateText$calvesText"
                            } else {
                                "-/-/-"
                            }
                            ClickableHistoryRow(
                                label = "Parto",
                                dateText = partoText,
                                onClick = { onNavigateToPartoHistory(cowId) }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Eventos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.eventos.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Nenhum evento cadastrado",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(uiState.eventos) { evento ->
                        EventCardInCowDetail(
                            evento = evento,
                            cowId = cowId,
                            onEdit = { eventoId ->
                                onNavigateToEventForm(eventoId)
                            },
                            onRemove = { eventoId ->
                                viewModel.removeCowFromEvent(eventoId, cowId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Vaca") },
            text = { Text("Tem certeza que deseja excluir esta vaca? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCow(onNavigateBack)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun HistoryRow(label: String, date: java.util.Date?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date?.let { 
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) 
            } ?: "-/-/-",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ClickableHistoryRow(
    label: String,
    date: java.util.Date? = null,
    dateText: String? = null,
    onClick: () -> Unit
) {
    val displayText = dateText ?: (date?.let { 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) 
    } ?: "-/-/-")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Última: $displayText",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableHistoryRow(
    label: String,
    date: java.util.Date?,
    onEdit: (java.util.Date?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = date?.let { 
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) 
                } ?: "-/-/-",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (date != null) {
                IconButton(
                    onClick = { onEdit(null) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpar",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onConfirm = {
                datePickerState.selectedDateMillis?.let {
                    onEdit(java.util.Date(it))
                } ?: onEdit(null)
                showDatePicker = false
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (color != androidx.compose.ui.graphics.Color.Unspecified) color else androidx.compose.ui.graphics.Color.Unspecified
        )
    }
}

@Composable
fun EventCard(
    evento: com.example.tonisfarm.domain.model.Evento,
    onEdit: (Long) -> Unit,
    onDelete: (com.example.tonisfarm.domain.model.Evento) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
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
                Text(
                    text = "Data do evento: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(evento.dataEvento)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                IconButton(onClick = { onDelete(evento) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun EventCardInCowDetail(
    evento: com.example.tonisfarm.domain.model.Evento,
    cowId: Long,
    onEdit: (Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
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
                    Spacer(modifier = Modifier.height(2.dp))
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
                IconButton(onClick = { onRemove(evento.id) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Remover desta vaca")
                }
            }
        }
    }
}

