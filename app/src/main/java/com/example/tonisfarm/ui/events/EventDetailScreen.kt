package com.example.tonisfarm.ui.events

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
import com.example.tonisfarm.domain.model.TipoEventoExtensions.getDisplayName
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventoId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventoId) {
        viewModel.loadEvento(eventoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(eventoId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
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
        } else if (uiState.evento == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Evento não encontrado")
            }
        } else {
            val evento = uiState.evento!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Tipo de Evento",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = evento.tipoEvento.getDisplayName(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Data do Evento",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(evento.dataEvento),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (evento.dataAlerta != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Data de Alerta",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(evento.dataAlerta),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            if (evento.descricao != null && evento.descricao.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Descrição",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = evento.descricao,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Vacas cadastradas para este evento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.cows.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Nenhuma vaca associada a este evento",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(uiState.cows) { cow ->
                        CowEventCard(
                            cow = cow,
                            onRemove = {
                                viewModel.removeCowFromEvent(eventoId, cow.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CowEventCard(
    cow: com.example.tonisfarm.domain.model.Cow,
    onRemove: () -> Unit
) {
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
                    text = cow.identificacao,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (cow.nome != null) {
                    Text(
                        text = cow.nome,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "${cow.raca} - ${String.format("%.1f", cow.pesoKg)} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remover",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

