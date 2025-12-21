package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import mohaamadreza.saemipour.no.vazheh.ui.components.ChildAppBarComponent
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.category
import novazheh.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ChildScreen(navController: NavController, viewModel: ChildViewModel) {
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

            Categories(navController, viewModel)
        }
    }
}

@Composable
private fun Categories(navController: NavController, viewModel: ChildViewModel) {
    LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(24.dp).clip(RoundedCornerShape(24.dp)),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(10) { index ->
            val styleModifier =
                    Modifier.widthIn(min = 160.dp)
                            .heightIn(min = 140.dp)
                            .background(
                                    color = MaterialTheme.colorScheme.surfaceBright,
                                    shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(24.dp)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                viewModel.onCategorySelected(index)
                                navController.navigate("game")
                            }

            Box(modifier = styleModifier, contentAlignment = Alignment.Center) {
                Image(
                        painterResource(Res.drawable.icon),
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        contentDescription = null,
                )
            }
        }
    }
}

@Preview(name = "ChildScreenPreview", showBackground = true, heightDp = 800, widthDp = 400)
@Composable
fun ChildScreenPreview() {
    // Mock or create a dummy viewmodel for preview if needed, or just standard creation
    // For preview, we might need a way to mock it basically.
    // Since ChildViewModel is open class (default), we can pass it.
    // However, viewModel() inside composable might fail in preview if not careful with
    // Context/Platform.
    // But here we are passing it as argument.
    ChildScreen(rememberNavController(), viewModel())
}
