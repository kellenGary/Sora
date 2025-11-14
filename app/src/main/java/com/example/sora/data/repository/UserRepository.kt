package com.example.sora.data.repository

import com.example.sora.auth.SupabaseClient
import com.example.sora.data.model.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.io.File

class UserRepository {
    private val client = SupabaseClient.supabase

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
}