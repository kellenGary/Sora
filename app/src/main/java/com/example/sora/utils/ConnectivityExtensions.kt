package com.example.sora.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Base interface for ViewModels that need to be aware of network connectivity.
 * Provides extension functions to handle network-dependent operations.
 */
interface ConnectivityAwareViewModel {
    val isNetworkConnected: StateFlow<Boolean>

    /**
     * Execute an operation only if network is connected.
     * If network is not connected, logs a warning and invokes the fallback.
     *
     * @param onNetworkConnected The operation to perform when network is available
     * @param onNetworkUnavailable Optional callback when network is unavailable
     */
    fun <T> executeIfConnected(
        onNetworkConnected: suspend () -> T,
        onNetworkUnavailable: suspend () -> Unit = {}
    )
}

/**
 * Extension function to handle network-dependent coroutine launches in a ViewModel.
 * This can be called from any ViewModel that has viewModelScope.
 *
 * @param networkConnectivityManager The network connectivity manager
 * @param onNetworkConnected The operation to perform when network is available
 * @param onNetworkUnavailable Optional callback when network is unavailable
 */
fun ViewModel.launchIfConnected(
    networkConnectivityManager: NetworkConnectivityManager,
    onNetworkConnected: suspend () -> Unit,
    onNetworkUnavailable: suspend () -> Unit = {}
) {
    viewModelScope.launch {
        if (networkConnectivityManager.isConnected.value) {
            try {
                onNetworkConnected()
            } catch (e: Exception) {
                // Handle network error
                e.printStackTrace()
                onNetworkUnavailable()
            }
        } else {
            onNetworkUnavailable()
        }
    }
}
