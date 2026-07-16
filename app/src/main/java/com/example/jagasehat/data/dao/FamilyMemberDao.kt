package com.example.jagasehat.data.dao

import androidx.room.*
import com.example.jagasehat.model.FamilyMember
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :memberId")
    suspend fun getMemberById(memberId: String): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMember)

    @Update
    suspend fun update(member: FamilyMember)

    @Query("DELETE FROM family_members WHERE id = :memberId")
    suspend fun deleteById(memberId: String)
}
