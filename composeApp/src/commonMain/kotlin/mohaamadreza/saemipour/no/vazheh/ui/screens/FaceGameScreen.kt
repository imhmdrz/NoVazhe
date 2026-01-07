package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.components.DragTarget
import mohaamadreza.saemipour.no.vazheh.ui.components.DragbleScreen
import mohaamadreza.saemipour.no.vazheh.ui.components.DropItem
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.face
import org.jetbrains.compose.resources.painterResource

@Composable
fun FaceGameScreen(
    navController: NavController,
    viewModel: FaceGameViewModel
) {
    // Audio updates callback
    val audioUpdates = remember {
        object : AudioUpdates {
            override fun onProgressUpdate(state: PlayerState) {
            }

            override fun onReady() {
            }

            override fun onError(exception: Exception) {
            }
        }
    }

    AudioProvider(audioUpdates = audioUpdates) { audioPlayer ->
        // Cleanup on dispose
        DisposableEffect(Unit) {
            onDispose {
                audioPlayer.cleanUp()
            }
        }

        DragbleScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5E6D3))
        ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "چهره‌سازی",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A3728),
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // Draggable items row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        Color(0xFFE8D5C4),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                viewModel.items.forEach { facePartItem ->
                    DragTarget(
                        dataToDrop = facePartItem,
                        viewModel = viewModel
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(2.dp, Color(0xFF8B7355), RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(facePartItem.resource),
                                contentDescription = facePartItem.id.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                }

                // Show placeholder if no items left
                if (viewModel.items.isEmpty()) {
                    Text(
                        text = "🎉 آفرین!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A3728)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Face area with drop zones
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                val faceWidth = maxWidth * 0.9f
                val faceHeight = maxHeight * 0.85f

                Box(
                    modifier = Modifier
                        .width(faceWidth)
                        .height(faceHeight),
                    contentAlignment = Alignment.Center
                ) {
                    // Face background image
                    Image(
                        painter = painterResource(Res.drawable.face),
                        contentDescription = "Face Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )

                    // Drop zones for each face part
                    viewModel.facePartItems.forEach { facePartItem ->
                        val isAlreadyPlaced = viewModel.placedParts.any { it.id == facePartItem.id }

                        // Visual position and size
                        val visualOffsetX = with(LocalDensity.current) {
                            (faceWidth * facePartItem.targetOffsetXPercent).roundToPx()
                        }
                        val visualOffsetY = with(LocalDensity.current) {
                            (faceHeight * facePartItem.targetOffsetYPercent).roundToPx()
                        }
                        val itemWidth = faceWidth * facePartItem.widthPercent
                        val itemHeight = faceHeight * facePartItem.heightPercent

                        // Drop zone position and size (can be different from visual)
                        val dropZoneWidth = faceWidth * facePartItem.dropZoneWidthPercent
                        val dropZoneHeight = faceHeight * facePartItem.dropZoneHeightPercent
                        val dropZoneOffsetX = with(LocalDensity.current) {
                            (faceWidth * (facePartItem.targetOffsetXPercent + facePartItem.dropZoneOffsetXPercent)).roundToPx()
                        }
                        val dropZoneOffsetY = with(LocalDensity.current) {
                            (faceHeight * (facePartItem.targetOffsetYPercent + facePartItem.dropZoneOffsetYPercent)).roundToPx()
                        }

                        // Visual layer (placed image)
                        if (isAlreadyPlaced) {
                            Box(
                                modifier = Modifier
                                    .zIndex(facePartItem.zIndex)
                                    .offset { IntOffset(visualOffsetX, visualOffsetY) }
                                    .align(Alignment.TopStart)
                            ) {
                                Image(
                                    painter = painterResource(facePartItem.resource),
                                    contentDescription = facePartItem.id.name,
                                    modifier = Modifier
                                        .width(itemWidth)
                                        .height(itemHeight)
                                        .clip(CircleShape)
                                        .blur(1.5.dp, edgeTreatment = BlurredEdgeTreatment(CircleShape)),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        } else {
                            // Drop zone layer (separate position/size for easier dropping)
                            Box(
                                modifier = Modifier
                                    .zIndex(facePartItem.zIndex + 10f) // Drop zones on top when dragging
                                    .offset { IntOffset(dropZoneOffsetX, dropZoneOffsetY) }
                                    .align(Alignment.TopStart)
                            ) {
                                DropItem<FacePartItem>(
                                    modifier = Modifier
                                        .width(dropZoneWidth)
                                        .height(dropZoneHeight)
                                ) { isInBound, droppedItem ->
                                    if (droppedItem != null && droppedItem.id == facePartItem.id) {
                                        LaunchedEffect(droppedItem) {
                                            viewModel.addPlacedPart(droppedItem)
                                            // Play audio when part is placed
                                            droppedItem.audioUrl?.let { url ->
                                                if (url.isNotEmpty()) {
                                                    audioPlayer.play(url)
                                                }
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .then(
                                                if (viewModel.isCurrentlyDragging) {
                                                    Modifier
                                                        .border(
                                                            width = 2.dp,
                                                            color = if (isInBound) Color(0xFF4CAF50) else Color(0xFFBDBDBD),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .background(
                                                            color = if (isInBound)
                                                                Color(0xFF4CAF50).copy(alpha = 0.3f)
                                                            else
                                                                Color.Gray.copy(alpha = 0.2f),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                } else {
                                                    Modifier
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Empty drop zone visual hint when dragging
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Win Dialog
        if (viewModel.isWin) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = Color(0xFF4CAF50),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "🎉 تبریک! 🎉",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "چهره کامل شد!",
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetGame() },
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = Color.White,
                            contentColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "بازی دوباره",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
        }
    }
}
