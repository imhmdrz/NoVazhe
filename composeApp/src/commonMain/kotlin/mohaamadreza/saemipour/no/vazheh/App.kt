package mohaamadreza.saemipour.no.vazheh

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mohaamadreza.saemipour.no.vazheh.ui.screens.ChildScreen
import mohaamadreza.saemipour.no.vazheh.ui.screens.GameScreen
import mohaamadreza.saemipour.no.vazheh.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        val navController = rememberNavController()
        NavHost(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding(), navController = navController, startDestination = "child") {
            composable("child") {
                OrientationWrapper(Orientation.Vertical) {
                    ChildScreen(navController = navController)
                }
            }
            composable("game") {
                OrientationWrapper(Orientation.Horizontal) {
                    GameScreen(navController = navController)
                }
            }
        }
    }
}

@Composable
expect inline fun OrientationWrapper(nextOrientation: Orientation, content: @Composable () -> Unit)
