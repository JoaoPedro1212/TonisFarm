package com.example.tonisfarm.domain.model

object TipoEventoExtensions {
    fun TipoEvento.getDisplayName(): String {
        return when (this) {
            TipoEvento.PARTO -> "PARTO"
            TipoEvento.VACINACAO -> "VACINAÇÃO"
            TipoEvento.VERMIFUGACAO -> "VERMIFUGAÇÃO"
            TipoEvento.PESAGEM -> "PESAGEM"
            TipoEvento.OUTRO -> "OUTRO"
        }
    }
}

