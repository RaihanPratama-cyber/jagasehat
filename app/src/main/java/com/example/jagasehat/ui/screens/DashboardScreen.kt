package com.example.jagasehat.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jagasehat.model.HealthRecord
import com.example.jagasehat.viewmodel.AppViewModel

@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    val memberNames = state.familyMembers.map { it.name }
    var selectedMember by remember { mutableStateOf(memberNames.firstOrNull() ?: "Pilih") }

    LaunchedEffect(memberNames) {
        if (memberNames.isNotEmpty() && (selectedMember == "Pilih" || selectedMember !in memberNames)) {
            selectedMember = memberNames.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.currentUser?.role == "User Biasa") {
            ModernDashboardUI(selectedMember, memberNames, { selectedMember = it }, viewModel)
        } else {
            val allRecords by viewModel.getAllHealthRecords().collectAsState(initial = emptyList())
            val familyScopes = remember(state.familyMembers, allRecords) {
                buildAdminFamilyScopes(state.familyMembers, allRecords)
            }
            var selectedOwner by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(familyScopes.map { it.ownerUsername }) {
                val availableOwners = familyScopes.map { it.ownerUsername }
                val shouldSelectFirst = selectedOwner == null || selectedOwner?.let { it !in availableOwners } == true
                if (availableOwners.isNotEmpty() && shouldSelectFirst) {
                    selectedOwner = availableOwners.first()
                }
            }

            val activeSelectedOwner = selectedOwner ?: familyScopes.firstOrNull()?.ownerUsername
            val selectedScope = familyScopes.find { it.ownerUsername == activeSelectedOwner }

            Text("Dashboard Admin", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981), modifier = Modifier.padding(top = 8.dp))
            Text("Pemantauan Kesehatan Per Keluarga", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            AdminFamilySelector(familyScopes, activeSelectedOwner) { selectedOwner = it }

            if (selectedScope == null) {
                EmptyAdminChartCard()
            } else {
                AdminLineGraphsUI(
                    records = selectedScope.records,
                    familyTitle = selectedScope.displayName,
                    memberCount = selectedScope.memberCount
                )
            }
        }
    }
}

private data class AdminFamilyGraphScope(
    val ownerUsername: String,
    val displayName: String,
    val memberCount: Int,
    val records: List<HealthRecord>
)

private fun buildAdminFamilyScopes(
    familyMembers: List<com.example.jagasehat.model.FamilyMember>,
    records: List<HealthRecord>
): List<AdminFamilyGraphScope> {
    val ownersFromMembers = familyMembers.mapNotNull { it.ownerUsername.takeIf { owner -> owner.isNotBlank() } }
    val ownersFromRecords = records.mapNotNull { it.ownerUsername.takeIf { owner -> owner.isNotBlank() } }
    val ownerUsernames = (ownersFromMembers + ownersFromRecords).distinct().sorted()

    return ownerUsernames.map { owner ->
        val membersInFamily = familyMembers.filter { it.ownerUsername == owner || it.id.endsWith("_$owner") }
        val memberIds = membersInFamily.map { it.id }.toSet()
        val recordsInFamily = records.filter { record ->
            record.ownerUsername == owner || record.memberId in memberIds || record.memberId.endsWith("_$owner")
        }.sortedBy { it.createdAt }

        AdminFamilyGraphScope(
            ownerUsername = owner,
            displayName = "Keluarga $owner",
            memberCount = membersInFamily.size,
            records = recordsInFamily
        )
    }
}

