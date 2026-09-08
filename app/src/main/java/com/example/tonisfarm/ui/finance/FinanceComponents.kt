package com.example.tonisfarm.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tonisfarm.domain.model.RecurrenceType

@Composable
fun RecurrenceBadge(
    recurrenceType: RecurrenceType,
    modifier: Modifier = Modifier
) {
    val text = when (recurrenceType) {
        RecurrenceType.MONTHLY -> "Mensal"
        RecurrenceType.YEARLY -> "Anual"
        RecurrenceType.NONE -> ""
    }
    
    if (text.isNotEmpty()) {
        Surface(
            modifier = modifier
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

