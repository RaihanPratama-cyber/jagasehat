package com.example.jagasehat.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId"), Index("ownerUsername")]
)
data class Reminder(
    @PrimaryKey
    val id: String,
    val ownerUsername: String = "",
    val memberId: String,
    val title: String,
    val time: String,
    val frequency: String,
    val enabled: Boolean
)
