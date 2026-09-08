@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tonisfarm.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tonisfarm.ui.cowform.DatePickerField
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VacinacaoFormScreen(
    cowId: Long,
    vacinacaoId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: VacinacaoFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cowId, vacinacaoId) {
        viewModel.init(cowId, vacinacaoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vacinacaoId != null) "Editar Vacinação" else "Nova Vacinação") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Lista suspensa de vacinas
            var expanded by remember { mutableStateOf(false) }
            val vaccineOptions = viewModel.getVaccineOptions().filter { it != "Todas" }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = uiState.vaccineName.ifEmpty { "Selecione uma vacina" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vacina *") },
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
                                viewModel.updateVaccineName(vaccine)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Campo de texto personalizado se "Outra" foi selecionado
            if (uiState.vaccineName == "Outra") {
                OutlinedTextField(
                    value = uiState.customVaccineName,
                    onValueChange = viewModel::updateCustomVaccineName,
                    label = { Text("Nome da Vacina *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Data
            DatePickerField(
                label = "Data da Vacinação *",
                date = uiState.date,
                onDateSelected = viewModel::updateDate
            )

            // Botão salvar
            Button(
                onClick = { viewModel.saveVacinacao(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Salvar")
                }
            }
        }
    }
}

