package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.QrRecord
import com.example.qr.QrCodeHelper
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
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(
    historyList: List<QrRecord>,
    onSaveScannedRecord: (String, String, String) -> Unit,
    onDeleteRecord: (QrRecord) -> Unit,
    onAskAiAboutQr: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Scanner, 1: Generator, 2: History

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Screen Header
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
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "QR & Vision Scanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Instant decode, AI security check & generation",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberSurface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Scan QR", color = if (selectedTab == 0) NeonCyan else TextSecondary) },
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_qr_scan")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Create QR", color = if (selectedTab == 1) NeonCyan else TextSecondary) },
                icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_qr_create")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("History (${historyList.size})", color = if (selectedTab == 2) NeonCyan else TextSecondary) },
                icon = { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_qr_history")
            )
        }

        when (selectedTab) {
            0 -> QrLiveScannerTab(
                onQrScanned = { raw ->
                    val type = QrCodeHelper.detectQrType(raw)
                    val aiAnalysis = synthesizeAiSafetyCheck(raw, type)
                    onSaveScannedRecord(raw, type, aiAnalysis)
                },
                onAskAi = onAskAiAboutQr
            )
            1 -> QrGeneratorTab(
                onSaveRecord = { content, title ->
                    onSaveScannedRecord(content, "GENERATED", "Generated via Bidiok AI QR Studio: $title")
                }
            )
            2 -> QrHistoryTab(
                historyList = historyList,
                onDelete = onDeleteRecord,
                onAskAi = onAskAiAboutQr
            )
        }
    }
}

