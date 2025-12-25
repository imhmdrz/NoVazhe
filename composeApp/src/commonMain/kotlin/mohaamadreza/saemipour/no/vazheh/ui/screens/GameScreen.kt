package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import mohaamadreza.saemipour.no.vazheh.ui.theme.BlackAlpha
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

    BackHandler {
        navController.popBackStack()
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

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Category title and progress
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedCategory?.nameFa ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                            Text(
                                text = "${uiState.currentWordIndex + 1} / ${uiState.words.size}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = BlackAlpha
                            )
                        }

                        // Word content
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            currentWord?.let { word ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
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
                        }

                        // Navigation buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Previous button
                            Image(
                                painterResource(Res.drawable.button),
                                contentDescription = "قبلی",
                                modifier = Modifier
                                    .rotate(180f)
                                    .alpha(if (viewModel.canGoPrevious()) 1f else 0.3f)
                                    .clickable(enabled = viewModel.canGoPrevious()) {
                                        viewModel.previousWord()
                                    }
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Next button
                            Image(
                                painterResource(Res.drawable.button),
                                contentDescription = "بعدی",
                                modifier = Modifier
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
}
