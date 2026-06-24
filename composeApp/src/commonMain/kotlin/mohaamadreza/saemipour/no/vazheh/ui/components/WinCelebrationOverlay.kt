package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
