package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.ui.components.ChildAppBarComponent
import mohaamadreza.saemipour.no.vazheh.ui.theme.BlackAlpha
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.category
import novazheh.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChildScreen(navController: NavController, viewModel: ChildViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChildAppBarComponent(
                name = "سلام ماهان",
                description = "بیا بازی کنیم، یاد بگیریم و خوش بگذرونیم!",
                onBackClick = navController::popBackStack
            )

            Spacer(Modifier.size(24.dp))
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
                            viewModel.onCategorySelected(category)
                            navController.navigate("game")
                        }
                    )
                }
            }
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
    Box(
        modifier = Modifier
            .widthIn(min = 160.dp)
            .heightIn(min = 140.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Background image from URL
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(category.iconUrl)
                .crossfade(true)
                .build(),
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
