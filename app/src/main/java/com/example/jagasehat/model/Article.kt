package com.example.jagasehat.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articles",
    indices = [Index("createdAtMillis")]
)
data class Article(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val content: String,
    val author: String,
    val createdAt: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val imageUri: String? = null
)
