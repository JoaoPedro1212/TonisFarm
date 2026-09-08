package com.example.tonisfarm.ui.finance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceChartsScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinanceChartsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChartData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gráficos Financeiros") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
        } else if (uiState.monthlySummaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma transação registrada",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Título principal
                Text(
                    text = "Resultado Financeiro dos Últimos 12 Meses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Card com gráfico
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        FinanceBarChart(
                            monthlyResults = uiState.monthlySummaries.reversed() // Ordem cronológica (antigo → novo)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calcula a escala adaptável do eixo Y baseada nos dados.
 * Retorna (yMin, yMax, ticks) onde ticks é a lista de valores para os rótulos.
 */
private fun calculateAdaptiveYAxisScale(values: List<Double>): Triple<Double, Double, List<Double>> {
    if (values.isEmpty()) {
        val defaultTop = 10000.0
        val defaultStep = 10000.0
        val defaultTicks = generateTicks(-defaultTop, defaultTop, defaultStep)
        return Triple(-defaultTop, defaultTop, defaultTicks)
    }
    
    // Calcular maxAbs = max(|valor|)
    val maxAbs = values.map { abs(it) }.maxOrNull() ?: 10000.0
    
    // Calcular top = ceil(maxAbs / 10_000) * 10_000
    val top = ceil(maxAbs / 10000.0) * 10000.0
    
    // Calcular step base: step = top / 5, depois arredondar para múltiplos de 10.000
    val stepBase = top / 5.0
    val step = ceil(stepBase / 10000.0) * 10000.0
    
    // Gerar ticks (máximo ~7 linhas)
    val ticks = generateTicks(-top, top, step)
    
    return Triple(-top, top, ticks)
}

/**
 * Gera os ticks do eixo Y, limitando a aproximadamente 7 linhas.
 */
private fun generateTicks(yMin: Double, yMax: Double, step: Double): List<Double> {
    val ticks = mutableListOf<Double>()
    var current = yMin
    
    while (current <= yMax) {
        ticks.add(current)
        current += step
    }
    
    // Se tiver mais de 7 ticks, aumentar o step
    if (ticks.size > 7) {
        val newStep = step * 2.0
        return generateTicks(yMin, yMax, newStep)
    }
    
    return ticks
}

/**
 * Formata um valor numérico para exibição no eixo Y.
 * Retorna apenas o número inteiro com separador de milhar usando ponto.
 */
private fun formatYAxisLabel(value: Double): String {
    val intValue = value.toInt()
    return NumberFormat.getNumberInstance(Locale.US).format(intValue).replace(",", ".")
}

/**
 * Formata o mês para exibição no eixo X.
 * Retorna formato abreviado: "Dez/24", "Jan/25", etc.
 */
private fun formatMonthLabel(monthString: String): String {
    // Se já está no formato "MMM/yy", retornar como está
    if (monthString.contains("/")) {
        return monthString
    }
    
    // Se estiver em formato longo (ex: "Dezembro 2024"), converter para "MMM/yy"
    try {
        val inputFormat = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
        val outputFormat = SimpleDateFormat("MMM/yy", Locale("pt", "BR"))
        val date = inputFormat.parse(monthString)
        if (date != null) {
            return outputFormat.format(date).replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() 
            }
        }
    } catch (e: Exception) {
        // Se falhar, retornar como está
        return monthString
    }
    
    return monthString
}

@Composable
fun FinanceBarChart(
    monthlyResults: List<MonthlySummary>
) {
    if (monthlyResults.isEmpty()) return

    // Calcular escala adaptável do eixo Y
    val values = monthlyResults.map { it.lucro }
    val (yMin, yMax, yTicks) = calculateAdaptiveYAxisScale(values)
    val yRange = yMax - yMin

    Column(modifier = Modifier.fillMaxSize()) {
        // Área do gráfico (sem título do eixo Y, ocupando toda a largura)
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.weight(1f)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Padding interno (reduzido para maximizar área do gráfico)
                    val paddingLeft = 50.dp.toPx() // Espaço para rótulos do eixo Y
                    val paddingRight = 8.dp.toPx()
                    val paddingTop = 8.dp.toPx() // Reduzido (sem título do eixo Y)
                    val paddingBottom = 60.dp.toPx() // Aumentado para acomodar meses rotacionados sem sobreposição

                    val chartWidth = size.width - paddingLeft - paddingRight
                    val chartHeight = size.height - paddingTop - paddingBottom
                    val barWidth = chartWidth / monthlyResults.size.coerceAtLeast(1)

                    // Fundo branco do gráfico
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(paddingLeft, paddingTop),
                        size = Size(chartWidth, chartHeight)
                    )

                    // Função auxiliar para calcular posição Y de um valor
                    fun getYPosition(value: Double): Float {
                        val normalized = (value - yMin) / yRange
                        return paddingTop + chartHeight * (1f - normalized.toFloat())
                    }

                    // Gridlines horizontais em todos os ticks (estilo simples e limpo)
                    yTicks.forEach { tickValue ->
                        val y = getYPosition(tickValue)
                        // Todas as linhas com mesmo estilo discreto
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.3f),
                            start = Offset(paddingLeft, y),
                            end = Offset(size.width - paddingRight, y),
                            strokeWidth = 0.8f
                        )
                    }

                    // Linha do zero (um pouco mais visível, mas não muito destacada)
                    val zeroY = getYPosition(0.0)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(paddingLeft, zeroY),
                        end = Offset(size.width - paddingRight, zeroY),
                        strokeWidth = 1f
                    )

                    // Desenhar rótulos numéricos do eixo Y (fora do gráfico, à esquerda)
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            textSize = 11.dp.toPx()
                            color = android.graphics.Color.parseColor("#424242")
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.create(
                                android.graphics.Typeface.DEFAULT,
                                android.graphics.Typeface.NORMAL
                            )
                        }

                        yTicks.forEach { tickValue ->
                            val y = getYPosition(tickValue)
                            val labelText = formatYAxisLabel(tickValue)
                            val textY = y + (paint.textSize / 3)

                            canvas.nativeCanvas.drawText(
                                labelText,
                                paddingLeft - 8.dp.toPx(),
                                textY,
                                paint
                            )
                        }
                    }

                    // Desenhar barras (estilo simples, sem cantos muito arredondados)
                    monthlyResults.forEachIndexed { index, summary ->
                        val x = paddingLeft + index * barWidth + barWidth / 2
                        val barActualWidth = barWidth - 16.dp.toPx() // Espaçamento maior entre barras

                        // Cor baseada no valor (verde para positivo, vermelho para negativo)
                        val color = when {
                            summary.lucro > 0 -> Color(0xFF2E7D32) // Verde para lucro
                            summary.lucro < 0 -> Color(0xFFC62828) // Vermelho para prejuízo
                            else -> Color(0xFF9E9E9E) // Cinza neutro para zero
                        }

                        // Posições da barra (sempre começam na linha do zero)
                        val barLeft = x - barActualWidth / 2
                        val barRight = x + barActualWidth / 2
                        val barTopY = getYPosition(summary.lucro)
                        val barBottomY = getYPosition(0.0)

                        val barTop = if (summary.lucro >= 0) barTopY else barBottomY
                        val barBottom = if (summary.lucro >= 0) barBottomY else barTopY

                        // Se valor for zero, desenhar linha pequena
                        if (abs(summary.lucro) < 0.01) {
                            drawLine(
                                color = color,
                                start = Offset(barLeft, barBottomY),
                                end = Offset(barRight, barBottomY),
                                strokeWidth = 2f
                            )
                            return@forEachIndexed
                        }

                        // Desenhar barra retangular simples (sem cantos arredondados)
                        drawRect(
                            color = color,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barActualWidth, abs(barBottom - barTop))
                        )
                    }
                }
            }

            // Rótulos dos meses (fora do gráfico, abaixo, rotacionados diagonalmente)
            // Usar Canvas para desenhar os meses com controle preciso e evitar sobreposição
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp) // Altura maior para acomodar meses rotacionados
                    .padding(horizontal = 50.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val monthWidth = size.width / monthlyResults.size.coerceAtLeast(1)
                    
                    monthlyResults.forEachIndexed { index, summary ->
                        val x = index * monthWidth + monthWidth / 2
                        val monthText = formatMonthLabel(summary.month)
                        
                        drawIntoCanvas { canvas ->
                            val paint = android.graphics.Paint().apply {
                                textSize = 10.dp.toPx()
                                color = android.graphics.Color.parseColor("#424242")
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                typeface = android.graphics.Typeface.create(
                                    android.graphics.Typeface.DEFAULT,
                                    android.graphics.Typeface.NORMAL
                                )
                            }
                            
                            // Calcular posição Y (próximo à parte inferior)
                            val textY = size.height - 8.dp.toPx()
                            
                            // Rotacionar o canvas para desenhar o texto diagonalmente
                            canvas.nativeCanvas.save()
                            canvas.nativeCanvas.translate(x, textY)
                            canvas.nativeCanvas.rotate(-35f) // Rotação de -35° para melhor legibilidade
                            canvas.nativeCanvas.drawText(monthText, 0f, 0f, paint)
                            canvas.nativeCanvas.restore()
                        }
                    }
                }
            }
        }
    }
}
