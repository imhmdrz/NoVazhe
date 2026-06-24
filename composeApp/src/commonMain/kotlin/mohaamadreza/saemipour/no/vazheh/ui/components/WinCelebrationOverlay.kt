package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import novazheh.composeapp.generated.resources.Res

/**
 * Full-screen celebration shown on winning screens: the [main_winning] Lottie animation
 * fills the whole screen while the [corner_winning] horn animation is placed in all four
 * corners. The source horn points to the top-right, so the bottom-left corner uses it as-is
 * and the remaining corners are rotated so every horn points inward toward the center.
 *
 * Drop it into a [androidx.compose.foundation.layout.BoxScope] (e.g. behind a win dialog);
 * it is purely decorative and does not intercept touches.
 */
@Composable
fun WinCelebrationOverlay(
    modifier: Modifier = Modifier,
    cornerSize: Dp = 130.dp,
) {
    val mainComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/main_winning.json").decodeToString()
        )
    }
    val cornerComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/corner_winning.json").decodeToString()
        )
    }

    val mainProgress by animateLottieCompositionAsState(
        composition = mainComposition,
        iterations = Compottie.IterateForever,
    )
    val cornerProgress by animateLottieCompositionAsState(
        composition = cornerComposition,
        iterations = Compottie.IterateForever,
    )

    val cornerPainter = rememberLottiePainter(
        composition = cornerComposition,
        progress = { cornerProgress },
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Full-screen winning animation behind everything else.
        Image(
            painter = rememberLottiePainter(
                composition = mainComposition,
                progress = { mainProgress },
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Horns in the four corners, each rotated to point inward.
        CornerHorn(cornerPainter, Alignment.BottomStart, 0f, cornerSize)
        CornerHorn(cornerPainter, Alignment.TopStart, 90f, cornerSize)
        CornerHorn(cornerPainter, Alignment.TopEnd, 180f, cornerSize)
        CornerHorn(cornerPainter, Alignment.BottomEnd, 270f, cornerSize)
    }
}

/**
 * A win dialog that shows the [WinCelebrationOverlay] *in front of* the dialog's dim scrim, so
 * the animation stays bright instead of being washed out behind the dimmed window. The centered
 * card mirrors a Material [androidx.compose.material3.AlertDialog] with [title]/[text]/[confirmButton]
 * slots, so existing win dialogs can be converted by simply renaming the call.
 */
@Composable
fun WinCelebrationDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = RoundedCornerShape(24.dp),
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // Bright celebration, rendered in front of the dialog window's dim scrim.
            WinCelebrationOverlay()

            // The dialog card on top of the celebration.
            Surface(
                shape = shape,
                color = containerColor,
                tonalElevation = 6.dp,
                modifier = modifier
                    .padding(horizontal = 32.dp)
                    .widthIn(min = 280.dp, max = 400.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    title?.let {
                        it()
                        Spacer(Modifier.height(16.dp))
                    }
                    text?.let {
                        it()
                        Spacer(Modifier.height(24.dp))
                    }
                    confirmButton()
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.CornerHorn(
    painter: Painter,
    alignment: Alignment,
    rotationDegrees: Float,
    size: Dp,
) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .align(alignment)
            .size(size)
            .rotate(rotationDegrees),
    )
}
