package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import mohaamadreza.saemipour.no.vazheh.data.QuizOptionDTO
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple40
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple80
import mohaamadreza.saemipour.no.vazheh.ui.theme.SkyBlue
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.QuizViewModel

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
                            onPlayAgain = { viewModel.retry() },
                            onBack = { 
                                viewModel.resetQuiz()
                                navController.popBackStack() 
                            }
                        )
                    }
                    uiState.currentQuestion != null -> {
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
    
    val message = when {
        scorePercent == 100 -> "عالی! همه پاسخ‌ها درست بود!"
        scorePercent >= 80 -> "آفرین! خیلی خوب بود!"
        scorePercent >= 50 -> "خوب بود! ادامه بده!"
        else -> "تلاش کن! دفعه بعد بهتر می‌شه!"
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Emoji
                Text(
                    text = emoji,
                    fontSize = 80.sp
                )
                
                // Message
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = scoreColor
                )
                
                // Score circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = scoreColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .border(
                            width = 4.dp,
                            color = scoreColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$scorePercent%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = "$totalCorrect از $totalAnswered",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("بازگشت")
                    }
                    
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple40
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("بازی مجدد")
                    }
                }
            }
        }
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
                    text = "سوال $progressText",
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
            .height(180.dp)
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
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                }
                
                // Word text
                Text(
                    text = option.wordFa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = option.wordEn,
                    style = MaterialTheme.typography.bodySmall,
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





