package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import mohaamadreza.saemipour.no.vazheh.data.WordDTO
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.components.DashboardButton
import mohaamadreza.saemipour.no.vazheh.ui.components.LoseOverlay
import mohaamadreza.saemipour.no.vazheh.ui.components.WinCelebration
import mohaamadreza.saemipour.no.vazheh.ui.components.backToDashboard
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple80
import mohaamadreza.saemipour.no.vazheh.ui.theme.SkyBlue

/** رنگ اختصاصی بازی «کدام متفاوت است؟» (نارنجی گرم) */
private val OddOneOutAccent = Color(0xFFE67E22)

@Composable
fun OddOneOutScreen(
    navController: NavController,
    viewModel: OddOneOutViewModel
) {
    val audioUpdates = remember {
        object : AudioUpdates {
            override fun onProgressUpdate(state: PlayerState) {}
            override fun onReady() {}
            override fun onError(exception: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.options.isEmpty() && viewModel.errorMessage == null && !viewModel.isLoading) {
            viewModel.load(rounds = 5)
        }
    }

    // پاسخ اشتباه: بعد از نمایش بازخورد، خودکار به دور بعد می‌رویم
    LaunchedEffect(viewModel.showWrongFeedback) {
        if (viewModel.showWrongFeedback) {
            delay(1100)
            viewModel.clearWrongFeedback()
            if (!viewModel.isGameOver) viewModel.advanceToNextRound()
        }
    }

    AudioProvider(audioUpdates = audioUpdates) { audioPlayer ->
        DisposableEffect(Unit) {
            onDispose { audioPlayer.cleanUp() }
        }

        // پخش صدای کلمه‌ی متفاوت در صورت پاسخ صحیح، سپس رفتن به دور بعد
        // نکته: پاک کردن lastCorrectWord باید «بعد» از delay باشد، وگرنه تغییر کلیدِ
        // LaunchedEffect همین کوروتین را وسط delay لغو می‌کند و هرگز به دور بعد نمی‌رویم.
        LaunchedEffect(viewModel.lastCorrectWord) {
            viewModel.lastCorrectWord?.let { word ->
                word.audioUrl?.let { url ->
                    if (url.isNotEmpty()) audioPlayer.play(url)
                }
                delay(1100)
                viewModel.clearLastCorrectWord()
                if (!viewModel.isGameOver) viewModel.advanceToNextRound()
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Purple80.copy(alpha = 0.18f),
                                OddOneOutAccent.copy(alpha = 0.12f),
                                SkyBlue.copy(alpha = 0.12f)
                            )
                        )
                    )
            ) {
                when {
                    viewModel.isLoading -> LoadingContent()
                    viewModel.errorMessage != null -> ErrorContent(
                        message = viewModel.errorMessage ?: "",
                        onRetry = { viewModel.retry() },
                        onBack = { navController.popBackStack() }
                    )
                    viewModel.options.isNotEmpty() -> OddOneOutContent(
                        viewModel = viewModel,
                        onBack = { navController.backToDashboard() },
                        onResetGame = { viewModel.resetGame() }
                    )
                }

                if (viewModel.isWin) {
                    // فقط انیمیشن برد؛ لمس صفحه = شروع دوباره بازی
                    WinCelebration(onTap = { viewModel.resetGame() })
                }

                if (viewModel.isLose) {
                    // ستاره‌ی کافی جمع نشد؛ لمس صفحه یا دکمه = تلاش دوباره
                    LoseOverlay(
                        stars = viewModel.stars,
                        totalRounds = viewModel.totalRounds,
                        onTap = { viewModel.resetGame() }
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
            CircularProgressIndicator(color = OddOneOutAccent, modifier = Modifier.size(64.dp))
            Text(
                text = "در حال آماده‌سازی بازی...",
                style = MaterialTheme.typography.titleMedium,
                color = OddOneOutAccent
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
            Text(text = "😕", fontSize = 64.sp)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = CoralRed,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(onClick = onBack) { Text("بازگشت") }
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
private fun OddOneOutContent(
    viewModel: OddOneOutViewModel,
    onBack: () -> Unit,
    onResetGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(
            currentRound = viewModel.currentRound,
            totalRounds = viewModel.totalRounds,
            onBack = onBack
        )

        Spacer(Modifier.height(12.dp))

        StarsRow(stars = viewModel.stars, totalRounds = viewModel.totalRounds)

        Spacer(Modifier.height(16.dp))

        Text(
            text = "کدوم متفاوته؟",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = OddOneOutAccent
        )

        viewModel.mainCategoryNameFa?.let { name ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = "۳ تای دیگه از دسته «$name» هستن",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(20.dp))

        OptionsGrid(
            options = viewModel.options,
            correctOptionId = viewModel.correctOptionId,
            wrongOptionId = viewModel.wrongOptionId,
            onOptionClick = { viewModel.onOptionSelected(it) },
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onResetGame,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OddOneOutAccent),
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
private fun Header(
    currentRound: Int,
    totalRounds: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "🔍 کدام متفاوت است؟",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OddOneOutAccent
            )
            Text(
                text = "اونی که با بقیه فرق داره رو پیدا کن!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (totalRounds > 0) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = OddOneOutAccent.copy(alpha = 0.12f)
                    )
                ) {
                    Text(
                        text = "$currentRound / $totalRounds",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OddOneOutAccent
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            DashboardButton(onClick = onBack, tint = OddOneOutAccent)
        }
    }
}

@Composable
private fun StarsRow(
    stars: Int,
    totalRounds: Int
) {
    val goldColor = Color(0xFFFFC107)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = goldColor.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalRounds) { index ->
                val isEarned = index < stars
                val starScale by animateFloatAsState(
                    targetValue = if (isEarned) 1f else 0.75f,
                    animationSpec = tween(durationMillis = 350),
                    label = "starScale$index"
                )
                Text(
                    text = if (isEarned) "⭐" else "☆",
                    fontSize = 26.sp,
                    color = if (isEarned) goldColor else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .scale(starScale)
                )
            }
        }
    }
}

@Composable
private fun OptionsGrid(
    options: List<WordDTO>,
    correctOptionId: Int?,
    wrongOptionId: Int?,
    onOptionClick: (WordDTO) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(options, key = { it.id }) { word ->
            OptionCard(
                word = word,
                isCorrect = correctOptionId == word.id,
                isWrong = wrongOptionId == word.id,
                isLocked = correctOptionId != null,
                onClick = { onOptionClick(word) }
            )
        }
    }
}

