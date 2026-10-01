package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.ui.theme.*
import com.example.util.CelebrationSoundHelper
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Root-level full-screen overlay for individual task completion confetti bursts.
 * Rendered at the root Box / Scaffold level with zIndex(999f) to eliminate parent clipping.
 */
@Composable
fun TaskConfettiOverlay(
    burstOrigin: Offset?,
    onBurstFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (burstOrigin == null) return

    val progress = remember(burstOrigin) { Animatable(0f) }

    LaunchedEffect(burstOrigin) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = LinearOutSlowInEasing)
        )
        onBurstFinished()
    }

    val currentProgress = progress.value
    if (currentProgress >= 1f) return

    val particles = remember(burstOrigin) {
        val colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFF6B81), // Coral Pink
            Color(0xFFFFA502), // Pastel Orange
            Color(0xFF2ED573), // Mint Green
            Color(0xFF70A1FF), // Sky Blue
            Color(0xFFA29BFE), // Lavender
            Color(0xFFFF7675)  // Warm Pink
        )
        List(36) { i ->
            val angle = (i * (360f / 36f) + Random.nextFloat() * 12f) * (Math.PI / 180f)
            val speed = 80f + Random.nextFloat() * 110f
            val color = colors[i % colors.size]
            val type = i % 4 // 0: circle, 1: star, 2: rect ribbon, 3: diamond sparkle
            val size = 6f + Random.nextFloat() * 7f
            val rotationSpeed = (Random.nextFloat() - 0.5f) * 900f
            TaskParticle(angle, speed, color, type, size, rotationSpeed)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .zIndex(999f)
    ) {
        val center = burstOrigin
        val alpha = (1f - currentProgress * 0.95f).coerceIn(0f, 1f)

        particles.forEach { p ->
            val dist = p.speed * currentProgress
            val gravity = currentProgress * currentProgress * 80f // downward gravitational acceleration
            val x = center.x + cos(p.angle).toFloat() * dist
            val y = center.y + sin(p.angle).toFloat() * dist + gravity
            val currentRot = p.rotationSpeed * currentProgress

            when (p.type) {
                0 -> {
                    // Crisp pastel circle
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * (1f - currentProgress * 0.25f),
                        center = Offset(x, y)
                    )
                }
                1 -> {
                    // 5-Pointed Star
                    rotate(degrees = currentRot, pivot = Offset(x, y)) {
                        drawStar(
                            center = Offset(x, y),
                            size = p.size * 2.4f,
                            color = p.color.copy(alpha = alpha)
                        )
                    }
                }
                2 -> {
                    // Fluttering Paper Confetti Ribbon
                    rotate(degrees = currentRot, pivot = Offset(x, y)) {
                        drawRoundRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(x - p.size, y - p.size * 0.5f),
                            size = Size(p.size * 2.4f, p.size * 1.2f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
                3 -> {
                    // Radiating Sparkle Diamond
                    rotate(degrees = currentRot, pivot = Offset(x, y)) {
                        drawSparkleDiamond(
                            center = Offset(x, y),
                            radius = p.size * 2.0f,
                            color = p.color.copy(alpha = alpha)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Local Task Item Confetti Popper Burst (fallback or inline support).
 */
@Composable
fun TaskConfettiPopper(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
        )
        onFinished()
    }

    val currentProgress = progress.value
    if (currentProgress >= 1f) return

    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFF6B81),
            Color(0xFFFFA502),
            Color(0xFF2ED573),
            Color(0xFF70A1FF),
            Color(0xFFA29BFE)
        )
        List(28) { i ->
            val angle = (i * (360f / 28f) + Random.nextFloat() * 15f) * (Math.PI / 180f)
            val speed = 65f + Random.nextFloat() * 75f
            val color = colors[i % colors.size]
            val type = i % 4
            val size = 5.5f + Random.nextFloat() * 5.5f
            val rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            TaskParticle(angle, speed, color, type, size, rotationSpeed)
        }
    }

    Canvas(
        modifier = modifier
            .size(160.dp)
            .zIndex(100f)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val alpha = (1f - currentProgress * 0.95f).coerceIn(0f, 1f)

        particles.forEach { p ->
            val dist = p.speed * currentProgress
            val gravity = currentProgress * currentProgress * 50f
            val x = center.x + cos(p.angle).toFloat() * dist
            val y = center.y + sin(p.angle).toFloat() * dist + gravity
            val currentRot = p.rotationSpeed * currentProgress

            when (p.type) {
                0 -> {
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * (1f - currentProgress * 0.3f),
                        center = Offset(x, y)
                    )
                }
                1 -> {
                    rotate(degrees = currentRot, pivot = Offset(x, y)) {
                        drawStar(
                            center = Offset(x, y),
                            size = p.size * 2.2f,
                            color = p.color.copy(alpha = alpha)
                        )
                    }
                }
                2 -> {
                    rotate(degrees = currentRot, pivot = Offset(x, y)) {
                        drawRoundRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(x - p.size, y - p.size * 0.5f),
                            size = Size(p.size * 2.2f, p.size * 1.1f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
                3 -> {
                    rotate(degrees = currentRot, pivot = Offset(x, y)) {
                        drawSparkleDiamond(
                            center = Offset(x, y),
                            radius = p.size * 1.8f,
                            color = p.color.copy(alpha = alpha)
                        )
                    }
                }
            }
        }
    }
}

private data class TaskParticle(
    val angle: Double,
    val speed: Float,
    val color: Color,
    val type: Int,
    val size: Float,
    val rotationSpeed: Float
)

/**
 * Full Screen Confetti Rain for 100% Day Completion
 */
@Composable
fun FullDayConfettiRain(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "confettiRain")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainTime"
    )

    val rainParticles = remember {
        val colors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFF6B81),
            Color(0xFFFFA502),
            Color(0xFF2ED573),
            Color(0xFF70A1FF),
            Color(0xFFA29BFE),
            Color(0xFFFF7675)
        )
        List(50) { i ->
            val startX = Random.nextFloat()
            val speedY = 0.5f + Random.nextFloat() * 0.8f
            val swayAmp = 20f + Random.nextFloat() * 30f
            val swayFreq = 2f + Random.nextFloat() * 4f
            val color = colors[i % colors.size]
            val size = 6f + Random.nextFloat() * 6f
            val type = i % 3
            RainParticle(startX, speedY, swayAmp, swayFreq, color, size, type, Random.nextFloat() * 360f)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .zIndex(100f)
    ) {
        val w = size.width
        val h = size.height

        rainParticles.forEach { p ->
            val progress = (time * p.speedY + p.startX) % 1f
            val y = progress * h
            val x = (p.startX * w) + sin(progress * p.swayFreq * Math.PI.toFloat()) * p.swayAmp
            val rot = p.initialRot + progress * 720f

            rotate(degrees = rot, pivot = Offset(x, y)) {
                when (p.type) {
                    0 -> {
                        drawRoundRect(
                            color = p.color.copy(alpha = 0.90f),
                            topLeft = Offset(x - p.size, y - p.size * 0.5f),
                            size = Size(p.size * 2.2f, p.size * 1.1f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                    1 -> {
                        drawStar(
                            center = Offset(x, y),
                            size = p.size * 2.0f,
                            color = p.color.copy(alpha = 0.95f)
                        )
                    }
                    else -> {
                        drawCircle(
                            color = p.color.copy(alpha = 0.90f),
                            radius = p.size * 0.9f,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }
}

private data class RainParticle(
    val startX: Float,
    val speedY: Float,
    val swayAmp: Float,
    val swayFreq: Float,
    val color: Color,
    val size: Float,
    val type: Int,
    val initialRot: Float
)

fun DrawScope.drawStar(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        val outerRadius = size
        val innerRadius = size * 0.45f
        for (i in 0 until 10) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = (i * 36 - 90) * (Math.PI / 180.0)
            val x = center.x + cos(angle).toFloat() * radius
            val y = center.y + sin(angle).toFloat() * radius
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(path, color = color, style = Fill)
}

fun DrawScope.drawSparkleDiamond(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticBezierTo(center.x, center.y, center.x + radius, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y + radius)
        quadraticBezierTo(center.x, center.y, center.x - radius, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color = color, style = Fill)
}

/**
 * Dancing To-Dodo Chicken holding Credit Score Board with full celebration overlay and synchronized 5-second audio
 */
@Composable
fun FullDayChickenCelebrationOverlay(
    creditScore: Int,
    milestoneMessage: String,
    onDismiss: () -> Unit
) {
    // Start 5-second celebration audio immediately upon entrance
    LaunchedEffect(Unit) {
        CelebrationSoundHelper.playDailyCelebration()
        // Auto-dismiss after 5 seconds
        delay(5000)
        CelebrationSoundHelper.stopDailyCelebration()
        onDismiss()
    }

    DisposableEffect(Unit) {
        onDispose {
            CelebrationSoundHelper.stopDailyCelebration()
        }
    }

    Dialog(
        onDismissRequest = {
            CelebrationSoundHelper.stopDailyCelebration()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2D3436).copy(alpha = 0.70f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    CelebrationSoundHelper.stopDailyCelebration()
                    onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .zIndex(10f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent dismiss when clicking dialog card */ },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = ToDodoCream),
                border = BorderStroke(2.dp, ToDodoYellow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating stars header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAY COMPLETE! ♡",
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontWeight = FontWeight.Bold,
                            color = ToDodoOrange,
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "✨", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dancing Official To-Dodo Mascot
                    DancingOfficialMascot(creditScore = creditScore)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = milestoneMessage,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ToDodoTextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = ToDodoYellowLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "+1 Credit Score Awarded! ⭐",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ToDodoTextDark,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            CelebrationSoundHelper.stopDailyCelebration()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ToDodoYellow)
                    ) {
                        Text(
                            text = "Continue ♡",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = ToDodoTextDark
                        )
                    }
                }
            }

            // Full Screen Falling Confetti Rain rendered OVER the background and card
            FullDayConfettiRain(modifier = Modifier.fillMaxSize().zIndex(100f))
        }
    }
}

/**
 * Dancing animation for the official to-dodo mascot without distorting its appearance.
 */
@Composable
fun DancingOfficialMascot(
    creditScore: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascotDance")

    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val wiggleAngle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggle"
    )

    Column(
        modifier = modifier
            .offset(y = bounceY.dp)
            .rotate(wiggleAngle),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OfficialToDodoMascot(
            size = 140.dp,
            showActionLines = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ToDodoYellow),
            border = BorderStroke(2.dp, Color(0xFF2E1C14)),
            modifier = Modifier.width(160.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CREDIT SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E1C14),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$creditScore",
                    style = MaterialTheme.typography.displaySmall.copy(fontFamily = PatrickHandFontFamily),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF2E1C14)
                )
            }
        }
    }
}
