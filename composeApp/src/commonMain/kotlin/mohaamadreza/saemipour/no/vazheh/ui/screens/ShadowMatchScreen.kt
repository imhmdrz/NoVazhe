package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import mohaamadreza.saemipour.no.vazheh.data.WordDTO
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.GameSounds
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.components.DashboardButton
import mohaamadreza.saemipour.no.vazheh.ui.components.LevelUpCelebration
import mohaamadreza.saemipour.no.vazheh.ui.components.WinCelebration
import mohaamadreza.saemipour.no.vazheh.ui.components.backToDashboard
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple80
import mohaamadreza.saemipour.no.vazheh.ui.theme.SkyBlue
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel

private val ShadowAccent = Color(0xFF4B3F72)
private const val UNMATCHED_NUMBER_SHADOW_ALPHA = 0.45f
private const val LEVEL_UP_NORMAL_CELEBRATION_MS = 2_250L
private enum class ShadowCelebrationStage { None, Normal, LevelUp, Final }

@Composable
fun ShadowMatchScreen(
    navController: NavController,
    viewModel: ShadowMatchViewModel,
    childViewModel: ChildViewModel,
    categoryId: Int
) {
    val childUiState by childViewModel.uiState.collectAsState()
    val audioUpdates = remember { object : AudioUpdates {
        override fun onProgressUpdate(state: PlayerState) = Unit
        override fun onReady() = Unit
        override fun onError(exception: Exception) = Unit
    } }
    LaunchedEffect(categoryId, childUiState.selectedChild?.id) {
        if (categoryId == COMBINED_CATEGORY_ID) viewModel.loadCombinedWords(childUiState.selectedChild?.id)
        else viewModel.loadWords(categoryId, childUiState.selectedChild?.id)
    }

    AudioProvider(audioUpdates) { audioPlayer ->
        DisposableEffect(Unit) { onDispose { audioPlayer.cleanUp() } }

        LaunchedEffect(viewModel.showWrongFeedback) {
            if (viewModel.showWrongFeedback) {
                audioPlayer.playAndAwait(GameSounds.wrong)
                viewModel.clearWrongFeedback()
            }
        }
        LaunchedEffect(viewModel.lastCorrectWord) {
            viewModel.lastCorrectWord?.let { word ->
                audioPlayer.playAndAwait(GameSounds.correct)
                word.audioUrl?.takeIf { it.isNotBlank() }?.let { audioPlayer.playAndAwait(it) }
                viewModel.finishCorrectFeedback()
            }
        }

        var showWin by remember { mutableStateOf(false) }
        var celebrationStage by remember { mutableStateOf(ShadowCelebrationStage.None) }
        LaunchedEffect(viewModel.isWin) {
            if (!viewModel.isWin) { showWin = false; celebrationStage = ShadowCelebrationStage.None }
            else { audioPlayer.play(GameSounds.win); showWin = true; celebrationStage = ShadowCelebrationStage.Normal }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                Purple80.copy(alpha = .3f), SkyBlue.copy(alpha = .2f), MintGreen.copy(alpha = .2f)
            )))) {
                when {
                    viewModel.isLoading -> LoadingContent()
                    viewModel.errorMessage != null -> ErrorContent(viewModel.errorMessage.orEmpty(), viewModel::retry) { navController.popBackStack() }
                    !showWin && viewModel.targets.isNotEmpty() -> ShadowMatchContent(viewModel, { navController.backToDashboard() }, viewModel::resetGame)
                }
                if (showWin) when (celebrationStage) {
                    ShadowCelebrationStage.Normal -> WinCelebration(
                        timeoutMillis = if (
                            viewModel.isLevelUpWin || viewModel.isAllLevelsCompletedWin
                        ) LEVEL_UP_NORMAL_CELEBRATION_MS else 8_000L,
                        onTap = {
                            when {
                                viewModel.isAllLevelsCompletedWin ->
                                    celebrationStage = ShadowCelebrationStage.Final
                                viewModel.isLevelUpWin ->
                                    celebrationStage = ShadowCelebrationStage.LevelUp
                                else -> viewModel.resetGame()
                            }
                        }
                    )
                    ShadowCelebrationStage.LevelUp -> LevelUpCelebration(onTap = viewModel::resetGame)
                    ShadowCelebrationStage.Final -> LevelUpCelebration(
                        title = "همه‌ی مرحله‌ها تموم شد!",
                        subtitle = "آفرین قهرمان! می‌تونی دوباره بازی کنی",
                        onTap = viewModel::resetGame
                    )
                    ShadowCelebrationStage.None -> Unit
                }
            }
        }
    }
}

