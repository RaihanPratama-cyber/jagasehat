package com.example.jagasehat.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jagasehat.model.Article
import com.example.jagasehat.model.FamilyMember
import com.example.jagasehat.model.HealthRecord
import com.example.jagasehat.model.Reminder
import com.example.jagasehat.repository.HealthRepository
import com.example.jagasehat.utils.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppViewModel(
    private val repository: HealthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private var rawFamilyMembers = emptyList<FamilyMember>()
    private var rawReminders = emptyList<Reminder>()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.getAllMembers().collect { members ->
                    rawFamilyMembers = members
                    applyRoleFilters()
                }
            } catch (e: Exception) {
                setError(e)
            }
        }
        viewModelScope.launch {
            try {
                repository.getAllReminders().collect { reminders ->
                    rawReminders = reminders
                    applyRoleFilters()
                }
            } catch (e: Exception) {
                setError(e)
            }
        }
        viewModelScope.launch {
            try {
                repository.getAllArticles().collect { articles ->
                    _state.update { it.copy(articles = articles, isLoading = false) }
                }
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    private fun applyRoleFilters() {
        val user = _state.value.currentUser ?: return
        if (user.role == "User Biasa") {
            val family = rawFamilyMembers.filter { it.ownerUsername == user.username || it.id.endsWith("_${user.username}") }
            val reminders = rawReminders.filter { it.ownerUsername == user.username || it.id.endsWith("_${user.username}") }
            _state.update { it.copy(familyMembers = family, reminders = reminders, isLoading = false) }
        } else {
            _state.update { it.copy(familyMembers = rawFamilyMembers, reminders = rawReminders, isLoading = false) }
        }
    }

    private fun setError(e: Exception) {
        _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Terjadi kesalahan") }
    }

    fun checkIsFirstLaunch(context: Context): Boolean {
        return context.getSharedPreferences("JagaSehatSettings", Context.MODE_PRIVATE)
            .getBoolean("isFirstLaunch", true)
    }

    fun completeOnboarding(context: Context) {
        context.getSharedPreferences("JagaSehatSettings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("isFirstLaunch", false)
            .apply()
    }

    fun registerUser(
        context: Context,
        username: String,
        name: String,
        role: String,
        password: String,
        email: String = "",
        phone: String = ""
    ): Boolean {
        val cleanUsername = username.trim()
        val sharedPref = context.getSharedPreferences("JagaSehatAuth", Context.MODE_PRIVATE)
        if (sharedPref.contains("${cleanUsername}_pass")) return false

        with(sharedPref.edit()) {
            putString("${cleanUsername}_pass", password)
            putString("${cleanUsername}_name", name.trim())
            putString("${cleanUsername}_role", role)
            putString("${cleanUsername}_email", email.trim())
            putString("${cleanUsername}_phone", phone.trim())
            apply()
        }
        return true
    }

    fun loginUser(context: Context, username: String, password: String): String {
        val cleanUsername = username.trim()
        if (cleanUsername == "admin" && password == "admin") {
            _state.update { it.copy(currentUser = User("admin1", "Kepala Admin", "Admin")) }
            applyRoleFilters()
            return "SUCCESS"
        }
        if (cleanUsername == "admin1" && password == "admin1") {
            _state.update { it.copy(currentUser = User("admin2", "Admin Pengawas", "Admin")) }
            applyRoleFilters()
            return "SUCCESS"
        }

        val sharedPref = context.getSharedPreferences("JagaSehatAuth", Context.MODE_PRIVATE)
        val savedPass = sharedPref.getString("${cleanUsername}_pass", null)

        if (savedPass == null) return "Username tidak ditemukan!"
        if (savedPass != password) return "Password salah!"

        val name = sharedPref.getString("${cleanUsername}_name", "Pengguna") ?: "Pengguna"
        val role = sharedPref.getString("${cleanUsername}_role", "User Biasa") ?: "User Biasa"

        _state.update { it.copy(currentUser = User(cleanUsername, name, role)) }
        applyRoleFilters()
        return "SUCCESS"
    }

    fun loginWithGoogle(context: Context, email: String, name: String) {
        val cleanEmail = email.trim()
        val sharedPref = context.getSharedPreferences("JagaSehatAuth", Context.MODE_PRIVATE)
        if (!sharedPref.contains("${cleanEmail}_pass")) {
            with(sharedPref.edit()) {
                putString("${cleanEmail}_pass", "GOOGLE_SSO_SECURE")
                putString("${cleanEmail}_name", name.trim())
                putString("${cleanEmail}_role", "User Biasa")
                apply()
            }
        }
        val role = sharedPref.getString("${cleanEmail}_role", "User Biasa") ?: "User Biasa"
        _state.update { it.copy(currentUser = User(cleanEmail, name.trim(), role)) }
        applyRoleFilters()
    }

    fun logoutUser() {
        _state.update { it.copy(currentUser = null) }
    }

    fun selectMember(memberId: String) {
        _state.update { it.copy(selectedMemberId = memberId) }
    }

    fun addFamilyMember(member: FamilyMember) {
        val user = _state.value.currentUser ?: return
        val newMember = member.copy(
            id = UUID.randomUUID().toString() + "_${user.username}",
            ownerUsername = user.username,
            name = member.name.trim(),
            relationship = member.relationship.trim(),
            bloodType = member.bloodType?.trim()
        )
        rawFamilyMembers = rawFamilyMembers + newMember
        applyRoleFilters()
        viewModelScope.launch {
            try {
                repository.insertMember(newMember)
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    fun updateFamilyMember(updatedMember: FamilyMember) {
        val user = _state.value.currentUser
        val fixedMember = updatedMember.copy(
            ownerUsername = updatedMember.ownerUsername.ifBlank { user?.username ?: "" },
            name = updatedMember.name.trim(),
            relationship = updatedMember.relationship.trim(),
            bloodType = updatedMember.bloodType?.trim()
        )
        rawFamilyMembers = rawFamilyMembers.map { if (it.id == fixedMember.id) fixedMember else it }
        applyRoleFilters()
        viewModelScope.launch {
            try {
                repository.updateMember(fixedMember)
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    fun deleteFamilyMember(memberId: String) {
        rawFamilyMembers = rawFamilyMembers.filter { it.id != memberId }
        applyRoleFilters()
        viewModelScope.launch {
            try {
                repository.deleteMember(memberId)
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    fun addArticle(
        title: String,
        category: String,
        content: String,
        author: String,
        imageUri: String? = null
    ): String {
        val now = System.currentTimeMillis()
        val article = Article(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            category = category.trim(),
            content = content.trim(),
            author = author.trim(),
            createdAt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(now)),
            createdAtMillis = now,
            imageUri = imageUri
        )
        _state.update { it.copy(articles = listOf(article) + it.articles) }
        viewModelScope.launch {
            try {
                repository.insertArticle(article)
            } catch (e: Exception) {
                setError(e)
            }
        }
        return article.id
    }

    fun deleteArticle(id: String) {
        _state.update { it.copy(articles = it.articles.filter { article -> article.id != id }) }
        viewModelScope.launch {
            try {
                repository.deleteArticle(id)
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    fun updateArticle(
        id: String,
        title: String,
        category: String,
        content: String,
        author: String,
        imageUri: String? = null
    ) {
        val current = _state.value.articles.find { it.id == id } ?: return
        val updated = current.copy(
            title = title.trim(),
            category = category.trim(),
            content = content.trim(),
            author = author.trim(),
            imageUri = imageUri ?: current.imageUri
        )
        _state.update { it.copy(articles = it.articles.map { article -> if (article.id == id) updated else article }) }
        viewModelScope.launch {
            try {
                repository.updateArticle(updated)
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    fun addReminder(memberId: String, title: String, time: String, frequency: String, context: Context? = null) {
        val user = _state.value.currentUser ?: return
        val reminder = Reminder(
            id = UUID.randomUUID().toString() + "_${user.username}",
            ownerUsername = user.username,
            memberId = memberId,
            title = title.trim(),
            time = time,
            frequency = frequency,
            enabled = true
        )
        rawReminders = rawReminders + reminder
        applyRoleFilters()
        context?.let { ReminderScheduler.schedule(it, reminder) }
        viewModelScope.launch {
            try {
                repository.insertReminder(reminder)
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    fun toggleReminder(reminderId: String, context: Context? = null) {
        rawReminders = rawReminders.map { if (it.id == reminderId) it.copy(enabled = !it.enabled) else it }
        val reminder = rawReminders.find { it.id == reminderId }
        applyRoleFilters()
        if (reminder != null && context != null) {
            if (reminder.enabled) ReminderScheduler.schedule(context, reminder) else ReminderScheduler.cancel(context, reminder.id)
        }
        viewModelScope.launch {
            try {
                if (reminder != null) repository.updateReminder(reminder)
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    fun deleteReminder(reminderId: String, context: Context? = null) {
        context?.let { ReminderScheduler.cancel(it, reminderId) }
        rawReminders = rawReminders.filter { it.id != reminderId }
        applyRoleFilters()
        viewModelScope.launch {
            try {
                repository.deleteReminder(reminderId)
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    fun rescheduleReminders(context: Context) {
        _state.value.reminders.filter { it.enabled }.forEach { ReminderScheduler.schedule(context, it) }
    }

    fun saveSpecificHealthData(
        context: Context,
        memberIdentifier: String,
        type: String,
        value1: Float,
        value2: Float = 0f,
        notes: String? = null
    ) {
        val user = _state.value.currentUser ?: return
        val member = rawFamilyMembers.find { it.id == memberIdentifier }
            ?: rawFamilyMembers.find { it.name == memberIdentifier && (it.ownerUsername == user.username || it.id.endsWith("_${user.username}")) }
            ?: return
        val now = System.currentTimeMillis()
        val record = HealthRecord(
            id = UUID.randomUUID().toString(),
            ownerUsername = user.username,
            memberId = member.id,
            date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(now)),
            createdAt = now,
            bloodPressureSystolic = if (type == "BP") value1.toInt() else null,
            bloodPressureDiastolic = if (type == "BP") value2.toInt() else null,
            bloodSugar = if (type == "SUGAR") value1 else null,
            heartRate = if (type == "BPM") value1.toInt() else null,
            weight = if (type == "WEIGHT") value1 else null,
            notes = notes?.trim()?.takeIf { it.isNotEmpty() } ?: "Dicatat via Dashboard"
        )

        viewModelScope.launch {
            try {
                repository.insertHealthRecord(record)
            } catch (e: Exception) {
                setError(e)
            }
        }
    }

    fun getLatestHealthRecord(memberId: String): Flow<HealthRecord?> {
        return repository.getRecordsByMemberId(memberId).map { records ->
            if (records.isEmpty()) return@map null
            HealthRecord(
                id = records.first().id,
                ownerUsername = records.first().ownerUsername,
                memberId = memberId,
                date = records.first().date,
                createdAt = records.first().createdAt,
                bloodPressureSystolic = records.firstNotNullOfOrNull { it.bloodPressureSystolic },
                bloodPressureDiastolic = records.firstNotNullOfOrNull { it.bloodPressureDiastolic },
                bloodSugar = records.firstNotNullOfOrNull { it.bloodSugar },
                heartRate = records.firstNotNullOfOrNull { it.heartRate },
                weight = records.firstNotNullOfOrNull { it.weight },
                notes = records.firstOrNull { !it.notes.isNullOrBlank() }?.notes
            )
        }
    }

    fun getAllHealthRecords(): Flow<List<HealthRecord>> {
        val user = _state.value.currentUser
        return if (user?.role == "User Biasa") {
            repository.getRecordsByOwner(user.username)
        } else {
            repository.getAllRecords()
        }
    }

    data class AnalysisResult(val status: String, val message: String, val color: Color)

    fun analyzeBloodPressure(sys: Int, dia: Int): AnalysisResult {
        return when {
            sys < 90 || dia < 60 -> AnalysisResult("Rendah", "Tekanan darah rendah (Hipotensi). Perbanyak minum air dan hindari berdiri tiba-tiba.", Color(0xFF3B82F6))
            sys in 90..120 && dia in 60..80 -> AnalysisResult("Normal", "Tekanan darah stabil, terus pertahankan! Bagus! Ini tanda kondisi fisik yang baik.", Color(0xFF10B981))
            sys in 121..129 && dia <= 80 -> AnalysisResult("Meningkat", "Tekanan darah sedikit naik. Kurangi konsumsi garam dan perbanyak olahraga.", Color(0xFFF59E0B))
            sys in 130..139 || dia in 81..89 -> AnalysisResult("Hipertensi 1", "Tekanan darah tinggi tingkat 1. Disarankan pemeriksaan kardiovaskular.", Color(0xFFF97316))
            else -> AnalysisResult("Tinggi", "Tekanan darah sangat tinggi! Segera periksa ke dokter dan hindari stres.", Color(0xFFEF4444))
        }
    }

    fun analyzeBloodSugar(sugar: Int): AnalysisResult {
        return when {
            sugar < 70 -> AnalysisResult("Rendah", "Gula darah rendah (Hipoglikemia). Segera konsumsi makanan/minuman manis.", Color(0xFF3B82F6))
            sugar in 70..100 -> AnalysisResult("Normal", "Gula darah stabil, nilai kesehatan bertambah! Pola makan seimbang menjaga vitalitas.", Color(0xFF10B981))
            sugar in 101..125 -> AnalysisResult("Pradiabetes", "Gula darah agak tinggi. Batasi asupan gula dan karbohidrat sederhana.", Color(0xFFF59E0B))
            else -> AnalysisResult("Diabetes", "Gula darah sangat tinggi! Disarankan bagi kelompok risiko tinggi untuk rutin skrining.", Color(0xFFEF4444))
        }
    }

    fun analyzeHeartRate(bpm: Int): AnalysisResult {
        return when {
            bpm < 60 -> AnalysisResult("Rendah", "Detak jantung di bawah normal (Bradikardia). Normal jika Anda atlet.", Color(0xFF3B82F6))
            bpm in 60..100 -> AnalysisResult("Normal", "Detak jantung istirahat yang sangat baik. Jantung Anda berfungsi optimal.", Color(0xFF10B981))
            else -> AnalysisResult("Tinggi", "Detak jantung cepat (Takikardia). Kurangi kafein dan cobalah rileks.", Color(0xFFEF4444))
        }
    }

    fun analyzeWeight(weight: Float): AnalysisResult {
        return AnalysisResult("Tersimpan", "Berat badan sebesar ${weight.toInt()} kg telah berhasil dicatat. Selalu pantau asupan nutrisi Anda dengan baik!", Color(0xFF10B981))
    }

    fun saveProfilePicture(context: Context, uriString: String) {
        val user = _state.value.currentUser ?: return
        context.getSharedPreferences("JagaSehatProfile", Context.MODE_PRIVATE)
            .edit()
            .putString("${user.username}_photo", uriString)
            .apply()
    }

    fun loadProfilePicture(context: Context): String? {
        val user = _state.value.currentUser ?: return null
        return context.getSharedPreferences("JagaSehatProfile", Context.MODE_PRIVATE)
            .getString("${user.username}_photo", null)
    }
}
