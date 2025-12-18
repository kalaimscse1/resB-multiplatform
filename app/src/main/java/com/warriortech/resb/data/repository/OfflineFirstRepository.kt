package com.warriortech.resb.data.repository

import com.warriortech.resb.util.ConnectionState
import com.warriortech.resb.util.NetworkMonitor
import kotlinx.coroutines.flow.first
import timber.log.Timber

abstract class OfflineFirstRepository(
    protected open val networkMonitor: NetworkMonitor
) {
    
    protected suspend fun isOnline(): Boolean {
        return try {
            networkMonitor.isOnline.first() == ConnectionState.Available
        } catch (e: Exception) {
            Timber.e(e, "Error checking network status")
            false
        }
    }
    
    protected inline fun <T> safeApiCall(
        onSuccess: (T) -> Unit = {},
        onError: (Exception) -> Unit = { Timber.e(it, "API call failed") },
        apiCall: () -> T
    ): T? {
        return try {
            val result = apiCall()
            onSuccess(result)
            result
        } catch (e: Exception) {
            onError(e)
            null
        }
    }
}
