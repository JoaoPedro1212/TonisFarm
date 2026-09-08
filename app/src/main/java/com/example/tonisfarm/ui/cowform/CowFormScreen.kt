@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tonisfarm.ui.cowform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CowFormScreen(
    cowId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: CowFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cowId) {
        cowId?.let { viewModel.loadCow(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cowId != null) "Editar Vaca" else "Nova Vaca") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveCow(onNavigateBack)
                        },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Salvar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && cowId != null) {
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

                OutlinedTextField(
                    value = uiState.identificacao,
                    onValueChange = viewModel::updateIdentificacao,
                    label = { Text("Identificação *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.nome,
                    onValueChange = viewModel::updateNome,
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                DatePickerField(
                    label = "Data de Nascimento *",
                    date = uiState.dataNascimento,
                    onDateSelected = viewModel::updateDataNascimento
                )

                OutlinedTextField(
                    value = uiState.pesoKg,
                    onValueChange = viewModel::updatePesoKg,
                    label = { Text("Peso (kg) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                RaceDropdown(
                    selectedRace = uiState.raca,
                    onRaceSelected = viewModel::updateRaca
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Está prenha?")
                    Switch(
                        checked = uiState.estaPrenha,
                        onCheckedChange = viewModel::updateEstaPrenha
                    )
                }

                if (uiState.estaPrenha) {
                    DatePickerField(
                        label = "Data de Início da Prenhez *",
                        date = uiState.dataInicioPrenhez,
                        onDateSelected = viewModel::updateDataInicioPrenhez
                    )
                }

                OutlinedTextField(
                    value = uiState.observacoes,
                    onValueChange = viewModel::updateObservacoes,
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    date: Date?,
    onDateSelected: (Date) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = date?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "",
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        readOnly = true,
        enabled = false
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onConfirm = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(Date(it))
                }
                showDatePicker = false
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun RaceDropdown(
    selectedRace: String,
    onRaceSelected: (String) -> Unit
) {
    val races = listOf("Nelore", "Angus", "Girolando", "Holandês", "Guzerá")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedRace,
            onValueChange = {},
            readOnly = true,
            label = { Text("Raça *") },
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
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        },
        text = content
    )
}