@Composable
private fun AdminFamilySelector(
    familyScopes: List<AdminFamilyGraphScope>,
    selectedOwner: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedScope = familyScopes.find { it.ownerUsername == selectedOwner }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = familyScopes.isNotEmpty()) { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(shape = CircleShape, color = Color(0xFFDCFCE7), modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(8.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Pilih keluarga", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Text(
                            selectedScope?.displayName ?: "Belum ada data keluarga",
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White, RoundedCornerShape(16.dp))
            ) {
                if (familyScopes.isEmpty()) {
                    DropdownMenuItem(text = { Text("Belum ada keluarga") }, onClick = { expanded = false })
                } else {
                    familyScopes.forEach { scope ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(scope.displayName, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    Text("${scope.memberCount} anggota • ${scope.records.size} data", fontSize = 12.sp, color = Color.Gray)
                                }
                            },
                            onClick = {
                                onSelect(scope.ownerUsername)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilySummaryHeader(familyTitle: String, memberCount: Int, recordCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFECFDF5),
        border = BorderStroke(1.dp, Color(0xFFD1FAE5))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(familyTitle, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF047857))
            Text("$memberCount anggota keluarga • $recordCount data kesehatan", fontSize = 13.sp, color = Color(0xFF64748B))
            Text("Grafik hanya memakai data keluarga ini, sehingga tidak tercampur dengan keluarga lain.", fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun EmptyAdminChartCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = CircleShape, color = Color(0xFFDCFCE7), modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(14.dp))
            }
            Text("Belum ada data keluarga", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text("Data grafik akan muncul setelah user menambahkan anggota keluarga dan menginput data kesehatan.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AdminLineGraphsUI(records: List<HealthRecord>, familyTitle: String, memberCount: Int) {
    val sortedRecords = records.sortedBy { it.createdAt }
    val bpPairs = sortedRecords.mapNotNull { record ->
        val sys = record.bloodPressureSystolic
        val dia = record.bloodPressureDiastolic
        if (sys != null && dia != null) sys.toFloat() to dia.toFloat() else null
    }.takeLast(10)

    val sysTrend = bpPairs.map { it.first }
    val diaTrend = bpPairs.map { it.second }
    val bpmTrend = sortedRecords.mapNotNull { it.heartRate?.toFloat() }.takeLast(10)
    val gulaTrend = sortedRecords.mapNotNull { it.bloodSugar }.takeLast(10)
    val beratTrend = sortedRecords.mapNotNull { it.weight }.takeLast(10)

    val avgSys = records.mapNotNull { it.bloodPressureSystolic }.averageValue()
    val avgDia = records.mapNotNull { it.bloodPressureDiastolic }.averageValue()
    val avgBpm = records.mapNotNull { it.heartRate }.averageValue()
    val avgGula = records.mapNotNull { it.bloodSugar?.toInt() }.averageValue()
    val avgBerat = records.mapNotNull { it.weight?.toInt() }.averageValue()

    FamilySummaryHeader(familyTitle = familyTitle, memberCount = memberCount, recordCount = records.size)

    HealthLineChartCard("Tekanan Darah", Icons.Default.FavoriteBorder, Color(0xFF3B82F6)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (avgSys > 0) "$avgSys" else "--", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF3B82F6))
                Text("Sistolik (mmHg)", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (avgDia > 0) "$avgDia" else "--", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF60A5FA))
                Text("Diastolik (mmHg)", fontSize = 12.sp, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        DoubleLineChartCanvas(sysTrend, diaTrend, Color(0xFF3B82F6), Color(0xFF60A5FA), Modifier.fillMaxWidth().height(150.dp))
    }

    HealthLineChartCard("Detak Jantung", Icons.Default.Favorite, Color(0xFFEF4444)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(if (avgBpm > 0) "$avgBpm" else "--", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                Text(" BPM", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SingleLineChartCanvas(bpmTrend, Color(0xFFEF4444), Modifier.fillMaxWidth().height(120.dp))
    }

    HealthLineChartCard("Gula Darah", Icons.Default.Star, Color(0xFFF59E0B)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(if (avgGula > 0) "$avgGula" else "--", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                Text(" mg/dL", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SingleLineChartCanvas(gulaTrend, Color(0xFFF59E0B), Modifier.fillMaxWidth().height(120.dp))
    }

    HealthLineChartCard("Berat Badan", Icons.Default.Person, Color(0xFF10B981)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(if (avgBerat > 0) "$avgBerat" else "--", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                Text(" Kg", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SingleLineChartCanvas(beratTrend, Color(0xFF10B981), Modifier.fillMaxWidth().height(120.dp))
    }
}

private fun List<Int>.averageValue(): Int {
    return if (isEmpty()) 0 else average().toInt()
}

@Composable
fun HealthLineChartCard(title: String, icon: ImageVector, color: Color, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) { Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp)) }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun SingleLineChartCanvas(data: List<Float>, lineColor: Color, modifier: Modifier) {
    if (data.isEmpty() || data.all { it == 0f }) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) { Text("Data belum tersedia", color = Color.LightGray) }
        return
    }
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val max = (data.maxOrNull() ?: 1f) * 1.2f
        val min = (data.minOrNull() ?: 0f) * 0.8f
        val range = if (max - min == 0f) 1f else (max - min)
        val stepX = w / (data.size - 1).coerceAtLeast(1)
        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = h - ((value - min) / range) * h
            if (index == 0) { path.moveTo(x, y); fillPath.moveTo(x, y) } else { path.lineTo(x, y); fillPath.lineTo(x, y) }
        }
        fillPath.lineTo(w, h); fillPath.lineTo(0f, h); fillPath.close()

        drawPath(fillPath, Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)))
        drawPath(path, lineColor, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val lastX = (data.size - 1) * stepX; val lastY = h - ((data.last() - min) / range) * h
        drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(lastX, lastY))
        drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(lastX, lastY), style = Stroke(2.dp.toPx()))
    }
}

