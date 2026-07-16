package com.example.jagasehat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jagasehat.ui.theme.Blue500
import com.example.jagasehat.ui.theme.Purple500
import com.example.jagasehat.ui.theme.Red500

@Composable
fun HealthDataInputCard(
    memberName: String?,
    onSave: (
        date: String,
        systolic: Int?,
        diastolic: Int?,
        bloodSugar: Float?,
        weight: Float?,
        notes: String?
    ) -> Unit
) {
    var date by remember { mutableStateOf("") }
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var bloodSugar by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Input Data Kesehatan",
                style = MaterialTheme.typography.titleMedium
            )

            memberName?.let {
                Text(
                    text = "Untuk: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Tanggal (yyyy-mm-dd)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Red500,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Tekanan Darah (mmHg)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = systolic,
                        onValueChange = { systolic = it },
                        placeholder = { Text("120") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Text(text = "/", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(
                        value = diastolic,
                        onValueChange = { diastolic = it },
                        placeholder = { Text("80") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Opacity,
                        contentDescription = null,
                        tint = Blue500,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Gula Darah (mg/dL)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedTextField(
                    value = bloodSugar,
                    onValueChange = { bloodSugar = it },
                    placeholder = { Text("100") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonitorWeight,
                        contentDescription = null,
                        tint = Purple500,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Berat Badan (kg)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    placeholder = { Text("60") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan (opsional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            Button(
                onClick = {
                    onSave(
                        date,
                        systolic.toIntOrNull(),
                        diastolic.toIntOrNull(),
                        bloodSugar.toFloatOrNull(),
                        weight.toFloatOrNull(),
                        notes.ifBlank { null }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = memberName != null && date.isNotBlank()
            ) {
                Text("Simpan Data Kesehatan")
            }
        }
    }
}
