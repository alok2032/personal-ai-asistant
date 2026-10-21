package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModelType
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

@Composable
fun GlowingCyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan.copy(alpha = 0.35f),
    contentColor: Color = CyberCard,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, CyberBorder, borderColor.copy(alpha = 0.1f))
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        color = contentColor,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        content()
    }
}

@Composable
fun NeonBadge(
    text: String,
    color: Color = NeonCyan,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AudioWaveformVisualizer(
    isActive: Boolean,
    amplitude: Float = 0.5f,
    modifier: Modifier = Modifier,
    barCount: Int = 14
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformPulse")

    val rand: List<Float> = remember(barCount) { List(barCount) { Random.nextFloat() * 0.4f + 0.3f } }

    Row(
        modifier = modifier
            .height(36.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animDuration = 400 + (i * 70) % 500
            val targetVal: Float = if (isActive) {
                (rand.getOrElse(i) { 0.5f }).coerceIn(0.2f, 1.0f) * (0.6f + amplitude * 0.4f)
            } else 0.15f

            val animatedHeightFraction by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = targetVal,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            val currentFraction = if (isActive) animatedHeightFraction else 0.15f
            val barColor = if (i % 2 == 0) NeonCyan else NeonPurple

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((36.dp * currentFraction).coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) barColor else TextMuted.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun ModelSelectorBar(
    selectedModel: AiModelType,
    onModelSelected: (AiModelType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AiModelType.entries.forEach { model ->
            val isSelected = model == selectedModel
            val icon: ImageVector = when (model) {
                AiModelType.BIDIOK_QUANTUM -> Icons.Default.Bolt
                AiModelType.GEMINI_PRO -> Icons.Default.AutoAwesome
                AiModelType.CHATGPT_ASTRA -> Icons.Default.Psychology
                AiModelType.CLAUDE_MYTHOS -> Icons.Default.Insights
                else -> Icons.Default.Bolt
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) model.badgeColor.copy(alpha = 0.2f)
                        else CyberCard
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) model.badgeColor else CyberBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onModelSelected(model) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("model_chip_${model.name.lowercase()}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = model.displayName,
                        tint = if (isSelected) model.badgeColor else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = model.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
