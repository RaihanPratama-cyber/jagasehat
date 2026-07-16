package com.example.jagasehat.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_records",
    foreignKeys = [
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId"), Index("ownerUsername"), Index("createdAt")]
)
data class HealthRecord(
    @PrimaryKey
    val id: String,
    val ownerUsername: String = "",
    val memberId: String,
    val date: String,
    val createdAt: Long = System.currentTimeMillis(),
    val bloodPressureSystolic: Int? = null,
    val bloodPressureDiastolic: Int? = null,
    val bloodSugar: Float? = null,
    val weight: Float? = null,
    val heartRate: Int? = null,
    val notes: String? = null
)
