package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Shadow
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
import novazheh.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource

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

    // Dialog state for picking a category (or combined) when launching
    // Memory Game or Quiz Game directly from the top row
    var pickerGameType by remember { mutableStateOf<GameType?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChildAppBarComponent(
                name = "سلام ${selectedChild?.name ?: "کودک"}",
                description = "بیا بازی کنیم، یاد بگیریم و خوش بگذرونیم!",
            )

            Spacer(Modifier.size(16.dp))

            // Game cards - row 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuizGameCard(
                    modifier = Modifier.weight(1f),
                    onClick = { pickerGameType = GameType.Quiz }
                )
                FaceGameCard(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("face-game") }
                )
            }

            Spacer(Modifier.size(12.dp))

            // Game cards - row 2 (Memory & Quiz - launch with category picker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MemoryGameCard(
                    modifier = Modifier.weight(1f),
                    onClick = { pickerGameType = GameType.Memory }
                )
                ColorSortingCard(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("color-sorting") }
                )
            }

            Spacer(Modifier.size(12.dp))

            // Game cards - row 3 (Shadow Match + Odd One Out)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShadowMatchCard(
                    modifier = Modifier.weight(1f),
                    onClick = { pickerGameType = GameType.Shadow }
                )
                OddOneOutCard(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("odd-one-out") }
                )
            }

            Spacer(Modifier.size(16.dp))
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = "آموزش",
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
                            viewModel.onCategorySelected(category)
                            navController.navigate("game")
                        }
                    )
                }
            }
        }

        // Category picker dialog for the top-row Memory / Quiz buttons
        // (lets the user pick a single category or play a combined session)
        pickerGameType?.let { gameType ->
            CategoryPickerDialog(
                gameType = gameType,
                categories = uiState.categories,
                onDismiss = { pickerGameType = null },
                onCategoryPicked = { category ->
                    pickerGameType = null
                    when (gameType) {
                        GameType.Memory -> navController.navigate("memory-game/${category.id}")
                        GameType.Quiz -> {
                            viewModel.onCategorySelected(category)
                            quizViewModel.startQuiz(category.id, selectedChild?.id, 5)
                            navController.navigate("quiz")
                        }
                        GameType.Shadow -> navController.navigate("shadow-match/${category.id}")
                    }
                },
                onCombinedPicked = {
                    pickerGameType = null
                    when (gameType) {
                        GameType.Memory -> navController.navigate("memory-game/$COMBINED_CATEGORY_ID")
                        GameType.Quiz -> {
                            quizViewModel.startCombinedQuiz(selectedChild?.id, 5)
                            navController.navigate("quiz")
                        }
                        GameType.Shadow -> navController.navigate("shadow-match/$COMBINED_CATEGORY_ID")
                    }
                }
            )
        }
    }
}

private enum class GameType { Memory, Quiz, Shadow }

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

/**
 * Memory Game Card - دکمه‌ای برای رفتن به بازی حافظه
 */
@Composable
private fun MemoryGameCard(
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
            color = Purple40
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
                    text = "بازی حافظه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Purple40
                )
                Text(
                    text = "جفت‌های مشابه!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = "🧠",
                fontSize = 28.sp
            )
        }
    }
}

/**
 * Quiz Game Card - دکمه‌ای برای رفتن به آزمون
 */
@Composable
private fun QuizGameCard(
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
            color = MintGreen
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
                    text = "آزمون",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MintGreen
                )
                Text(
                    text = "گوش کن!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = "🎯",
                fontSize = 28.sp
            )
        }
    }
}

/**
 * Shadow Match Game Card - دکمه‌ای برای رفتن به بازی سایه‌ها
 */
@Composable
private fun ShadowMatchCard(
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
            color = ShadowGameAccent
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
                    text = "بازی سایه‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ShadowGameAccent
                )
                Text(
                    text = "سایه رو تطبیق بده!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = "🌑",
                fontSize = 28.sp
            )
        }
    }
}

/**
 * Odd One Out Game Card - دکمه‌ای برای رفتن به بازی «کدام متفاوت است؟»
 * این بازی همیشه از همه دسته‌بندی‌ها استفاده می‌کند، پس بدون picker مستقیم باز می‌شود.
 */
