package com.example.tonisfarm.util

import com.example.tonisfarm.domain.model.calcularIdadeEmTexto
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

object DateUtils {
    fun calculateAge(birthDate: Date): String {
        val birthLocalDate = birthDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return calcularIdadeEmTexto(birthLocalDate, LocalDate.now())
    }
}

