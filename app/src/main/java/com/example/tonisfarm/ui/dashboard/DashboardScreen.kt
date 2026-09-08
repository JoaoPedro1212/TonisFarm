package com.example.tonisfarm.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tonisfarm.R
import com.example.tonisfarm.domain.model.TipoEventoExtensions.getDisplayName
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardHeader(
    onInfoClick: () -> Unit,
    topPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Banner ocupando toda a largura
        Image(
            painter = painterResource(id = R.drawable.tonis_farm_banner),
            contentDescription = "Banner Toni's Farm",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
            contentScale = ContentScale.Crop
        )

        // Ícone "i" sobreposto no canto superior direito
        IconButton(
            onClick = onInfoClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = topPadding + 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Informações",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCowList: () -> Unit,
    onNavigateToPregnantCows: () -> Unit,
    onNavigateToCowDetail: (Long) -> Unit,
    onNavigateToAllEvents: () -> Unit,
    onNavigateToFinance: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInfoDialog by remember { mutableStateOf(false) }

    // Processar recorrências e atualizar dados quando a tela for exibida
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            DashboardHeader(
                onInfoClick = { showInfoDialog = true },
                topPadding = paddingValues.calculateTopPadding()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Conteúdo abaixo do banner com padding
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 0.dp, bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "Visão Geral",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Total de Vacas",
                            value = uiState.totalCows.toString(),
                            icon = null, // Usaremos emoji
                            emoji = "🐄",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCowList
                        )
                        StatCard(
                            title = "Prenhas",
                            value = uiState.pregnantCows.toString(),
                            icon = Icons.Default.Favorite,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToPregnantCows
                        )
                    }
                }

                item {
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                    val financialValue = currencyFormat.format(uiState.currentMonthFinancialSituation)
                    val financialColor = when {
                        uiState.currentMonthFinancialSituation > 0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
                        uiState.currentMonthFinancialSituation < 0 -> androidx.compose.ui.graphics.Color(0xFFF44336) // Vermelho
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToFinance
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Financeiro",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = financialValue,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = financialColor
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Próximos Eventos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        TextButton(onClick = onNavigateToAllEvents) {
                            Text("Ver todos")
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.upcomingEvents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Nenhum evento próximo",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(uiState.upcomingEvents) { evento ->
                        EventCard(
                            evento = evento,
                            onClick = { onNavigateToAllEvents() } // Navegar para lista de eventos ao invés de detalhe de vaca
                        )
                    }
                }
            }
        }
    }
    
    if (showInfoDialog) {
        InfoDialog(onDismiss = { showInfoDialog = false })
    }
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Este aplicativo é um presente de João Pedro Moura Penafiel de Angelo ao homem que ele tem orgulho de chamar de Pai, Antônio Carlos de Angelo Filho, somos muito gratos à você por tudo o que faz por nós, todas as lutas e sacrifícios nunca passaram despercebidos, você é um líder e o pilar desta família junto com a Mãe, espero que este app seja de grande ajuda nesta jornada. Te amamos muito!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // TODO: Substituir R.drawable.imagem_homenagem_pai pelo drawable real quando a imagem for adicionada
                Image(
                    painter = painterResource(id = R.drawable.imagem_homenagem_pai),
                    contentDescription = "Imagem de homenagem",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    emoji: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onClick ?: {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Ícone no canto superior direito
            if (emoji != null) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd),
                    tint = if (icon == Icons.Default.Favorite) 
                        androidx.compose.ui.graphics.Color(0xFFF44336) // Vermelho para coração
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Número centralizado
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.Center)
            )
            
            // Título na parte inferior centralizada
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun EventCard(
    evento: com.example.tonisfarm.domain.model.Evento,
    onClick: () -> Unit
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
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = "Evento",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

