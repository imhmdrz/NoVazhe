package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
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

    LaunchedEffect(viewModel.showWrongFeedback) {
        if (viewModel.showWrongFeedback) {
            delay(800)
            viewModel.clearWrongFeedback()
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
                    Column {
                        Text(
                            text = "🎨 بازی رنگ‌ها",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Purple40
                        )
                        Text(
                            text = "هر شکل رو به سبد هم‌رنگش ببر!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "بازگشت",
                            tint = Purple40
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
                    text = "نگه دار و بکش به سبدها:",
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

                // Baskets
                Text(
                    text = "سبدهای رنگی:",
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

            // Win dialog
            if (viewModel.isWin) {
                WinDialog(
                    onPlayAgain = { viewModel.resetGame() },
                    onBack = { navController.popBackStack() }
                )
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
                text = "$sortedCount از $totalItems",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MintGreen
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "مرتب شده",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
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
            .height(120.dp),
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
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(item.color.color.copy(alpha = 0.2f))
                .border(3.dp, item.color.color, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.emoji,
                fontSize = 36.sp
            )
        }
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

        // Basket visual
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            basket.color.color.copy(alpha = 0.3f),
                            basket.color.color.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(
                    width = if (isCurrentDropTarget && dragState.isDragging) 4.dp else 3.dp,
                    color = if (isCurrentDropTarget && dragState.isDragging) MintGreen else basket.color.color,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (basket.items.isEmpty()) {
                Text(
                    text = "🧺",
                    fontSize = 32.sp
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = basket.items.lastOrNull()?.emoji ?: "🧺",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "${basket.items.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = basket.color.color
                    )
                }
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

@Composable
private fun WinDialog(
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = MintGreen,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌈",
                    fontSize = 64.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "آفرین! همه رنگ‌ها رو مرتب کردی!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = "رنگ‌ها رو خوب یاد گرفتی! 🎨",
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "بازگشت",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MintGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "بازی دوباره",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
