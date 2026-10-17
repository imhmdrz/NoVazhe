package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.delay
import novazheh.composeapp.generated.resources.Res
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-screen celebration shown on winning screens: the [main_winning] Lottie animation
 * fills the whole screen while the [corner_winning] horn animation is placed in all four
 * corners. The source horn points to the top-right, so the bottom-left corner uses it as-is
 * and the remaining corners are rotated so every horn points inward toward the center.
 *
 * Drop it into a [androidx.compose.foundation.layout.BoxScope] (e.g. behind a win dialog);
 * it is purely decorative and does not intercept touches.
 */
@Composable
fun WinCelebrationOverlay(
    modifier: Modifier = Modifier,
    cornerSize: Dp = 130.dp,
) {
    val mainComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/main_winning.json").decodeToString()
        )
    }
    val main2Composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/main_winning2.json").decodeToString()
        )
    }
    val main3Composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/main_winning3.json").decodeToString()
        )
    }
    val main4Composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/main_winning4.json").decodeToString()
        )
    }
    val cornerComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/corner_winning.json").decodeToString()
        )
    }

    val mainProgress by animateLottieCompositionAsState(
        composition = mainComposition,
        iterations = Compottie.IterateForever,
    )
    val main2Progress by animateLottieCompositionAsState(
        composition = main2Composition,
        iterations = Compottie.IterateForever,
    )
    val main3Progress by animateLottieCompositionAsState(
        composition = main3Composition,
        iterations = Compottie.IterateForever,
    )
    val main4Progress by animateLottieCompositionAsState(
        composition = main4Composition,
        iterations = Compottie.IterateForever,
    )
    val cornerProgress by animateLottieCompositionAsState(
        composition = cornerComposition,
        iterations = Compottie.IterateForever,
    )

    val cornerPainter = rememberLottiePainter(
        composition = cornerComposition,
        progress = { cornerProgress },
    )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = rememberLottiePainter(
                composition = mainComposition,
                progress = { mainProgress },
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Image(
            painter = rememberLottiePainter(
                composition = main2Composition,
                progress = { main2Progress },
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Image(
            painter = rememberLottiePainter(
                composition = main3Composition,
                progress = { main3Progress },
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Image(
            painter = rememberLottiePainter(
                composition = main4Composition,
                progress = { main4Progress },
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.align(Alignment.Center),
        )

        CornerHorn(cornerPainter, Alignment.BottomStart, 0f, cornerSize)
        CornerHorn(cornerPainter, Alignment.TopStart, 90f, cornerSize)
        CornerHorn(cornerPainter, Alignment.TopEnd, 180f, cornerSize)
        CornerHorn(cornerPainter, Alignment.BottomEnd, 270f, cornerSize)
    }
}

/**
 * Full-screen win celebration with no dialog card: only the animations are shown, and a tap
 * anywhere on the screen restarts the game via [onTap].
 */
@Composable
fun WinCelebration(
    onTap: () -> Unit,
    isLevelUp: Boolean = false,
    timeoutMillis: Long = CELEBRATION_TIMEOUT_MS,
) {
    if (isLevelUp) {
        LevelUpCelebration(onTap = onTap, timeoutMillis = timeoutMillis)
    } else {
        CelebrationDialog(onTap = onTap, timeoutMillis = timeoutMillis) {
            WinCelebrationOverlay()
        }
    }
}

@Composable
fun LevelUpCelebration(
    onTap: () -> Unit,
    timeoutMillis: Long = CELEBRATION_TIMEOUT_MS,
    title: String = "مرحله بعد!",
    subtitle: String = "آفرین! رفتی مرحله بعد",
) {
    CelebrationDialog(onTap = onTap, timeoutMillis = timeoutMillis) {
        LevelUpCelebrationScene(title = title, subtitle = subtitle)
    }
}

@Composable
private fun CelebrationDialog(
    onTap: () -> Unit,
    timeoutMillis: Long,
    content: @Composable BoxScope.() -> Unit
) {
    var completed by remember { mutableStateOf(false) }
    fun finish() {
        if (!completed) {
            completed = true
            onTap()
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(timeoutMillis)
        finish()
    }

    Dialog(
        onDismissRequest = ::finish,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = ::finish
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}

@Composable
private fun BoxScope.LevelUpCelebrationScene(
    title: String,
    subtitle: String,
) {
    val backgroundMotion = rememberInfiniteTransition(label = "levelUpBackground")
    val confettiProgress by backgroundMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )
    val trophyPulse by backgroundMotion.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyPulse"
    )
    val orbitProgress by backgroundMotion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "trophyOrbit"
    )
    val ambientProgress = remember { Animatable(0f) }
    val beamProgress = remember { Animatable(0f) }
    val trophyProgress = remember { Animatable(0f) }
    val impactProgress = remember { Animatable(0f) }
    val landingGlow = remember { Animatable(0f) }
    val titleProgress = remember { Animatable(0f) }
    val subtitleProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // A single sequence keeps the reveal cinematic rather than allowing unrelated
        // animations to race each other during composition.
        ambientProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        beamProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        // The first descent is controlled; the final approach uses spring physics so the
        // trophy feels heavy and lands rather than simply sliding into place.
        trophyProgress.animateTo(
            targetValue = 0.76f,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
        )
        trophyProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.42f, stiffness = 260f)
        )
        landingGlow.animateTo(1f, tween(durationMillis = 160, easing = FastOutSlowInEasing))
        impactProgress.animateTo(1f, tween(durationMillis = 620, easing = FastOutSlowInEasing))
        landingGlow.animateTo(0.3f, tween(durationMillis = 560, easing = LinearEasing))
        titleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
        subtitleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF102E54),
                            Color(0xFF245D88),
                            Color(0xFF397D9B),
                            Color(0xFF6FCFB8)
                        )
                    )
                )
    ) {
        MagicLightBeam(
            ambientProgress = ambientProgress.value,
            beamProgress = beamProgress.value
        )
        LevelUpConfetti(confettiProgress = confettiProgress)
        TrophyImpact(
            progress = impactProgress.value,
            orbitProgress = orbitProgress,
            modifier = Modifier.align(Alignment.Center)
        )
        TrophyGlow(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-28).dp),
            pulse = trophyPulse,
            landingGlow = landingGlow.value
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrophyArtwork(
                modifier = Modifier
                    .offset(y = ((1f - trophyProgress.value) * -350f - 14f).dp)
                    .rotate((1f - trophyProgress.value) * -18f)
                    .scale((0.4f + 0.6f * trophyProgress.value) * trophyPulse)
                    .graphicsLayer {
                        alpha = trophyProgress.value.coerceIn(0f, 1f)
                    }
            )
            Text(
                text = title,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .scale(0.82f + 0.18f * titleProgress.value),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF17406D).copy(alpha = titleProgress.value),
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .scale(0.9f + 0.1f * subtitleProgress.value),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF126B64).copy(alpha = subtitleProgress.value),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Kept separate from the reveal choreography so a transparent trophy PNG/vector can replace
 * the emoji later without changing the level-up timeline or state flow.
 */
