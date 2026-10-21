package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ai.BidiokAiEngine
import com.example.data.model.AiModelType
import com.example.data.model.CreativeCraft
import com.example.data.service.FileConverterEngine
import com.example.data.service.PdfGeneratorEngine
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
import kotlinx.coroutines.launch
import java.net.URLEncoder

@Composable
fun CreativeHubScreen(
    onSaveCraft: (CreativeCraft) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Image Studio, 1: Video Studio, 2: PDF Creator, 3: File Converter

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = NeonPurple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Bidiok Super Creative Studios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Image Synthesis • Video Pacing • PDF Documents • File Converter",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberSurface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = when (selectedTab) {
                        0 -> NeonCyan
                        1 -> NeonPurple
                        2 -> NeonEmerald
                        else -> NeonAmber
                    }
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Image Studio", fontSize = 11.sp, color = if (selectedTab == 0) NeonCyan else TextSecondary) },
                icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Video Studio", fontSize = 11.sp, color = if (selectedTab == 1) NeonPurple else TextSecondary) },
                icon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("PDF Creator", fontSize = 11.sp, color = if (selectedTab == 2) NeonEmerald else TextSecondary) },
                icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Converter", fontSize = 11.sp, color = if (selectedTab == 3) NeonAmber else TextSecondary) },
                icon = { Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        when (selectedTab) {
            0 -> ImageVisionCraftTab(onSaveCraft)
            1 -> VideoCraftTab(onSaveCraft)
            2 -> PdfDocumentCraftTab(onSaveCraft)
            3 -> UniversalFileConverterTab()
        }
    }
}

@Composable
fun ImageVisionCraftTab(onSaveCraft: (CreativeCraft) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("Cybernetic warrior in neon rain-soaked galaxy metropolis") }
    var selectedStyle by remember { mutableStateOf("Cyberpunk Neon") }
    var resultText by remember { mutableStateOf<String?>(null) }
    var generatedImageUrl by remember { mutableStateOf<String?>("https://image.pollinations.ai/prompt/Cybernetic%20warrior%20neon%20rain%20galaxy?width=1024&height=1024&nologo=true") }
    var isCrafting by remember { mutableStateOf(false) }

    val styles = listOf("Cyberpunk Neon", "Photorealistic 8K", "Anime Masterpiece", "Isometric 3D", "Dark Gothic", "Cosmic Fantasy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Aesthetic Preset:", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        val scroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { st ->
                val isSel = st == selectedStyle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else CyberCard)
                        .border(1.dp, if (isSel) NeonCyan else CyberBorder, RoundedCornerShape(12.dp))
                        .clickable { selectedStyle = st }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(st, style = MaterialTheme.typography.labelSmall, color = if (isSel) NeonCyan else TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Image Subject / Creative Prompt", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("image_craft_prompt_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard
            ),
            shape = RoundedCornerShape(14.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                scope.launch {
                    isCrafting = true
                    val fullPrompt = "$prompt, $selectedStyle style, 8k resolution, award winning masterpiece"
                    val encoded = try {
                        URLEncoder.encode(fullPrompt, "UTF-8")
                    } catch (e: Exception) {
                        "cyberpunk+neon+galaxy"
                    }
                    generatedImageUrl = "https://image.pollinations.ai/prompt/$encoded?width=1024&height=1024&nologo=true&seed=${System.currentTimeMillis() % 10000}"

                    val res = BidiokAiEngine.processQuery(
                        prompt = "Craft image spec for: $fullPrompt",
                        modelType = AiModelType.BIDIOK_QUANTUM,
                        deepReasoningEnabled = true,
                        webSearchEnabled = false
                    )
                    resultText = res.content
                    isCrafting = false
                }
            },
            enabled = prompt.isNotBlank() && !isCrafting,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("craft_image_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberBackground)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isCrafting) "Synthesizing AI Artwork..." else "Generate AI Picture", color = CyberBackground, fontWeight = FontWeight.Bold)
        }

        // Live Generated Artwork Preview
        Spacer(modifier = Modifier.height(16.dp))
        GlowingCyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NeonCyan.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (generatedImageUrl != null) {
                    AsyncImage(
                        model = generatedImageUrl,
                        contentDescription = "AI Generated Artwork",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantum Diffusion Render (1024x1024)", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                    Row {
                        IconButton(onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Generated by Bidiok AI: $prompt\n$generatedImageUrl")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share AI Picture"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = NeonCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        if (resultText != null) {
            Spacer(modifier = Modifier.height(16.dp))
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
                        NeonBadge(text = "CRAFTED SPECIFICATION", color = NeonCyan)
                        Row {
                            IconButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Image Spec", resultText))
                                Toast.makeText(context, "Copied prompt", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {
                                val craft = CreativeCraft(
                                    type = "IMAGE",
                                    title = "Image Craft: $prompt",
                                    prompt = prompt,
                                    resultText = resultText ?: ""
                                )
                                onSaveCraft(craft)
                                Toast.makeText(context, "Saved Craft to Vault", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(resultText ?: "", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, lineHeight = 21.sp)
                }
            }
        }
    }
}

@Composable
fun VideoCraftTab(onSaveCraft: (CreativeCraft) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("Hyper-speed drone race through neon cyber city towers in asteroid belt") }
    var resultText by remember { mutableStateOf<String?>(null) }
    var isCrafting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Video Direction & Pacing:", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Video Concept / Storyboard Idea", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard
            ),
            shape = RoundedCornerShape(14.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                scope.launch {
                    isCrafting = true
                    val res = BidiokAiEngine.processQuery(
                        prompt = "Generate cinematic video storyboard and motion cues for: $prompt",
                        modelType = AiModelType.BIDIOK_QUANTUM,
                        deepReasoningEnabled = true,
                        webSearchEnabled = false
                    )
                    resultText = res.content
                    isCrafting = false
                }
            },
            enabled = prompt.isNotBlank() && !isCrafting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
        ) {
            Icon(Icons.Default.Movie, contentDescription = null, tint = CyberBackground)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isCrafting) "Synthesizing Storyboard..." else "Generate Cinematic Storyboard", color = CyberBackground, fontWeight = FontWeight.Bold)
        }

        if (resultText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            GlowingCyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonPurple
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeonBadge(text = "STORYBOARD TIMELINE", color = NeonPurple)
                        IconButton(onClick = {
                            val craft = CreativeCraft(
                                type = "VIDEO",
                                title = "Video Storyboard: $prompt",
                                prompt = prompt,
                                resultText = resultText ?: ""
                            )
                            onSaveCraft(craft)
                            Toast.makeText(context, "Saved Storyboard to Vault", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(resultText ?: "", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, lineHeight = 21.sp)
                }
            }
        }
    }
}

