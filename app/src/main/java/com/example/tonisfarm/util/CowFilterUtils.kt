package com.example.tonisfarm.util

import com.example.tonisfarm.domain.model.Cow

/**
 * Enum para tipos de filtro de peso
 */
enum class WeightFilterType {
    MORE_THAN,    // Mais de
    LESS_THAN,    // Menos de
    EQUAL_TO      // Igual a
}

/**
 * Classe para armazenar os filtros de vacas
 */
data class CowFilters(
    val searchQuery: String = "",           // Filtro por ID ou Nome
    val selectedRace: String = "Todas",    // Raça selecionada ("Todas" ou nome da raça)
    val weightFilterType: WeightFilterType? = null,  // Tipo de filtro de peso
    val weightValue: Double? = null         // Valor do peso para filtrar
) {
    /**
     * Verifica se algum filtro está ativo
     */
    fun hasActiveFilters(): Boolean {
        return searchQuery.isNotBlank() || 
               (selectedRace != "Todas") || 
               (weightFilterType != null && weightValue != null)
    }
}

/**
 * Função utilitária para filtrar vacas com base nos critérios fornecidos
 */
fun filterCows(
    cows: List<Cow>,
    filters: CowFilters
): List<Cow> {
    return cows.filter { cow ->
        // Filtro por ID ou Nome
        val matchesSearch = filters.searchQuery.isBlank() || {
            val query = filters.searchQuery.lowercase()
            cow.identificacao.lowercase().contains(query) ||
            cow.nome?.lowercase()?.contains(query) == true
        }()

        // Filtro por Raça
        val matchesRace = filters.selectedRace == "Todas" || cow.raca == filters.selectedRace

        // Filtro por Peso
        val matchesWeight = when {
            filters.weightFilterType == null || filters.weightValue == null -> true
            filters.weightFilterType == WeightFilterType.MORE_THAN -> cow.pesoKg > filters.weightValue
            filters.weightFilterType == WeightFilterType.LESS_THAN -> cow.pesoKg < filters.weightValue
            filters.weightFilterType == WeightFilterType.EQUAL_TO -> 
                kotlin.math.abs(cow.pesoKg - filters.weightValue) < 0.01 // Tolerância para comparação de Double
            else -> true
        }

        matchesSearch && matchesRace && matchesWeight
    }
}

