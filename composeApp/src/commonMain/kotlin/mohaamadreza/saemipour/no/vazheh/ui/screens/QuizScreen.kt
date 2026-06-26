package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import mohaamadreza.saemipour.no.vazheh.data.ChildProgressStatsDTO
import mohaamadreza.saemipour.no.vazheh.data.QuizOptionDTO
import mohaamadreza.saemipour.no.vazheh.data.RecentQuizAttemptDTO
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.components.WinCelebrationOverlay
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple40
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple80
import mohaamadreza.saemipour.no.vazheh.ui.theme.SkyBlue
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.QuizViewModel

/**
 * تبدیل اعداد انگلیسی به اعداد فارسی
 */
private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.map { ch ->
        if (ch in '0'..'9') persianDigits[ch - '0'] else ch
    }.joinToString("")
}

private fun Int.toPersianDigits(): String = this.toString().toPersianDigits()

/**
 * Quiz Screen - صفحه آزمون
 * صوت کلمه پخش می‌شود و فرزند کلمه درست را انتخاب می‌کند
 */
@Composable
fun QuizScreen(
    navController: NavController,
    viewModel: QuizViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }

    val audioUpdates = remember {
        object : AudioUpdates {
            override fun onProgressUpdate(playerState: PlayerState) {
                isPlaying = playerState.isPlaying
            }

            override fun onReady() {}
            override fun onError(exception: Exception) {
                isPlaying = false
            }
        }
    }

    AudioProvider(audioUpdates) { player ->

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
                    uiState.isLoadingQuestions -> {
                        LoadingContent()
                    }

                    uiState.errorMessage != null -> {
                        ErrorContent(
                            message = uiState.errorMessage ?: "",
                            onRetry = { viewModel.retry() },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    uiState.isQuizCompleted -> {
                        QuizCompletedContent(
                            totalCorrect = uiState.totalCorrect,
                            totalAnswered = uiState.totalAnswered,
                            scorePercent = uiState.scorePercent,
                            progressStats = if (uiState.hasChildId) uiState.progressStats else null,
                            recentAttempts = if (uiState.hasChildId) uiState.recentAttempts else emptyList(),
                            onPlayAgain = { viewModel.retry() },
                            onBack = {
                                viewModel.resetQuiz()
                                navController.popBackStack()
                            }
                        )
                    }

                    uiState.currentQuestion != null -> {
                        // Slide each new question in while the previous one slides out.
                        AnimatedContent(
                            targetState = uiState.currentQuestionIndex,
                            transitionSpec = {
                                (slideInHorizontally(tween(350)) { it } + fadeIn(tween(350))) togetherWith
                                        (slideOutHorizontally(tween(350)) { -it } + fadeOut(
                                            tween(
                                                350
                                            )
                                        ))
                            },
                            label = "question"
                        ) { _ ->
                            QuizContent(
                                progressText = uiState.progressText,
                                progressPercent = (uiState.currentQuestionIndex + 1).toFloat() / uiState.questions.size,
                                options = uiState.currentQuestion?.options ?: emptyList(),
                                correctWordId = uiState.currentQuestion?.wordId ?: 0,
                                selectedOptionId = uiState.selectedOptionId,
                                isAnswerSubmitted = uiState.isAnswerSubmitted,
                                lastAnswerCorrect = uiState.lastAnswerCorrect,
                                correctWordFa = uiState.correctWordFa,
                                canSubmitAnswer = uiState.canSubmitAnswer,
                                canGoToNextQuestion = uiState.canGoToNextQuestion,
                                hasMoreQuestions = uiState.hasMoreQuestions,
                                isSubmittingAnswer = uiState.isSubmittingAnswer,
                                isPlaying = isPlaying,
                                onPlayAudio = {
                                    val audioUrl = uiState.currentQuestion?.audioUrl
                                    if (!audioUrl.isNullOrEmpty()) {
                                        player.play(audioUrl)
                                    }
                                },
                                onStopAudio = { player.pause() },
                                onOptionSelected = { viewModel.selectOption(it) },
                                onSubmitAnswer = { viewModel.submitAnswer() },
                                onNextQuestion = { viewModel.nextQuestion() },
                                onFinishQuiz = { viewModel.finishQuiz() },
                                onBack = {
                                    player.pause()
                                    viewModel.resetQuiz()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    else -> {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "سوالی برای نمایش وجود ندارد",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
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
                text = "در حال آماده‌سازی آزمون...",
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
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = CoralRed,
                modifier = Modifier.size(64.dp)
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
private fun QuizCompletedContent(
    totalCorrect: Int,
    totalAnswered: Int,
    scorePercent: Int,
    progressStats: ChildProgressStatsDTO?,
    recentAttempts: List<RecentQuizAttemptDTO>,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    val scoreColor = when {
        scorePercent >= 80 -> MintGreen
        scorePercent >= 50 -> SkyBlue
        else -> CoralRed
    }

    val emoji = when {
        scorePercent >= 80 -> "🎉"
        scorePercent >= 50 -> "👍"
        else -> "💪"
    }

    val title = when {
        scorePercent == 100 -> "آفرین قهرمان!"
        scorePercent >= 80 -> "عالی بود!"
        scorePercent >= 50 -> "خوب بود!"
        else -> "ادامه بده!"
    }

    val message = when {
        scorePercent == 100 -> "همه پاسخ‌ها درست بود!"
        scorePercent >= 80 -> "نتیجه خیلی خوبی گرفتی!"
        scorePercent >= 50 -> "می‌تونی بهتر هم بشی!"
        else -> "دفعه بعد بهتر می‌شه!"
    }

    val stars = when {
        scorePercent >= 80 -> 3
        scorePercent >= 50 -> 2
        scorePercent >= 25 -> 1
        else -> 0
    }

    // Animated score counter
    val animatedScore by animateFloatAsState(
        targetValue = scorePercent.toFloat(),
        animationSpec = tween(durationMillis = 1500),
        label = "score"
    )

    // Pulse animation for emoji
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Rotation for decorative element
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        scoreColor.copy(alpha = 0.25f),
                        Purple80.copy(alpha = 0.2f),
                        SkyBlue.copy(alpha = 0.15f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Celebration animation behind the result, only for winning scores
        if (scorePercent >= 80) {
            WinCelebrationOverlay()
        }

        // Decorative rotating background circle
        Box(
            modifier = Modifier
                .size(360.dp)
                .rotate(rotation)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            scoreColor.copy(alpha = 0.08f),
                            Color.Transparent,
                            scoreColor.copy(alpha = 0.12f),
                            Color.Transparent,
                            scoreColor.copy(alpha = 0.08f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(36.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    scoreColor.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Animated emoji
                    Text(
                        text = emoji,
                        fontSize = 90.sp,
                        modifier = Modifier.scale(pulseScale)
                    )

                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = scoreColor
                    )

                    // Stars row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) { index ->
                            val isFilled = index < stars
                            val starScale by animateFloatAsState(
                                targetValue = if (isFilled) 1f else 0.7f,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    delayMillis = 200 + index * 200
                                ),
                                label = "starScale$index"
                            )
                            Text(
                                text = if (isFilled) "⭐" else "☆",
                                fontSize = 44.sp,
                                modifier = Modifier.scale(starScale),
                                color = if (isFilled) scoreColor else Color.Gray.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // Message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = DarkText.copy(alpha = 0.8f)
                    )

                    // Score circle with gradient
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        scoreColor.copy(alpha = 0.2f),
                                        scoreColor.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 5.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        scoreColor,
                                        scoreColor.copy(alpha = 0.6f),
                                        scoreColor
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${animatedScore.toInt().toPersianDigits()}٪",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor
                            )
                            Text(
                                text = "امتیاز",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatChip(
                            modifier = Modifier.weight(1f),
                            label = "درست",
                            value = totalCorrect.toPersianDigits(),
                            color = MintGreen,
                            icon = "✅"
                        )
                        StatChip(
                            modifier = Modifier.weight(1f),
                            label = "اشتباه",
                            value = (totalAnswered - totalCorrect).toPersianDigits(),
                            color = CoralRed,
                            icon = "❌"
                        )
                        StatChip(
                            modifier = Modifier.weight(1f),
                            label = "کل",
                            value = totalAnswered.toPersianDigits(),
                            color = SkyBlue,
                            icon = "📊"
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onPlayAgain,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple40
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "بازی مجدد",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Purple40
                            ),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Purple40)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "بازگشت",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Learning progress summary (logged-in child only)
            if (progressStats != null) {
                ProgressSummaryCard(progressStats)
            }
            if (recentAttempts.isNotEmpty()) {
                RecentAttemptsCard(recentAttempts)
            }
        }
    }
}

@Composable
private fun ProgressSummaryCard(stats: ChildProgressStatsDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(0.92f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📈 پیشرفت ${stats.childName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    modifier = Modifier.weight(1f),
                    label = "یاد گرفته",
                    value = "${stats.learnedWords.toPersianDigits()}/${stats.totalWords.toPersianDigits()}",
                    color = MintGreen,
                    icon = "🎓"
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    label = "در حال یادگیری",
                    value = stats.inProgressWords.toPersianDigits(),
                    color = SkyBlue,
                    icon = "📚"
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    label = "دقت کلی",
                    value = "${stats.overallAccuracyPercent.toInt().toPersianDigits()}٪",
                    color = Purple40,
                    icon = "🎯"
                )
            }
        }
    }
}

@Composable
private fun RecentAttemptsCard(attempts: List<RecentQuizAttemptDTO>) {
    Card(
        modifier = Modifier.fillMaxWidth(0.92f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🕑 آزمون‌های اخیر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            attempts.take(10).forEach { attempt ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (attempt.isCorrect) "✅" else "❌",
                        fontSize = 18.sp
                    )
                    Text(
                        text = attempt.wordFa,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = DarkText,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )
                    Text(
                        text = if (attempt.isCorrect) "درست" else "اشتباه",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (attempt.isCorrect) MintGreen else CoralRed
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    icon: String
) {
    Column(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                color = color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
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

@Composable
private fun QuizContent(
    progressText: String,
    progressPercent: Float,
    options: List<QuizOptionDTO>,
    correctWordId: Int,
    selectedOptionId: Int?,
    isAnswerSubmitted: Boolean,
    lastAnswerCorrect: Boolean?,
    correctWordFa: String?,
    canSubmitAnswer: Boolean,
    canGoToNextQuestion: Boolean,
    hasMoreQuestions: Boolean,
    isSubmittingAnswer: Boolean,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onOptionSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onFinishQuiz: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with progress and back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "سوال ${progressText.toPersianDigits()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Purple40,
                    trackColor = Purple40.copy(alpha = 0.2f)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Back button
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.rotate(180f),
                    contentDescription = "بازگشت",
                    tint = Purple40
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Play audio button
        PlayAudioButton(
            isPlaying = isPlaying,
            onClick = { if (isPlaying) onStopAudio() else onPlayAudio() }
        )

        Text(
            text = "🔊 صدا را گوش کن و تصویر درست را انتخاب کن!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Options grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(options) { option ->
                val isSelected = selectedOptionId == option.wordId
                val isCorrect = option.wordId == correctWordId
                val showResult = isAnswerSubmitted

                OptionCard(
                    option = option,
                    isSelected = isSelected,
                    showResult = showResult,
                    isCorrectAnswer = isCorrect,
                    onClick = {
                        if (!isAnswerSubmitted) {
                            onOptionSelected(option.wordId)
                        }
                    }
                )
            }
        }

        // Feedback message
        AnimatedVisibility(
            visible = isAnswerSubmitted && lastAnswerCorrect != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FeedbackMessage(
                isCorrect = lastAnswerCorrect == true,
                correctWordFa = correctWordFa
            )
        }

        Spacer(Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            when {
                isSubmittingAnswer -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Purple40
                    )
                }

                canSubmitAnswer -> {
                    Button(
                        onClick = onSubmitAnswer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple40
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "تأیید پاسخ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                isAnswerSubmitted -> {
                    Button(
                        onClick = if (hasMoreQuestions) onNextQuestion else onFinishQuiz,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasMoreQuestions) SkyBlue else MintGreen
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (hasMoreQuestions) "سوال بعدی" else "پایان آزمون",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            modifier = Modifier.rotate(180f),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayAudioButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isPlaying) MintGreen else Purple40,
                        if (isPlaying) MintGreen.copy(alpha = 0.7f) else Purple80
                    )
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "پخش صدا",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun OptionCard(
    option: QuizOptionDTO,
    isSelected: Boolean,
    showResult: Boolean,
    isCorrectAnswer: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        showResult && isCorrectAnswer -> MintGreen
        showResult && isSelected && !isCorrectAnswer -> CoralRed
        isSelected -> Purple40
        else -> Color.Transparent
    }

    val backgroundColor = when {
        showResult && isCorrectAnswer -> MintGreen.copy(alpha = 0.1f)
        showResult && isSelected && !isCorrectAnswer -> CoralRed.copy(alpha = 0.1f)
        isSelected -> Purple40.copy(alpha = 0.1f)
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .border(
                width = 3.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = !showResult, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                // Image
                if (!option.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(option.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = option.wordFa,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.height(10.dp))
                }

                // Word text
                Text(
                    text = option.wordFa,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = option.wordEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Result icon overlay
            if (showResult) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    when {
                        isCorrectAnswer -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "درست",
                                tint = MintGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        isSelected && !isCorrectAnswer -> {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "اشتباه",
                                tint = CoralRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackMessage(
    isCorrect: Boolean,
    correctWordFa: String?
) {
    val backgroundColor = if (isCorrect) MintGreen else CoralRed
    val message = if (isCorrect) {
        "🎉 آفرین! پاسخ درست بود!"
    } else {
        "❌ اشتباه بود! پاسخ درست: $correctWordFa"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = backgroundColor,
            textAlign = TextAlign.Center
        )
    }
}






