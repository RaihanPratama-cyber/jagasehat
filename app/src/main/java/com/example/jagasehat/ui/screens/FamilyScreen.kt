package com.example.jagasehat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jagasehat.model.FamilyMember
import com.example.jagasehat.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    val familyMembers = state.familyMembers
    val context = LocalContext.current

    var showFormDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<FamilyMember?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<FamilyMember?>(null) }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    memberToEdit = null
                    showFormDialog = true
                },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(28.dp))
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
                text = "Daftar Keluarga",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Kelola profil kesehatan orang-orang tercinta",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (familyMembers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.padding(20.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada keluarga", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 16.sp)
                        Text("Tekan tombol + untuk menambahkan", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(familyMembers) { member ->
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(Color(0xFFDCFCE7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.name.replaceFirstChar { it.uppercase() },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${member.relationship.uppercase()} • ${member.age} THN • GOL. ${member.bloodType?.uppercase() ?: "-"}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        IconButton(onClick = {
                                            memberToEdit = member
                                            showFormDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFEF2F2),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        IconButton(onClick = {
                                            memberToDelete = member
                                            showDeleteDialog = true
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                        }
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
        var inputName by remember { mutableStateOf(memberToEdit?.name ?: "") }
        var inputAge by remember { mutableStateOf(memberToEdit?.age?.toString() ?: "") }
        var inputBloodType by remember { mutableStateOf(memberToEdit?.bloodType ?: "") }
        var inputRelationship by remember { mutableStateOf(memberToEdit?.relationship ?: "") }

        var expandedRelationship by remember { mutableStateOf(false) }
        val relationshipOptions = listOf("Ayah", "Ibu", "Anak", "Kakek", "Nenek", "Saudara")

        var expandedBloodType by remember { mutableStateOf(false) }
        val bloodTypeOptions = listOf("A", "B", "AB", "O", "Tidak Tahu")

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF10B981),
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF1F5F9)
        )

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
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (memberToEdit == null) "Anggota Baru" else "Edit Profil",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Lengkapi detail informasi keluarga",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        placeholder = { Text("Nama Lengkap", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        singleLine = true
                    )

                    Box {
                        OutlinedTextField(
                            value = inputRelationship,
                            onValueChange = { },
                            readOnly = true,
                            placeholder = { Text("Pilih Status", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { expandedRelationship = true })

                        DropdownMenu(
                            expanded = expandedRelationship,
                            onDismissRequest = { expandedRelationship = false },
                            modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                        ) {
                            relationshipOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        inputRelationship = option
                                        expandedRelationship = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = inputAge,
                            onValueChange = { inputAge = it },
                            placeholder = { Text("Umur", color = Color.Gray) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = inputBloodType,
                                onValueChange = { },
                                readOnly = true,
                                placeholder = { Text("Gol. Darah", color = Color.Gray) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8)) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = textFieldColors,
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { expandedBloodType = true })

                            DropdownMenu(
                                expanded = expandedBloodType,
                                onDismissRequest = { expandedBloodType = false },
                                modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                            ) {
                                bloodTypeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            inputBloodType = option
                                            expandedBloodType = false
                                        }
                                    )
                                }
                            }
                        }
                    }
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
                            val ageInt = inputAge.toIntOrNull()
                            if (inputName.isNotBlank() && inputRelationship.isNotBlank() && ageInt != null && ageInt in 1..120) {
                                if (memberToEdit != null) {
                                    val updatedMember = memberToEdit!!.copy(
                                        name = inputName, age = ageInt, bloodType = inputBloodType, relationship = inputRelationship
                                    )
                                    viewModel.updateFamilyMember(updatedMember)
                                } else {
                                    val newMember = FamilyMember(
                                        id = System.currentTimeMillis().toString(),
                                        name = inputName, age = ageInt, bloodType = inputBloodType, relationship = inputRelationship
                                    )
                                    viewModel.addFamilyMember(newMember)
                                }
                                showFormDialog = false
                            } else {
                                android.widget.Toast.makeText(context, "Lengkapi data dengan benar", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) { Text("Simpan Data", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }

                    TextButton(
                        onClick = { showFormDialog = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                }
            },
            dismissButton = null
        )
    }

    if (showDeleteDialog && memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Hapus Anggota?", fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus '${memberToDelete?.name}' dari daftar keluarga? Data yang dihapus tidak dapat dikembalikan.", color = Color.DarkGray)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFamilyMember(memberToDelete!!.id)
                        showDeleteDialog = false
                        memberToDelete = null
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Ya, Hapus Data", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold) }
            }
        )
    }
}
