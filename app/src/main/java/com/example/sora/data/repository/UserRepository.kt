package com.example.sora.data.repository

import com.example.sora.auth.SupabaseClient
import com.example.sora.data.model.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.io.File

class UserRepository {
    private val client = SupabaseClient.supabase

    suspend fun getAllUsers(): List<User> {
        return client.postgrest["users"]
            .select()
            .decodeList<User>()
    }
    @OptIn(kotlinx.serialization.InternalSerializationApi::class)
    suspend fun getUser(id: String): User? {
        return client.postgrest["users"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<User>()
    }

    suspend fun uploadProfilePicture(imageFile: File): Result<String> {
        val storage = client.storage.from("avatars")

        val userId = client.auth.currentUserOrNull()?.id
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val path = "$userId/avatar.jpg"

            storage.upload(path, imageFile.readBytes(), upsert = true)
            val newUrl = storage.publicUrl(path)

            client.postgrest["users"]
                .update({ set("avatar_url", newUrl) }) {
                    filter { eq("id", userId) }
                }
            Result.success(newUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    suspend fun updateUserActiveStatus(isActive: Boolean, explicitUserId: String? = null) {
        val userId = explicitUserId ?: client.auth.currentUserOrNull()?.id ?: return
        try {
            client.postgrest["users"].update({
                set("is_active", isActive)
            }) {
                filter { eq("id", userId) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getActiveFriends(): List<User> {
        val currentUser = client.auth.currentUserOrNull() ?: return emptyList()

        // 1. Get friends IDs
        val friends = client.postgrest["follow_user"]
            .select {
                filter { eq("follower_id", currentUser.id) }
            }
            .decodeList<FriendFollow>()
            .map { it.followeeId }

        if (friends.isEmpty()) return emptyList()

        // 2. Get active users from friends list
        return client.postgrest["users"]
            .select {
                filter {
                    isIn("id", friends)
                    eq("is_active", true)
                }
            }
            .decodeList<User>()
    }
}