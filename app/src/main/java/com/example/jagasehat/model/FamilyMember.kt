package com.example.jagasehat.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "family_members",
    indices = [Index("ownerUsername")]
)
data class FamilyMember(
    @PrimaryKey
    val id: String,
    val ownerUsername: String = "",
    val name: String,
    val relationship: String,
    val age: Int,
    val bloodType: String? = null
)
