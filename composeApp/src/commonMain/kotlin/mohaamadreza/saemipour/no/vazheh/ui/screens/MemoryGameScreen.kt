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
import androidx.compose.material3.AlertDialog
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
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple40
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple80
import mohaamadreza.saemipour.no.vazheh.ui.theme.SkyBlue

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
        viewModel.loadWords(categoryId, pairCount = 6)
    }

    AudioProvider(audioUpdates = audioUpdates) { audioPlayer ->
        DisposableEffect(Unit) {
            onDispose {
                audioPlayer.cleanUp()
            }
        }

        LaunchedEffect(viewModel.currentMatchedWord) {
            viewModel.currentMatchedWord?.let { word ->
                word.audioUrl?.let { url ->
                    if (url.isNotEmpty()) {
                        audioPlayer.play(url)
                    }
                }
                viewModel.clearMatchedWord()
            }
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
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                if (viewModel.isWin) {
                    WinDialog(
                        moves = viewModel.moves,
                        onPlayAgain = { viewModel.resetGame() },
                        onBack = { navController.popBackStack() }
                    )
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
            
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "بازگشت",
                    tint = Purple40
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                emoji = "🎯",
                label = "حرکت‌ها",
                value = moves.toString(),
                color = SkyBlue
            )
            StatCard(
                emoji = "✅",
                label = "جفت‌ها",
                value = "$matchedPairs / $totalPairs",
                color = MintGreen
            )
        }

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
private fun StatCard(
    emoji: String,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
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

@Composable
private fun WinDialog(
    moves: Int,
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
                    text = "🎉",
                    fontSize = 64.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "آفرین! بردی!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = "تو با $moves حرکت همه جفت‌ها رو پیدا کردی!",
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
