package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Exact vector reproduction of the official to-dodo chick mascot holding the checklist.
 * Matches the uploaded reference logo faithfully.
 */
@Composable
fun OfficialToDodoMascot(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showActionLines: Boolean = true,
    scoreText: String? = null
) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            drawOfficialMascot(
                showActionLines = showActionLines,
                scoreText = scoreText
            )
        }
    }
}

fun DrawScope.drawOfficialMascot(
    showActionLines: Boolean = true,
    scoreText: String? = null
) {
    val scale = size.width / 200f

    // Official to-dodo palette
    val darkBrown = Color(0xFF2E1C14)
    val bodyYellow = Color(0xFFFED843)
    val beakOrange = Color(0xFFFF922B)
    val blushPink = Color(0xFFFFA39E).copy(alpha = 0.85f)
    val paperWhite = Color(0xFFFFFFFF)
    val checkOrange = Color(0xFFFED843)
    val strokeWidth = 5.2f * scale
    val mediumStroke = 3.8f * scale
    val thinStroke = 2.4f * scale

    // 1. Action / Excitement lines on top right of head
    if (showActionLines) {
        drawLine(
            color = darkBrown,
            start = Offset(132f * scale, 40f * scale),
            end = Offset(143f * scale, 33f * scale),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = darkBrown,
            start = Offset(138f * scale, 53f * scale),
            end = Offset(150f * scale, 50f * scale),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = darkBrown,
            start = Offset(140f * scale, 65f * scale),
            end = Offset(151f * scale, 66f * scale),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )
    }

    // 2. Main Body & Head Silhouette with 3-lobed crest
    val mascotPath = Path().apply {
        // Start from base left
        moveTo(74f * scale, 126f * scale)
        // Left body bulge up to cheek
        cubicTo(56f * scale, 114f * scale, 52f * scale, 75f * scale, 68f * scale, 58f * scale)
        cubicTo(74f * scale, 52f * scale, 78f * scale, 48f * scale, 81f * scale, 43f * scale)

        // Crest Lobes (3 distinctive tufts on top)
        // Lobe 1 (Left)
        cubicTo(76f * scale, 34f * scale, 83f * scale, 28f * scale, 88f * scale, 34f * scale)
        // Lobe 2 (Center - highest)
        cubicTo(90f * scale, 24f * scale, 102f * scale, 24f * scale, 104f * scale, 35f * scale)
        // Lobe 3 (Right)
        cubicTo(108f * scale, 32f * scale, 116f * scale, 37f * scale, 114f * scale, 46f * scale)

        // Right side of head and body
        cubicTo(138f * scale, 55f * scale, 142f * scale, 88f * scale, 134f * scale, 106f * scale)
        cubicTo(126f * scale, 122f * scale, 115f * scale, 128f * scale, 98f * scale, 128f * scale)
        cubicTo(88f * scale, 128f * scale, 79f * scale, 128f * scale, 74f * scale, 126f * scale)
        close()
    }

    // Fill body
    drawPath(mascotPath, color = bodyYellow, style = Fill)
    // Stroke body
    drawPath(
        mascotPath,
        color = darkBrown,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 3. Left Cheek Blush (Pastel Pink)
    drawCircle(
        color = blushPink,
        radius = 9.5f * scale,
        center = Offset(76f * scale, 86f * scale)
    )

    // 4. Right Cheek Blush (Pastel Pink)
    drawCircle(
        color = blushPink,
        radius = 9.5f * scale,
        center = Offset(124f * scale, 78f * scale)
    )

    // 5. Left Eye (Solid glossy dark bead)
    drawCircle(
        color = darkBrown,
        radius = 4.8f * scale,
        center = Offset(84f * scale, 78f * scale)
    )

    // 6. Right Eye (Cute Winking Arc)
    val winkPath = Path().apply {
        moveTo(112f * scale, 73f * scale)
        quadraticBezierTo(117f * scale, 68f * scale, 122f * scale, 75f * scale)
    }
    drawPath(
        winkPath,
        color = darkBrown,
        style = Stroke(
            width = 4.2f * scale,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 7. Beak (Soft horizontal orange oval)
    val beakLeft = 92f * scale
    val beakTop = 74f * scale
    val beakWidth = 20f * scale
    val beakHeight = 12f * scale
    drawOval(
        color = beakOrange,
        topLeft = Offset(beakLeft, beakTop),
        size = Size(beakWidth, beakHeight)
    )
    drawOval(
        color = darkBrown,
        topLeft = Offset(beakLeft, beakTop),
        size = Size(beakWidth, beakHeight),
        style = Stroke(width = mediumStroke)
    )

    // 8. Checklist Notepad (White card held in hands)
    val padLeft = 90f * scale
    val padTop = 84f * scale
    val padW = 46f * scale
    val padH = 52f * scale
    val padRadius = CornerRadius(8f * scale, 8f * scale)

    // Pad Fill & Stroke
    drawRoundRect(
        color = paperWhite,
        topLeft = Offset(padLeft, padTop),
        size = Size(padW, padH),
        cornerRadius = padRadius
    )
    drawRoundRect(
        color = darkBrown,
        topLeft = Offset(padLeft, padTop),
        size = Size(padW, padH),
        cornerRadius = padRadius,
        style = Stroke(width = mediumStroke)
    )

    // Checklist Items on Pad
    // Row 1 (Checked)
    val box1Top = padTop + 8f * scale
    val boxLeft = padLeft + 7f * scale
    val boxSize = 8.5f * scale
    drawRoundRect(
        color = checkOrange,
        topLeft = Offset(boxLeft, box1Top),
        size = Size(boxSize, boxSize),
        cornerRadius = CornerRadius(2.5f * scale, 2.5f * scale)
    )
    drawRoundRect(
        color = darkBrown,
        topLeft = Offset(boxLeft, box1Top),
        size = Size(boxSize, boxSize),
        cornerRadius = CornerRadius(2.5f * scale, 2.5f * scale),
        style = Stroke(width = 1.6f * scale)
    )
    // Checkmark 1
    val check1 = Path().apply {
        moveTo(boxLeft + 2f * scale, box1Top + 4.5f * scale)
        lineTo(boxLeft + 3.8f * scale, box1Top + 6.8f * scale)
        lineTo(boxLeft + 7f * scale, box1Top + 2.2f * scale)
    }
    drawPath(check1, color = darkBrown, style = Stroke(width = 1.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // Lines 1
    drawLine(color = darkBrown, start = Offset(boxLeft + 12f * scale, box1Top + 4.5f * scale), end = Offset(padLeft + padW - 6f * scale, box1Top + 4.5f * scale), strokeWidth = thinStroke, cap = StrokeCap.Round)

    // Row 2 (Checked)
    val box2Top = padTop + 20f * scale
    drawRoundRect(
        color = checkOrange,
        topLeft = Offset(boxLeft, box2Top),
        size = Size(boxSize, boxSize),
        cornerRadius = CornerRadius(2.5f * scale, 2.5f * scale)
    )
    drawRoundRect(
        color = darkBrown,
        topLeft = Offset(boxLeft, box2Top),
        size = Size(boxSize, boxSize),
        cornerRadius = CornerRadius(2.5f * scale, 2.5f * scale),
        style = Stroke(width = 1.6f * scale)
    )
    // Checkmark 2
    val check2 = Path().apply {
        moveTo(boxLeft + 2f * scale, box2Top + 4.5f * scale)
        lineTo(boxLeft + 3.8f * scale, box2Top + 6.8f * scale)
        lineTo(boxLeft + 7f * scale, box2Top + 2.2f * scale)
    }
    drawPath(check2, color = darkBrown, style = Stroke(width = 1.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // Lines 2
    drawLine(color = darkBrown, start = Offset(boxLeft + 12f * scale, box2Top + 4.5f * scale), end = Offset(padLeft + padW - 6f * scale, box2Top + 4.5f * scale), strokeWidth = thinStroke, cap = StrokeCap.Round)

    // Row 3 (Empty Checkbox or 3rd task)
    val box3Top = padTop + 32f * scale
    drawRoundRect(
        color = Color(0xFFF1F2F6),
        topLeft = Offset(boxLeft, box3Top),
        size = Size(boxSize, boxSize),
        cornerRadius = CornerRadius(2.5f * scale, 2.5f * scale)
    )
    drawRoundRect(
        color = darkBrown.copy(alpha = 0.5f),
        topLeft = Offset(boxLeft, box3Top),
        size = Size(boxSize, boxSize),
        cornerRadius = CornerRadius(2.5f * scale, 2.5f * scale),
        style = Stroke(width = 1.6f * scale)
    )
    // Lines 3
    drawLine(color = darkBrown, start = Offset(boxLeft + 12f * scale, box3Top + 4.5f * scale), end = Offset(padLeft + padW - 8f * scale, box3Top + 4.5f * scale), strokeWidth = thinStroke, cap = StrokeCap.Round)

    // 9. Left Hand (Holding pad)
    val leftHand = Path().apply {
        moveTo(78f * scale, 98f * scale)
        cubicTo(68f * scale, 102f * scale, 76f * scale, 118f * scale, 92f * scale, 108f * scale)
    }
    drawPath(leftHand, color = bodyYellow, style = Fill)
    drawPath(leftHand, color = darkBrown, style = Stroke(width = mediumStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // 10. Right Hand (Holding pad)
    val rightHand = Path().apply {
        moveTo(133f * scale, 92f * scale)
        cubicTo(145f * scale, 96f * scale, 145f * scale, 112f * scale, 133f * scale, 112f * scale)
    }
    drawPath(rightHand, color = bodyYellow, style = Fill)
    drawPath(rightHand, color = darkBrown, style = Stroke(width = mediumStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
