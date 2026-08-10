package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.delay
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.GameSounds
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.components.DashboardButton
import mohaamadreza.saemipour.no.vazheh.ui.components.WinCelebration
import mohaamadreza.saemipour.no.vazheh.ui.components.backToDashboard
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple40
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple80
import mohaamadreza.saemipour.no.vazheh.ui.theme.SkyBlue

/** Sentinel categoryId indicating a combined (cross-category) memory game */
const val COMBINED_CATEGORY_ID: Int = -1

@Composable
fun MemoryGameScreen(
    navController: NavController,
    viewModel: MemoryGameViewModel,
    categoryId: Int
) {
    val audioUpdates = remember {
        object : AudioUpdates {
            override fun onProgressUpdate(state: PlayerState) {}
            override fun onReady() {}
            override fun onError(exception: Exception) {}
        }
    }

    LaunchedEffect(categoryId) {
        if (categoryId == COMBINED_CATEGORY_ID) {
            viewModel.loadCombinedWords(pairCount = 6)
        } else {
            viewModel.loadWords(categoryId, pairCount = 6)
        }
    }

    AudioProvider(audioUpdates = audioUpdates) { audioPlayer ->
        DisposableEffect(Unit) {
            onDispose {
                audioPlayer.cleanUp()
            }
        }

        // جفت درست: اول جینگل درست، بعد صدای کلمه
        LaunchedEffect(viewModel.currentMatchedWord) {
            viewModel.currentMatchedWord?.let { word ->
                audioPlayer.play(GameSounds.correct)
                word.audioUrl?.let { url ->
                    if (url.isNotEmpty()) {
                        delay(700)
                        audioPlayer.play(url)
                    }
                }
                viewModel.clearMatchedWord()
            }
        }

        // صدای برد در پایان بازی
        LaunchedEffect(viewModel.isWin) {
            if (viewModel.isWin) audioPlayer.play(GameSounds.win)
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
                when {
                    viewModel.isLoading -> {
                        LoadingContent()
                    }
                    viewModel.errorMessage != null -> {
                        ErrorContent(
                            message = viewModel.errorMessage ?: "",
                            onRetry = { viewModel.retry() },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    viewModel.cards.isNotEmpty() -> {
                        MemoryGameContent(
                            cards = viewModel.cards,
                            moves = viewModel.moves,
                            matchedPairs = viewModel.matchedPairs,
                            totalPairs = viewModel.totalPairs,
                            onCardClick = { viewModel.onCardClick(it) },
                            onResetGame = { viewModel.resetGame() },
                            onBack = { navController.backToDashboard() }
                        )
                    }
                }

                if (viewModel.isWin) {
                    // فقط انیمیشن برد؛ لمس صفحه = شروع دوباره بازی
                    WinCelebration(onTap = { viewModel.resetGame() })
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Purple40,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "در حال آماده‌سازی بازی...",
                style = MaterialTheme.typography.titleMedium,
                color = Purple40
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "😕",
                fontSize = 64.sp
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = CoralRed,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(onClick = onBack) {
                    Text("بازگشت")
                }
                Button(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("تلاش مجدد")
                }
            }
        }
    }
}

@Composable
private fun MemoryGameContent(
    cards: List<MemoryCard>,
    moves: Int,
    matchedPairs: Int,
    totalPairs: Int,
    onCardClick: (MemoryCard) -> Unit,
    onResetGame: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🧠 بازی حافظه",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Purple40
                )
                Text(
                    text = "جفت‌های مشابه را پیدا کن!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            DashboardButton(onClick = onBack, tint = Purple40)
        }

        Spacer(Modifier.height(16.dp))

        // ستاره طلایی برای هر جفت صحیح
        StarsRow(
            matchedPairs = matchedPairs,
            totalPairs = totalPairs
        )

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(cards, key = { it.id }) { card ->
                MemoryCardItem(
                    card = card,
                    onClick = { onCardClick(card) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onResetGame,
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
}

@Composable
private fun StarsRow(
    matchedPairs: Int,
    totalPairs: Int
) {
    val goldColor = Color(0xFFFFC107)
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
                repeat(totalPairs) { index ->
                    val isEarned = index < matchedPairs
                    val starScale by animateFloatAsState(
                        targetValue = if (isEarned) 1f else 0.75f,
                        animationSpec = tween(durationMillis = 350),
                        label = "starScale$index"
                    )
                    Text(
                        text = if (isEarned) "⭐" else "☆",
                        fontSize = 28.sp,
                        color = if (isEarned) goldColor else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .scale(starScale)
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryCardItem(
    card: MemoryCard,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(400),
        label = "cardFlip"
    )

    val scale by animateFloatAsState(
        targetValue = if (card.isMatched) 0.95f else 1f,
        animationSpec = tween(300),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFlipped && !card.isMatched) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                card.isMatched -> MintGreen.copy(alpha = 0.3f)
                rotation > 90f -> Color.White
                else -> Purple40
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (card.isFlipped) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation > 90f) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!card.word.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(card.word.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = card.word.wordFa,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = card.word.wordFa,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Purple80.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

