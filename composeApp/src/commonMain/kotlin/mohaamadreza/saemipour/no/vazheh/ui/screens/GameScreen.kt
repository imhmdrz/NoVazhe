package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.background
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GameScreen(navController: NavController) {
    BackHandler {}
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Image(
            painter = painterResource(Res.drawable.background),
            contentDescription = "Demo Image",
            modifier = Modifier.fillMaxSize().zIndex(-1f),
            contentScale = ContentScale.Crop,
        )
    }
}