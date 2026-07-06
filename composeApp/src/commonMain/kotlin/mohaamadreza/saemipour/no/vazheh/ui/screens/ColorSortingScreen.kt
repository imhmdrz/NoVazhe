package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.components.DashboardButton
import mohaamadreza.saemipour.no.vazheh.ui.components.WinCelebration
import mohaamadreza.saemipour.no.vazheh.ui.components.backToDashboard
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple40
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple80
import mohaamadreza.saemipour.no.vazheh.ui.theme.SkyBlue

private val LocalColorDragInfo = compositionLocalOf { ColorDragInfo() }

private class ColorDragInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<ColorItem?>(null)
}

@Composable
fun ColorSortingScreen(
    navController: NavController,
    viewModel: ColorSortingViewModel
) {
    val dragState = remember { ColorDragInfo() }

    // Audio callbacks
    val audioUpdates = remember {
        object : AudioUpdates {
            override fun onProgressUpdate(state: PlayerState) {}
            override fun onReady() {}
            override fun onError(exception: Exception) {}
        }
    }

    LaunchedEffect(viewModel.showWrongFeedback) {
        if (viewModel.showWrongFeedback) {
            delay(800)
            viewModel.clearWrongFeedback()
        }
    }

    AudioProvider(audioUpdates = audioUpdates) { audioPlayer ->
        DisposableEffect(Unit) {
            onDispose {
                audioPlayer.cleanUp()
            }
        }

        // پخش صدای رنگ هنگامی که آیتم به درستی در باکس قرار می‌گیرد
        LaunchedEffect(viewModel.currentSoundUrl) {
            viewModel.currentSoundUrl?.let { url ->
                if (url.isNotEmpty()) {
                    audioPlayer.play(url)
                }
                viewModel.clearCurrentSound()
            }
        }

        CompositionLocalProvider(LocalColorDragInfo provides dragState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
            Column(
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
            if (dragState.isDragging) {
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

            // Wrong feedback overlay
            AnimatedVisibility(
                visible = viewModel.showWrongFeedback,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                WrongFeedbackOverlay()
            }

            // فقط انیمیشن برد؛ لمس صفحه = شروع دوباره بازی
            if (viewModel.isWin) {
                WinCelebration(onTap = { viewModel.resetGame() })
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "✅", fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = " مرتب شده ${sortedCount.toPersianDigits()} از ${totalItems.toPersianDigits()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MintGreen
            )
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
                    text = "🎉 آفرین! همه رو مرتب کردی!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MintGreen
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(items, key = { it.id }) { item ->
                    ColorDragTarget(
                        item = item,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorDragTarget(
    item: ColorItem,
    viewModel: ColorSortingViewModel
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    val dragState = LocalColorDragInfo.current

    val content: @Composable () -> Unit = {
        Balloon(color = item.color.color)
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned {
                currentPosition = it.localToWindow(Offset.Zero)
            }
            .pointerInput(item.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        viewModel.startDragging()
                        dragState.dataToDrop = item
                        dragState.isDragging = true
                        dragState.dragPosition = currentPosition + offset
                        dragState.draggableComposable = content
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragState.dragOffset += Offset(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        viewModel.stopDragging()
                        dragState.isDragging = false
                        dragState.dragOffset = Offset.Zero
                    },
                    onDragCancel = {
                        viewModel.stopDragging()
                        dragState.isDragging = false
                        dragState.dragOffset = Offset.Zero
                    }
                )
            }
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
    modifier: Modifier = Modifier
) {
    // جایگاه هر بادکنک نسبت به وسطِ بالای باکس
    val slots = listOf(
        DpOffset(0.dp, 4.dp),
        DpOffset((-26).dp, 20.dp),
        DpOffset(26.dp, 20.dp),
        DpOffset((-13).dp, 34.dp),
        DpOffset(13.dp, 34.dp)
    )
    val balloonWidth = 46.dp
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        baskets.forEach { basket ->
            ColorDropItem(
                basket = basket,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ColorDropItem(
    basket: ColorBasket,
    viewModel: ColorSortingViewModel
) {
    val dragState = LocalColorDragInfo.current
    val dragPosition = dragState.dragPosition
    val dragOffset = dragState.dragOffset
    var isCurrentDropTarget by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                coordinates.boundsInWindow().let { rect ->
                    isCurrentDropTarget = rect.contains(dragPosition + dragOffset)
                }
            }
    ) {
        // Get the dropped data when drag ends and we're the target
        val droppedItem: ColorItem? = if (isCurrentDropTarget && !dragState.isDragging) {
            dragState.dataToDrop
        } else {
            null
        }

        // Handle the drop
        LaunchedEffect(droppedItem) {
            if (droppedItem != null) {
                viewModel.sortItemToBasket(droppedItem, basket)
                dragState.dataToDrop = null
            }
        }

        // بادکنک‌های جمع‌شده، بزرگ‌تر و آویزان به باکس
        BasketBalloonCluster(
            items = basket.items,
            modifier = Modifier
                .width(96.dp)
                .height(104.dp)
        )

        // Box visual
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(width = 88.dp, height = 90.dp)
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
                            MintGreen
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
                            MintGreen
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
            color = basket.color.color
        )
    }
}

@Composable
private fun WrongFeedbackOverlay() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(Color.Red.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "❌",
            fontSize = 56.sp
        )
    }
}


private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.map { ch ->
        if (ch in '0'..'9') persianDigits[ch - '0'] else ch
    }.joinToString("")
}

private fun Int.toPersianDigits(): String = this.toString().toPersianDigits()
