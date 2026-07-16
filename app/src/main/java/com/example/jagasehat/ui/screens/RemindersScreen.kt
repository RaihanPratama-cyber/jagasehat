package com.example.jagasehat.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.jagasehat.viewmodel.AppViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    val reminders = state.reminders
    val familyMembers = state.familyMembers
    val context = LocalContext.current

    var showFormDialog by remember { mutableStateOf(false) }
    var editReminderId by remember { mutableStateOf<String?>(null) }

    var inputTitle by remember { mutableStateOf("") }
    var inputTime by remember { mutableStateOf("08:00") }

    var reminderToDelete by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(reminders) {
        viewModel.rescheduleReminders(context)
    }

    val playAlarmSound = {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            RingtoneManager.getRingtone(context, uri).play()
        } catch (_: Exception) {
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editReminderId = null
                    inputTitle = ""
                    inputTime = "08:00"
                    showFormDialog = true
                },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pengingat", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pengingat",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Jadwal obat dan rutinitas kesehatan",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.padding(20.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada pengingat", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 16.sp)
                        Text("Tekan tombol + di pojok kanan bawah", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(reminders) { reminder ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .height(48.dp)
                                            .width(4.dp)
                                            .background(if (reminder.enabled) Color(0xFF10B981) else Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = reminder.time,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 28.sp,
                                            color = if (reminder.enabled) Color(0xFF1E293B) else Color.Gray
                                        )
                                        Text(
                                            text = reminder.title,
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = {
                                        playAlarmSound()
                                        Toast.makeText(context, "🔊 Tes Suara Alarm", Toast.LENGTH_SHORT).show()
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = "Tes", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                    }

                                    IconButton(onClick = {
                                        editReminderId = reminder.id
                                        inputTitle = reminder.title
                                        inputTime = reminder.time
                                        showFormDialog = true
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                                    }

                                    Switch(
                                        checked = reminder.enabled,
                                        onCheckedChange = { viewModel.toggleReminder(reminder.id, context) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF10B981),
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFCBD5E1)
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFEF2F2),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        IconButton(onClick = { reminderToDelete = reminder.id }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showFormDialog) {
            val selectedMemberId by remember { mutableStateOf(familyMembers.firstOrNull()?.id ?: "") }
            var showTimePicker by remember { mutableStateOf(false) }

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF1F5F9)
            )

            if (showTimePicker) {
                val initialHour = inputTime.substringBefore(":").toIntOrNull() ?: 8
                val initialMinute = inputTime.substringAfter(":").toIntOrNull() ?: 0
                val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)

                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    title = { Text("Pilih Jam Pengingat", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TimePicker(state = timePickerState)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            inputTime = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }) { Text("Selesai", fontWeight = FontWeight.Bold, color = Color(0xFF10B981)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text("Batal", color = Color.Gray) }
                    }
                )
            }

            AlertDialog(
                onDismissRequest = { showFormDialog = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(28.dp),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(16.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (editReminderId == null) "Buat Pengingat" else "Edit Pengingat",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Kelola jadwal rutin keluarga",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            placeholder = { Text("Nama Pengingat (Misal: Minum Obat)", color = Color.Gray) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = inputTime,
                            onValueChange = { },
                            readOnly = true,
                            placeholder = { Text("Pilih Jam", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTimePicker = true },
                            colors = textFieldColors,
                            enabled = false
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (inputTitle.isNotBlank() && inputTime.isNotBlank() && selectedMemberId.isNotBlank()) {
                                    if (editReminderId != null) {
                                        viewModel.deleteReminder(editReminderId!!, context)
                                    }
                                    viewModel.addReminder(selectedMemberId, inputTitle, inputTime, "Harian", context)
                                    showFormDialog = false
                                } else {
                                    Toast.makeText(context, "Mohon lengkapi data", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) { Text("Simpan Alarm", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }

                        TextButton(
                            onClick = { showFormDialog = false },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                    }
                },
                dismissButton = null
            )
        }

        if (reminderToDelete != null) {
            AlertDialog(
                onDismissRequest = { reminderToDelete = null },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Hapus Pengingat?", fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444)) },
                text = { Text("Apakah Anda yakin ingin menghapus alarm ini? Jadwal yang dihapus tidak dapat dikembalikan.", color = Color.DarkGray) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteReminder(reminderToDelete!!, context); reminderToDelete = null },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) { Text("Ya, Hapus Alarm", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { reminderToDelete = null },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            )
        }
    }
}
