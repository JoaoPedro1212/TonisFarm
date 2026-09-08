package com.example.tonisfarm.ui.cowlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tonisfarm.ui.events.RaceFilterDropdown
import com.example.tonisfarm.ui.events.WeightFilterRow
import com.example.tonisfarm.util.WeightFilterType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CowListScreen(
    showPregnantOnly: Boolean = false,
    onNavigateBack: () -> Unit,
    onNavigateToCowForm: (Long?) -> Unit,
    onNavigateToCowDetail: (Long) -> Unit,
    viewModel: CowListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(showPregnantOnly) {
        viewModel.init(showPregnantOnly)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showPregnantOnly) "Vacas Prenhas" else "Lista de Vacas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToCowForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Vaca")
            }
        }
    ) { paddingValues ->
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
                // Filtro 1: ID ou Nome
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por identificação ou nome...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    singleLine = true
                )

                // Filtro 2: Raça
                RaceFilterDropdown(
                    selectedRace = uiState.selectedRace,
                    onRaceSelected = viewModel::updateSelectedRace
                )

                // Filtro 3: Peso
                WeightFilterRow(
                    filterType = uiState.weightFilterType,
                    weightValue = uiState.weightValue,
                    onFilterTypeChanged = viewModel::updateWeightFilterType,
                    onWeightValueChanged = viewModel::updateWeightValue
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.cows.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) 
                            "Nenhuma vaca cadastrada" 
                        else 
                            "Nenhuma vaca encontrada",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.cows) { cow ->
                        CowListItem(
                            cow = cow,
                            onClick = { onNavigateToCowDetail(cow.id) },
                            onDelete = { viewModel.deleteCow(cow) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CowListItem(
    cow: com.example.tonisfarm.domain.model.Cow,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${cow.raca} • ${String.format("%.1f", cow.pesoKg)} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (cow.estaPrenha) {
                    val mesesAtual = cow.getMesesPrenhezAtual() ?: 0
                    Text(
                        text = "Prenha (${mesesAtual} meses)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

