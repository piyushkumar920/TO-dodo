package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Official to-dodo Mascot: Cute yellow chick holding a checklist with winking eye and pencil
 */
@Composable
fun ToDodoMascot(modifier: Modifier = Modifier, size: Dp = 120.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.035f
        val darkBrown = Color(0xFF3E2723)
        val bodyYellow = Color(0xFFFDCB6E)
        val blushPink = Color(0xFFFF7675)
        val beakOrange = Color(0xFFFF9F43)
        val paperWhite = Color(0xFFFFFFFF)

        // Main body (rounded oval shape)
        val bodyPath = Path().apply {
            moveTo(w * 0.25f, h * 0.65f)
            // Left cheek curve
            cubicTo(w * 0.05f, h * 0.50f, w * 0.15f, h * 0.25f, w * 0.40f, h * 0.20f)
            // Head tuft
            cubicTo(w * 0.42f, h * 0.10f, w * 0.58f, h * 0.10f, w * 0.60f, h * 0.20f)
            // Right cheek curve
            cubicTo(w * 0.85f, h * 0.25f, w * 0.95f, h * 0.50f, w * 0.75f, h * 0.65f)
            // Bottom body curve
            cubicTo(w * 0.80f, h * 0.85f, w * 0.20f, h * 0.85f, w * 0.25f, h * 0.65f)
            close()
        }

        // Fill body
        drawPath(bodyPath, color = bodyYellow)
        // Outline body
        drawPath(bodyPath, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Left eye (open circle)
        drawCircle(
            color = darkBrown,
            radius = w * 0.045f,
            center = Offset(w * 0.40f, h * 0.40f)
        )
        // Eye twinkle
        drawCircle(
            color = Color.White,
            radius = w * 0.015f,
            center = Offset(w * 0.385f, h * 0.385f)
        )

        // Right eye (winking curve: ^ or >)
        val winkPath = Path().apply {
            moveTo(w * 0.62f, h * 0.43f)
            lineTo(w * 0.67f, h * 0.38f)
            lineTo(w * 0.72f, h * 0.43f)
        }
        drawPath(winkPath, color = darkBrown, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Left pink blush
        drawCircle(
            color = blushPink.copy(alpha = 0.6f),
            radius = w * 0.06f,
            center = Offset(w * 0.32f, h * 0.48f)
        )

        // Right pink blush
        drawCircle(
            color = blushPink.copy(alpha = 0.6f),
            radius = w * 0.06f,
            center = Offset(w * 0.70f, h * 0.48f)
        )

        // Cute oval Beak
        drawOval(
            color = beakOrange,
            topLeft = Offset(w * 0.48f, h * 0.42f),
            size = Size(w * 0.12f, h * 0.08f)
        )
        drawOval(
            color = darkBrown,
            topLeft = Offset(w * 0.48f, h * 0.42f),
            size = Size(w * 0.12f, h * 0.08f),
            style = Stroke(width = strokeW * 0.8f)
        )

        // Cute Checklist Notepad held in hands
        val padTopLeft = Offset(w * 0.45f, h * 0.48f)
        val padSize = Size(w * 0.32f, h * 0.36f)
        drawRoundRect(
            color = paperWhite,
            topLeft = padTopLeft,
            size = padSize,
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )
        drawRoundRect(
            color = darkBrown,
            topLeft = padTopLeft,
            size = padSize,
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
            style = Stroke(width = strokeW * 0.85f)
        )

        // Checklist checkboxes and lines
        // Check 1
        drawRoundRect(
            color = ToDodoYellow,
            topLeft = Offset(w * 0.49f, h * 0.54f),
            size = Size(w * 0.06f, h * 0.06f),
            cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
        )
        val check1 = Path().apply {
            moveTo(w * 0.505f, h * 0.57f)
            lineTo(w * 0.525f, h * 0.585f)
            lineTo(w * 0.55f, h * 0.55f)
        }
        drawPath(check1, color = darkBrown, style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round))
        // Line 1
        drawLine(color = darkBrown.copy(alpha = 0.7f), start = Offset(w * 0.58f, h * 0.57f), end = Offset(w * 0.72f, h * 0.57f), strokeWidth = strokeW * 0.7f, cap = StrokeCap.Round)

        // Check 2
        drawRoundRect(
            color = ToDodoYellow,
            topLeft = Offset(w * 0.49f, h * 0.64f),
            size = Size(w * 0.06f, h * 0.06f),
            cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
        )
        val check2 = Path().apply {
            moveTo(w * 0.505f, h * 0.67f)
            lineTo(w * 0.525f, h * 0.685f)
            lineTo(w * 0.55f, h * 0.65f)
        }
        drawPath(check2, color = darkBrown, style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round))
        // Line 2
        drawLine(color = darkBrown.copy(alpha = 0.7f), start = Offset(w * 0.58f, h * 0.67f), end = Offset(w * 0.72f, h * 0.67f), strokeWidth = strokeW * 0.7f, cap = StrokeCap.Round)

        // Empty box 3
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(w * 0.49f, h * 0.74f),
            size = Size(w * 0.06f, h * 0.06f),
            cornerRadius = CornerRadius(w * 0.01f, w * 0.01f),
            style = Stroke(width = strokeW * 0.6f)
        )
        // Line 3
        drawLine(color = darkBrown.copy(alpha = 0.4f), start = Offset(w * 0.58f, h * 0.77f), end = Offset(w * 0.72f, h * 0.77f), strokeWidth = strokeW * 0.7f, cap = StrokeCap.Round)

        // Little hands holding the pad
        drawCircle(color = bodyYellow, radius = w * 0.05f, center = Offset(w * 0.44f, h * 0.60f))
        drawCircle(color = darkBrown, radius = w * 0.05f, center = Offset(w * 0.44f, h * 0.60f), style = Stroke(width = strokeW * 0.8f))

        drawCircle(color = bodyYellow, radius = w * 0.05f, center = Offset(w * 0.78f, h * 0.60f))
        drawCircle(color = darkBrown, radius = w * 0.05f, center = Offset(w * 0.78f, h * 0.60f), style = Stroke(width = strokeW * 0.8f))

        // Cute excitement marks top right
        val mark1 = Path().apply { moveTo(w * 0.78f, h * 0.22f); lineTo(w * 0.86f, h * 0.16f) }
        val mark2 = Path().apply { moveTo(w * 0.83f, h * 0.32f); lineTo(w * 0.92f, h * 0.31f) }
        drawPath(mark1, color = darkBrown, style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round))
        drawPath(mark2, color = darkBrown, style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round))
    }
}

