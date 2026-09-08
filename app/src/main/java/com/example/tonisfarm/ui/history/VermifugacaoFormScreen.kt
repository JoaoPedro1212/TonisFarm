@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tonisfarm.ui.history

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
import com.example.tonisfarm.ui.cowform.DatePickerField

@Composable
fun VermifugacaoFormScreen(
    cowId: Long,
    vermifugacaoId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: VermifugacaoFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cowId, vermifugacaoId) {
        viewModel.init(cowId, vermifugacaoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vermifugacaoId != null) "Editar Vermifugação" else "Nova Vermifugação") },
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

            // Lista suspensa de vermífugos
            var expanded by remember { mutableStateOf(false) }
            val drugOptions = viewModel.getDrugOptions().filter { it != "Todas" }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = uiState.drugName.ifEmpty { "Selecione um vermífugo" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vermífugo *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    drugOptions.forEach { drug ->
                        DropdownMenuItem(
                            text = { Text(drug) },
                            onClick = {
                                viewModel.updateDrugName(drug)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Campo de texto personalizado se "Outro" foi selecionado
            if (uiState.drugName == "Outro") {
                OutlinedTextField(
                    value = uiState.customDrugName,
                    onValueChange = viewModel::updateCustomDrugName,
                    label = { Text("Nome do Vermífugo *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Data
            DatePickerField(
                label = "Data da Vermifugação *",
                date = uiState.date,
                onDateSelected = viewModel::updateDate
            )

            // Botão salvar
            Button(
                onClick = { viewModel.saveVermifugacao(onNavigateBack) },
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

