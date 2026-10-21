package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.CloudSyncManager
import com.example.ui.components.GlowingCyberCard
import com.example.ui.components.NeonBadge
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CloudSyncScreen(
    syncManager: CloudSyncManager,
    chatCount: Int,
    qrCount: Int,
    modelCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val syncState by syncManager.syncState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "SyncRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "syncRot"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Seamless Cloud Mesh Sync",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Zero-latency multi-device continuous replication",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Sync Status Hero Card
        GlowingCyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (syncState.isSyncing) NeonCyan else NeonEmerald
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (syncState.isSyncing) NeonCyan.copy(alpha = 0.2f)
                            else NeonEmerald.copy(alpha = 0.2f)
                        )
                        .border(
                            1.5.dp,
                            if (syncState.isSyncing) NeonCyan else NeonEmerald,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (syncState.isSyncing) Icons.Default.Refresh else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (syncState.isSyncing) NeonCyan else NeonEmerald,
                        modifier = Modifier
                            .size(32.dp)
                            .then(if (syncState.isSyncing) Modifier.rotate(rotationAngle) else Modifier)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (syncState.isSyncing) "Synchronizing Assets..." else "Continuous Cloud Sync Active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = syncState.statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                NeonBadge(
                    text = "Last Synced: ${syncState.lastSyncFormatted}",
                    color = if (syncState.isSyncing) NeonCyan else NeonEmerald
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        syncManager.triggerInstantSync {
                            Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !syncState.isSyncing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instant_sync_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    if (syncState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyberBackground,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Replicating Across Cloud...", color = CyberBackground)
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = CyberBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync Now Across All Devices", color = CyberBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Device Pairing & Multi-Device Access
        GlowingCyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = CyberBorder
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Devices, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instant Device Pairing", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    NeonBadge(text = "${syncState.activeDevicesCount} Devices Linked", color = NeonPurple)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter this Quantum Pairing Code on any other phone, tablet, or web desktop to instantly share your AI knowledge vault and 3D models:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pairing Code Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurface)
                        .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DEVICE PAIRING CODE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = syncState.pairingCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple,
                            letterSpacing = 2.sp
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Bidiok Pairing Code", syncState.pairingCode))
                            Toast.makeText(context, "Pairing code copied!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = {
                            syncManager.generateNewPairingCode()
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate Code", tint = NeonPurple, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connected Devices List
                Text("Active Network Nodes:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))

                ConnectedDeviceRow(
                    name = "Pixel Phone (This Device)",
                    typeIcon = Icons.Default.PhoneAndroid,
                    status = "Current Session",
                    isOnline = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                ConnectedDeviceRow(
                    name = "Galaxy Tab S9 (Tablet)",
                    typeIcon = Icons.Default.Tablet,
                    status = "Synced 3 mins ago",
                    isOnline = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                ConnectedDeviceRow(
                    name = "Bidiok Web Desktop (Chrome)",
                    typeIcon = Icons.Default.Laptop,
                    status = "Synced 12 mins ago",
                    isOnline = true
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Synced Vault Metrics
        GlowingCyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = CyberBorder
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Replicated Vault Metrics", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Chats Synced", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("$chatCount", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    }
                    Column {
                        Text("QR Records", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("$qrCount", style = MaterialTheme.typography.titleMedium, color = NeonPurple)
                    }
                    Column {
                        Text("3D Assets", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("$modelCount", style = MaterialTheme.typography.titleMedium, color = NeonEmerald)
                    }
                    Column {
                        Text("Encryption", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("AES-256", style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Real-Time Auto-Replication", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Switch(
                        checked = syncState.isAutoSyncActive,
                        onCheckedChange = { syncManager.toggleAutoSync(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ConnectedDeviceRow(
    name: String,
    typeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    status: String,
    isOnline: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(typeIcon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(name, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(status, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) NeonEmerald else TextMuted)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isOnline) "ONLINE" else "OFFLINE", style = MaterialTheme.typography.labelSmall, color = if (isOnline) NeonEmerald else TextMuted, fontSize = 10.sp)
        }
    }
}