@Composable
fun DoubleLineChartCanvas(data1: List<Float>, data2: List<Float>, color1: Color, color2: Color, modifier: Modifier) {
    if (data1.isEmpty() || data2.isEmpty() || (data1.all { it == 0f } && data2.all { it == 0f })) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) { Text("Data belum tersedia", color = Color.LightGray) }
        return
    }
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val max1 = data1.maxOrNull() ?: 0f; val max2 = data2.maxOrNull() ?: 0f
        val min1 = data1.minOrNull() ?: 0f; val min2 = data2.minOrNull() ?: 0f
        val overallMax = maxOf(max1, max2) * 1.2f
        val overallMin = minOf(min1, min2) * 0.8f
        val range = if (overallMax - overallMin == 0f) 1f else (overallMax - overallMin)
        val stepX = w / (data1.size - 1).coerceAtLeast(1)

        val drawLine = { data: List<Float>, color: Color ->
            val path = Path(); val fillPath = Path()
            data.forEachIndexed { index, value ->
                val x = index * stepX; val y = h - ((value - overallMin) / range) * h
                if (index == 0) { path.moveTo(x, y); fillPath.moveTo(x, y) } else { path.lineTo(x, y); fillPath.lineTo(x, y) }
            }
            fillPath.lineTo(w, h); fillPath.lineTo(0f, h); fillPath.close()
            drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent)))
            drawPath(path, color, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            val lastX = (data.size - 1) * stepX; val lastY = h - ((data.last() - overallMin) / range) * h
            drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(lastX, lastY))
            drawCircle(color, radius = 4.dp.toPx(), center = Offset(lastX, lastY), style = Stroke(2.dp.toPx()))
        }
        drawLine(data2, color2)
        drawLine(data1, color1)
    }
}

