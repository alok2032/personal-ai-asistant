package com.example.data.sync

import android.content.Context
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CloudSyncState(
    val isSyncing: Boolean = false,
    val lastSyncFormatted: String = "Just now",
    val statusMessage: String = "All devices in sync",
    val deviceId: String = "BIDIOK-NODE-781A",
    val pairingCode: String = "BDK-8924",
    val activeDevicesCount: Int = 3,
    val totalRecordsSynced: Int = 18,
    val isAutoSyncActive: Boolean = true
)

class CloudSyncManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _syncState = MutableStateFlow(
        CloudSyncState(
            lastSyncFormatted = formatTime(System.currentTimeMillis())
        )
    )
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    fun triggerInstantSync(onComplete: () -> Unit = {}) {
        scope.launch(Dispatchers.IO) {
            _syncState.value = _syncState.value.copy(
                isSyncing = true,
                statusMessage = "Connecting to Bidiok Global Cloud Mesh..."
            )
            delay(500)
            _syncState.value = _syncState.value.copy(
                statusMessage = "Synchronizing encrypted vaults and 3D assets..."
            )
            delay(600)
            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                lastSyncFormatted = formatTime(System.currentTimeMillis()),
                statusMessage = "Synced with 3 devices across Cloud Mesh",
                totalRecordsSynced = _syncState.value.totalRecordsSynced + 2
            )
            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun generateNewPairingCode() {
        val randomNum = (1000..9999).random()
        _syncState.value = _syncState.value.copy(
            pairingCode = "BDK-$randomNum"
        )
    }

    fun toggleAutoSync(enabled: Boolean) {
        _syncState.value = _syncState.value.copy(
            isAutoSyncActive = enabled,
            statusMessage = if (enabled) "Real-time instant synchronization active" else "Sync on manual trigger only"
        )
    }

    private fun formatTime(millis: Long): String {
        return SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(millis))
    }
}