@Composable private fun LoadingContent() = Box(Modifier.fillMaxSize(), Alignment.Center) {
    CircularProgressIndicator(color = ShadowAccent, modifier = Modifier.size(64.dp))
}

@Composable private fun ErrorContent(message: String, onRetry: () -> Unit, onBack: () -> Unit) = Box(Modifier.fillMaxSize(), Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
        Text("😕", fontSize = 64.sp)
        Text(message, color = CoralRed, textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { TextButton(onClick = onBack) { Text("بازگشت") }; Button(onClick = onRetry) { Text("تلاش مجدد") } }
    }
}

@Composable private fun ShadowMatchContent(viewModel: ShadowMatchViewModel, onBack: () -> Unit, onResetGame: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Header(viewModel.currentLevel, viewModel.currentSublevel, onBack)
        Spacer(Modifier.height(10.dp)); StarsRow(viewModel.stars); Spacer(Modifier.height(10.dp))
        Text("سایه‌ها مال کدومن؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ShadowAccent)
        Spacer(Modifier.height(10.dp))
        TargetsGrid(viewModel.targets, viewModel.matchedTargetIds, viewModel.numberTargetIds)
        Spacer(Modifier.height(if (viewModel.currentLevel == 3) 24.dp else 14.dp))
        OptionsGrid(viewModel.options, viewModel.matchedTargetIds, viewModel.correctOptionId, viewModel.wrongOptionId, viewModel.isInputLocked, viewModel::onOptionSelected, Modifier.weight(1f))
        Spacer(Modifier.height(10.dp))
        Button(onClick = onResetGame, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = ShadowAccent), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text("شروع دوباره", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun Header(level: Int, sublevel: Int, onBack: () -> Unit) = Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
    Column { Text("بازی سایه‌ها", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ShadowAccent); Text("سطح $level • مرحله $sublevel از 3", color = Color.Gray) }
    DashboardButton(onClick = onBack, tint = ShadowAccent)
}

@Composable private fun StarsRow(stars: Int) = Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107).copy(alpha = .08f))) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.Center) {
        repeat(3) { index -> Text(if (index < stars) "⭐" else "☆", fontSize = 26.sp, modifier = Modifier.padding(horizontal = 3.dp)) }
    }
}