@Composable
fun ModernDashboardUI(selectedMember: String, memberNames: List<String>, onMemberChange: (String) -> Unit, viewModel: AppViewModel) {
    val context = LocalContext.current
    val memberId = viewModel.state.value.familyMembers.find { it.name == selectedMember }?.id ?: ""
    val latestRecord by remember(memberId) { viewModel.getLatestHealthRecord(memberId) }.collectAsState(initial = null)

    var expandedMemberDropdown by remember { mutableStateOf(false) }
    var showBPDialog by remember { mutableStateOf(false) }
    var showSugarDialog by remember { mutableStateOf(false) }
    var showBPMDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }

    var analysisResult by remember { mutableStateOf<AppViewModel.AnalysisResult?>(null) }
    var analysisValue by remember { mutableStateOf("") }
    var analysisUnit by remember { mutableStateOf("") }

    val formTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF10B981),
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFF1F5F9)
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Pemeriksaan untuk:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Box {
            Surface(
                modifier = Modifier.clickable { expandedMemberDropdown = true },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Color(0xFFEFF6FF), modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.padding(4.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedMember, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                }
            }
            DropdownMenu(expanded = expandedMemberDropdown, onDismissRequest = { expandedMemberDropdown = false }, modifier = Modifier.background(Color.White, RoundedCornerShape(16.dp))) {
                if (memberNames.isEmpty()) {
                    DropdownMenuItem(text = { Text("Belum ada keluarga") }, onClick = { expandedMemberDropdown = false })
                } else {
                    memberNames.forEach { name -> DropdownMenuItem(text = { Text(name, fontWeight = FontWeight.Medium) }, onClick = { onMemberChange(name); expandedMemberDropdown = false }) }
                }
            }
        }
    }

    val bpm = latestRecord?.heartRate ?: 0
    BigHealthCard("Detak Jantung", if (bpm > 0) bpm.toString() else "--", "BPM", Color(0xFFFFE4E6), Color(0xFF881337), Color(0xFFE11D48)) { if (memberNames.isNotEmpty()) showBPMDialog = true else android.widget.Toast.makeText(context, "Tambah keluarga dulu!", android.widget.Toast.LENGTH_SHORT).show() }

    val sys = latestRecord?.bloodPressureSystolic ?: 0
    val dia = latestRecord?.bloodPressureDiastolic ?: 0
    BigHealthCard("Tekanan Darah", if (sys > 0) "$sys/$dia" else "--", "mmHg", Color(0xFFE0F2FE), Color(0xFF0369A1), Color(0xFF0284C7)) { if (memberNames.isNotEmpty()) showBPDialog = true }

    val gula = latestRecord?.bloodSugar?.toInt() ?: 0
    BigHealthCard("Gula Darah", if (gula > 0) gula.toString() else "--", "mg/dL", Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFD97706)) { if (memberNames.isNotEmpty()) showSugarDialog = true }

    val berat = latestRecord?.weight?.toInt() ?: 0
    BigHealthCard("Berat Badan", if (berat > 0) berat.toString() else "--", "Kg", Color(0xFFDCFCE7), Color(0xFF14532D), Color(0xFF16A34A)) { if (memberNames.isNotEmpty()) showWeightDialog = true }


    if (showBPDialog) {
        var sysInput by remember { mutableStateOf("") }
        var diaInput by remember { mutableStateOf("") }
        var noteInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBPDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(shape = CircleShape, color = Color(0xFFDCFCE7), modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tekanan Darah", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E293B))
                    Text("Masukkan hasil tensi terbaru", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = sysInput, onValueChange = { sysInput = it },
                            placeholder = { Text("Sistolik", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = formTextFieldColors, singleLine = true
                        )
                        OutlinedTextField(
                            value = diaInput, onValueChange = { diaInput = it },
                            placeholder = { Text("Diastolik", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = formTextFieldColors, singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = noteInput, onValueChange = { noteInput = it },
                        placeholder = { Text("Catatan (opsional)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = formTextFieldColors, singleLine = true
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val sysValue = sysInput.toIntOrNull()
                            val diaValue = diaInput.toIntOrNull()
                            if (sysValue != null && diaValue != null && sysValue > 0 && diaValue > 0) {
                                viewModel.saveSpecificHealthData(context, memberId, "BP", sysValue.toFloat(), diaValue.toFloat(), noteInput)
                                showBPDialog = false
                                analysisValue = "$sysValue/$diaValue"
                                analysisUnit = "mmHg"
                                analysisResult = viewModel.analyzeBloodPressure(sysValue, diaValue)
                            } else {
                                android.widget.Toast.makeText(context, "Masukkan angka yang valid", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) { Text("Simpan Data", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }
                    TextButton(onClick = { showBPDialog = false }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            }, dismissButton = null
        )
    }

    if (showSugarDialog) {
        var gulaInput by remember { mutableStateOf("") }
        var noteInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSugarDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(shape = CircleShape, color = Color(0xFFDCFCE7), modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Gula Darah", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E293B))
                    Text("Masukkan kadar gula terbaru", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = gulaInput, onValueChange = { gulaInput = it },
                        placeholder = { Text("mg/dL (Misal: 90)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = formTextFieldColors, singleLine = true
                    )
                    OutlinedTextField(
                        value = noteInput, onValueChange = { noteInput = it },
                        placeholder = { Text("Catatan (opsional)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = formTextFieldColors, singleLine = true
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val gulaValue = gulaInput.toFloatOrNull()
                            if (gulaValue != null && gulaValue > 0f) {
                                viewModel.saveSpecificHealthData(context, memberId, "SUGAR", gulaValue, notes = noteInput)
                                showSugarDialog = false
                                analysisValue = gulaValue.toInt().toString()
                                analysisUnit = "mg/dL"
                                analysisResult = viewModel.analyzeBloodSugar(gulaValue.toInt())
                            } else {
                                android.widget.Toast.makeText(context, "Masukkan angka yang valid", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) { Text("Simpan Data", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }
                    TextButton(onClick = { showSugarDialog = false }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            }, dismissButton = null
        )
    }

    if (showBPMDialog) {
        var bpmInput by remember { mutableStateOf("") }
        var noteInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBPMDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(shape = CircleShape, color = Color(0xFFDCFCE7), modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Detak Jantung", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E293B))
                    Text("Masukkan hasil pengukuran", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = bpmInput, onValueChange = { bpmInput = it },
                        placeholder = { Text("BPM (Misal: 80)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = formTextFieldColors, singleLine = true
                    )
                    OutlinedTextField(
                        value = noteInput, onValueChange = { noteInput = it },
                        placeholder = { Text("Catatan (opsional)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = formTextFieldColors, singleLine = true
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val bpmValue = bpmInput.toIntOrNull()
                            if (bpmValue != null && bpmValue > 0) {
                                viewModel.saveSpecificHealthData(context, memberId, "BPM", bpmValue.toFloat(), notes = noteInput)
                                showBPMDialog = false
                                analysisValue = bpmValue.toString()
                                analysisUnit = "BPM"
                                analysisResult = viewModel.analyzeHeartRate(bpmValue)
                            } else {
                                android.widget.Toast.makeText(context, "Masukkan angka yang valid", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) { Text("Simpan Data", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }
                    TextButton(onClick = { showBPMDialog = false }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            }, dismissButton = null
        )
    }

    if (showWeightDialog) {
        var weightInput by remember { mutableStateOf("") }
        var noteInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(shape = CircleShape, color = Color(0xFFDCFCE7), modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Berat Badan", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E293B))
                    Text("Masukkan berat badan terbaru", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = weightInput, onValueChange = { weightInput = it },
                        placeholder = { Text("Kg (Misal: 65)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = formTextFieldColors, singleLine = true
                    )
                    OutlinedTextField(
                        value = noteInput, onValueChange = { noteInput = it },
                        placeholder = { Text("Catatan (opsional)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = formTextFieldColors, singleLine = true
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val weightValue = weightInput.toFloatOrNull()
                            if (weightValue != null && weightValue > 0f) {
                                viewModel.saveSpecificHealthData(context, memberId, "WEIGHT", weightValue, notes = noteInput)
                                showWeightDialog = false
                                analysisValue = weightValue.toInt().toString()
                                analysisUnit = "Kg"
                                analysisResult = viewModel.analyzeWeight(weightValue)
                            } else {
                                android.widget.Toast.makeText(context, "Masukkan angka yang valid", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) { Text("Simpan Data", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }
                    TextButton(onClick = { showWeightDialog = false }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            }, dismissButton = null
        )
    }

    if (analysisResult != null) {
        AlertDialog(
            onDismissRequest = { analysisResult = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text(analysisValue, fontSize = 56.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(analysisUnit, fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = analysisResult!!.color.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "${analysisResult!!.status}!",
                            color = analysisResult!!.color,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    Text(analysisResult!!.message, textAlign = TextAlign.Center, color = Color.DarkGray, fontSize = 14.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { analysisResult = null },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = analysisResult!!.color),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Tutup", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }
            }, dismissButton = null
        )
    }
}

@Composable
fun BigHealthCard(title: String, valueStr: String, unit: String, containerColor: Color, contentColor: Color, buttonColor: Color, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = contentColor)
            Text("Hasil terbaru: ${if (valueStr != "--") "Tersedia" else "Belum ada data"}", fontSize = 14.sp, color = contentColor.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(valueStr, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    if (valueStr != "--") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(unit, fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Data", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
