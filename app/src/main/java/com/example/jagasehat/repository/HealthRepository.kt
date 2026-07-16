package com.example.jagasehat.repository

import com.example.jagasehat.data.database.AppDatabase
import com.example.jagasehat.model.*
import kotlinx.coroutines.flow.Flow

class HealthRepository(database: AppDatabase) {

    private val memberDao = database.familyMemberDao()
    private val healthRecordDao = database.healthRecordDao()
    private val reminderDao = database.reminderDao()
    private val articleDao = database.articleDao()

    fun getAllMembers(): Flow<List<FamilyMember>> = memberDao.getAllMembers()

    suspend fun getMemberById(id: String) = memberDao.getMemberById(id)

    suspend fun insertMember(member: FamilyMember) = memberDao.insert(member)

    suspend fun updateMember(member: FamilyMember) = memberDao.update(member)

    suspend fun deleteMember(memberId: String) = memberDao.deleteById(memberId)

    fun getAllRecords(): Flow<List<HealthRecord>> = healthRecordDao.getAllRecords()

    fun getRecordsByMemberId(memberId: String): Flow<List<HealthRecord>> =
        healthRecordDao.getRecordsByMemberId(memberId)

    fun getRecordsByOwner(ownerUsername: String): Flow<List<HealthRecord>> =
        healthRecordDao.getRecordsByOwner(ownerUsername)

    suspend fun insertHealthRecord(record: HealthRecord) = healthRecordDao.insert(record)

    suspend fun deleteHealthRecord(recordId: String) = healthRecordDao.deleteById(recordId)

    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun insertReminder(reminder: Reminder) = reminderDao.insert(reminder)

    suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)

    suspend fun deleteReminder(reminderId: String) = reminderDao.deleteById(reminderId)

    fun getAllArticles(): Flow<List<Article>> = articleDao.getAllArticles()

    suspend fun insertArticle(article: Article) = articleDao.insert(article)

    suspend fun updateArticle(article: Article) = articleDao.update(article)

    suspend fun deleteArticle(articleId: String) = articleDao.deleteById(articleId)

}
