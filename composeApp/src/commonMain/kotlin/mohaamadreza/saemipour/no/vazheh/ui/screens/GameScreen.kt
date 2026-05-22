package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple40
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple80
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.background
import novazheh.composeapp.generated.resources.button
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GameScreen(navController: NavController, viewModel: ChildViewModel) {
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

    // Start timer when entering the game screen
    LaunchedEffect(Unit) {
        viewModel.resetTimerForNewSession()
        viewModel.startTimer()
    }

    // Handle timer finish - navigate back to mother screen
    LaunchedEffect(uiState.isTimerFinished) {
        if (uiState.isTimerFinished) {
            viewModel.clearTimerFinished()
            viewModel.stopTimer()
            // Navigate back to mother screen
            navController.navigate("mother") {
                popUpTo("mother") { inclusive = true }
            }
        }
    }

    AudioProvider(audioUpdates) { player ->
        // Auto-play audio when word changes
        val currentWord = viewModel.getCurrentWord()
        LaunchedEffect(uiState.currentWordIndex) {
            player.pause()
            currentWord?.audioUrl?.let {
                player.play(it)
            }
        }
        
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background
                Image(
                    painter = painterResource(Res.drawable.background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().zIndex(-1f),
                    contentScale = ContentScale.Crop,
                )

                // Timer display at top-right corner
                if (uiState.isTimerEnabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(
                                color = if (uiState.remainingTimeSeconds <= 60) CoralRed else Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .zIndex(10f)
                    ) {
                        Text(
                            text = "⏱️ ${uiState.formattedRemainingTime}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }

                when {
                    uiState.isLoadingWords -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Purple80
                            )
                        }
                    }
                    uiState.errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    uiState.words.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "کلمه‌ای در این دسته‌بندی یافت نشد",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )
                        }
                    }
                    else -> {
                        // Main content container with fixed button positions
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            // Previous button - fixed at start (right in RTL)
                            Image(
                                painterResource(Res.drawable.button),
                                contentDescription = "قبلی",
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(bottom = 155.dp, start = 20.dp)
                                    .rotate(180f)
                                    .alpha(if (viewModel.canGoPrevious()) 1f else 0.3f)
                                    .clickable(enabled = viewModel.canGoPrevious()) {
                                        player.pause()
                                        viewModel.previousWord()
                                    }
                            )


                            // Next button - fixed at end (left in RTL)
                            Image(
                                painterResource(Res.drawable.button),
                                contentDescription = "بعدی",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 155.dp, end = 20.dp)
                                    .alpha(if (viewModel.canGoNext()) 1f else 0.3f)
                                    .clickable(enabled = viewModel.canGoNext()) {
                                        player.pause()
                                        viewModel.nextWord()
                                    }
                            )

                            // Word content - always centered
                            currentWord?.let { word ->
                                // اگر دسته اعداد بود، پس‌زمینه خاکستری برای تصاویر سفید
                                val category = uiState.selectedCategory
                                val isNumbersCategory = category != null && (
                                    category.nameEn.equals("Numbers", ignoreCase = true) ||
                                        category.nameEn.equals("Number", ignoreCase = true) ||
                                        category.nameFa.contains("عدد") ||
                                        category.nameFa.contains("اعداد")
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.align(Alignment.Center).clickable {
                                        if (!word.audioUrl.isNullOrEmpty()) {
                                            if (isPlaying) {
                                                player.pause()
                                            } else {
                                                player.play(word.audioUrl!!)
                                            }
                                        }
                                    }
                                ) {
                                    val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                                        .data(word.imageUrl)
                                        .crossfade(true)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .diskCacheKey(word.wordFa)
                                        .build()

                                    val painter = rememberAsyncImagePainter(
                                        model = imageRequest
                                    )

                                    if (isNumbersCategory) {
                                        Box(
                                            modifier = Modifier
                                                .heightIn(max = 250.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(Color(0xFF9E9E9E))
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painter,
                                                contentDescription = null,
                                                modifier = Modifier.heightIn(max = 226.dp)
                                            )
                                        }
                                    } else {
                                        Image(
                                            painter = painter,
                                            contentDescription = null,
                                            modifier = Modifier.heightIn(max = 250.dp).clip(RoundedCornerShape(24.dp))
                                        )
                                    }

                                    Spacer(modifier = Modifier.size(12.dp))


                                    // Word in Persian - clickable to play audio
                                    Text(
                                        text = word.wordFa,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                    )

                                    Spacer(modifier = Modifier.size(12.dp))

                                    // Audio play/pause button
                                    if (!word.audioUrl.isNullOrEmpty()) {
                                        GameAudioButton(
                                            isPlaying = isPlaying,
                                            onClick = {
                                                if (isPlaying) {
                                                    player.pause()
                                                } else {
                                                    player.play(word.audioUrl!!)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Audio play/pause button for GameScreen
 */
@Composable
private fun GameAudioButton(
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
            contentDescription = if (isPlaying) "توقف صدا" else "پخش صدا",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}
