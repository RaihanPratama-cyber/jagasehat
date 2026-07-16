package com.example.jagasehat.viewmodel

import com.example.jagasehat.model.*

data class User(
    val username: String,
    val name: String,
    val role: String
)

data class AppState(
    val currentUser: User? = null,
    val selectedMemberId: String? = null,
    val familyMembers: List<FamilyMember> = emptyList(),
    val healthRecords: List<HealthRecord> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
