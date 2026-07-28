package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkOnBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.OrangeBrand

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R

@Composable
fun ElephantLogoCanvas(
    modifier: Modifier = Modifier,
    primaryColor: Color = OrangeBrand,
    secondaryColor: Color = ForestGreen
) {
    Image(
        painter = painterResource(id = R.drawable.baou_logo),
        contentDescription = "Logo BAOU Finance",
        contentScale = ContentScale.Fit,
        modifier = modifier.clip(RoundedCornerShape(24.dp))
    )
}

@Composable
fun LineTrendChart(
    modifier: Modifier = Modifier,
    color: Color = ForestGreen,
    isGaining: Boolean = true
) {
    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(isGaining) {
        animateProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Define beautiful nodes for the bezier curve
        val points = listOf(
            Offset(0f, height * 0.80f),
            Offset(width * 0.15f, height * 0.75f),
            Offset(width * 0.30f, height * 0.50f),
            Offset(width * 0.45f, height * 0.65f),
            Offset(width * 0.60f, height * 0.30f),
            Offset(width * 0.75f, height * 0.70f),
            Offset(width * 0.88f, height * 0.20f),
            Offset(width, height * 0.15f)
        )

        val strokePath = Path().apply {
            val firstPoint = points.first()
            moveTo(firstPoint.x, firstPoint.y)
            for (i in 1 until points.size) {
                val from = points[i - 1]
                val to = points[i]
                // Draw clean cubic interpolation
                val controlX = (from.x + to.x) / 2f
                cubicTo(controlX, from.y, controlX, to.y, to.x, to.y)
            }
        }

        // Draw horizontal grid references
        val gridOpacity = 0.08f
        drawLine(
            color = DarkOnBackground.copy(alpha = gridOpacity),
            start = Offset(0f, height * 0.25f),
            end = Offset(width, height * 0.25f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = DarkOnBackground.copy(alpha = gridOpacity),
            start = Offset(0f, height * 0.50f),
            end = Offset(width, height * 0.50f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = DarkOnBackground.copy(alpha = gridOpacity),
            start = Offset(0f, height * 0.75f),
            end = Offset(width, height * 0.75f),
            strokeWidth = 1.dp.toPx()
        )

        // Draw bezier path with animatable clip
        drawContext.canvas.save()
        drawContext.canvas.clipRect(
            left = 0f,
            top = 0f,
            right = width * animateProgress.value,
            bottom = height
        )

        // Fill area under the line with beautiful ambient gradient
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        val fillGradient = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.25f),
                color.copy(alpha = 0.00f)
            ),
            startY = height * 0.15f,
            endY = height
        )
        drawPath(path = fillPath, brush = fillGradient)

        // Draw main trend stroke line
        drawPath(
            path = strokePath,
            color = color,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw end pulsing point
        val endPoint = points.last()
        drawCircle(
            color = color,
            radius = 5.dp.toPx(),
            center = endPoint
        )
        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = 9.dp.toPx(),
            center = endPoint,
            style = Stroke(width = 1.5.dp.toPx())
        )

        drawContext.canvas.restore()
    }
}

@Composable
fun BarVisualizer(
    modifier: Modifier = Modifier,
    heights: List<Float> = listOf(0.40f, 0.55f, 0.45f, 0.70f, 0.90f, 0.85f, 1.00f)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val gap = 6.dp.toPx()
        val numBars = heights.size
        val barWidth = (width - (gap * (numBars - 1))) / numBars

        for (i in 0 until numBars) {
            val barHeight = height * heights[i]
            val xOffset = i * (barWidth + gap)
            val yOffset = height - barHeight

            val color = when {
                i == numBars - 1 -> OrangeBrand
                i >= numBars - 3 -> ForestGreen
                else -> DarkOnBackground.copy(alpha = 0.12f)
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(xOffset, yOffset),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureDrawn: (Boolean) -> Unit
) {
    val points = remember { mutableStateListOf<Offset>() }
    val pointsDrawn = remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            points.add(offset)
                            pointsDrawn.value = true
                            onSignatureDrawn(true)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            points.add(change.position)
                        },
                        onDragEnd = {
                            points.add(Offset.Unspecified)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.isNotEmpty()) {
                    val signaturePath = Path()
                    var isFirst = true

                    for (point in points) {
                        if (point == Offset.Unspecified) {
                            isFirst = true
                        } else {
                            if (isFirst) {
                                signaturePath.moveTo(point.x, point.y)
                                isFirst = false
                            } else {
                                signaturePath.lineTo(point.x, point.y)
                            }
                        }
                    }

                    drawPath(
                        path = signaturePath,
                        color = DarkOnBackground,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            // Placeholder Text overlay when empty
            if (!pointsDrawn.value) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Signez ici avec votre doigt",
                        color = DarkOnBackground.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Floating trash delete action
            if (pointsDrawn.value) {
                IconButton(
                    onClick = {
                        points.clear()
                        pointsDrawn.value = false
                        onSignatureDrawn(false)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Effacer la signature",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