@Composable
private fun TrophyArtwork(modifier: Modifier = Modifier) {
    Text(
        text = "🏆",
        fontSize = 132.sp,
        modifier = modifier
    )
}

private const val CELEBRATION_TIMEOUT_MS = 8_000L

@Composable
private fun BoxScope.LevelUpConfetti(confettiProgress: Float) {
    val palette = remember {
        listOf(
            Color(0xFFFFC928),
            Color(0xFFFF5FA2),
            Color(0xFF00B8F5),
            Color(0xFF44D16D),
            Color(0xFFFF7A35),
            Color(0xFF8D6BFF)
        )
    }
    val confetti = remember {
        List(48) { index ->
            ConfettiPiece(
                xFraction = ((index * 37) % 100) / 100f,
                startOffset = ((index * 23) % 100) / 100f,
                fallSpeed = 0.72f + ((index * 11) % 9) * 0.055f,
                drift = -34f + ((index * 17) % 69),
                wave = 12f + ((index * 13) % 30),
                size = (5 + (index * 7) % 7).dp,
                color = palette[index % palette.size],
                rotationDegrees = ((index * 29) % 120) - 60f,
                rotationSpeed = if (index % 2 == 0) 180f else -220f,
                isCircle = index % 5 == 0
            )
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        confetti.forEachIndexed { index, piece ->
            val loopProgress = (confettiProgress * piece.fallSpeed + piece.startOffset) % 1f
            val wavePhase = (loopProgress * 2f * PI).toFloat()
            val particleSize = piece.size.toPx()
            val center = Offset(
                x = size.width * piece.xFraction +
                    piece.drift * loopProgress +
                    sin(wavePhase + index).toFloat() * piece.wave,
                y = -particleSize * 2f + (size.height + particleSize * 4f) * loopProgress
            )
            rotate(piece.rotationDegrees + loopProgress * piece.rotationSpeed, center) {
                if (piece.isCircle) {
                    drawCircle(
                        color = piece.color.copy(alpha = 0.78f),
                        radius = particleSize * 0.42f,
                        center = center
                    )
                } else {
                    drawRect(
                        color = piece.color.copy(alpha = 0.86f),
                        topLeft = Offset(center.x - particleSize / 2f, center.y - particleSize / 2f),
                        size = Size(particleSize, particleSize * 0.52f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrophyGlow(
    modifier: Modifier = Modifier,
    pulse: Float,
    landingGlow: Float
) {
    Box(
        modifier = modifier
            .size(260.dp)
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((220 + 52 * landingGlow).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFF3BF).copy(alpha = 0.25f + 0.28f * landingGlow),
                            Color(0xFFFFC107).copy(alpha = 0.10f + 0.20f * landingGlow),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(164.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
    }
}

@Composable
private fun BoxScope.MagicLightBeam(
    ambientProgress: Float,
    beamProgress: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.05f)
        drawCircle(
            color = Color(0xFFFFD75A).copy(alpha = 0.20f * ambientProgress),
            radius = size.minDimension * (0.26f + 0.16f * ambientProgress),
            center = center
        )
        val beamWidth = size.width * (0.12f + 0.33f * beamProgress)
        val beam = Path().apply {
            moveTo(center.x - beamWidth * 0.24f, 0f)
            lineTo(center.x + beamWidth * 0.24f, 0f)
            lineTo(center.x + beamWidth, size.height * 0.72f)
            lineTo(center.x - beamWidth, size.height * 0.72f)
            close()
        }
        drawPath(
            path = beam,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF3B0).copy(alpha = 0.34f * beamProgress),
                    Color(0xFFFFCA46).copy(alpha = 0.12f * beamProgress),
                    Color.Transparent
                )
            )
        )
        drawRect(Color(0xFF061A32).copy(alpha = 0.16f * (1f - ambientProgress)))
    }
}

@Composable
private fun BoxScope.TrophyImpact(
    progress: Float,
    orbitProgress: Float,
    modifier: Modifier = Modifier
) {
    val burst = remember {
        List(22) { index ->
            BurstParticle(
                angle = (index * 47 % 360).toFloat(),
                distance = 64f + (index * 19 % 76),
                radius = 3.5f + (index % 4) * 1.6f,
                speed = 0.78f + (index % 5) * 0.07f
            )
        }
    }
    Canvas(modifier = modifier.size(310.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val impact = progress.coerceIn(0f, 1f)
        if (impact > 0f && impact < 1f) {
            drawCircle(
                color = Color(0xFFFFE58B).copy(alpha = (1f - impact) * 0.64f),
                radius = size.minDimension * (0.10f + impact * 0.34f),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.dp.toPx())
            )
            burst.forEach { particle ->
                val angle = particle.angle * PI / 180.0
                val distance = particle.distance * impact * particle.speed
                val point = Offset(
                    center.x + cos(angle).toFloat() * distance,
                    center.y + sin(angle).toFloat() * distance
                )
                drawGoldenStar(
                    center = point,
                    radius = particle.radius * (1.2f - impact * 0.35f),
                    alpha = (1f - impact) * 0.95f,
                    rotationDegrees = particle.angle + impact * 180f
                )
            }
        }
        repeat(7) { index ->
            val angle = (orbitProgress * 360f + index * (360f / 7f)) * PI / 180.0
            val radius = size.minDimension * (0.31f + if (index % 2 == 0) 0.04f else 0f)
            drawGoldenStar(
                center = Offset(
                    center.x + cos(angle).toFloat() * radius,
                    center.y + sin(angle).toFloat() * radius
                ),
                radius = if (index % 2 == 0) 7f else 4.8f,
                alpha = 0.72f,
                rotationDegrees = orbitProgress * 180f + index * 32f
            )
        }
    }
}

private fun DrawScope.drawGoldenStar(
    center: Offset,
    radius: Float,
    alpha: Float,
    rotationDegrees: Float
) {
    val path = Path()
    repeat(10) { pointIndex ->
        val angle = (rotationDegrees - 90f + pointIndex * 36f) * PI / 180.0
        val pointRadius = if (pointIndex % 2 == 0) radius else radius * 0.43f
        val point = Offset(
            center.x + cos(angle).toFloat() * pointRadius,
            center.y + sin(angle).toFloat() * pointRadius
        )
        if (pointIndex == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    drawPath(path, Color(0xFFFFE278).copy(alpha = alpha.coerceIn(0f, 1f)))
}

private data class ConfettiPiece(
    val xFraction: Float,
    val startOffset: Float,
    val fallSpeed: Float,
    val drift: Float,
    val wave: Float,
    val size: Dp,
    val color: Color,
    val rotationDegrees: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

private data class BurstParticle(
    val angle: Float,
    val distance: Float,
    val radius: Float,
    val speed: Float
)

@Composable
private fun BoxScope.CornerHorn(
    painter: Painter,
    alignment: Alignment,
    rotationDegrees: Float,
    size: Dp,
) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .align(alignment)
            .size(size)
            .rotate(rotationDegrees),
    )
}
