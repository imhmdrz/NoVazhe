package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.ui.components.ChildAppBarComponent
import mohaamadreza.saemipour.no.vazheh.ui.theme.BlackAlpha
import mohaamadreza.saemipour.no.vazheh.ui.theme.MintGreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.Purple40
import mohaamadreza.saemipour.no.vazheh.ui.theme.SkyBlue
import mohaamadreza.saemipour.no.vazheh.ui.theme.peachPink
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.QuizViewModel
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.category
import novazheh.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChildScreen(
    navController: NavController, 
    viewModel: ChildViewModel,
    quizViewModel: QuizViewModel
) {
    BackHandler {}
    val uiState by viewModel.uiState.collectAsState()
    val selectedChild = uiState.selectedChild
    
    // Dialog state for mode selection
    var showModeDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryDTO?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChildAppBarComponent(
                name = "سلام ${selectedChild?.name ?: "کودک"}",
                description = "بیا بازی کنیم، یاد بگیریم و خوش بگذرونیم!",
            )

            Spacer(Modifier.size(16.dp))
            
            // Game cards row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Face Game Button
                FaceGameCard(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("face-game") }
                )
                
                // Color Sorting Game Button
                ColorSortingCard(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("color-sorting") }
                )
            }
            
            Spacer(Modifier.size(16.dp))
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(Res.string.category),
                style = MaterialTheme.typography.headlineLarge
            )

            when {
                uiState.isLoadingCategories -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                            TextButton(onClick = { viewModel.retry() }) {
                                Text("تلاش مجدد")
                            }
                        }
                    }
                }
                uiState.categories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "دسته‌بندی‌ای یافت نشد",
                            style = MaterialTheme.typography.bodyLarge,
                            color = BlackAlpha
                        )
                    }
                }
                else -> {
                    Categories(
                        categories = uiState.categories,
                        onCategoryClick = { category ->
                            selectedCategory = category
                            showModeDialog = true
                        }
                    )
                }
            }
        }
        
        // Mode selection dialog
        if (showModeDialog && selectedCategory != null) {
            ModeSelectionDialog(
                categoryName = selectedCategory?.nameFa ?: "",
                onDismiss = { showModeDialog = false },
                onGameMode = {
                    showModeDialog = false
                    selectedCategory?.let { category ->
                        viewModel.onCategorySelected(category)
                        navController.navigate("game")
                    }
                },
                onQuizMode = {
                    showModeDialog = false
                    selectedCategory?.let { category ->
                        viewModel.onCategorySelected(category)
                        val childId = selectedChild?.id ?: 0
                        quizViewModel.startQuiz(category.id, childId, 5)
                        navController.navigate("quiz")
                    }
                },
                onMemoryMode = {
                    showModeDialog = false
                    selectedCategory?.let { category ->
                        navController.navigate("memory-game/${category.id}")
                    }
                }
            )
        }
    }
}

/**
 * Dialog for selecting between Game (learning), Quiz, and Memory mode
 * دیالوگ انتخاب بین حالت یادگیری، آزمون و بازی حافظه
 */
@Composable
private fun ModeSelectionDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onGameMode: () -> Unit,
    onQuizMode: () -> Unit,
    onMemoryMode: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "📚 $categoryName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "چی می‌خوای انجام بدی؟",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Mode buttons - first row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Learning Mode
                    ModeCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🎮",
                        title = "یادگیری",
                        description = "کلمه‌ها رو ببین و یاد بگیر",
                        backgroundColor = SkyBlue,
                        onClick = onGameMode
                    )
                    
                    // Quiz Mode
                    ModeCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🎯",
                        title = "آزمون",
                        description = "صدا رو گوش کن و جواب بده",
                        backgroundColor = MintGreen,
                        onClick = onQuizMode
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Memory Game Mode
                ModeCard(
                    modifier = Modifier.fillMaxWidth(),
                    emoji = "🧠",
                    title = "بازی حافظه",
                    description = "جفت‌های مشابه رو پیدا کن!",
                    backgroundColor = Purple40,
                    onClick = onMemoryMode
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Cancel button
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "انصراف",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    description: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 36.sp
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun Categories(
    categories: List<CategoryDTO>,
    onCategoryClick: (CategoryDTO) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(24.dp).clip(RoundedCornerShape(24.dp)),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(categories) { category ->
            CategoryItem(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun CategoryItem(
    category: CategoryDTO,
    onClick: () -> Unit
) {
    val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
        .data(category.iconUrl)
        .crossfade(true)
        .build()
    
    val painter = rememberAsyncImagePainter(
        model = imageRequest
    )
    
    Box(
        modifier = Modifier
            .widthIn(min = 160.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Background image from URL with logging
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
        )
        
        // Icon overlay
        Image(
            painter = painterResource(Res.drawable.icon),
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            contentDescription = null,
        )
        
        // Text overlay
        Text(
            text = category.nameFa,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
        )
    }
}

/**
 * Face Game Card - A special button to navigate to the Face Game
 * کارت بازی چهره - دکمه‌ای برای رفتن به بازی چهره
 */
@Composable
private fun FaceGameCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 2.dp,
            color = peachPink
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "بازی چهره",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = peachPink
                    )
                    Text(
                        text = "اجزای صورت رو یاد بگیر!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "شروع بازی",
                tint = peachPink,
                modifier = Modifier.size(28.dp).rotate(180f)
            )
        }
    }
}

/**
 * Color Sorting Game Card - A button to navigate to the Color Sorting Game
 * کارت بازی رنگ‌ها - دکمه‌ای برای رفتن به بازی مرتب‌سازی رنگ‌ها
 */
@Composable
private fun ColorSortingCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 2.dp,
            color = SkyBlue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "بازی رنگ‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkyBlue
                )
                Text(
                    text = "رنگ‌ها رو مرتب کن!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Text(
                text = "🎨",
                fontSize = 28.sp
            )
        }
    }
}
