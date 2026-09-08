package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.runtime.withFrameNanos
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.GameSounds
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.components.DashboardButton
import mohaamadreza.saemipour.no.vazheh.ui.components.LevelUpCelebration
import mohaamadreza.saemipour.no.vazheh.ui.components.WinCelebration
import mohaamadreza.saemipour.no.vazheh.ui.components.backToDashboard
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple40
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple80
import mohaamadreza.saemipour.no.vazheh.ui.theme.SkyBlue
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel

private val LocalColorDragInfo = compositionLocalOf { ColorDragInfo() }

/** فاصله‌ی بین جینگل «درست» و پخش صدای نام رنگ */
private const val COLOR_SOUND_DELAY_MS = 700L

/** حداکثر انتظار برای شروع پخش صدای رنگ (اگر رنگ صدا نداشته باشد) */
private const val WAIT_FOR_SOUND_START_MS = 2_000L

/** حداکثر انتظار برای پایان صدای رنگ، تا در صورت خطای پخش گیر نکنیم */
private const val WAIT_FOR_SOUND_END_MS = 8_000L

private const val LEVEL_UP_NORMAL_CELEBRATION_MS = 2_250L

private enum class ColorCelebrationStage {
    None,
    Normal,
    LevelUp,
}

private data class BalloonPopEvent(
    val eventId: Long,
    val itemId: Int,
    val color: Color,
    val centerInWindow: Offset,
    val widthPx: Int,
    val heightPx: Int
)

private class ColorDragInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggedSize by mutableStateOf(IntSize.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<ColorItem?>(null)
    var popEvent by mutableStateOf<BalloonPopEvent?>(null)

    private var nextPopEventId = 0L

    fun nextPopEvent(
        item: ColorItem,
        centerInWindow: Offset,
        sourceSize: IntSize
    ): BalloonPopEvent {
        nextPopEventId += 1
        return BalloonPopEvent(
            eventId = nextPopEventId,
            itemId = item.id,
            color = item.color.color,
            centerInWindow = centerInWindow,
            widthPx = sourceSize.width,
            heightPx = sourceSize.height
        )
    }
}

