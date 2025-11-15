package com.example.sora.viewmodel

import kotlinx.coroutines.flow.StateFlow

interface IFriendsViewModel {
    val filteredUsers: StateFlow<List<UserUi>>
    val searchQuery: StateFlow<String>

    fun updateSearchQuery(newValue: String)
    fun follow(userId: String)
    fun unfollow(userId: String)
}