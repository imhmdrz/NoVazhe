package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import mohaamadreza.saemipour.no.vazheh.player.AudioProvider
import mohaamadreza.saemipour.no.vazheh.player.AudioUpdates
import mohaamadreza.saemipour.no.vazheh.player.GameSounds
import mohaamadreza.saemipour.no.vazheh.player.PlayerState
import mohaamadreza.saemipour.no.vazheh.ui.components.DragTarget
import mohaamadreza.saemipour.no.vazheh.ui.components.DashboardButton
import mohaamadreza.saemipour.no.vazheh.ui.components.WinCelebration
import mohaamadreza.saemipour.no.vazheh.ui.components.LevelUpCelebration
import mohaamadreza.saemipour.no.vazheh.ui.components.backToDashboard
import mohaamadreza.saemipour.no.vazheh.ui.components.DragbleScreen
import mohaamadreza.saemipour.no.vazheh.ui.components.DropItem
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun FaceGameScreen(
    navController: NavController,
    viewModel: FaceGameViewModel,
    childViewModel: ChildViewModel
) {
    val childUiState by childViewModel.uiState.collectAsState()
    val selectedChildId = childUiState.selectedChild?.id
    LaunchedEffect(selectedChildId) { viewModel.loadGame(selectedChildId) }
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

        var showWin by remember { mutableStateOf(false) }
        var celebrationStage by remember { mutableStateOf(FaceCelebrationStage.None) }
        LaunchedEffect(viewModel.isWin) {
            if (!viewModel.isWin) {
                showWin = false
                celebrationStage = FaceCelebrationStage.None
            } else {
                audioPlayer.play(GameSounds.win)
                showWin = true
                celebrationStage = FaceCelebrationStage.Normal
            }
        }

        DragbleScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5E6D3))
        ) {
            if (viewModel.isProgressLoading) {
                Text("در حال بارگذاری پیشرفت...", modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.progressError != null) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(viewModel.progressError.orEmpty(), textAlign = TextAlign.Center)
                    Button(onClick = viewModel::retryProgressLoad) { Text("تلاش دوباره") }
                }
            } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title + دکمه داشبورد (بالا سمت چپ)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    DashboardButton(
                        onClick = { navController.backToDashboard() },
                        tint = Color(0xFF4A3728),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                    )
                    Text(
                        text = "چهره و بدن‌سازی",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A3728),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = { viewModel.resetCurrentRound() },
                        enabled = !viewModel.isCompleting,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "شروع دوباره",
                            tint = Color(0xFF4A3728)
                        )
                    }
                }

                Text("مرحله ${viewModel.currentLevel}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3728))
                Text((0 until 3).joinToString("  ") { if (it < viewModel.stars) "⭐" else "☆" }, fontSize = 25.sp)
                if (viewModel.isCompleting) Text("در حال ثبت پیشرفت...", color = Color(0xFF4A3728))

                // Draggable items grid (2 rows / 3 columns) with bigger items
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            Color(0xFFE8D5C4),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    if (viewModel.items.isEmpty()) {
                        Text(
                            text = "🎉 آفرین!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A3728),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 270.dp),
                            userScrollEnabled = true
                        ) {
                            items(viewModel.items, key = { it.id.name }) { facePartItem ->
                                DragTarget(
                                    dataToDrop = facePartItem,
                                    viewModel = viewModel
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .border(
                                                2.dp,
                                                Color(0xFF8B7355),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(8.dp),
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
                        }
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
                    val faceSet = viewModel.activeFaceSet
                    val facePainter = painterResource(faceSet.mainImage)
                    val intrinsicSize = facePainter.intrinsicSize
                    val imageAspectRatio = if (
                        intrinsicSize.width.isFinite() && intrinsicSize.height.isFinite() &&
                        intrinsicSize.width > 0f && intrinsicSize.height > 0f
                    ) {
                        intrinsicSize.width / intrinsicSize.height
                    } else {
                        faceSet.aspectRatio
                    }
                    val viewportAspectRatio = faceWidth / faceHeight
                    val fittedFaceWidth: Dp
                    val fittedFaceHeight: Dp
                    if (viewportAspectRatio > imageAspectRatio) {
                        fittedFaceHeight = faceHeight
                        fittedFaceWidth = faceHeight * imageAspectRatio
                    } else {
                        fittedFaceWidth = faceWidth
                        fittedFaceHeight = faceWidth / imageAspectRatio
                    }

                    Box(
                        modifier = Modifier
                            .width(faceWidth)
                            .height(faceHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        // This inner box exactly matches the ContentScale.Fit image bounds.
                        // Normalized part and hitbox geometry is evaluated only within it.
                        Box(
                            modifier = Modifier
                                .width(fittedFaceWidth)
                                .height(fittedFaceHeight),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Image(
                                painter = facePainter,
                                contentDescription = "Face Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                            )

                            faceSet.parts.forEach { facePartItem ->
                                val isAlreadyPlaced =
                                    viewModel.placedParts.any { it.id == facePartItem.id }

                                val visualOffsetX = with(LocalDensity.current) {
                                    (fittedFaceWidth * facePartItem.targetOffsetXPercent).roundToPx()
                                }
                                val visualOffsetY = with(LocalDensity.current) {
                                    (fittedFaceHeight * facePartItem.targetOffsetYPercent).roundToPx()
                                }
                                val itemWidth = fittedFaceWidth * facePartItem.widthPercent
                                val itemHeight = fittedFaceHeight * facePartItem.heightPercent
                                val dropZoneWidth = fittedFaceWidth * facePartItem.dropZoneWidthPercent
                                val dropZoneHeight = fittedFaceHeight * facePartItem.dropZoneHeightPercent
                                val dropZoneOffsetX = with(LocalDensity.current) {
                                    (fittedFaceWidth * (facePartItem.targetOffsetXPercent + facePartItem.dropZoneOffsetXPercent)).roundToPx()
                                }
                                val dropZoneOffsetY = with(LocalDensity.current) {
                                    (fittedFaceHeight * (facePartItem.targetOffsetYPercent + facePartItem.dropZoneOffsetYPercent)).roundToPx()
                                }

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
                                                .blur(
                                                    1.5.dp,
                                                    edgeTreatment = BlurredEdgeTreatment(CircleShape)
                                                ),
                                            contentScale = ContentScale.Fit,
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .zIndex(facePartItem.zIndex + 10f)
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
                                                    audioPlayer.play(GameSounds.correct)
                                                    droppedItem.audioUrl?.let { url ->
                                                        if (url.isNotEmpty()) {
                                                            delay(700)
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
                                                                    color = if (isInBound) Color(0xFF4CAF50).copy(alpha = 0.3f)
                                                                    else Color.Gray.copy(alpha = 0.2f),
                                                                    shape = RoundedCornerShape(8.dp)
                                                                )
                                                        } else {
                                                            Modifier
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) { }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (viewModel.completionError != null) {
                Column(Modifier.align(Alignment.BottomCenter).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(viewModel.completionError.orEmpty(), color = Color(0xFFB3261E), textAlign = TextAlign.Center)
                    Button(onClick = viewModel::retryCompletion, enabled = !viewModel.isCompleting) { Text("ثبت دوباره") }
                }
            }

            if (showWin) when (celebrationStage) {
                FaceCelebrationStage.Normal -> WinCelebration(
                    timeoutMillis = if (viewModel.isLevelUpWin || viewModel.isAllLevelsCompletedWin) 2_250L else 8_000L,
                    onTap = {
                        when {
                            viewModel.isAllLevelsCompletedWin -> celebrationStage = FaceCelebrationStage.Final
                            viewModel.isLevelUpWin -> celebrationStage = FaceCelebrationStage.LevelUp
                            else -> viewModel.finishCelebration()
                        }
                    }
                )
                FaceCelebrationStage.LevelUp -> LevelUpCelebration(onTap = {
                    viewModel.finishCelebration()
                    showWin = false
                    celebrationStage = FaceCelebrationStage.None
                })
                FaceCelebrationStage.Final -> key(viewModel.finalizationRetryKey) {
                    LevelUpCelebration(
                        title = "همه‌ی مرحله‌ها تموم شد!",
                        subtitle = if (viewModel.completionError == null) {
                            "آفرین قهرمان! برای شروع یک شخصیت تازه لمس کن"
                        } else {
                            "ثبت بازی بعدی انجام نشد؛ برای تلاش دوباره لمس کن"
                        },
                        onTap = viewModel::finishCelebration
                    )
                }
                FaceCelebrationStage.None -> Unit
            }
            }
        }
    }
}

private enum class FaceCelebrationStage { None, Normal, LevelUp, Final }
