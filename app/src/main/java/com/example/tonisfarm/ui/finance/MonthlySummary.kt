package com.example.tonisfarm.ui.finance

/**
 * Representa o resumo financeiro de um mês específico.
 * Usado para exibir dados financeiros mensais em gráficos e listas.
 */
data class MonthlySummary(
    val month: String,
    val receitas: Double,
    val despesas: Double,
    val lucro: Double
)

