package com.example.sora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sora.auth.AuthRepository
import com.example.sora.data.repository.FollowRepository
import com.example.sora.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserUi(
    val id: String,
    val displayName: String?,
    val avatarUrl: String?,
    val isFollowed: Boolean
)

class FriendsViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val followRepository: FollowRepository = FollowRepository(),
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserUi>>(emptyList())
    val users: StateFlow<List<UserUi>> = _users.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredUsers = combine(_users, _searchQuery) { users, query ->
        if (query.isBlank()) users
        else users.filter { (it.displayName ?: "").contains(query, ignoreCase = true) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            val userList = userRepository.getAllUsers()
            val currentUserId = authRepository.getCurrentUser()?.id

            val uiList = userList
                .filter { it.id != currentUserId }
                .map { user ->
                val followed = followRepository.isFollowing(user.id)
                UserUi(
                    id = user.id,
                    displayName = user.displayName,
                    avatarUrl = user.avatarUrl,
                    isFollowed = followed
                )
            }

            _users.value = uiList

        }
    }

    fun updateSearchQuery(newValue: String) {
        _searchQuery.value = newValue
    }

    fun follow(userId: String) {
        viewModelScope.launch {
            followRepository.followUser(userId)

            _users.value = _users.value.map { user ->
                if (user.id == userId) user.copy(isFollowed = true) else user
            }
        }
    }

    fun unfollow(userId: String) {
        viewModelScope.launch {
            followRepository.unfollowUser(userId)
        }
    }
}