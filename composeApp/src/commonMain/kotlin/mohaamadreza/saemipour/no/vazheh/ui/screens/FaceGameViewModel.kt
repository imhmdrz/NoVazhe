package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.abroo1
import novazheh.composeapp.generated.resources.abroo2
import novazheh.composeapp.generated.resources.eye1
import novazheh.composeapp.generated.resources.eye2
import novazheh.composeapp.generated.resources.lips
import novazheh.composeapp.generated.resources.nouse
import org.jetbrains.compose.resources.DrawableResource

enum class FacePart {
    LEFT_EYEBROW,
    RIGHT_EYEBROW,
    LEFT_EYE,
    RIGHT_EYE,
    NOSE,
    LIPS
}

data class FacePartItem(
    val id: FacePart,
    val resource: DrawableResource,
    // Visual position and size
    val targetOffsetXPercent: Float,
    val targetOffsetYPercent: Float,
    val widthPercent: Float = 0.15f,
    val heightPercent: Float = 0.08f,
    val zIndex: Float = 0f,
    // Drop zone - can be different from visual for easier dropping with overlaps
    val dropZoneWidthPercent: Float = widthPercent,
    val dropZoneHeightPercent: Float = heightPercent,
    val dropZoneOffsetXPercent: Float = 0f, // Offset from visual center
    val dropZoneOffsetYPercent: Float = 0f, // Offset from visual center

    val audioUrl: String? = null
)

class FaceGameViewModel : ViewModel() {
    var isCurrentlyDragging by mutableStateOf(false)
        private set

    var isWin by mutableStateOf(false)
        private set

    val facePartItems = listOf(
        FacePartItem(
            id = FacePart.LEFT_EYEBROW,
            resource = Res.drawable.abroo1,
            targetOffsetXPercent = 0.24f,
            targetOffsetYPercent = 0.38f,
            widthPercent = 0.21f,
            heightPercent = 0.09f,
            zIndex = 4f,
            dropZoneWidthPercent = 0.18f,
            dropZoneHeightPercent = 0.06f,
            dropZoneOffsetYPercent = -0.01f // Move drop zone up to avoid eye overlap
        ),
        FacePartItem(
            id = FacePart.RIGHT_EYEBROW,
            resource = Res.drawable.abroo2,
            targetOffsetXPercent = 0.492f,
            targetOffsetYPercent = 0.399f,
            widthPercent = 0.205f,
            heightPercent = 0.085f,
            zIndex = 4f,
            dropZoneWidthPercent = 0.18f,
            dropZoneHeightPercent = 0.06f,
            dropZoneOffsetYPercent = -0.01f // Move drop zone up to avoid eye overlap
        ),
        FacePartItem(
            id = FacePart.LEFT_EYE,
            resource = Res.drawable.eye1,
            targetOffsetXPercent = 0.25f,
            targetOffsetYPercent = 0.43f,
            widthPercent = 0.17f,
            heightPercent = 0.09f,
            zIndex = 3f,
            dropZoneWidthPercent = 0.14f,
            dropZoneHeightPercent = 0.07f,
            dropZoneOffsetXPercent = -0.01f // Move left to avoid nose overlap
        ),
        FacePartItem(
            id = FacePart.RIGHT_EYE,
            resource = Res.drawable.eye2,
            targetOffsetXPercent = 0.48f,
            targetOffsetYPercent = 0.443f,
            widthPercent = 0.17f,
            heightPercent = 0.09f,
            zIndex = 3f,
            dropZoneWidthPercent = 0.14f,
            dropZoneHeightPercent = 0.07f,
            dropZoneOffsetXPercent = 0.01f // Move right to avoid nose overlap
        ),
        FacePartItem(
            id = FacePart.NOSE,
            resource = Res.drawable.nouse,
            targetOffsetXPercent = 0.34f,
            targetOffsetYPercent = 0.45f,
            widthPercent = 0.20f,
            heightPercent = 0.14f,
            zIndex = 2f,
            dropZoneWidthPercent = 0.12f,
            dropZoneHeightPercent = 0.08f,
            dropZoneOffsetXPercent = 0.04f, // Center the drop zone
            dropZoneOffsetYPercent = 0.02f  // Move down to avoid eye overlap
        ),
        FacePartItem(
            id = FacePart.LIPS,
            resource = Res.drawable.lips,
            targetOffsetXPercent = 0.275f,
            targetOffsetYPercent = 0.52f,
            widthPercent = 0.28f,
            heightPercent = 0.20f,
            zIndex = 1f,
            dropZoneWidthPercent = 0.22f,
            dropZoneHeightPercent = 0.10f,
            dropZoneOffsetXPercent = 0.03f, // Center the drop zone
            dropZoneOffsetYPercent = 0.04f  // Move down to avoid nose overlap
        )
    )

    var items by mutableStateOf(facePartItems.toList())
        private set

    var placedParts = mutableStateListOf<FacePartItem>()
        private set

    fun startDragging() {
        isCurrentlyDragging = true
    }

    fun stopDragging() {
        isCurrentlyDragging = false
    }

    fun addPlacedPart(item: FacePartItem) {
        if (!placedParts.any { it.id == item.id }) {
            placedParts.add(item)
            items = items.filter { it.id != item.id }
            checkWin()
        }
    }

    private fun checkWin() {
        if (placedParts.size == facePartItems.size) {
            isWin = true
        }
    }

    fun resetGame() {
        placedParts.clear()
        items = facePartItems.toList()
        isWin = false
        isCurrentlyDragging = false
    }
}
