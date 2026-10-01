package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lightweight Party Popper Confetti explosion beside a completed task
 */
@Composable
fun PartyPopperEffect(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val transitionProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = LinearOutSlowInEasing)
        )
        onFinished()
    }

    val progress = transitionProgress.value
    if (progress >= 1f) return

    val particles = remember {
        List(14) { index ->
            val angle = (index * (360f / 14f) + Random.nextFloat() * 20f) * (Math.PI / 180f)
            val speed = 35f + Random.nextFloat() * 40f
            val color = when (index % 5) {
                0 -> ToDodoYellow
                1 -> ToDodoOrange
                2 -> ToDodoPink
                3 -> ToDodoLavender
                else -> ToDodoGreen
            }
            val size = 4f + Random.nextFloat() * 4f
            Triple(angle, speed, color to size)
        }
    }

    Canvas(modifier = modifier.size(60.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val alpha = (1f - progress).coerceIn(0f, 1f)

        particles.forEach { (angle, speed, props) ->
            val (color, pSize) = props
            val dist = speed * progress
            val x = center.x + cos(angle).toFloat() * dist
            val y = center.y + sin(angle).toFloat() * dist - (progress * 10f) // gentle upward arch

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = pSize * (1f - (progress * 0.4f)),
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Dancing To-Dodo Chicken holding Credit Score Board with full celebration overlay
 */
@Composable
fun FullDayChickenCelebrationOverlay(
    creditScore: Int,
    milestoneMessage: String,
    onDismiss: () -> Unit
) {
    // Auto-dismiss after 4 seconds if untouched
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2D3436).copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
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
                        Text(text = "✨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAY COMPLETE! ♡",
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontWeight = FontWeight.Bold,
                            color = ToDodoOrange,
                            fontSize = 26.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "✨", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dancing Chicken with Credit Score Board
                    DancingChickenWithBoard(creditScore = creditScore)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = milestoneMessage,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ToDodoTextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = ToDodoYellowLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "+1 Credit Score Awarded! ⭐",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ToDodoTextDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
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
        }
    }
}

/**
 * Animated dancing chicken character holding a board
 */
@Composable
fun DancingChickenWithBoard(
    creditScore: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chickenDance")

    // Rhythmic bounce animation
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -16f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Playful side-to-side wiggle / tilt
    val wiggleAngle by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggle"
    )

    // Wing flap scale
    val wingFlap by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wingFlap"
    )

    Column(
        modifier = modifier
            .offset(y = bounceY.dp)
            .rotate(wiggleAngle),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Chicken graphic
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val strokeW = w * 0.035f
                val darkBrown = Color(0xFF3E2723)
                val bodyYellow = Color(0xFFFDCB6E)
                val blushPink = Color(0xFFFF7675)
                val beakOrange = Color(0xFFFF9F43)

                // Chicken main body
                val bodyPath = Path().apply {
                    moveTo(w * 0.25f, h * 0.65f)
                    cubicTo(w * 0.05f, h * 0.50f, w * 0.15f, h * 0.25f, w * 0.40f, h * 0.20f)
                    cubicTo(w * 0.42f, h * 0.08f, w * 0.58f, h * 0.08f, w * 0.60f, h * 0.20f)
                    cubicTo(w * 0.85f, h * 0.25f, w * 0.95f, h * 0.50f, w * 0.75f, h * 0.65f)
                    cubicTo(w * 0.80f, h * 0.85f, w * 0.20f, h * 0.85f, w * 0.25f, h * 0.65f)
                    close()
                }
                drawPath(bodyPath, color = bodyYellow)
                drawPath(bodyPath, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

                // Left Wing (Flapping)
                val leftWing = Path().apply {
                    moveTo(w * 0.18f, h * 0.45f)
                    cubicTo(w * 0.02f, h * 0.35f * wingFlap, w * 0.02f, h * 0.65f * wingFlap, w * 0.22f, h * 0.60f)
                }
                drawPath(leftWing, color = bodyYellow)
                drawPath(leftWing, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round))

                // Right Wing (Flapping)
                val rightWing = Path().apply {
                    moveTo(w * 0.82f, h * 0.45f)
                    cubicTo(w * 0.98f, h * 0.35f * wingFlap, w * 0.98f, h * 0.65f * wingFlap, w * 0.78f, h * 0.60f)
                }
                drawPath(rightWing, color = bodyYellow)
                drawPath(rightWing, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round))

                // Happy Eyes (^ ^)
                val eye1 = Path().apply {
                    moveTo(w * 0.32f, h * 0.40f)
                    lineTo(w * 0.38f, h * 0.33f)
                    lineTo(w * 0.44f, h * 0.40f)
                }
                drawPath(eye1, color = darkBrown, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round))

                val eye2 = Path().apply {
                    moveTo(w * 0.56f, h * 0.40f)
                    lineTo(w * 0.62f, h * 0.33f)
                    lineTo(w * 0.68f, h * 0.40f)
                }
                drawPath(eye2, color = darkBrown, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round))

                // Blushes
                drawCircle(color = blushPink.copy(alpha = 0.6f), radius = w * 0.06f, center = Offset(w * 0.28f, h * 0.48f))
                drawCircle(color = blushPink.copy(alpha = 0.6f), radius = w * 0.06f, center = Offset(w * 0.72f, h * 0.48f))

                // Beak
                drawOval(color = beakOrange, topLeft = Offset(w * 0.44f, h * 0.41f), size = Size(w * 0.12f, h * 0.08f))
                drawOval(color = darkBrown, topLeft = Offset(w * 0.44f, h * 0.41f), size = Size(w * 0.12f, h * 0.08f), style = Stroke(width = strokeW * 0.8f))

                // Little feet
                drawLine(color = darkBrown, start = Offset(w * 0.38f, h * 0.85f), end = Offset(w * 0.38f, h * 0.94f), strokeWidth = strokeW * 0.9f, cap = StrokeCap.Round)
                drawLine(color = darkBrown, start = Offset(w * 0.62f, h * 0.85f), end = Offset(w * 0.62f, h * 0.94f), strokeWidth = strokeW * 0.9f, cap = StrokeCap.Round)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Credit Score Board held by the chicken
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ToDodoYellow),
            border = BorderStroke(2.dp, Color(0xFF3E2723)),
            modifier = Modifier.width(150.dp)
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
                    color = Color(0xFF3E2723),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$creditScore",
                    style = MaterialTheme.typography.displaySmall.copy(fontFamily = PatrickHandFontFamily),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF3E2723)
                )
            }
        }
    }
}
