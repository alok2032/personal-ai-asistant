package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.Point3D
import com.example.data.model.RenderableMesh
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import kotlin.math.cos
import kotlin.math.sin

enum class RenderStyle {
    WIREFRAME_GLOW,
    SOLID_HOLO,
    POINTS_ONLY
}

@Composable
fun ThreeDCanvasView(
    mesh: RenderableMesh,
    renderStyle: RenderStyle = RenderStyle.WIREFRAME_GLOW,
    autoRotate: Boolean = true,
    scaleFactor: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    var manualYaw by remember { mutableFloatStateOf(0.4f) }
    var manualPitch by remember { mutableFloatStateOf(0.3f) }

    val infiniteTransition = rememberInfiniteTransition(label = "3DAutoSpin")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing)
        ),
        label = "spinAngle"
    )

    val currentYaw = if (autoRotate) manualYaw + animatedAngle else manualYaw
    val currentPitch = manualPitch

    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(CyberSurface, CyberBackground),
                    radius = 800f
                )
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    manualYaw += dragAmount.x * 0.012f
                    manualPitch = (manualPitch - dragAmount.y * 0.012f).coerceIn(-1.5f, 1.5f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f

            val cameraDistance = 450f
            val fov = 350f * scaleFactor

            // Transform each 3D vertex
            val cosY = cos(currentYaw)
            val sinY = sin(currentYaw)
            val cosP = cos(currentPitch)
            val sinP = sin(currentPitch)

            val projectedPoints = ArrayList<Offset?>(mesh.vertices.size)
            val zDepths = FloatArray(mesh.vertices.size)

            for (i in mesh.vertices.indices) {
                val v = mesh.vertices[i]
                // Rotate around Y-axis (Yaw)
                val x1 = v.x * cosY - v.z * sinY
                val z1 = v.x * sinY + v.z * cosY

                // Rotate around X-axis (Pitch)
                val y2 = v.y * cosP - z1 * sinP
                val z2 = v.y * sinP + z1 * cosP

                zDepths[i] = z2

                val effectiveZ = z2 + cameraDistance
                if (effectiveZ > 20f) {
                    val factor = fov / effectiveZ
                    val screenX = centerX + x1 * factor
                    val screenY = centerY - y2 * factor
                    projectedPoints.add(Offset(screenX, screenY))
                } else {
                    projectedPoints.add(null)
                }
            }

            // Draw holographic faces if SOLID_HOLO
            if (renderStyle == RenderStyle.SOLID_HOLO) {
                mesh.faces.take(60).forEach { face ->
                    val pts = face.indices.mapNotNull { if (it in projectedPoints.indices) projectedPoints[it] else null }
                    if (pts.size >= 3) {
                        val path = Path().apply {
                            moveTo(pts[0].x, pts[0].y)
                            for (p in 1 until pts.size) {
                                lineTo(pts[p].x, pts[p].y)
                            }
                            close()
                        }
                        val baseColor = Color(face.colorHex)
                        drawPath(
                            path = path,
                            color = baseColor.copy(alpha = 0.22f)
                        )
                    }
                }
            }

            // Draw wireframe edges
            if (renderStyle != RenderStyle.POINTS_ONLY) {
                mesh.edges.forEach { edge ->
                    val p1 = if (edge.startIndex in projectedPoints.indices) projectedPoints[edge.startIndex] else null
                    val p2 = if (edge.endIndex in projectedPoints.indices) projectedPoints[edge.endIndex] else null

                    if (p1 != null && p2 != null) {
                        val avgZ = (zDepths[edge.startIndex] + zDepths[edge.endIndex]) / 2f
                        val alpha = ((avgZ + 150f) / 300f).coerceIn(0.25f, 1.0f)
                        val strokeColor = if (edge.startIndex % 2 == 0) NeonCyan else NeonPurple

                        drawLine(
                            color = strokeColor.copy(alpha = alpha),
                            start = p1,
                            end = p2,
                            strokeWidth = 2.2f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Draw vertices / quantum nodes
            projectedPoints.forEachIndexed { idx, pt ->
                if (pt != null) {
                    val z = zDepths[idx]
                    val nodeRadius = if (renderStyle == RenderStyle.POINTS_ONLY) 4.5f else 3f
                    val alpha = ((z + 150f) / 300f).coerceIn(0.4f, 1.0f)
                    drawCircle(
                        color = NeonCyan.copy(alpha = alpha),
                        radius = nodeRadius,
                        center = pt
                    )
                }
            }
        }
    }
}