@Composable
private fun OddOneOutCard(
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
            color = OddOneOutGameAccent
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
                    text = "متفاوت کدومه؟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OddOneOutGameAccent
                )
                Text(
                    text = "با بقیه فرق داره!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = "🔍",
                fontSize = 28.sp
            )
        }
    }
}

/**
 * Dialog that lets the user pick a category (or play a combined session
 * pulling from all categories) when launching the Memory or Quiz game
 * from the top game-cards row.
 * دیالوگ انتخاب دسته‌بندی یا حالت ترکیبی برای بازی حافظه و آزمون
 */
@Composable
private fun CategoryPickerDialog(
    gameType: GameType,
    categories: List<CategoryDTO>,
    onDismiss: () -> Unit,
    onCategoryPicked: (CategoryDTO) -> Unit,
    onCombinedPicked: () -> Unit
) {
    val accent = when (gameType) {
        GameType.Memory -> Purple40
        GameType.Quiz -> MintGreen
        GameType.Shadow -> ShadowGameAccent
    }
    val emoji = when (gameType) {
        GameType.Memory -> "🧠"
        GameType.Quiz -> "🎯"
        GameType.Shadow -> "🌑"
    }
    val title = when (gameType) {
        GameType.Memory -> "بازی حافظه"
        GameType.Quiz -> "آزمون"
        GameType.Shadow -> "بازی سایه‌ها"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$emoji $title",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "یک دسته‌بندی انتخاب کن یا بازی ترکیبی رو شروع کن",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                if (categories.isEmpty()) {
                    Text(
                        text = "دسته‌بندی‌ای موجود نیست",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.heightIn(max = 360.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Combined "all categories" option, styled like a category tile,
                        // sits first in the grid.
                        item {
                            CombinedPickItem(
                                categories = categories,
                                accent = accent,
                                onClick = onCombinedPicked
                            )
                        }
                        items(categories) { category ->
                            CategoryPickItem(
                                category = category,
                                accent = accent,
                                onClick = { onCategoryPicked(category) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text(text = "انصراف", color = Color.Gray)
                }
            }
        }
    }
}

/**
 * Grid tile for the "combined / all categories" option, sized like [CategoryPickItem].
 * Its background is a 2×2 collage built from the first four categories' images (filling
 * empty quadrants with the accent colour), under a dark scrim with a centered label.
 * پس‌زمینه از تصاویر چند دسته‌بندی اول ساخته می‌شود
 */
@Composable
private fun CombinedPickItem(
    categories: List<CategoryDTO>,
    accent: Color,
    onClick: () -> Unit
) {
    val previews = categories.take(4)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 2×2 collage background from the first categories
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(2) { row ->
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    repeat(2) { col ->
                        val category = previews.getOrNull(row * 2 + col)
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            if (category != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalPlatformContext.current)
                                            .data(category.iconUrl)
                                            .crossfade(true)
                                            .build()
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(accent.copy(alpha = 0.35f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dark scrim so the label stays readable over the collage
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Image(
            painter = painterResource(Res.drawable.icon),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(20.dp),
            contentDescription = null,
        )

        // Accent border to mark it as the special "combined" option
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = accent,
                    shape = RoundedCornerShape(14.dp)
                )
        )

        // Centered label
        Text(
            text = "🎲 ترکیبی",
            style = MaterialTheme.typography.titleSmall.copy(shadow = Shadow()),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

/**
 * Compact version of [CategoryItem] used inside [CategoryPickerDialog].
 * Shows the category image as a background with the name overlay,
 * mirroring [CategoryItem] but at a smaller size.
 */
@Composable
private fun CategoryPickItem(
    category: CategoryDTO,
    accent: Color,
    onClick: () -> Unit
) {
    val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
        .data(category.iconUrl)
        .crossfade(true)
        .build()

    val painter = rememberAsyncImagePainter(model = imageRequest)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Background image from URL
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
        )

        // Accent border overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.5.dp,
                    color = accent.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp)
                )
        )

        // Icon overlay (smaller)
        Image(
            painter = painterResource(Res.drawable.icon),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(20.dp),
            contentDescription = null,
        )

        // Text overlay
        Text(
            text = category.nameFa,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        )
    }
}
