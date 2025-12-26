package mohaamadreza.saemipour.no.vazheh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray

private val DarkColorScheme =
    darkColorScheme(primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
        background = Color.White,
        onSurface = Color.Black,
        onSurfaceVariant = BlackAlpha
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
        background = Color.White,
        onSurface = Color.Black,
        onSurfaceVariant = BlackAlpha
    )

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography.invoke(), content = content)
}