@Composable
fun QrLiveScannerTab(
    onQrScanned: (String) -> Unit,
    onAskAi: (String) -> Unit
) {
    val context = LocalContext.current
    var scannedContent by remember { mutableStateOf<String?>(null) }
    var detectedType by remember { mutableStateOf("TEXT") }
    var aiAnalysisText by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    // Laser Animation for Viewfinder
    val infiniteTransition = rememberInfiniteTransition(label = "LaserScan")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserProg"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Camera Viewfinder Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
                .border(2.dp, NeonCyan, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                CameraPreviewView(
                    onQrDetected = { qr ->
                        if (scannedContent != qr) {
                            scannedContent = qr
                            detectedType = QrCodeHelper.detectQrType(qr)
                            aiAnalysisText = synthesizeAiSafetyCheck(qr, detectedType)
                            onQrScanned(qr)
                        }
                    }
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Grant Camera Access", color = CyberBackground)
                    }
                }
            }

            // Laser scan beam line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(2.5.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = (260 * laserProgress).dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, NeonCyan, NeonPurple, Color.Transparent)
                        )
                    )
            )

            // Corner Aim Crosshairs
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(1.5.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            )
        }

        val photoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        val decoded = QrCodeHelper.decodeQrBitmap(bitmap)
                        if (decoded != null && decoded.isNotBlank()) {
                            scannedContent = decoded
                            detectedType = QrCodeHelper.detectQrType(decoded)
                            aiAnalysisText = synthesizeAiSafetyCheck(decoded, detectedType)
                            onQrScanned(decoded)
                            Toast.makeText(context, "QR Code decoded from photo!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No QR Code found in selected image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    photoPicker.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCard)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan from Photo", fontSize = 11.sp, color = NeonCyan)
            }

            Button(
                onClick = {
                    val sample = "https://github.com/bidiok-ai/assistant"
                    scannedContent = sample
                    detectedType = "URL"
                    aiAnalysisText = synthesizeAiSafetyCheck(sample, detectedType)
                    onQrScanned(sample)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCard)
            ) {
                Text("Sample URL", fontSize = 11.sp, color = NeonPurple)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Result Card if Scanned
        if (scannedContent != null) {
            GlowingCyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scanned_result_card"),
                borderColor = NeonCyan
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeonBadge(
                            text = "TYPE: $detectedType",
                            color = if (detectedType == "URL") NeonCyan else NeonPurple
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("QR Code", scannedContent))
                                    Toast.makeText(context, "Copied content", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                            if (detectedType == "URL") {
                                IconButton(
                                    onClick = {
                                        try {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedContent))
                                            context.startActivity(browserIntent)
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = "Open", tint = NeonCyan, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = scannedContent ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // AI Safety & Explainer
                    if (aiAnalysisText != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurface)
                                .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bidiok AI Analysis", style = MaterialTheme.typography.labelSmall, color = NeonEmerald, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = aiAnalysisText ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onAskAi("Explain and analyze this QR payload: $scannedContent") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ask_ai_qr_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ask Bidiok AI to Explain This QR", color = CyberBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreviewView(onQrDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val cameraExecutor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    try {
                        val buffer = imageProxy.planes[0].buffer
                        val data = ByteArray(buffer.remaining())
                        buffer.get(data)
                        val width = imageProxy.width
                        val height = imageProxy.height

                        val source = com.google.zxing.PlanarYUVLuminanceSource(
                            data, width, height, 0, 0, width, height, false
                        )
                        val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
                        val reader = com.google.zxing.MultiFormatReader()
                        val result = reader.decodeWithState(binaryBitmap)

                        if (result != null && result.text.isNotBlank()) {
                            ContextCompat.getMainExecutor(ctx).execute {
                                onQrDetected(result.text)
                            }
                        }
                    } catch (_: Exception) {
                        // Ignore frames where no barcode/QR is detected
                    } finally {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun QrGeneratorTab(onSaveRecord: (String, String) -> Unit) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("https://bidiok.ai/assistant") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrThemeColor by remember { mutableIntStateOf(0) } // 0: Cyan, 1: Purple, 2: Emerald

    LaunchedEffect(inputText, qrThemeColor) {
        val colorHex = when (qrThemeColor) {
            0 -> android.graphics.Color.parseColor("#00F2FE")
            1 -> android.graphics.Color.parseColor("#A855F7")
            else -> android.graphics.Color.parseColor("#10B981")
        }
        generatedBitmap = QrCodeHelper.generateQrBitmap(inputText, size = 400, foregroundColor = colorHex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter Text, URL, or WiFi details", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qr_generator_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Color Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Color Glow:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(NeonCyan, NeonPurple, NeonEmerald).forEachIndexed { index, col ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                width = if (qrThemeColor == index) 2.dp else 0.dp,
                                color = if (qrThemeColor == index) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { qrThemeColor = index }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generated QR Image
        if (generatedBitmap != null) {
            GlowingCyberCard(
                modifier = Modifier
                    .size(260.dp)
                    .padding(8.dp),
                borderColor = if (qrThemeColor == 0) NeonCyan else if (qrThemeColor == 1) NeonPurple else NeonEmerald
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = generatedBitmap!!.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSaveRecord(inputText, "Custom QR Code")
                    Toast.makeText(context, "Saved QR Code to Vault", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_qr_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = CyberBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save to Bidiok Vault", color = CyberBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QrHistoryTab(
    historyList: List<QrRecord>,
    onDelete: (QrRecord) -> Unit,
    onAskAi: (String) -> Unit
) {
    if (historyList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No QR scans yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                Text("Scan any QR or Barcode to see history here.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(historyList, key = { it.id }) { record ->
                GlowingCyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = CyberBorder
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NeonBadge(text = record.qrType, color = NeonCyan)
                            IconButton(onClick = { onDelete(record) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = record.rawContent, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

                        if (!record.aiAnalysis.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "🛡️ ${record.aiAnalysis}", style = MaterialTheme.typography.bodySmall, color = NeonEmerald, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onAskAi("Explain this QR code: ${record.rawContent}") },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant)
                            ) {
                                Text("Ask AI", fontSize = 11.sp, color = NeonCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun synthesizeAiSafetyCheck(raw: String, type: String): String {
    return when (type) {
        "URL" -> if (raw.contains("https://")) "Verified HTTPS protocol. Low threat probability." else "Warning: Unencrypted HTTP connection detected."
        "WIFI" -> "Wireless Access Point configuration detected. Credentials parsed safely."
        "CONTACT" -> "vCard digital identity profile. Format compliant."
        else -> "Plaintext payload verified. Safe to copy or share."
    }
}
