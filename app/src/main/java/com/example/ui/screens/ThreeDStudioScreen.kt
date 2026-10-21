package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import android.content.Intent
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ThreeDGenerator
import com.example.data.model.RenderableMesh
import com.example.data.model.Saved3DModel
import com.example.ui.components.GlowingCyberCard
import com.example.ui.components.NeonBadge
import com.example.ui.components.RenderStyle
import com.example.ui.components.ThreeDCanvasView
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
fun ThreeDStudioScreen(
    initialPrompt: String = "",
    onSaveModel: (Saved3DModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customPrompt by remember { mutableStateOf(if (initialPrompt.isNotBlank()) initialPrompt else "Quantum Singularity Core") }
    var currentMesh by remember { mutableStateOf(ThreeDGenerator.generateMeshForPrompt(customPrompt)) }
    var autoRotate by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var renderStyle by remember { mutableStateOf(RenderStyle.WIREFRAME_GLOW) }
    var showObjDialog by remember { mutableStateOf(false) }

    val presetModels = listOf(
        "Quantum Core" to { ThreeDGenerator.getQuantumCoreMesh() },
        "Cyber Drone" to { ThreeDGenerator.getCyberDroneMesh() },
        "Space Fighter" to { ThreeDGenerator.getSpaceFighterMesh() },
        "Hypercar GT" to { ThreeDGenerator.getHypercarMesh() },
        "Diamond Gem" to { ThreeDGenerator.getDiamondMesh() },
        "Bionic Arm" to { ThreeDGenerator.getRoboticsArmMesh() },
        "Portal Ring" to { ThreeDGenerator.getTorusPortalMesh() }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Studio Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "3D CAD & Mesh Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time 360° interactive parametric modeling",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            NeonBadge(text = "${currentMesh.vertices.size} Vertices", color = NeonCyan)
        }

        // Preset Chips
        val presetScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(presetScroll)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetModels.forEach { (label, meshProvider) ->
                val isSelected = currentMesh.title.contains(label, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberCard)
                        .border(1.dp, if (isSelected) NeonCyan else CyberBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            currentMesh = meshProvider()
                            customPrompt = label
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) NeonCyan else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Interactive 3D Viewport Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("interactive_3d_viewport")
        ) {
            ThreeDCanvasView(
                mesh = currentMesh,
                renderStyle = renderStyle,
                autoRotate = autoRotate,
                scaleFactor = zoomScale,
                modifier = Modifier.fillMaxSize()
            )

            // Viewport Controls Overlay (Top Right)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { autoRotate = !autoRotate },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CyberSurface.copy(alpha = 0.8f))
                        .border(1.dp, CyberBorder, CircleShape)
                        .testTag("toggle_auto_spin")
                ) {
                    Icon(
                        imageVector = if (autoRotate) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Auto Spin",
                        tint = if (autoRotate) NeonCyan else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        zoomScale = 1.0f
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CyberSurface.copy(alpha = 0.8f))
                        .border(1.dp, CyberBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Zoom",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Gesture Hint (Bottom Left)
            Text(
                text = "👆 Drag to rotate 360°",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Shading Styles & Zoom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Style Selector
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    RenderStyle.WIREFRAME_GLOW to "Wireframe",
                    RenderStyle.SOLID_HOLO to "Hologram",
                    RenderStyle.POINTS_ONLY to "Points"
                ).forEach { (style, name) ->
                    val isSelected = renderStyle == style
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonPurple.copy(alpha = 0.25f) else CyberSurfaceVariant)
                            .border(1.dp, if (isSelected) NeonPurple else CyberBorder, RoundedCornerShape(12.dp))
                            .clickable { renderStyle = style }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) NeonPurple else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Zoom Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(140.dp)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Slider(
                    value = zoomScale,
                    onValueChange = { zoomScale = it },
                    valueRange = 0.5f..2.2f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = CyberBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Custom AI 3D Generation Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Generate Any Custom 3D Object:",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = { customPrompt = it },
                    placeholder = { Text("e.g., Cyberpunk Satellite, Quantum Engine...", color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_3d_prompt"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CyberCard,
                        unfocusedContainerColor = CyberCard
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (customPrompt.isNotBlank()) {
                            currentMesh = ThreeDGenerator.generateMeshForPrompt(customPrompt)
                        }
                    },
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("generate_3d_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberBackground)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Synthesize", color = CyberBackground, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Model Architecture & Geometry Specs Card
        GlowingCyberCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            borderColor = CyberBorder
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = currentMesh.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentMesh.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Vertices", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("${currentMesh.vertices.size}", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    }
                    Column {
                        Text("Edges", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("${currentMesh.edges.size}", style = MaterialTheme.typography.titleMedium, color = NeonPurple)
                    }
                    Column {
                        Text("Faces", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("${currentMesh.faces.size}", style = MaterialTheme.typography.titleMedium, color = NeonEmerald)
                    }
                    Column {
                        Text("Mesh Format", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("OBJ / CAD", style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons (Copy OBJ, Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("OBJ 3D Model", currentMesh.objCode))
                            Toast.makeText(context, "Wavefront .OBJ copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy .OBJ Code", fontSize = 12.sp, color = NeonCyan)
                    }

                    Button(
                        onClick = {
                            val saved = Saved3DModel(
                                title = currentMesh.title,
                                prompt = customPrompt,
                                meshType = currentMesh.meshType,
                                vertexCount = currentMesh.vertices.size,
                                polygonCount = currentMesh.faces.size,
                                description = currentMesh.description,
                                objCode = currentMesh.objCode
                            )
                            onSaveModel(saved)
                            Toast.makeText(context, "Saved 3D Model to Vault", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Vault", fontSize = 12.sp, color = CyberBackground, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            try {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, currentMesh.objCode)
                                    putExtra(Intent.EXTRA_TITLE, "${currentMesh.title}.obj")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share 3D Model .OBJ"))
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberSurfaceVariant)
                            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share .OBJ", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