@Composable
fun ColorSortingScreen(
    navController: NavController,
    viewModel: ColorSortingViewModel,
    childViewModel: ChildViewModel
) {
    val dragState = remember { ColorDragInfo() }
    val childUiState by childViewModel.uiState.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(childUiState.selectedChild?.id) {
        viewModel.loadGame(childUiState.selectedChild?.id)
    }

    // Audio callbacks
    val audioUpdates = remember {
        object : AudioUpdates {
            override fun onProgressUpdate(state: PlayerState) {
                isPlaying = state.isPlaying
            }
            override fun onReady() {}
            override fun onError(exception: Exception) {
                isPlaying = false
            }
        }
    }

    AudioProvider(audioUpdates = audioUpdates) { audioPlayer ->
        DisposableEffect(Unit) {
            onDispose {
                audioPlayer.cleanUp()
            }
        }

        // جای‌گذاری اشتباه: صدای اشتباه همراه با بازخورد تصویری
        LaunchedEffect(viewModel.showWrongFeedback) {
            if (viewModel.showWrongFeedback) {
                delay(800)
                viewModel.clearWrongFeedback()
            }
        }

        // Align the existing wrong-answer sound with the visual pop snap.
        LaunchedEffect(dragState.popEvent?.eventId) {
            val event = dragState.popEvent ?: return@LaunchedEffect
            delay(90)
            if (dragState.popEvent?.eventId == event.eventId) {
                audioPlayer.play(GameSounds.wrong)
            }
        }

        // جای‌گذاری درست: اول جینگل درست، بعد صدای رنگ
        LaunchedEffect(viewModel.currentSoundUrl) {
            viewModel.currentSoundUrl?.let { url ->
                audioPlayer.play(GameSounds.correct)
                if (url.isNotEmpty()) {
                    delay(COLOR_SOUND_DELAY_MS)
                    audioPlayer.play(url)
                }
                viewModel.clearCurrentSound()
            }
        }

        // تشویق پایان بازی: اول صدای آخرین رنگ تا آخر پخش می‌شود و تنها بعد از آن
        // صدای برد و انیمیشن تشویق می‌آید، تا روی صدای رنگ نیفتد.
        var showWin by remember { mutableStateOf(false) }
        var celebrationStage by remember { mutableStateOf(ColorCelebrationStage.None) }
        LaunchedEffect(viewModel.isWin) {
            if (!viewModel.isWin) {
                showWin = false
                celebrationStage = ColorCelebrationStage.None
                return@LaunchedEffect
            }
            delay(COLOR_SOUND_DELAY_MS + 200)
            withTimeoutOrNull(WAIT_FOR_SOUND_START_MS) { while (!isPlaying) delay(50) }
            withTimeoutOrNull(WAIT_FOR_SOUND_END_MS) { while (isPlaying) delay(50) }
            delay(400)
            audioPlayer.play(GameSounds.win)
            celebrationStage = ColorCelebrationStage.Normal
            showWin = true
        }

        CompositionLocalProvider(LocalColorDragInfo provides dragState) {
        val celebrationActive =
            viewModel.isWin || showWin || celebrationStage != ColorCelebrationStage.None
        var rootOriginInWindow by remember { mutableStateOf(Offset.Zero) }
        LaunchedEffect(dragState.isDragging) {
            if (!dragState.isDragging && dragState.dataToDrop != null) {
                // Give a target drop effect one frame to consume a valid release.
                withFrameNanos { }
                if (!dragState.isDragging) {
                    dragState.dataToDrop = null
                    dragState.dragOffset = Offset.Zero
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    rootOriginInWindow = it.localToWindow(Offset.Zero)
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Purple80.copy(alpha = 0.3f),
                            SkyBlue.copy(alpha = 0.2f),
                            MintGreen.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            if (!celebrationActive) Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    DashboardButton(
                        onClick = { navController.backToDashboard() },
                        tint = Purple40
                    )
                    Column {
                        Text(
                            text = "🎨 بازی رنگ‌ها",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Purple40
                        )
                        Text(
                            text = "بادکنک‌ها را به باکس هم‌رنگشان ببر!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (childUiState.selectedChild != null) {
                    StarsRow(earnedStars = viewModel.celebrationStarsOverride ?: viewModel.successfulGames)
                    Spacer(Modifier.height(16.dp))
                }

                // Progress
                ProgressCard(
                    sortedCount = viewModel.sortedCount,
                    totalItems = viewModel.totalItems
                )

                Spacer(Modifier.height(24.dp))

                // Items to sort
                Text(
                    text = "بادکنک‌ها را نگه دار و بکش به باکس‌ها:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(Modifier.height(12.dp))

                // Draggable items area
                ItemsArea(
                    items = viewModel.items.filter { !it.isSorted },
                    viewModel = viewModel
                )

                Spacer(Modifier.height(32.dp))

                // Boxes
                Text(
                    text = "باکس‌های رنگی:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(Modifier.height(12.dp))

                BasketsRow(
                    baskets = viewModel.baskets,
                    viewModel = viewModel
                )

                Spacer(Modifier.weight(1f))

                // Reset button
                Button(
                    onClick = { viewModel.resetGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple40
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "شروع دوباره",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Dragging overlay - renders dragged item at root level
            if (!celebrationActive && dragState.isDragging) {
                var targetSize by remember { mutableStateOf(IntSize.Zero) }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val offset = dragState.dragPosition + dragState.dragOffset
                            scaleX = 1.3f
                            scaleY = 1.3f
                            alpha = if (targetSize == IntSize.Zero) 0f else 0.9f
                            translationX = offset.x - (targetSize.width / 2)
                            translationY = offset.y - (targetSize.height / 2)
                        }
                        .onGloballyPositioned {
                            targetSize = it.size
                        }
                ) {
                    dragState.draggableComposable?.invoke()
                }
            }

            dragState.popEvent?.let { event ->
                BalloonPopEffect(
                    event = event,
                    rootOriginInWindow = rootOriginInWindow,
                    onFinished = {
                        if (dragState.popEvent?.eventId == event.eventId) {
                            dragState.popEvent = null
                        }
                    }
                )
            }

            // فقط انیمیشن برد؛ لمس صفحه = شروع دوباره بازی
            if (showWin) {
                when (celebrationStage) {
                    ColorCelebrationStage.Normal -> WinCelebration(
                        timeoutMillis = if (viewModel.isLevelUpWin) {
                            LEVEL_UP_NORMAL_CELEBRATION_MS
                        } else {
                            WAIT_FOR_SOUND_END_MS
                        },
                        onTap = {
                            if (viewModel.isLevelUpWin) {
                                celebrationStage = ColorCelebrationStage.LevelUp
                            } else {
                                viewModel.resetGame()
                            }
                        }
                    )

                    ColorCelebrationStage.LevelUp -> LevelUpCelebration(
                        onTap = { viewModel.resetGame() }
                    )

                    ColorCelebrationStage.None -> Unit
                }
            }
        }
    }
    }
}

@Composable
private fun StarsRow(
    earnedStars: Int
) {
    val goldColor = Color(0xFFFFC107)
    val displayedStars = earnedStars.coerceIn(0, 3)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = goldColor.copy(alpha = 0.02f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ستاره‌های تو",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B6F00)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    val isEarned = index < displayedStars
                    Text(
                        text = if (isEarned) "⭐" else "☆",
                        fontSize = 28.sp,
                        color = if (isEarned) goldColor else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(
    sortedCount: Int,
    totalItems: Int
) {
    val filledTicks = when {
        totalItems <= 0 -> 0
        sortedCount >= totalItems -> 3
        else -> (sortedCount.coerceAtLeast(0) * 3 / totalItems).coerceIn(0, 3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MintGreen.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val isFilled = index < filledTicks
                val scale by animateFloatAsState(
                    targetValue = if (isFilled) 1f else 0.9f,
                    animationSpec = tween(220),
                    label = "sortProgressScale$index"
                )
                val indicatorColor by animateColorAsState(
                    targetValue = if (isFilled) MintGreen else Color.Gray.copy(alpha = 0.45f),
                    animationSpec = tween(220),
                    label = "sortProgressColor$index"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(32.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(CircleShape)
                        .background(
                            if (isFilled) {
                                indicatorColor.copy(alpha = 0.18f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .border(2.dp, indicatorColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFilled) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = indicatorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemsArea(
    items: List<ColorItem>,
    viewModel: ColorSortingViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎉 آفرین! همه رو مرتب کردی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MintGreen
                )
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val spacing = when (items.size) {
                    3 -> 18.dp
                    4 -> 12.dp
                    else -> 8.dp
                }
                val slotWidth = (maxWidth - spacing * (items.size - 1)) / items.size
                val balloonWidth = slotWidth.coerceIn(48.dp, 64.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        if (index > 0) Spacer(Modifier.width(spacing))
                        key(item.id) {
                            ColorDragTarget(
                                item = item,
                                viewModel = viewModel,
                                balloonWidth = balloonWidth
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PopFragment(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val rotation: Float,
    val shape: Int
)

@Composable
private fun BalloonPopEffect(
    event: BalloonPopEvent,
    rootOriginInWindow: Offset,
    onFinished: () -> Unit
) {
    val progress = remember(event.eventId) { Animatable(0f) }
    val fragments = remember(event.eventId, event.widthPx, event.heightPx) {
        listOf(
            PopFragment(-2.85f, 1.15f, 0.19f, -70f, 0),
            PopFragment(-2.35f, 1.35f, 0.14f, 95f, 1),
            PopFragment(-1.85f, 1.05f, 0.17f, 150f, 2),
            PopFragment(-1.25f, 1.3f, 0.13f, -120f, 1),
            PopFragment(-0.72f, 1.18f, 0.16f, 80f, 0),
            PopFragment(-0.18f, 1.42f, 0.12f, 135f, 2),
            PopFragment(0.42f, 1.12f, 0.18f, -90f, 1),
            PopFragment(1.02f, 1.32f, 0.13f, 60f, 0),
            PopFragment(1.65f, 1.08f, 0.16f, -145f, 2),
            PopFragment(2.22f, 1.3f, 0.12f, 110f, 1),
            PopFragment(2.78f, 1.16f, 0.15f, -55f, 0),
            PopFragment(3.35f, 1.38f, 0.11f, 170f, 2)
        )
    }
    val density = LocalDensity.current
    val center = event.centerInWindow - rootOriginInWindow
    val width = event.widthPx.coerceAtLeast(1).toFloat()
    val height = event.heightPx.coerceAtLeast(1).toFloat()
    val radius = maxOf(width, height) * 0.42f

    LaunchedEffect(event.eventId) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    val animationProgress = progress.value
    val anticipation = (animationProgress / 0.15f).coerceIn(0f, 1f)
    val inflate = ((animationProgress - 0.15f) / 0.13f).coerceIn(0f, 1f)
    val bodyAlpha = if (animationProgress < 0.28f) {
        1f
    } else {
        (1f - (animationProgress - 0.28f) / 0.14f).coerceIn(0f, 1f)
    }
    val bodyScaleX = when {
        animationProgress < 0.15f -> 1f + 0.05f * anticipation
        animationProgress < 0.28f -> 1.05f + 0.14f * inflate
        else -> 1.19f
    }
    val bodyScaleY = when {
        animationProgress < 0.15f -> 1f - 0.04f * anticipation
        animationProgress < 0.28f -> 0.96f + 0.23f * inflate
        else -> 1.19f
    }
    val fragmentProgress = ((animationProgress - 0.23f) / 0.77f).coerceIn(0f, 1f)
    val ringProgress = ((animationProgress - 0.2f) / 0.25f).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (ringProgress > 0f && ringProgress < 1f) {
                drawCircle(
                    color = event.color.copy(alpha = 0.32f * (1f - ringProgress)),
                    radius = radius * (0.35f + 1.35f * ringProgress),
                    center = center,
                    style = Stroke(width = (width * 0.035f).coerceAtLeast(2f))
                )
            }

            if (fragmentProgress > 0f) {
                fragments.forEach { fragment ->
                    val travel = fragment.distance * radius * fragmentProgress
                    val fragmentCenter = Offset(
                        x = center.x + cos(fragment.angle) * travel,
                        y = center.y + sin(fragment.angle) * travel + height * 0.16f * fragmentProgress * fragmentProgress
                    )
                    val fragmentSize = (width * fragment.size).coerceAtLeast(4f)
                    val alpha = (1f - fragmentProgress).coerceIn(0f, 1f)

                    rotate(
                        degrees = fragment.rotation * fragmentProgress,
                        pivot = fragmentCenter
                    ) {
                        when (fragment.shape) {
                            0 -> drawCircle(
                                color = event.color.copy(alpha = alpha),
                                radius = fragmentSize * 0.45f,
                                center = fragmentCenter
                            )

                            1 -> drawRoundRect(
                                color = event.color.copy(alpha = alpha),
                                topLeft = Offset(
                                    fragmentCenter.x - fragmentSize * 0.55f,
                                    fragmentCenter.y - fragmentSize * 0.28f
                                ),
                                size = Size(fragmentSize * 1.1f, fragmentSize * 0.56f),
                                cornerRadius = CornerRadius(fragmentSize * 0.18f)
                            )

                            else -> {
                                val path = Path().apply {
                                    moveTo(fragmentCenter.x, fragmentCenter.y - fragmentSize * 0.55f)
                                    lineTo(
                                        fragmentCenter.x + fragmentSize * 0.5f,
                                        fragmentCenter.y + fragmentSize * 0.4f
                                    )
                                    lineTo(
                                        fragmentCenter.x - fragmentSize * 0.38f,
                                        fragmentCenter.y + fragmentSize * 0.48f
                                    )
                                    close()
                                }
                                drawPath(path = path, color = event.color.copy(alpha = alpha))
                            }
                        }
                    }
                }

                repeat(5) { index ->
                    val angle = -2.65f + index * 1.22f
                    val travel = radius * (0.75f + index % 2 * 0.2f) * fragmentProgress
                    drawCircle(
                        color = Color.White.copy(alpha = 0.55f * (1f - fragmentProgress)),
                        radius = (width * 0.035f).coerceAtLeast(2f),
                        center = Offset(
                            center.x + cos(angle) * travel,
                            center.y + sin(angle) * travel
                        )
                    )
                }
            }
        }

        if (bodyAlpha > 0f) {
            val widthDp = with(density) { width.toDp() }
            val heightDp = with(density) { height.toDp() }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (center.x - width / 2f).roundToInt(),
                            (center.y - height / 2f).roundToInt()
                        )
                    }
                    .size(width = widthDp, height = heightDp)
                    .graphicsLayer {
                        alpha = bodyAlpha
                        scaleX = bodyScaleX
                        scaleY = bodyScaleY
                    },
                contentAlignment = Alignment.Center
            ) {
                Balloon(color = event.color, width = widthDp)
            }
        }
    }
}

@Composable
private fun ColorDragTarget(
    item: ColorItem,
    viewModel: ColorSortingViewModel,
    balloonWidth: Dp
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    var currentSize by remember { mutableStateOf(IntSize.Zero) }
    val dragState = LocalColorDragInfo.current
    val isPopping = dragState.popEvent?.itemId == item.id

    val content: @Composable () -> Unit = {
        Balloon(color = item.color.color, width = balloonWidth)
    }

    val dragModifier = if (isPopping) {
        Modifier
    } else {
        Modifier.pointerInput(item.id) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    viewModel.startDragging()
                    dragState.dataToDrop = item
                    dragState.isDragging = true
                    dragState.dragOffset = Offset.Zero
                    dragState.dragPosition = currentPosition + offset
                    dragState.draggedSize = currentSize
                    dragState.draggableComposable = content
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragState.dragOffset += Offset(dragAmount.x, dragAmount.y)
                },
                onDragEnd = {
                    viewModel.stopDragging()
                    dragState.isDragging = false
                },
                onDragCancel = {
                    viewModel.stopDragging()
                    dragState.isDragging = false
                    dragState.dataToDrop = null
                    dragState.dragOffset = Offset.Zero
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned {
                currentPosition = it.localToWindow(Offset.Zero)
                currentSize = it.size
            }
            .graphicsLayer {
                alpha = if (isPopping) 0f else 1f
            }
            .then(dragModifier)
    ) {
        content()
    }
}

/**
 * یک بادکنک رنگی؛ اندازه با [width] تعیین می‌شود و بقیه‌ی اجزا متناسب با آن مقیاس می‌گیرند.
 * برای بادکنک‌های بسته‌شده به باکس، نخ جدا کشیده می‌شود پس [showString] را false بدهید.
 */
@Composable
private fun Balloon(
    color: Color,
    width: Dp = 64.dp,
    showString: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // بدنه بادکنک
        Box(
            modifier = Modifier
                .size(width = width, height = width * 1.22f)
                .clip(RoundedCornerShape(50))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            color,
                            color.copy(alpha = 0.85f)
                        ),
                        center = Offset(20f, 20f),
                        radius = 120f
                    )
                )
                .border(
                    width = 2.dp,
                    color = color.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.TopStart
        ) {
            // نقطه نور (highlight)
            Box(
                modifier = Modifier
                    .padding(start = width * 0.19f, top = width * 0.16f)
                    .size(width * 0.22f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f))
            )
        }
        // گره کوچک پایین بادکنک
        Box(
            modifier = Modifier
                .size(width = width * 0.125f, height = width * 0.094f)
                .background(color.copy(alpha = 0.9f))
        )
        if (showString) {
            // نخ بادکنک
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(22.dp)
                    .background(Color.Gray.copy(alpha = 0.7f))
            )
        }
    }
}

/**
 * دسته‌ی بادکنک‌های جمع‌شده‌ی یک باکس: بادکنک‌ها بزرگ‌تر بالای باکس شناورند و
 * هرکدام با یک نخ منحنی به لبه‌ی باکس بسته شده‌اند. تکان‌خوردن آرام هر بادکنک
 * و ظاهرشدن با انیمیشن، حس بادکنک واقعی را برای کودک می‌سازد.
 */
@Composable
private fun BasketBalloonCluster(
    items: List<ColorItem>,
    modifier: Modifier = Modifier,
    balloonWidth: Dp = 46.dp
) {
    // جایگاه هر بادکنک نسبت به وسطِ بالای باکس
    val slots = listOf(
        DpOffset(0.dp, 4.dp),
        DpOffset((-26).dp, 20.dp),
        DpOffset(26.dp, 20.dp),
        DpOffset((-13).dp, 34.dp),
        DpOffset(13.dp, 34.dp)
    )
    // بدنه + گره؛ نقطه‌ی شروع نخ از همین‌جاست
    val balloonBodyHeight = balloonWidth * 1.22f + balloonWidth * 0.094f

    val shown = items.take(slots.size)
    val infiniteTransition = rememberInfiniteTransition(label = "balloonBob")
    val bobs = shown.mapIndexed { index, _ ->
        infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500 + index * 300, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bob$index"
        )
    }

    val bobRangePx = with(LocalDensity.current) { 4.dp.toPx() }

    Box(modifier = modifier) {
        // نخ‌ها: از ته هر بادکنک تا وسط لبه‌ی باکس (با کمی خمیدگی طبیعی)
        Canvas(modifier = Modifier.matchParentSize()) {
            val anchor = Offset(size.width / 2f, size.height)
            shown.forEachIndexed { index, _ ->
                val slot = slots[index]
                val bob = bobs[index].value * bobRangePx
                val startX = size.width / 2f + slot.x.toPx()
                val startY = slot.y.toPx() + balloonBodyHeight.toPx() + bob
                val stringPath = Path().apply {
                    moveTo(startX, startY)
                    quadraticBezierTo(
                        (startX + anchor.x) / 2f,
                        (startY + anchor.y) / 2f + 8.dp.toPx(),
                        anchor.x,
                        anchor.y
                    )
                }
                drawPath(
                    path = stringPath,
                    color = Color.Gray.copy(alpha = 0.65f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        shown.forEachIndexed { index, item ->
            val slot = slots[index]
            // هر بادکنک با انیمیشن کوچک→بزرگ ظاهر می‌شود
            val appear = remember { MutableTransitionState(false).apply { targetState = true } }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = slot.x, y = slot.y)
                    .graphicsLayer { translationY = bobs[index].value * bobRangePx }
            ) {
                AnimatedVisibility(
                    visibleState = appear,
                    enter = scaleIn(animationSpec = tween(400)) + fadeIn(tween(400))
                ) {
                    Balloon(color = item.color.color, width = balloonWidth, showString = false)
                }
            }
        }
    }
}

@Composable
private fun BasketsRow(
    baskets: List<ColorBasket>,
    viewModel: ColorSortingViewModel
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val spacing = when (baskets.size) {
            3 -> 12.dp
            4 -> 6.dp
            else -> 4.dp
        }
        val slotWidth = (maxWidth - spacing * (baskets.size - 1)) / baskets.size
        val basketWidth = (slotWidth - 4.dp).coerceIn(60.dp, 88.dp)
        val clusterBalloonWidth = (basketWidth * 0.52f).coerceIn(32.dp, 46.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            baskets.forEachIndexed { index, basket ->
                if (index > 0) Spacer(Modifier.width(spacing))
                key(basket.color) {
                    Box(modifier = Modifier.width(slotWidth)) {
                        ColorDropItem(
                            basket = basket,
                            viewModel = viewModel,
                            basketWidth = basketWidth,
                            clusterBalloonWidth = clusterBalloonWidth
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorDropItem(
    basket: ColorBasket,
    viewModel: ColorSortingViewModel,
    basketWidth: Dp,
    clusterBalloonWidth: Dp
) {
    val dragState = LocalColorDragInfo.current
    val dragPosition = dragState.dragPosition
    val dragOffset = dragState.dragOffset
    var targetBounds by remember { mutableStateOf<Rect?>(null) }
    val currentPointer = dragPosition + dragOffset
    val pointerInTarget = targetBounds?.contains(currentPointer) == true
    val isCurrentDropTarget = dragState.isDragging && pointerInTarget

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                targetBounds = coordinates.boundsInWindow()
            }
            .fillMaxWidth()
    ) {
        // Get the dropped data when drag ends and we're the target
        val droppedItem: ColorItem? = if (!dragState.isDragging && pointerInTarget) {
            dragState.dataToDrop
        } else {
            null
        }

        // Handle the drop
        LaunchedEffect(droppedItem) {
            if (droppedItem != null) {
                val wasCorrect = viewModel.sortItemToBasket(droppedItem, basket)
                if (!wasCorrect) {
                    dragState.popEvent = dragState.nextPopEvent(
                        item = droppedItem,
                        centerInWindow = currentPointer,
                        sourceSize = dragState.draggedSize
                    )
                }
                dragState.dataToDrop = null
                dragState.dragOffset = Offset.Zero
            }
        }

        // بادکنک‌های جمع‌شده، بزرگ‌تر و آویزان به باکس
        BasketBalloonCluster(
            items = basket.items,
            modifier = Modifier
                .width(basketWidth)
                .height(104.dp),
            balloonWidth = clusterBalloonWidth
        )

        // Box visual
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(width = basketWidth, height = 90.dp)
        ) {
            // lid (درب باکس)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(basket.color.color.copy(alpha = 0.95f))
                    .border(
                        width = if (isCurrentDropTarget && dragState.isDragging) 3.dp else 2.dp,
                        color = if (isCurrentDropTarget && dragState.isDragging)
                            basket.color.color
                        else
                            basket.color.color,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            )
            // body (بدنه باکس)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                basket.color.color.copy(alpha = 0.35f),
                                basket.color.color.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .border(
                        width = if (isCurrentDropTarget && dragState.isDragging) 4.dp else 3.dp,
                        color = if (isCurrentDropTarget && dragState.isDragging)
                            basket.color.color
                        else
                            basket.color.color,
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // بادکنک‌های جمع‌شده حالا بالای باکس آویزانند (BasketBalloonCluster)
            }
        }

        Spacer(Modifier.height(4.dp))

        // Color name
        Text(
            text = basket.color.nameFa,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = basket.color.color,
            maxLines = 1
        )
    }
}