@Composable
private fun OptionCard(
    word: WordDTO,
    isCorrect: Boolean,
    isWrong: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isCorrect -> MintGreen
        isWrong -> CoralRed
        else -> OddOneOutAccent.copy(alpha = 0.3f)
    }
    val borderWidth = if (isCorrect || isWrong) 3.dp else 1.5.dp

    val scale by animateFloatAsState(
        targetValue = when {
            isCorrect -> 1.05f
            isWrong -> 0.95f
            else -> 1f
        },
        animationSpec = tween(250),
        label = "optionScale"
    )

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .clickable(enabled = !isLocked) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCorrect -> MintGreen.copy(alpha = 0.12f)
                isWrong -> CoralRed.copy(alpha = 0.12f)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!word.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(word.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = word.wordFa,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = word.wordFa,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            if (isCorrect) {
                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    FeedbackBadge(emoji = "✅", background = MintGreen)
                }
            } else if (isWrong) {
                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    FeedbackBadge(emoji = "❌", background = CoralRed)
                }
            }
        }
    }
}

@Composable
private fun FeedbackBadge(emoji: String, background: Color) {
    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(36.dp)
            .background(background.copy(alpha = 0.95f), CircleShape)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 18.sp)
    }
}

/** Re-export accent for ChildScreen card to keep colors in sync */
internal val OddOneOutGameAccent: Color = OddOneOutAccent
