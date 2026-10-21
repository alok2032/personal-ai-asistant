package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ModelAstra
import com.example.ui.theme.ModelBidiok
import com.example.ui.theme.ModelClaude
import com.example.ui.theme.ModelDeepSeek
import com.example.ui.theme.ModelGemini
import com.example.ui.theme.ModelGroq

enum class AiModelType(
    val displayName: String,
    val subtitle: String,
    val badgeColor: Color,
    val iconName: String,
    val description: String,
    val isSuperFast: Boolean = true
) {
    BIDIOK_QUANTUM(
        displayName = "Bidiok Quantum Ultra",
        subtitle = "v4.5 Hybrid Core",
        badgeColor = ModelBidiok,
        iconName = "bolt",
        description = "Super-speed multi-modal synthesizer with quantum reasoning and live web synthesis."
    ),
    GEMINI_PRO(
        displayName = "Gemini 2.5 Flash / Pro",
        subtitle = "Google DeepMind",
        badgeColor = ModelGemini,
        iconName = "auto_awesome",
        description = "Advanced multimodal understanding, instant web grounding, and code analysis."
    ),
    CHATGPT_ASTRA(
        displayName = "ChatGPT Astra GPT-4o",
        subtitle = "OpenAI Neural",
        badgeColor = ModelAstra,
        iconName = "psychology",
        description = "High-precision conversational logic, mathematics, and structured reasoning."
    ),
    CLAUDE_MYTHOS(
        displayName = "Claude 3.5 Sonnet",
        subtitle = "Anthropic Architecture",
        badgeColor = ModelClaude,
        iconName = "insights",
        description = "Nuanced philosophical thinking, deep document analysis, and creative synthesis."
    ),
    DEEPSEEK_R1(
        displayName = "DeepSeek R1 / V3",
        subtitle = "DeepSeek AI",
        badgeColor = ModelDeepSeek,
        iconName = "memory",
        description = "Revolutionary open-weight mathematical reasoning and step-by-step logic."
    ),
    GROQ_LLAMA(
        displayName = "Groq Llama 3.3 70B",
        subtitle = "Groq LPUs",
        badgeColor = ModelGroq,
        iconName = "speed",
        description = "Lightning-fast inference engine delivering instantaneous answers at 500+ tokens/sec."
    )
}