@Composable
fun PdfDocumentCraftTab(onSaveCraft: (CreativeCraft) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var documentTitle by remember { mutableStateOf("Bidiok AI Quantum Architecture Report") }
    var documentText by remember {
        mutableStateOf(
            "Project Bidiok AI represents a breakthrough in multi-engine mobile computing. By combining on-device local neural execution with high-throughput cloud synchronization and multimodal spatial processing, users achieve sub-100ms reasoning latency across diverse task domains including 3D mesh reconstruction, QR intelligence, and cross-platform synchronization.\n\nSection 1: Multi-Model Consensus Architecture\nIntegrating OpenAI GPT-4o, Google Gemini 2.5 Flash, Anthropic Claude 3.5 Sonnet, DeepSeek R1, and Groq Llama 3.3 creates unprecedented verification accuracy and depth.\n\nSection 2: Mobile Security & Zero-Trust Verification\nAll biometric credentials and private keys remain encrypted inside hardware keystore storage."
        )
    }
    var resultText by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("PDF Creator & Intelligence Engine:", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = documentTitle,
            onValueChange = { documentTitle = it },
            label = { Text("Document Title", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonEmerald,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = documentText,
            onValueChange = { documentText = it },
            label = { Text("Document Body / Report Content", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonEmerald,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard
            ),
            shape = RoundedCornerShape(14.dp),
            maxLines = 8
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    try {
                        val pdfFile = PdfGeneratorEngine.createDocumentPdf(
                            context = context,
                            title = documentTitle,
                            content = documentText
                        )
                        PdfGeneratorEngine.sharePdf(context, pdfFile)
                        Toast.makeText(context, "Generated: ${pdfFile.name}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CyberBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF Document", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        isAnalyzing = true
                        val res = BidiokAiEngine.processQuery(
                            prompt = "Summarize and analyze this document: $documentText",
                            modelType = AiModelType.BIDIOK_QUANTUM,
                            deepReasoningEnabled = true,
                            webSearchEnabled = false
                        )
                        resultText = res.content
                        isAnalyzing = false
                    }
                },
                enabled = documentText.isNotBlank() && !isAnalyzing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isAnalyzing) "Analyzing..." else "AI Enhance", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (resultText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            GlowingCyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonEmerald
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeonBadge(text = "EXECUTIVE SUMMARY", color = NeonEmerald)
                        Row {
                            IconButton(onClick = {
                                val pdfFile = PdfGeneratorEngine.createDocumentPdf(
                                    context = context,
                                    title = "AI Executive Summary: $documentTitle",
                                    content = resultText ?: ""
                                )
                                PdfGeneratorEngine.sharePdf(context, pdfFile)
                            }) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {
                                val craft = CreativeCraft(
                                    type = "PDF",
                                    title = "Document Brief: $documentTitle",
                                    prompt = documentTitle,
                                    resultText = resultText ?: ""
                                )
                                onSaveCraft(craft)
                                Toast.makeText(context, "Saved Brief to Vault", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(resultText ?: "", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, lineHeight = 21.sp)
                }
            }
        }
    }
}

@Composable
fun UniversalFileConverterTab() {
    val context = LocalContext.current
    var conversionMode by remember { mutableIntStateOf(0) } // 0: Text->PDF, 1: Image->PDF, 2: JSON->CSV, 3: CSV->JSON
    var inputText by remember {
        mutableStateOf(
            "[\n  {\"id\": 1, \"model\": \"GPT-4o\", \"speed\": \"High\"},\n  {\"id\": 2, \"model\": \"Claude 3.5\", \"speed\": \"Very High\"},\n  {\"id\": 3, \"model\": \"Gemini 2.5\", \"speed\": \"Ultra\"}\n]"
        )
    }
    var convertedOutput by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(context, "Image loaded for PDF conversion", Toast.LENGTH_SHORT).show()
        }
    }

    val modes = listOf("JSON to CSV", "CSV to JSON", "Text to PDF", "Image to PDF")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Universal File Converter Engine:", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        // Mode Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEachIndexed { idx, name ->
                val isSel = conversionMode == idx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) NeonAmber.copy(alpha = 0.2f) else CyberCard)
                        .border(1.dp, if (isSel) NeonAmber else CyberBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            conversionMode = idx
                            convertedOutput = null
                            if (idx == 0) {
                                inputText = "[\n  {\"model\": \"GPT-4o\", \"company\": \"OpenAI\"},\n  {\"model\": \"Claude 3.5\", \"company\": \"Anthropic\"}\n]"
                            } else if (idx == 1) {
                                inputText = "model,company\nGPT-4o,OpenAI\nClaude 3.5,Anthropic\nGemini 2.5,Google"
                            } else if (idx == 2) {
                                inputText = "# Project Notes\n\nInstant conversion from Markdown/Text into standard printable PDF file with professional styling."
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(name, style = MaterialTheme.typography.labelSmall, color = if (isSel) NeonAmber else TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (conversionMode == 3) {
            // Image to PDF Mode
            GlowingCyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonAmber
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected for PDF",
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NeonAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedImageUri == null) "Select Photo from Device" else "Change Photo", color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            selectedImageUri?.let { uri ->
                                val pdfFile = FileConverterEngine.convertImageToPdf(context, uri, "Bidiok Image Document")
                                if (pdfFile != null) {
                                    PdfGeneratorEngine.sharePdf(context, pdfFile)
                                    Toast.makeText(context, "Converted Photo to PDF!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Conversion failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = selectedImageUri != null,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CyberBackground)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Convert & Share PDF", color = CyberBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Textual conversion (JSON <-> CSV or Text -> PDF)
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Input Content", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonAmber,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CyberCard,
                    unfocusedContainerColor = CyberCard
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 8
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    when (conversionMode) {
                        0 -> convertedOutput = FileConverterEngine.convertJsonToCsv(inputText)
                        1 -> convertedOutput = FileConverterEngine.convertCsvToJson(inputText)
                        2 -> {
                            val pdf = FileConverterEngine.convertTextToPdf(context, "Document Export", inputText)
                            PdfGeneratorEngine.sharePdf(context, pdf)
                            convertedOutput = "PDF Generated & Ready for Sharing:\n${pdf.absolutePath}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber)
            ) {
                Icon(Icons.Default.Transform, contentDescription = null, tint = CyberBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Convert Format Now", color = CyberBackground, fontWeight = FontWeight.Bold)
            }
        }

        if (convertedOutput != null) {
            Spacer(modifier = Modifier.height(16.dp))
            GlowingCyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonAmber
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeonBadge(text = "CONVERTED RESULT", color = NeonAmber)
                        Row {
                            IconButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Converted Result", convertedOutput))
                                Toast.makeText(context, "Copied converted text", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, convertedOutput)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Converted Data"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(convertedOutput ?: "", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, lineHeight = 21.sp)
                }
            }
        }
    }
}
