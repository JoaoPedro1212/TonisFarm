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
fun PartoFormScreen(
    cowId: Long,
    partoId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: PartoFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cowId, partoId) {
        viewModel.init(cowId, partoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (partoId != null) "Editar Parto" else "Novo Parto") },
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

            // Quantidade de bezerros
            OutlinedTextField(
                value = uiState.calvesCount,
                onValueChange = viewModel::updateCalvesCount,
                label = { Text("Quantidade de Bezerros *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Data
            DatePickerField(
                label = "Data do Parto *",
                date = uiState.date,
                onDateSelected = viewModel::updateDate
            )

            // Botão salvar
            Button(
                onClick = { viewModel.saveParto(onNavigateBack) },
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