/**
 * Cute sleeping cat on a cushion (matching reference image "Focus Today")
 */
@Composable
fun SleepingCat(modifier: Modifier = Modifier, size: Dp = 100.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.035f
        val darkBrown = Color(0xFF3E2723)
        val catWhite = Color(0xFFFFFFFF)
        val cushionYellow = Color(0xFFFFF3B0)
        val blushPink = Color(0xFFFF7675)

        // Cushion (yellow soft oval base)
        drawOval(
            color = cushionYellow,
            topLeft = Offset(w * 0.10f, h * 0.55f),
            size = Size(w * 0.80f, h * 0.35f)
        )
        drawOval(
            color = darkBrown,
            topLeft = Offset(w * 0.10f, h * 0.55f),
            size = Size(w * 0.80f, h * 0.35f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // Cat body (white sleeping curled shape)
        val catBody = Path().apply {
            moveTo(w * 0.25f, h * 0.65f)
            cubicTo(w * 0.20f, h * 0.40f, w * 0.45f, h * 0.35f, w * 0.65f, h * 0.42f)
            cubicTo(w * 0.82f, h * 0.48f, w * 0.85f, h * 0.70f, w * 0.68f, h * 0.72f)
            cubicTo(w * 0.45f, h * 0.75f, w * 0.30f, h * 0.75f, w * 0.25f, h * 0.65f)
            close()
        }
        drawPath(catBody, color = catWhite)
        drawPath(catBody, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Cat head (left side)
        val catHead = Path().apply {
            moveTo(w * 0.28f, h * 0.58f)
            cubicTo(w * 0.18f, h * 0.48f, w * 0.22f, h * 0.32f, w * 0.38f, h * 0.35f)
            cubicTo(w * 0.48f, h * 0.36f, w * 0.52f, h * 0.52f, w * 0.45f, h * 0.60f)
            close()
        }
        drawPath(catHead, color = catWhite)
        drawPath(catHead, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Left ear
        val leftEar = Path().apply {
            moveTo(w * 0.25f, h * 0.38f)
            lineTo(w * 0.22f, h * 0.25f)
            lineTo(w * 0.32f, h * 0.32f)
        }
        drawPath(leftEar, color = catWhite)
        drawPath(leftEar, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Right ear
        val rightEar = Path().apply {
            moveTo(w * 0.35f, h * 0.33f)
            lineTo(w * 0.42f, h * 0.24f)
            lineTo(w * 0.46f, h * 0.36f)
        }
        drawPath(rightEar, color = catWhite)
        drawPath(rightEar, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Sleeping closed eyes (two cute smiling curves: u u)
        val eye1 = Path().apply {
            moveTo(w * 0.28f, h * 0.48f)
            cubicTo(w * 0.30f, h * 0.52f, w * 0.34f, h * 0.52f, w * 0.36f, h * 0.48f)
        }
        drawPath(eye1, color = darkBrown, style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round))

        val eye2 = Path().apply {
            moveTo(w * 0.38f, h * 0.48f)
            cubicTo(w * 0.40f, h * 0.52f, w * 0.44f, h * 0.52f, w * 0.46f, h * 0.48f)
        }
        drawPath(eye2, color = darkBrown, style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round))

        // Blush
        drawCircle(color = blushPink.copy(alpha = 0.5f), radius = w * 0.035f, center = Offset(w * 0.26f, h * 0.53f))
        drawCircle(color = blushPink.copy(alpha = 0.5f), radius = w * 0.035f, center = Offset(w * 0.47f, h * 0.53f))

        // Nose
        drawCircle(color = darkBrown, radius = w * 0.015f, center = Offset(w * 0.37f, h * 0.52f))

        // Curled tail
        val tail = Path().apply {
            moveTo(w * 0.68f, h * 0.68f)
            cubicTo(w * 0.80f, h * 0.70f, w * 0.84f, h * 0.55f, w * 0.76f, h * 0.50f)
        }
        drawPath(tail, color = darkBrown, style = Stroke(width = strokeW * 1.1f, cap = StrokeCap.Round))

        // Cute floating heart above
        val heart = Path().apply {
            moveTo(w * 0.55f, h * 0.22f)
            cubicTo(w * 0.50f, h * 0.15f, w * 0.45f, h * 0.20f, w * 0.55f, h * 0.30f)
            cubicTo(w * 0.65f, h * 0.20f, w * 0.60f, h * 0.15f, w * 0.55f, h * 0.22f)
        }
        drawPath(heart, color = blushPink)
    }
}

/**
 * Cute Hand-Drawn Smiling Sun with Rays (matching reference header card)
 */
@Composable
fun SunDoodle(modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.06f
        val darkBrown = Color(0xFF3E2723)
        val sunYellow = Color(0xFFFDCB6E)
        val center = Offset(w / 2f, h / 2f)
        val radius = w * 0.25f

        // Sun face
        drawCircle(color = sunYellow, radius = radius, center = center)
        drawCircle(color = darkBrown, radius = radius, center = center, style = Stroke(width = strokeW))

        // Smiling eyes
        drawCircle(color = darkBrown, radius = w * 0.035f, center = Offset(w * 0.42f, h * 0.46f))
        drawCircle(color = darkBrown, radius = w * 0.035f, center = Offset(w * 0.58f, h * 0.46f))

        // Smile
        val smile = Path().apply {
            moveTo(w * 0.44f, h * 0.55f)
            cubicTo(w * 0.48f, h * 0.62f, w * 0.52f, h * 0.62f, w * 0.56f, h * 0.55f)
        }
        drawPath(smile, color = darkBrown, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round))

        // Rays (8 rays)
        val rayLength = w * 0.14f
        val rayStart = radius + (w * 0.05f)
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 45.0)).toFloat()
            val startX = center.x + Math.cos(angle.toDouble()).toFloat() * rayStart
            val startY = center.y + Math.sin(angle.toDouble()).toFloat() * rayStart
            val endX = center.x + Math.cos(angle.toDouble()).toFloat() * (rayStart + rayLength)
            val endY = center.y + Math.sin(angle.toDouble()).toFloat() * (rayStart + rayLength)
            drawLine(color = darkBrown, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = strokeW * 0.85f, cap = StrokeCap.Round)
        }
    }
}

