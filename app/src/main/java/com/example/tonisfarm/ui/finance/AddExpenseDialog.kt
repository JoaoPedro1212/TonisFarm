package com.example.tonisfarm.ui.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tonisfarm.domain.model.FinancialTransaction
import com.example.tonisfarm.domain.model.RecurrenceType
import com.example.tonisfarm.domain.model.TipoTransacao
import com.example.tonisfarm.ui.cowform.DatePickerField
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (FinancialTransaction) -> Unit,
    transaction: FinancialTransaction? = null // Para edição
) {
    var valor by remember { mutableStateOf(transaction?.valor?.toString() ?: "") }
    var descricao by remember { mutableStateOf(transaction?.descricao ?: "") }
    var categoria by remember { mutableStateOf(transaction?.categoria ?: "Ração") }
    var selectedDate by remember { mutableStateOf(transaction?.data ?: Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isMonthly by remember { mutableStateOf(transaction?.recurrenceType == RecurrenceType.MONTHLY) }
    var isYearly by remember { mutableStateOf(transaction?.recurrenceType == RecurrenceType.YEARLY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (transaction != null) "Editar Despesa" else "Adicionar Despesa") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = valor,
                    onValueChange = { valor = it; errorMessage = null },
                    label = { Text("Valor (R$) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMessage != null
                )

                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it; errorMessage = null },
                    label = { Text("Descrição *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    )
                )

                DatePickerField(
                    label = "Data",
                    date = selectedDate,
                    onDateSelected = { selectedDate = it }
                )

                // Checkboxes de recorrência
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isMonthly,
                            onCheckedChange = { checked ->
                                isMonthly = checked
                                if (checked) isYearly = false
                            }
                        )
                        Text(
                            text = "Despesa Mensal",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isYearly,
                            onCheckedChange = { checked ->
                                isYearly = checked
                                if (checked) isMonthly = false
                            }
                        )
                        Text(
                            text = "Despesa Anual",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val valorDouble = valor.replace(",", ".").toDoubleOrNull()
                    if (valorDouble == null || valorDouble <= 0) {
                        errorMessage = "Valor inválido"
                        return@TextButton
                    }
                    if (descricao.isBlank()) {
                        errorMessage = "Descrição é obrigatória"
                        return@TextButton
                    }

                    // Determinar recurrenceType e nextOccurrenceDate
                    val recurrenceType = when {
                        isMonthly -> RecurrenceType.MONTHLY
                        isYearly -> RecurrenceType.YEARLY
                        else -> RecurrenceType.NONE
                    }
                    
                    // Se for edição e a transação original tinha recorrência, manter o nextOccurrenceDate original
                    // a menos que a data tenha mudado ou a recorrência tenha sido alterada
                    val datesEqual = if (transaction != null) {
                        val cal1 = Calendar.getInstance()
                        cal1.time = transaction.data
                        val cal2 = Calendar.getInstance()
                        cal2.time = selectedDate
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                        cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
                    } else {
                        false
                    }
                    
                    val nextOccurrenceDate = if (transaction != null && transaction.recurrenceType != RecurrenceType.NONE && 
                        datesEqual && transaction.recurrenceType == recurrenceType) {
                        // Manter o nextOccurrenceDate original se nada mudou
                        transaction.nextOccurrenceDate
                    } else {
                        // Recalcular nextOccurrenceDate
                        when (recurrenceType) {
                            RecurrenceType.MONTHLY -> {
                                val calendar = Calendar.getInstance()
                                calendar.time = selectedDate
                                calendar.add(Calendar.MONTH, 1)
                                calendar.time
                            }
                            RecurrenceType.YEARLY -> {
                                val calendar = Calendar.getInstance()
                                calendar.time = selectedDate
                                calendar.add(Calendar.YEAR, 1)
                                calendar.time
                            }
                            RecurrenceType.NONE -> null
                        }
                    }

                    val newTransaction = FinancialTransaction(
                        id = transaction?.id ?: 0,
                        tipo = TipoTransacao.DESPESA,
                        categoria = categoria,
                        valor = valorDouble,
                        data = selectedDate,
                        descricao = descricao,
                        recurrenceType = recurrenceType,
                        nextOccurrenceDate = nextOccurrenceDate
                    )
                    onConfirm(newTransaction)
                    onDismiss()
                }
            ) {
                Text(if (transaction != null) "Salvar" else "Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

