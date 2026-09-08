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
import androidx.compose.ui.graphics.drawscope.rotate
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
) {
    CelebrationDialog(onTap = onTap, timeoutMillis = timeoutMillis) {
        LevelUpCelebrationScene()
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
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyPulse"
    )
    val leftFlagProgress = remember { Animatable(0f) }
    val rightFlagProgress = remember { Animatable(0f) }
    val trophyProgress = remember { Animatable(0f) }
    val titleProgress = remember { Animatable(0f) }
    val subtitleProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        leftFlagProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 280, delayMillis = 180, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        rightFlagProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 280, delayMillis = 240, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        trophyProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.46f, stiffness = 250f)
        )
    }
    LaunchedEffect(Unit) {
        titleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 260, delayMillis = 760, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        subtitleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 240, delayMillis = 940, easing = FastOutSlowInEasing)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE7FBFF),
                        Color(0xFF73D7F7),
                        Color(0xFF56DEC9),
                        Color(0xFFA9F3E3)
                    )
                )
            )
    ) {
        LevelUpConfetti(confettiProgress = confettiProgress)
        TrophyGlow(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-28).dp),
            pulse = trophyPulse
        )
        AnimatedFlag(
            emoji = "🚩",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-112).dp, y = (-116).dp),
            progress = leftFlagProgress.value,
            direction = -1f
        )
        AnimatedFlag(
            emoji = "🚩",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 112.dp, y = (-116).dp),
            progress = rightFlagProgress.value,
            direction = 1f
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆",
                fontSize = 126.sp,
                modifier = Modifier
                    .offset(y = ((1f - trophyProgress.value) * 120f).dp)
                    .scale((0.32f + 0.76f * trophyProgress.value) * trophyPulse)
            )
            Text(
                text = "مرحله بعد!",
                modifier = Modifier
                    .padding(top = 14.dp)
                    .scale(0.82f + 0.18f * titleProgress.value),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF17406D).copy(alpha = titleProgress.value),
                textAlign = TextAlign.Center
            )
            Text(
                text = "آفرین! رفتی مرحله بعد",
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
    pulse: Float
) {
    Box(
        modifier = modifier
            .size(260.dp)
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFE082).copy(alpha = 0.32f),
                            Color(0xFFFFC107).copy(alpha = 0.12f),
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
private fun AnimatedFlag(
    emoji: String,
    modifier: Modifier = Modifier,
    progress: Float,
    direction: Float
) {
    val wave = rememberInfiniteTransition(label = "flagWave")
    val rotation by wave.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flagRotation"
    )

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = (direction * (1f - progress) * 160f).toInt(),
                    y = ((1f - progress) * -36f).toInt()
                )
            }
            .scale(0.55f + 0.45f * progress)
            .rotate(direction * rotation),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 42.sp)
    }
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
