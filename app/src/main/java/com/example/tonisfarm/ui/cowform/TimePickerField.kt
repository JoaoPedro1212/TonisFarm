package com.example.tonisfarm.ui.cowform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    date: Date?,
    onDateSelected: (Date) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }
    
    if (date != null) {
        calendar.time = date
    }

    OutlinedTextField(
        value = date?.let { 
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) 
        } ?: "",
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true },
        readOnly = true,
        enabled = false
    )

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val selectedCalendar = Calendar.getInstance()
                if (date != null) {
                    selectedCalendar.time = date
                }
                selectedCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                selectedCalendar.set(Calendar.MINUTE, timePickerState.minute)
                onDateSelected(selectedCalendar.time)
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        },
        text = content
    )
}