/**
 * Celebrating cat with tiny star sparkles (matching streak and milestone cards)
 */
@Composable
fun CelebratingCat(modifier: Modifier = Modifier, size: Dp = 80.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.04f
        val darkBrown = Color(0xFF3E2723)
        val catWhite = Color(0xFFFFFFFF)
        val blushPink = Color(0xFFFF7675)

        // Cat face
        drawCircle(color = catWhite, radius = w * 0.32f, center = Offset(w * 0.50f, h * 0.55f))
        drawCircle(color = darkBrown, radius = w * 0.32f, center = Offset(w * 0.50f, h * 0.55f), style = Stroke(width = strokeW))

        // Ears
        val leftEar = Path().apply {
            moveTo(w * 0.28f, h * 0.35f)
            lineTo(w * 0.20f, h * 0.15f)
            lineTo(w * 0.40f, h * 0.25f)
        }
        drawPath(leftEar, color = catWhite)
        drawPath(leftEar, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round))

        val rightEar = Path().apply {
            moveTo(w * 0.60f, h * 0.25f)
            lineTo(w * 0.80f, h * 0.15f)
            lineTo(w * 0.72f, h * 0.35f)
        }
        drawPath(rightEar, color = catWhite)
        drawPath(rightEar, color = darkBrown, style = Stroke(width = strokeW, cap = StrokeCap.Round))

        // Happy closed curve eyes (^ ^)
        val eye1 = Path().apply {
            moveTo(w * 0.34f, h * 0.54f)
            lineTo(w * 0.40f, h * 0.46f)
            lineTo(w * 0.46f, h * 0.54f)
        }
        drawPath(eye1, color = darkBrown, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round))

        val eye2 = Path().apply {
            moveTo(w * 0.54f, h * 0.54f)
            lineTo(w * 0.60f, h * 0.46f)
            lineTo(w * 0.66f, h * 0.54f)
        }
        drawPath(eye2, color = darkBrown, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round))

        // Pink cheeks
        drawCircle(color = blushPink.copy(alpha = 0.6f), radius = w * 0.05f, center = Offset(w * 0.30f, h * 0.60f))
        drawCircle(color = blushPink.copy(alpha = 0.6f), radius = w * 0.05f, center = Offset(w * 0.70f, h * 0.60f))

        // Mouth (cute cat smile :3)
        val mouth = Path().apply {
            moveTo(w * 0.44f, h * 0.62f)
            cubicTo(w * 0.47f, h * 0.66f, w * 0.50f, h * 0.66f, w * 0.50f, h * 0.62f)
            cubicTo(w * 0.50f, h * 0.66f, w * 0.53f, h * 0.66f, w * 0.56f, h * 0.62f)
        }
        drawPath(mouth, color = darkBrown, style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round))

        // Whiskers
        drawLine(color = darkBrown, start = Offset(w * 0.18f, h * 0.56f), end = Offset(w * 0.28f, h * 0.56f), strokeWidth = strokeW * 0.7f, cap = StrokeCap.Round)
        drawLine(color = darkBrown, start = Offset(w * 0.72f, h * 0.56f), end = Offset(w * 0.82f, h * 0.56f), strokeWidth = strokeW * 0.7f, cap = StrokeCap.Round)
    }
}

/**
 * Circular progress ring with cozy yellow/cream fill and Patrick Hand percentage
 */
@Composable
fun CircularProgressRing(
    percentage: Int,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progressRing"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background track
            drawArc(
                color = Color(0xFFFFF4CC),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active progress arc
            drawArc(
                color = ToDodoYellow,
                startAngle = -90f,
                sweepAngle = (animatedPercentage / 100f) * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
