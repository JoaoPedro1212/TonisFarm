@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tonisfarm.ui.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.tonisfarm.ui.cowform.DatePickerField
import com.example.tonisfarm.ui.cowform.TimePickerField
import com.example.tonisfarm.util.WeightFilterType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventFormScreen(
    cowId: Long?,
    eventoId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: EventFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cowId) {
        cowId?.let { viewModel.setInitialCowId(it) }
    }

    LaunchedEffect(eventoId) {
        eventoId?.let { viewModel.loadEvento(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventoId != null) "Editar Evento" else "Novo Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveEvento(onNavigateBack)
                        },
                        enabled = !uiState.isLoading && uiState.selectedCowIds.isNotEmpty()
                    ) {
                        Text("Salvar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && eventoId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Text(
                    text = "Selecionar Vacas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Filtro 1: ID ou Nome
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    label = { Text("Buscar por ID ou Nome") },
                    modifier = Modifier.fillMaxWidth(),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selecionadas: ${uiState.selectedCowIds.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = { viewModel.selectAllCows() }) {
                        Text("Adicionar todas")
                    }
                }

                if (uiState.displayedCows.isEmpty()) {
                    Text(
                        text = "Nenhuma vaca encontrada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.displayedCows.forEach { cow ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleCowSelection(cow.id) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cow.identificacao,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
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
                            Checkbox(
                                checked = uiState.selectedCowIds.contains(cow.id),
                                onCheckedChange = { viewModel.toggleCowSelection(cow.id) }
                            )
                        }
                        HorizontalDivider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.selectedCowIds.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Selecione pelo menos uma vaca para criar um evento",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tipo de Evento",
                    style = MaterialTheme.typography.labelLarge
                )

                TipoEvento.values().forEach { tipo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(tipo.getDisplayName())
                        RadioButton(
                            selected = uiState.tipoEvento == tipo,
                            onClick = { viewModel.updateTipoEvento(tipo) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de tipo de vacina (apenas para eventos de Vacinação)
                if (uiState.tipoEvento == TipoEvento.VACINACAO) {
                    VaccineTypeDropdown(
                        selectedVaccine = uiState.vaccineType,
                        customVaccineType = uiState.customVaccineType,
                        onVaccineSelected = viewModel::updateVaccineType,
                        onCustomVaccineChanged = viewModel::updateCustomVaccineType,
                        vaccineOptions = viewModel.getVaccineOptions()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Campo de tipo de vermífugo (apenas para eventos de Vermifugação)
                if (uiState.tipoEvento == TipoEvento.VERMIFUGACAO) {
                    DewormingTypeDropdown(
                        selectedDeworming = uiState.dewormingType,
                        customDewormingType = uiState.customDewormingType,
                        onDewormingSelected = viewModel::updateDewormingType,
                        onCustomDewormingChanged = viewModel::updateCustomDewormingType,
                        dewormingOptions = viewModel.getDewormingOptions()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Campos de peso e bezerros foram removidos - serão preenchidos após o evento acontecer

                DatePickerField(
                    label = "Data do Evento *",
                    date = uiState.dataEvento,
                    onDateSelected = viewModel::updateDataEvento
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Horário do Evento (opcional)")
                    Switch(
                        checked = uiState.horaEvento != null,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                viewModel.updateHoraEvento(java.util.Date())
                            } else {
                                viewModel.updateHoraEvento(null)
                            }
                        }
                    )
                }

                if (uiState.horaEvento != null) {
                    TimePickerField(
                        label = "Hora do Evento",
                        date = uiState.horaEvento,
                        onDateSelected = { date ->
                            // Manter a data do evento como base para a hora
                            val currentHora = uiState.horaEvento
                            val dataEvento = uiState.dataEvento ?: java.util.Date()
                            if (currentHora != null) {
                                val calendar = Calendar.getInstance()
                                calendar.time = dataEvento
                                val timeCalendar = Calendar.getInstance()
                                timeCalendar.time = date
                                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                                calendar.set(Calendar.SECOND, 0)
                                calendar.set(Calendar.MILLISECOND, 0)
                                viewModel.updateHoraEvento(calendar.time)
                            } else {
                                viewModel.updateHoraEvento(date)
                            }
                        }
                    )
                }

                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = viewModel::updateDescricao,
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Ativar Alerta?")
                    Switch(
                        checked = uiState.dataAlerta != null,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                viewModel.updateDataAlerta(java.util.Date())
                            } else {
                                viewModel.updateDataAlerta(null)
                            }
                        }
                    )
                }

                if (uiState.dataAlerta != null) {
                    DatePickerField(
                        label = "Data de Alerta",
                        date = uiState.dataAlerta,
                        onDateSelected = { date ->
                            // Manter a hora atual ao mudar a data
                            val currentAlerta = uiState.dataAlerta
                            if (currentAlerta != null) {
                                val calendar = Calendar.getInstance()
                                calendar.time = date
                                val alertaCalendar = Calendar.getInstance()
                                alertaCalendar.time = currentAlerta
                                calendar.set(Calendar.HOUR_OF_DAY, alertaCalendar.get(Calendar.HOUR_OF_DAY))
                                calendar.set(Calendar.MINUTE, alertaCalendar.get(Calendar.MINUTE))
                                viewModel.updateDataAlerta(calendar.time)
                            } else {
                                viewModel.updateDataAlerta(date)
                            }
                        }
                    )
                    TimePickerField(
                        label = "Hora de Alerta",
                        date = uiState.dataAlerta,
                        onDateSelected = { date ->
                            // Manter a data atual ao mudar a hora
                            val currentAlerta = uiState.dataAlerta
                            if (currentAlerta != null) {
                                val calendar = Calendar.getInstance()
                                calendar.time = currentAlerta
                                val timeCalendar = Calendar.getInstance()
                                timeCalendar.time = date
                                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                                viewModel.updateDataAlerta(calendar.time)
                            } else {
                                viewModel.updateDataAlerta(date)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RaceFilterDropdown(
    selectedRace: String,
    onRaceSelected: (String) -> Unit
) {
    val races = listOf("Todas", "Nelore", "Angus", "Girolando", "Holandês", "Guzerá")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedRace,
            onValueChange = {},
            readOnly = true,
            label = { Text("Filtrar por Raça") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            races.forEach { race ->
                DropdownMenuItem(
                    text = { Text(race) },
                    onClick = {
                        onRaceSelected(race)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun WeightFilterRow(
    filterType: WeightFilterType?,
    weightValue: String,
    onFilterTypeChanged: (WeightFilterType?) -> Unit,
    onWeightValueChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filterTypes = listOf(
        "Sem filtro" to null,
        "Mais de" to WeightFilterType.MORE_THAN,
        "Menos de" to WeightFilterType.LESS_THAN,
        "Igual a" to WeightFilterType.EQUAL_TO
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
                        WeightFilterType.MORE_THAN -> "Mais de"
                        WeightFilterType.LESS_THAN -> "Menos de"
                        WeightFilterType.EQUAL_TO -> "Igual a"
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
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineTypeDropdown(
    selectedVaccine: String,
    customVaccineType: String,
    onVaccineSelected: (String) -> Unit,
    onCustomVaccineChanged: (String) -> Unit,
    vaccineOptions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedVaccine.ifEmpty { "Selecione uma vacina" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Vacina") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                vaccineOptions.forEach { vaccine ->
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

        if (selectedVaccine == "Outra") {
            OutlinedTextField(
                value = customVaccineType,
                onValueChange = onCustomVaccineChanged,
                label = { Text("Nome da Vacina") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DewormingTypeDropdown(
    selectedDeworming: String,
    customDewormingType: String,
    onDewormingSelected: (String) -> Unit,
    onCustomDewormingChanged: (String) -> Unit,
    dewormingOptions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedDeworming.ifEmpty { "Selecione um vermífugo" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Vermífugo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                dewormingOptions.forEach { deworming ->
                    DropdownMenuItem(
                        text = { Text(deworming) },
                        onClick = {
                            onDewormingSelected(deworming)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (selectedDeworming == "Outro") {
            OutlinedTextField(
                value = customDewormingType,
                onValueChange = onCustomDewormingChanged,
                label = { Text("Nome do Vermífugo") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