@Composable private fun TargetsGrid(
    targets: List<WordDTO>,
    matchedIds: Set<Int>,
    numberTargetIds: Set<Int>
) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val gap = 8.dp
        val cardSize = when (targets.size) {
            1 -> minOf(maxWidth, 180.dp)
            2 -> minOf((maxWidth - gap) / 2, 180.dp)
            3 -> minOf((maxWidth - gap * 2) / 3, 120.dp)
            else -> minOf((maxWidth - gap) / 2, 140.dp)
        }
        when (targets.size) {
            1 -> ShadowCard(
                targets.first(),
                targets.first().id in matchedIds,
                targets.first().id in numberTargetIds,
                Modifier.size(cardSize)
            )
            2, 3 -> Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                targets.forEach { target ->
                    ShadowCard(
                        target,
                        target.id in matchedIds,
                        target.id in numberTargetIds,
                        Modifier.size(cardSize)
                    )
                }
            }
            else -> Column(
                verticalArrangement = Arrangement.spacedBy(gap),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                targets.chunked(2).forEach { rowTargets ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowTargets.forEach { target ->
                            ShadowCard(
                                target,
                                target.id in matchedIds,
                                target.id in numberTargetIds,
                                Modifier.size(cardSize)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ShadowCard(
    target: WordDTO,
    matched: Boolean,
    isNumberTarget: Boolean,
    modifier: Modifier
) {
    val scale by animateFloatAsState(if (matched) 1.05f else 1f, tween(250), label = "shadowScale")
    Card(
        modifier.scale(scale),
        RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (matched) MintGreen.copy(alpha = .12f) else Color.White
        ),
        border = BorderStroke(if (matched) 3.dp else 1.5.dp, if (matched) MintGreen else ShadowAccent.copy(alpha = .3f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (matched) {
                            listOf(MintGreen.copy(alpha = .04f), MintGreen.copy(alpha = .14f))
                        } else {
                            listOf(ShadowAccent.copy(alpha = .05f), ShadowAccent.copy(alpha = .18f))
                        }
                    )
                )
                .padding(10.dp),
            Alignment.Center
        ) {
        Crossfade(matched, animationSpec = tween(250), label = "shadowReveal") { isMatched ->
            AsyncImage(ImageRequest.Builder(LocalPlatformContext.current).data(target.imageUrl).crossfade(true).build(), target.wordFa, Modifier.fillMaxSize().alpha(if (!isMatched && isNumberTarget) UNMATCHED_NUMBER_SHADOW_ALPHA else 1f), contentScale = ContentScale.Fit,
                colorFilter = if (isMatched) null else ColorFilter.tint(ShadowAccent, BlendMode.SrcIn))
        }
            if (matched) FeedbackBadge(isCorrect = true)
        }
    }
}

@Composable private fun OptionsGrid(options: List<WordDTO>, matchedIds: Set<Int>, correctId: Int?, wrongId: Int?, inputLocked: Boolean, onClick: (WordDTO) -> Unit, modifier: Modifier) = LazyVerticalGrid(
    columns = GridCells.Fixed(if (options.size <= 4) 2 else 3), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier.fillMaxWidth()
) { items(options, key = { it.id }) { word ->
    OptionCard(word, word.id in matchedIds || correctId == word.id, wrongId == word.id, inputLocked || word.id in matchedIds) { onClick(word) }
} }

@Composable private fun OptionCard(word: WordDTO, isCorrect: Boolean, isWrong: Boolean, locked: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isCorrect) 1.05f else if (isWrong) .95f else 1f, tween(250), label = "optionScale")
    val border = when { isCorrect -> MintGreen; isWrong -> CoralRed; else -> ShadowAccent.copy(alpha = .3f) }
    Card(Modifier.aspectRatio(1f).scale(scale).clickable(enabled = !locked, onClick = onClick), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = when { isCorrect -> MintGreen.copy(alpha = .12f); isWrong -> CoralRed.copy(alpha = .12f); else -> Color.White }), border = BorderStroke(if (isCorrect || isWrong) 3.dp else 1.5.dp, border)) {
        Box(Modifier.fillMaxSize().padding(8.dp), Alignment.Center) {
            AsyncImage(ImageRequest.Builder(LocalPlatformContext.current).data(word.imageUrl).crossfade(true).build(), word.wordFa, Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
            if (isCorrect || isWrong) FeedbackBadge(isCorrect)
        }
    }
}

@Composable
private fun BoxScope.FeedbackBadge(isCorrect: Boolean) {
    Box(
        Modifier
            .align(Alignment.BottomEnd)
            .padding(4.dp)
            .size(32.dp)
            .background((if (isCorrect) MintGreen else CoralRed).copy(alpha = .95f), CircleShape)
            .border(2.dp, Color.White, CircleShape),
        Alignment.Center
    ) {
        Text(if (isCorrect) "✅" else "❌", fontSize = 16.sp)
    }
}

internal val ShadowGameAccent: Color = ShadowAccent
