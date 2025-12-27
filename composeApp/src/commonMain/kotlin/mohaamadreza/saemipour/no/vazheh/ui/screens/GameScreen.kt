package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
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
import mohaamadreza.saemipour.no.vazheh.ui.theme.BlackAlpha
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
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

    BackHandler {
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
                    val currentWord = viewModel.getCurrentWord()

                    // Main content container with fixed button positions
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(100.dp)
                    ) {
                        // Previous button - fixed at start (right in RTL)
                        Image(
                            painterResource(Res.drawable.button),
                            contentDescription = "قبلی",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .rotate(180f)
                                .alpha(if (viewModel.canGoPrevious()) 1f else 0.3f)
                                .clickable(enabled = viewModel.canGoPrevious()) {
                                    viewModel.previousWord()
                                }
                        )

                        // Word content - always centered
                        currentWord?.let { word ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                // Word in Persian
                                Text(
                                    text = word.wordFa,
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                // Word in English
                                Text(
                                    text = word.wordEn,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = BlackAlpha,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Next button - fixed at end (left in RTL)
                        Image(
                            painterResource(Res.drawable.button),
                            contentDescription = "بعدی",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .alpha(if (viewModel.canGoNext()) 1f else 0.3f)
                                .clickable(enabled = viewModel.canGoNext()) {
                                    viewModel.nextWord()
                                }
                        )
                    }
                }
            }
        }
    }
}
