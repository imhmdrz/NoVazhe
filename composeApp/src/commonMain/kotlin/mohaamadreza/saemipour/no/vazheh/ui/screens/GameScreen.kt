package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.background
import novazheh.composeapp.generated.resources.button
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GameScreen(navController: NavController, viewModel: ChildViewModel) {
    BackHandler {}
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                    painter = painterResource(Res.drawable.background),
                    contentDescription = "Demo Image",
                    modifier = Modifier.fillMaxSize().zIndex(-1f),
                    contentScale = ContentScale.Crop,
            )

            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxSize().padding(20.dp)
            ) {
                Image(
                        painterResource(Res.drawable.button),
                        contentDescription = null,
                        modifier = Modifier.rotate(180f)
                )

                //                Image(
                //                    painterResource(Res.drawable.avatar),
                //                    contentDescription = null,
                //                )

                Image(
                        painterResource(Res.drawable.button),
                        contentDescription = null,
                )
            }
        }
    }
}
