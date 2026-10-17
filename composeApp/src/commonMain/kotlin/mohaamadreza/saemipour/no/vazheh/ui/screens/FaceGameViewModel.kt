package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.FaceGameProgressDTO
import mohaamadreza.saemipour.no.vazheh.data.ProgressRepository
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.abroo1
import novazheh.composeapp.generated.resources.abroo2
import novazheh.composeapp.generated.resources.eye1
import novazheh.composeapp.generated.resources.eye2
import novazheh.composeapp.generated.resources.face
import novazheh.composeapp.generated.resources.face_02_left_eye
import novazheh.composeapp.generated.resources.face_02_left_eyebrow
import novazheh.composeapp.generated.resources.face_02_lips
import novazheh.composeapp.generated.resources.face_02_main
import novazheh.composeapp.generated.resources.face_02_nose
import novazheh.composeapp.generated.resources.face_02_right_eye
import novazheh.composeapp.generated.resources.face_02_right_eyebrow
import novazheh.composeapp.generated.resources.character_03_left_eye
import novazheh.composeapp.generated.resources.character_03_left_eyebrow
import novazheh.composeapp.generated.resources.character_03_left_hand
import novazheh.composeapp.generated.resources.character_03_left_foot
import novazheh.composeapp.generated.resources.character_03_progression_level_1_base
import novazheh.composeapp.generated.resources.character_03_progression_level_2_base
import novazheh.composeapp.generated.resources.character_03_progression_level_3_base
import novazheh.composeapp.generated.resources.character_03_lips
import novazheh.composeapp.generated.resources.character_03_nose
import novazheh.composeapp.generated.resources.character_03_right_eye
import novazheh.composeapp.generated.resources.character_03_right_eyebrow
import novazheh.composeapp.generated.resources.character_03_right_hand
import novazheh.composeapp.generated.resources.character_03_right_foot
import novazheh.composeapp.generated.resources.character_04_left_eyebrow
import novazheh.composeapp.generated.resources.character_04_left_eye
import novazheh.composeapp.generated.resources.character_04_left_hand
import novazheh.composeapp.generated.resources.character_04_progression_level_1_base
import novazheh.composeapp.generated.resources.character_04_progression_level_2_base
import novazheh.composeapp.generated.resources.character_04_progression_level_3_base
import novazheh.composeapp.generated.resources.character_04_lips
import novazheh.composeapp.generated.resources.character_04_nose
import novazheh.composeapp.generated.resources.character_04_right_eyebrow
import novazheh.composeapp.generated.resources.character_04_right_eye
import novazheh.composeapp.generated.resources.character_04_right_hand
import novazheh.composeapp.generated.resources.character_04_left_foot
import novazheh.composeapp.generated.resources.character_04_right_foot
import novazheh.composeapp.generated.resources.character_05_left_eyebrow
import novazheh.composeapp.generated.resources.character_05_left_eye
import novazheh.composeapp.generated.resources.character_05_left_hand
import novazheh.composeapp.generated.resources.character_05_progression_level_1_base
import novazheh.composeapp.generated.resources.character_05_progression_level_2_base
import novazheh.composeapp.generated.resources.character_05_progression_level_3_base
import novazheh.composeapp.generated.resources.character_05_lips
import novazheh.composeapp.generated.resources.character_05_nose
import novazheh.composeapp.generated.resources.character_05_right_eyebrow
import novazheh.composeapp.generated.resources.character_05_right_eye
import novazheh.composeapp.generated.resources.character_05_right_hand
import novazheh.composeapp.generated.resources.character_05_left_foot
import novazheh.composeapp.generated.resources.character_05_right_foot
import novazheh.composeapp.generated.resources.character_06_left_eyebrow
import novazheh.composeapp.generated.resources.character_06_left_eye
import novazheh.composeapp.generated.resources.character_06_left_hand
import novazheh.composeapp.generated.resources.character_06_progression_level_1_base
import novazheh.composeapp.generated.resources.character_06_progression_level_2_base
import novazheh.composeapp.generated.resources.character_06_progression_level_3_base
import novazheh.composeapp.generated.resources.character_06_lips
import novazheh.composeapp.generated.resources.character_06_nose
import novazheh.composeapp.generated.resources.character_06_right_eyebrow
import novazheh.composeapp.generated.resources.character_06_right_eye
import novazheh.composeapp.generated.resources.character_06_right_hand
import novazheh.composeapp.generated.resources.character_06_left_foot
import novazheh.composeapp.generated.resources.character_06_right_foot
import novazheh.composeapp.generated.resources.character_07_left_eyebrow
import novazheh.composeapp.generated.resources.character_07_left_eye
import novazheh.composeapp.generated.resources.character_07_left_hand
import novazheh.composeapp.generated.resources.character_07_progression_level_1_base
import novazheh.composeapp.generated.resources.character_07_progression_level_2_base
import novazheh.composeapp.generated.resources.character_07_progression_level_3_base
import novazheh.composeapp.generated.resources.character_07_lips
import novazheh.composeapp.generated.resources.character_07_nose
import novazheh.composeapp.generated.resources.character_07_right_eyebrow
import novazheh.composeapp.generated.resources.character_07_right_eye
import novazheh.composeapp.generated.resources.character_07_right_hand
import novazheh.composeapp.generated.resources.character_07_left_foot
import novazheh.composeapp.generated.resources.character_07_right_foot
import novazheh.composeapp.generated.resources.face_02_progression_level_1_base
import novazheh.composeapp.generated.resources.face_02_progression_level_2_base
import novazheh.composeapp.generated.resources.face_01_progression_level_1_base
import novazheh.composeapp.generated.resources.face_01_progression_level_2_base
import novazheh.composeapp.generated.resources.lips
import novazheh.composeapp.generated.resources.nouse
import org.jetbrains.compose.resources.DrawableResource

enum class FacePart {
    LEFT_EYEBROW,
    RIGHT_EYEBROW,
    LEFT_EYE,
    RIGHT_EYE,
    NOSE,
    LIPS,
    LEFT_HAND,
    RIGHT_HAND,
    LEFT_ARM,
    RIGHT_ARM,
    LEFT_LEG,
    RIGHT_LEG,
    LEFT_FOOT,
    RIGHT_FOOT
}

data class FacePartItem(
    val id: FacePart,
    val resource: DrawableResource,
    // Visual position and size, normalized to the rendered main-image bounds.
    val targetOffsetXPercent: Float,
    val targetOffsetYPercent: Float,
    val widthPercent: Float = 0.15f,
    val heightPercent: Float = 0.08f,
    val zIndex: Float = 0f,
    // Drop zone geometry is independent of the visible image for easier dropping.
    val dropZoneWidthPercent: Float = widthPercent,
    val dropZoneHeightPercent: Float = heightPercent,
    val dropZoneOffsetXPercent: Float = 0f,
    val dropZoneOffsetYPercent: Float = 0f,
    val audioUrl: String? = null
)

data class FaceSet(
    val id: String,
    val mainImage: DrawableResource,
    val aspectRatio: Float,
    val parts: List<FacePartItem>,
    val characterId: String = id,
    val levelIndex: Int = 0
)

data class CharacterSet(
    val id: String,
    val levels: Map<Int, FaceSet>
)

private const val EYEBROW_AUDIO = "https://ik.imagekit.io/mhmdrzsaemi/Face/novazhe/abroo.mp3"
private const val EYE_AUDIO = "https://ik.imagekit.io/mhmdrzsaemi/Face/novazhe/cheshm.mp3"
private const val NOSE_AUDIO = "https://ik.imagekit.io/mhmdrzsaemi/Face/novazhe/bini.mp3"
private const val LIPS_AUDIO = "https://ik.imagekit.io/mhmdrzsaemi/Face/novazhe/dahan.mp3"

private val faceSetRegistry = listOf(
    FaceSet(
        id = "face_01",
        mainImage = Res.drawable.face,
        aspectRatio = 783f / 741f,
        parts = listOf(
            FacePartItem(
                id = FacePart.LEFT_EYEBROW,
                resource = Res.drawable.abroo1,
                audioUrl = EYEBROW_AUDIO,
                targetOffsetXPercent = 0.24f,
                targetOffsetYPercent = 0.38f,
                widthPercent = 0.21f,
                heightPercent = 0.09f,
                zIndex = 4f,
                dropZoneWidthPercent = 0.18f,
                dropZoneHeightPercent = 0.06f,
                dropZoneOffsetYPercent = -0.01f
            ),
            FacePartItem(
                id = FacePart.RIGHT_EYEBROW,
                resource = Res.drawable.abroo2,
                audioUrl = EYEBROW_AUDIO,
                targetOffsetXPercent = 0.492f,
                targetOffsetYPercent = 0.399f,
                widthPercent = 0.205f,
                heightPercent = 0.085f,
                zIndex = 4f,
                dropZoneWidthPercent = 0.18f,
                dropZoneHeightPercent = 0.06f,
                dropZoneOffsetYPercent = -0.01f
            ),
            FacePartItem(
                id = FacePart.LEFT_EYE,
                resource = Res.drawable.eye1,
                audioUrl = EYE_AUDIO,
                targetOffsetXPercent = 0.25f,
                targetOffsetYPercent = 0.43f,
                widthPercent = 0.17f,
                heightPercent = 0.09f,
                zIndex = 3f,
                dropZoneWidthPercent = 0.14f,
                dropZoneHeightPercent = 0.07f,
                dropZoneOffsetXPercent = -0.01f
            ),
            FacePartItem(
                id = FacePart.RIGHT_EYE,
                resource = Res.drawable.eye2,
                audioUrl = EYE_AUDIO,
                targetOffsetXPercent = 0.48f,
                targetOffsetYPercent = 0.443f,
                widthPercent = 0.17f,
                heightPercent = 0.09f,
                zIndex = 3f,
                dropZoneWidthPercent = 0.14f,
                dropZoneHeightPercent = 0.07f,
                dropZoneOffsetXPercent = 0.01f
            ),
            FacePartItem(
                id = FacePart.NOSE,
                resource = Res.drawable.nouse,
                audioUrl = NOSE_AUDIO,
                targetOffsetXPercent = 0.34f,
                targetOffsetYPercent = 0.45f,
                widthPercent = 0.20f,
                heightPercent = 0.14f,
                zIndex = 2f,
                dropZoneWidthPercent = 0.12f,
                dropZoneHeightPercent = 0.08f,
                dropZoneOffsetXPercent = 0.04f,
                dropZoneOffsetYPercent = 0.02f
            ),
            FacePartItem(
                id = FacePart.LIPS,
                resource = Res.drawable.lips,
                audioUrl = LIPS_AUDIO,
                targetOffsetXPercent = 0.275f,
                targetOffsetYPercent = 0.52f,
                widthPercent = 0.28f,
                heightPercent = 0.20f,
                zIndex = 1f,
                dropZoneWidthPercent = 0.22f,
                dropZoneHeightPercent = 0.10f,
                dropZoneOffsetXPercent = 0.03f,
                dropZoneOffsetYPercent = 0.04f
            )
        )
    ),
    FaceSet(
        id = "face_02",
        mainImage = Res.drawable.face_02_main,
        aspectRatio = 1f,
        parts = listOf(
            // These image boxes are derived from the source crop transforms, so their
            // rendered pixels map back to the corresponding regions in the 1254px portrait.
            FacePartItem(
                id = FacePart.LEFT_EYEBROW,
                resource = Res.drawable.face_02_left_eyebrow,
                audioUrl = EYEBROW_AUDIO,
                targetOffsetXPercent = 0.236f,
                targetOffsetYPercent = 0.258f,
                widthPercent = 0.222f,
                heightPercent = 0.222f,
                zIndex = 4f,
                dropZoneWidthPercent = 0.17f,
                dropZoneHeightPercent = 0.075f,
                dropZoneOffsetXPercent = 0.015f,
                dropZoneOffsetYPercent = 0.077f
            ),
            FacePartItem(
                id = FacePart.RIGHT_EYEBROW,
                resource = Res.drawable.face_02_right_eyebrow,
                audioUrl = EYEBROW_AUDIO,
                targetOffsetXPercent = 0.543f,
                targetOffsetYPercent = 0.258f,
                widthPercent = 0.222f,
                heightPercent = 0.222f,
                zIndex = 4f,
                dropZoneWidthPercent = 0.17f,
                dropZoneHeightPercent = 0.075f,
                dropZoneOffsetXPercent = 0.015f,
                dropZoneOffsetYPercent = 0.077f
            ),
            FacePartItem(
                id = FacePart.LEFT_EYE,
                resource = Res.drawable.face_02_left_eye,
                audioUrl = EYE_AUDIO,
                targetOffsetXPercent = 0.236f,
                targetOffsetYPercent = 0.376f,
                widthPercent = 0.222f,
                heightPercent = 0.222f,
                zIndex = 3f,
                dropZoneWidthPercent = 0.145f,
                dropZoneHeightPercent = 0.105f,
                dropZoneOffsetXPercent = 0.045f,
                dropZoneOffsetYPercent = 0.055f
            ),
            FacePartItem(
                id = FacePart.RIGHT_EYE,
                resource = Res.drawable.face_02_right_eye,
                audioUrl = EYE_AUDIO,
                targetOffsetXPercent = 0.543f,
                targetOffsetYPercent = 0.376f,
                widthPercent = 0.222f,
                heightPercent = 0.222f,
                zIndex = 3f,
                dropZoneWidthPercent = 0.145f,
                dropZoneHeightPercent = 0.105f,
                dropZoneOffsetXPercent = 0.045f,
                dropZoneOffsetYPercent = 0.055f
            ),
            FacePartItem(
                id = FacePart.NOSE,
                resource = Res.drawable.face_02_nose,
                audioUrl = NOSE_AUDIO,
                targetOffsetXPercent = 0.434f,
                targetOffsetYPercent = 0.503f,
                widthPercent = 0.154f,
                heightPercent = 0.154f,
                zIndex = 2f,
                dropZoneWidthPercent = 0.12f,
                dropZoneHeightPercent = 0.12f,
                dropZoneOffsetXPercent = 0.017f,
                dropZoneOffsetYPercent = 0.037f
            ),
            FacePartItem(
                id = FacePart.LIPS,
                resource = Res.drawable.face_02_lips,
                audioUrl = LIPS_AUDIO,
                targetOffsetXPercent = 0.34f,
                targetOffsetYPercent = 0.519f,
                widthPercent = 0.333f,
                heightPercent = 0.333f,
                zIndex = 1f,
                dropZoneWidthPercent = 0.24f,
                dropZoneHeightPercent = 0.09f,
                dropZoneOffsetXPercent = 0.035f,
                dropZoneOffsetYPercent = 0.111f
            )
        )
    )
)

private fun characterPart(
    id: FacePart,
    resource: DrawableResource,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    z: Float = 0f,
    dropX: Float = x,
    dropY: Float = y,
    dropWidth: Float = width,
    dropHeight: Float = height,
    audioUrl: String? = null
) = FacePartItem(
    id = id,
    resource = resource,
    targetOffsetXPercent = x,
    targetOffsetYPercent = y,
    widthPercent = width,
    heightPercent = height,
    zIndex = z,
    dropZoneWidthPercent = dropWidth,
    dropZoneHeightPercent = dropHeight,
    dropZoneOffsetXPercent = dropX - x,
    dropZoneOffsetYPercent = dropY - y,
    audioUrl = audioUrl
)

private data class PartCrop(val x: Int, val y: Int, val width: Int, val height: Int)

private fun croppedCharacterPart(
    id: FacePart,
    resource: DrawableResource,
    crop: PartCrop,
    dropX: Float,
    dropY: Float,
    dropWidth: Float,
    dropHeight: Float,
    audioUrl: String? = null
): FacePartItem {
    val x = crop.x / 1024f
    val y = crop.y / 1536f
    val width = crop.width / 1024f
    val height = crop.height / 1536f
    return characterPart(
        id = id,
        resource = resource,
        x = x,
        y = y,
        width = width,
        height = height,
        z = when (id) {
            FacePart.LEFT_EYEBROW, FacePart.RIGHT_EYEBROW -> 6f
            FacePart.LEFT_EYE, FacePart.RIGHT_EYE -> 5f
            FacePart.NOSE -> 4f
            FacePart.LIPS -> 3f
            FacePart.LEFT_HAND, FacePart.RIGHT_HAND, FacePart.LEFT_ARM, FacePart.RIGHT_ARM -> 2f
            FacePart.LEFT_LEG, FacePart.RIGHT_LEG, FacePart.LEFT_FOOT, FacePart.RIGHT_FOOT -> 1f
        },
        dropX = dropX,
        dropY = dropY,
        dropWidth = dropWidth,
        dropHeight = dropHeight,
        audioUrl = audioUrl
    )
}

private fun characterFacialParts(
    resources: List<DrawableResource>,
    crops: List<PartCrop>
): List<FacePartItem> {
    require(resources.size == 6 && crops.size == 6)
    val ids = listOf(
        FacePart.LEFT_EYEBROW, FacePart.RIGHT_EYEBROW,
        FacePart.LEFT_EYE, FacePart.RIGHT_EYE, FacePart.NOSE, FacePart.LIPS
    )
    val audio = listOf(EYEBROW_AUDIO, EYEBROW_AUDIO, EYE_AUDIO, EYE_AUDIO, NOSE_AUDIO, LIPS_AUDIO)
    val dropScales = listOf(0.72f to 0.52f, 0.72f to 0.52f, 0.76f to 0.70f, 0.76f to 0.70f, 0.72f to 0.68f, 0.78f to 0.68f)
    return ids.indices.map { index ->
        val crop = crops[index]
        val x = crop.x / 1024f
        val y = crop.y / 1536f
        val width = crop.width / 1024f
        val height = crop.height / 1536f
        val (scaleX, scaleY) = dropScales[index]
        croppedCharacterPart(
            id = ids[index],
            resource = resources[index],
            crop = crop,
            dropX = x + width * (1f - scaleX) / 2f,
            dropY = y + height * (1f - scaleY) / 2f,
            dropWidth = width * scaleX,
            dropHeight = height * scaleY,
            audioUrl = audio[index]
        )
    }
}

private val LEVEL_1_PART_IDS = setOf(FacePart.LEFT_EYE, FacePart.RIGHT_EYE, FacePart.NOSE)
private val LEVEL_2_PART_IDS = setOf(FacePart.LEFT_EYEBROW, FacePart.RIGHT_EYEBROW, FacePart.LIPS)
private val LEVEL_3_PART_IDS = setOf(FacePart.LEFT_HAND, FacePart.RIGHT_HAND, FacePart.LEFT_FOOT, FacePart.RIGHT_FOOT)

private fun partsForLevel(parts: List<FacePartItem>, level: Int) = when (level) {
    0 -> parts.filter { it.id in LEVEL_1_PART_IDS }
    1 -> parts.filter { it.id in LEVEL_2_PART_IDS }
    2 -> parts.filter { it.id in LEVEL_3_PART_IDS }
    else -> error("Unsupported face/body level: $level")
}

private fun legacyFaceCharacter(
    source: FaceSet,
    level1Base: DrawableResource,
    level2Base: DrawableResource
): CharacterSet {
    val level1 = source.copy(
        id = "${source.characterId}_level_1",
        mainImage = level1Base,
        parts = partsForLevel(source.parts, 0),
        levelIndex = 0
    )
    val level2 = source.copy(
        id = "${source.characterId}_level_2",
        mainImage = level2Base,
        parts = partsForLevel(source.parts, 1),
        levelIndex = 1
    )
    return CharacterSet(source.characterId, mapOf(0 to level1, 1 to level2))
}

private fun createAdditionalCharacter(
    id: String,
    level1Base: DrawableResource,
    level2Base: DrawableResource,
    level3Base: DrawableResource,
    faceResources: List<DrawableResource>,
    faceCrops: List<PartCrop>,
    handResources: List<DrawableResource>,
    handCrops: List<PartCrop>,
    footResources: List<DrawableResource>,
    footCrops: List<PartCrop>
): CharacterSet {
    require(handResources.size == 2 && handCrops.size == 2)
    require(footResources.size == 2 && footCrops.size == 2)
    val faceParts = characterFacialParts(faceResources, faceCrops)
    val hands = listOf(FacePart.LEFT_HAND, FacePart.RIGHT_HAND).mapIndexed { index, part ->
        val crop = handCrops[index]
        val x = crop.x / 1024f
        val y = crop.y / 1536f
        val width = crop.width / 1024f
        val height = crop.height / 1536f
        croppedCharacterPart(
            part, handResources[index], crop,
            dropX = x + width * 0.10f,
            dropY = y + height * 0.10f,
            dropWidth = width * 0.80f,
            dropHeight = height * 0.78f
        )
    }
    val feet = listOf(FacePart.LEFT_FOOT, FacePart.RIGHT_FOOT).mapIndexed { index, part ->
        val crop = footCrops[index]
        val x = crop.x / 1024f
        val y = crop.y / 1536f
        val width = crop.width / 1024f
        val height = crop.height / 1536f
        croppedCharacterPart(
            part, footResources[index], crop,
            dropX = x + width * 0.07f,
            dropY = y + height * 0.12f,
            dropWidth = width * 0.86f,
            dropHeight = height * 0.76f
        )
    }
    val aspectRatio = 1024f / 1536f
    val levels = mapOf(
        0 to FaceSet("${id}_level_1", level1Base, aspectRatio, partsForLevel(faceParts, 0), id, 0),
        1 to FaceSet("${id}_level_2", level2Base, aspectRatio, partsForLevel(faceParts, 1), id, 1),
        2 to FaceSet("${id}_level_3", level3Base, aspectRatio, hands + feet, id, 2)
    )
    return CharacterSet(id, levels)
}

private val character03 = createAdditionalCharacter(
    id = "character_03",
    level1Base = Res.drawable.character_03_progression_level_1_base,
    level2Base = Res.drawable.character_03_progression_level_2_base,
    level3Base = Res.drawable.character_03_progression_level_3_base,
    faceResources = listOf(Res.drawable.character_03_left_eyebrow, Res.drawable.character_03_right_eyebrow, Res.drawable.character_03_left_eye, Res.drawable.character_03_right_eye, Res.drawable.character_03_nose, Res.drawable.character_03_lips),
    faceCrops = listOf(PartCrop(344, 229, 107, 57), PartCrop(556, 229, 109, 57), PartCrop(347, 302, 109, 90), PartCrop(538, 302, 120, 90), PartCrop(468, 376, 96, 49), PartCrop(428, 424, 166, 56)),
    handResources = listOf(Res.drawable.character_03_left_hand, Res.drawable.character_03_right_hand),
    handCrops = listOf(PartCrop(177, 868, 123, 216), PartCrop(724, 868, 123, 216)),
    footResources = listOf(Res.drawable.character_03_left_foot, Res.drawable.character_03_right_foot),
    footCrops = listOf(PartCrop(236, 1328, 225, 185), PartCrop(562, 1328, 241, 185))
)

private val character04 = createAdditionalCharacter(
    id = "character_04",
    level1Base = Res.drawable.character_04_progression_level_1_base,
    level2Base = Res.drawable.character_04_progression_level_2_base,
    level3Base = Res.drawable.character_04_progression_level_3_base,
    faceResources = listOf(Res.drawable.character_04_left_eyebrow, Res.drawable.character_04_right_eyebrow, Res.drawable.character_04_left_eye, Res.drawable.character_04_right_eye, Res.drawable.character_04_nose, Res.drawable.character_04_lips),
    faceCrops = listOf(PartCrop(335, 218, 137, 62), PartCrop(527, 218, 145, 62), PartCrop(330, 288, 145, 109), PartCrop(520, 288, 151, 109), PartCrop(448, 350, 135, 75), PartCrop(426, 420, 176, 72)),
    handResources = listOf(Res.drawable.character_04_left_hand, Res.drawable.character_04_right_hand),
    handCrops = listOf(PartCrop(163, 884, 130, 183), PartCrop(727, 884, 136, 184)),
    footResources = listOf(Res.drawable.character_04_left_foot, Res.drawable.character_04_right_foot),
    footCrops = listOf(PartCrop(236, 1328, 225, 185), PartCrop(562, 1328, 241, 185))
)

private val character05 = createAdditionalCharacter(
    id = "character_05",
    level1Base = Res.drawable.character_05_progression_level_1_base,
    level2Base = Res.drawable.character_05_progression_level_2_base,
    level3Base = Res.drawable.character_05_progression_level_3_base,
    faceResources = listOf(Res.drawable.character_05_left_eyebrow, Res.drawable.character_05_right_eyebrow, Res.drawable.character_05_left_eye, Res.drawable.character_05_right_eye, Res.drawable.character_05_nose, Res.drawable.character_05_lips),
    faceCrops = listOf(PartCrop(369, 215, 124, 55), PartCrop(529, 215, 147, 55), PartCrop(357, 276, 140, 94), PartCrop(523, 276, 151, 94), PartCrop(465, 340, 100, 75), PartCrop(435, 395, 157, 71)),
    handResources = listOf(Res.drawable.character_05_left_hand, Res.drawable.character_05_right_hand),
    handCrops = listOf(PartCrop(178, 880, 112, 175), PartCrop(730, 880, 106, 175)),
    footResources = listOf(Res.drawable.character_05_left_foot, Res.drawable.character_05_right_foot),
    footCrops = listOf(PartCrop(236, 1328, 225, 185), PartCrop(562, 1328, 241, 185))
)

private val character06 = createAdditionalCharacter(
    id = "character_06",
    level1Base = Res.drawable.character_06_progression_level_1_base,
    level2Base = Res.drawable.character_06_progression_level_2_base,
    level3Base = Res.drawable.character_06_progression_level_3_base,
    faceResources = listOf(Res.drawable.character_06_left_eyebrow, Res.drawable.character_06_right_eyebrow, Res.drawable.character_06_left_eye, Res.drawable.character_06_right_eye, Res.drawable.character_06_nose, Res.drawable.character_06_lips),
    faceCrops = listOf(PartCrop(339, 224, 146, 62), PartCrop(529, 224, 155, 62), PartCrop(338, 292, 151, 105), PartCrop(524, 292, 157, 105), PartCrop(455, 366, 117, 64), PartCrop(420, 420, 180, 66)),
    handResources = listOf(Res.drawable.character_06_left_hand, Res.drawable.character_06_right_hand),
    handCrops = listOf(PartCrop(172, 915, 122, 165), PartCrop(730, 915, 122, 165)),
    footResources = listOf(Res.drawable.character_06_left_foot, Res.drawable.character_06_right_foot),
    footCrops = listOf(PartCrop(236, 1328, 225, 185), PartCrop(562, 1328, 241, 185))
)

private val character07 = createAdditionalCharacter(
    id = "character_07",
    level1Base = Res.drawable.character_07_progression_level_1_base,
    level2Base = Res.drawable.character_07_progression_level_2_base,
    level3Base = Res.drawable.character_07_progression_level_3_base,
    faceResources = listOf(Res.drawable.character_07_left_eyebrow, Res.drawable.character_07_right_eyebrow, Res.drawable.character_07_left_eye, Res.drawable.character_07_right_eye, Res.drawable.character_07_nose, Res.drawable.character_07_lips),
    faceCrops = listOf(PartCrop(350, 230, 135, 46), PartCrop(526, 230, 144, 46), PartCrop(348, 295, 151, 102), PartCrop(516, 295, 161, 102), PartCrop(468, 369, 94, 49), PartCrop(440, 430, 145, 50)),
    handResources = listOf(Res.drawable.character_07_left_hand, Res.drawable.character_07_right_hand),
    handCrops = listOf(PartCrop(175, 895, 113, 155), PartCrop(690, 895, 160, 155)),
    footResources = listOf(Res.drawable.character_07_left_foot, Res.drawable.character_07_right_foot),
    footCrops = listOf(PartCrop(236, 1328, 225, 185), PartCrop(562, 1328, 241, 185))
)

private val characterSetRegistry = listOf(
    legacyFaceCharacter(faceSetRegistry[0], Res.drawable.face_01_progression_level_1_base, Res.drawable.face_01_progression_level_2_base),
    legacyFaceCharacter(faceSetRegistry[1], Res.drawable.face_02_progression_level_1_base, Res.drawable.face_02_progression_level_2_base),
    character03,
    character04,
    character05,
    character06,
    character07
)

private fun validateFaceSets(faceSets: List<FaceSet>) {
    require(faceSets.isNotEmpty()) { "At least one face set is required." }
    require(faceSets.map { it.id }.toSet().size == faceSets.size) { "Face set IDs must be unique." }
    faceSets.forEach { faceSet ->
        require(faceSet.id.isNotBlank()) { "Face set IDs cannot be blank." }
        require(faceSet.characterId.isNotBlank() && faceSet.levelIndex in 0..2) {
            "Face set ${faceSet.id} has an invalid character or level."
        }
        require(faceSet.aspectRatio > 0f && faceSet.aspectRatio.isFinite()) {
            "Face set ${faceSet.id} must have a finite positive aspect ratio."
        }
        require(faceSet.parts.isNotEmpty()) { "Face set ${faceSet.id} must contain parts." }
        val partIds = faceSet.parts.map { it.id }
        require(partIds.toSet().size == partIds.size) { "Face set ${faceSet.id} has duplicate part IDs." }
        val expectedPartIds = when (faceSet.levelIndex) {
            0 -> LEVEL_1_PART_IDS
            1 -> LEVEL_2_PART_IDS
            2 -> LEVEL_3_PART_IDS
            else -> error("Unsupported level ${faceSet.levelIndex}")
        }
        require(partIds.toSet() == expectedPartIds) {
            "Face set ${faceSet.id} must contain exactly the required parts for level ${faceSet.levelIndex + 1}."
        }
        faceSet.parts.forEach { part ->
            require(part.targetOffsetXPercent.isFinite() && part.targetOffsetYPercent.isFinite() &&
                part.widthPercent.isFinite() && part.heightPercent.isFinite() &&
                part.targetOffsetXPercent >= 0f && part.targetOffsetYPercent >= 0f &&
                part.widthPercent > 0f && part.heightPercent > 0f &&
                part.targetOffsetXPercent + part.widthPercent <= 1f &&
                part.targetOffsetYPercent + part.heightPercent <= 1f
            ) { "Face set ${faceSet.id} has invalid visual geometry for ${part.id}." }
            val dropX = part.targetOffsetXPercent + part.dropZoneOffsetXPercent
            val dropY = part.targetOffsetYPercent + part.dropZoneOffsetYPercent
            require(part.dropZoneWidthPercent.isFinite() && part.dropZoneHeightPercent.isFinite() &&
                dropX.isFinite() && dropY.isFinite() &&
                part.dropZoneWidthPercent > 0f && part.dropZoneHeightPercent > 0f &&
                dropX >= 0f && dropY >= 0f &&
                dropX + part.dropZoneWidthPercent <= 1f &&
                dropY + part.dropZoneHeightPercent <= 1f
            ) { "Face set ${faceSet.id} has invalid drop-zone geometry for ${part.id}." }
        }
    }
}

private fun validateCharacterSets(characterSets: List<CharacterSet>) {
    require(characterSets.isNotEmpty())
    require(characterSets.map { it.id }.toSet().size == characterSets.size) {
        "Character set IDs must be unique."
    }
    characterSets.forEach { character ->
        require(character.id.isNotBlank() && character.levels.isNotEmpty())
        character.levels.forEach { (level, activity) ->
            require(level in 0..2 && activity.levelIndex == level && activity.characterId == character.id) {
                "Character ${character.id} has an incorrectly owned activity for level $level."
            }
        }
    }
    validateFaceSets(characterSets.flatMap { it.levels.values })
}

class FaceGameViewModel(
    private val progressRepository: ProgressRepository
) : ViewModel() {
    val availableCharacterSets: List<CharacterSet> = characterSetRegistry
    private val fullRunCharacterSets = availableCharacterSets.filter { character ->
        (0..2).all { it in character.levels }
    }

    var levelIndex by mutableStateOf(0)
        private set
    val currentLevel: Int get() = levelIndex + 1

    var stars by mutableStateOf(0)
        private set

    var activeCharacterSet by mutableStateOf(characterSetRegistry.first())
        private set

    var activeFaceSet by mutableStateOf(characterSetRegistry.first().levels.getValue(0))
        private set

    private var childId: Int? = null
    private var progressLoadGeneration = 0
    private var previousCharacterId: String? = null
    private var nextLevelAfterCompletion: Int? = null
    private var pendingNextCharacterId: String? = null

    var isProgressLoading by mutableStateOf(true)
        private set

    var progressError by mutableStateOf<String?>(null)
        private set

    var isCompleting by mutableStateOf(false)
        private set

    var completionError by mutableStateOf<String?>(null)
        private set

    var finalizationRetryKey by mutableStateOf(0)
        private set

    var isLevelUpWin by mutableStateOf(false)
        private set

    var isAllLevelsCompletedWin by mutableStateOf(false)
        private set

    var isCurrentlyDragging by mutableStateOf(false)
        private set

    var isWin by mutableStateOf(false)
        private set

    var items by mutableStateOf(activeFaceSet.parts.toList())
        private set

    val placedParts = mutableStateListOf<FacePartItem>()

    init {
        validateCharacterSets(availableCharacterSets)
        require(fullRunCharacterSets.isNotEmpty()) { "At least one character must support all three levels." }
    }

    fun loadGame(selectedChildId: Int?) {
        val generation = ++progressLoadGeneration
        childId = selectedChildId
        isProgressLoading = true
        progressError = null
        if (selectedChildId == null) {
            val character = chooseCharacter(excluding = previousCharacterId)
            applyProgress(FaceGameProgressDTO(0, 0, character.id), character)
            isProgressLoading = false
            return
        }
        viewModelScope.launch {
            progressRepository.getFaceGameProgress(selectedChildId).fold(
                onSuccess = { response ->
                    if (generation != progressLoadGeneration) return@fold
                    val progress = response.data
                    if (response.success && progress != null) {
                        restoreOrStartRun(progress, generation)
                    } else {
                        progressError = response.message.ifBlank { "خطا در بارگذاری پیشرفت بازی" }
                        isProgressLoading = false
                    }
                },
                onFailure = {
                    if (generation != progressLoadGeneration) return@fold
                    progressError = "خطا در اتصال برای بارگذاری پیشرفت بازی"
                    isProgressLoading = false
                }
            )
        }
    }

    fun retryProgressLoad() = loadGame(childId)

    private fun restoreOrStartRun(progress: FaceGameProgressDTO, generation: Int) {
        val storedCharacter = progress.activeCharacterId?.let { id ->
            fullRunCharacterSets.firstOrNull { it.id == id }
        }
        if (storedCharacter != null) {
            applyProgress(progress, storedCharacter)
            isProgressLoading = false
            return
        }

        // Old rows have no character. The server preserves unfinished levels and
        // converts an old terminal (2,1) row into a fresh run during this operation.
        val candidate = chooseCharacter(excluding = previousCharacterId)
        val selectedChildId = childId ?: return
        viewModelScope.launch {
            progressRepository.startFaceGameRun(selectedChildId, candidate.id).fold(
                onSuccess = { response ->
                    if (generation != progressLoadGeneration) return@fold
                    val persisted = response.data
                    val character = persisted?.activeCharacterId?.let { id ->
                        fullRunCharacterSets.firstOrNull { it.id == id }
                    }
                    if (response.success && persisted != null && character != null) {
                        applyProgress(persisted, character)
                        isProgressLoading = false
                    } else {
                        progressError = response.message.ifBlank { "شروع بازی ثبت نشد؛ دوباره تلاش کن" }
                        isProgressLoading = false
                    }
                },
                onFailure = {
                    if (generation != progressLoadGeneration) return@fold
                    progressError = "برای ثبت شخصیت بازی دوباره تلاش کن"
                    isProgressLoading = false
                }
            )
        }
    }

    private fun applyProgress(progress: FaceGameProgressDTO, character: CharacterSet) {
        levelIndex = progress.levelIndex.coerceIn(0, 2)
        stars = (levelIndex + progress.successfulGames.coerceIn(0, 1)).coerceIn(0, 3)
        activeCharacterSet = character
        activeFaceSet = character.levels.getValue(levelIndex)
        previousCharacterId = character.id
        pendingNextCharacterId = null
        nextLevelAfterCompletion = null
        resetCurrentRound()
        if (levelIndex == 2 && progress.successfulGames > 0) {
            // A completed run remains recoverable if the app closed during celebration.
            placedParts.addAll(activeFaceSet.parts)
            items = emptyList()
            isAllLevelsCompletedWin = true
            isWin = true
        }
    }

    private fun chooseCharacter(excluding: String?): CharacterSet {
        val alternatives = fullRunCharacterSets.filter { it.id != excluding }
        return (alternatives.ifEmpty { fullRunCharacterSets }).random()
    }

    private fun submitCompletion() {
        if (isCompleting || isWin) return
        isCompleting = true
        completionError = null
        val completedLevel = levelIndex
        viewModelScope.launch {
            if (childId == null) {
                val next = if (completedLevel < 2) {
                    FaceGameProgressDTO(completedLevel + 1, 0, activeCharacterSet.id)
                } else {
                    FaceGameProgressDTO(2, 1, activeCharacterSet.id)
                }
                acceptCompletion(completedLevel, next)
                return@launch
            }
            progressRepository.completeFaceGame(childId!!, completedLevel, activeCharacterSet.id).fold(
                onSuccess = { response ->
                    val progress = response.data
                    if (response.success && progress != null) {
                        acceptCompletion(completedLevel, progress)
                    } else {
                        isCompleting = false
                        completionError = response.message.ifBlank { "ثبت پیشرفت انجام نشد؛ دوباره تلاش کن" }
                    }
                },
                onFailure = {
                    isCompleting = false
                    completionError = "برای ثبت پیشرفت دوباره تلاش کن"
                }
            )
        }
    }

    private fun acceptCompletion(completedLevel: Int, progress: FaceGameProgressDTO) {
        if (progress.activeCharacterId != activeCharacterSet.id) {
            isCompleting = false
            completionError = "شخصیت فعال بازی تغییر کرده است؛ پیشرفت را دوباره بارگذاری کن"
            return
        }
        val hadCompletedAllLevels = stars >= 3
        stars = (progress.levelIndex + progress.successfulGames).coerceIn(0, 3)
        nextLevelAfterCompletion = progress.levelIndex.coerceIn(0, 2)
        isLevelUpWin = completedLevel < 2 && progress.levelIndex > completedLevel
        isAllLevelsCompletedWin = completedLevel == 2 && !hadCompletedAllLevels
        isCompleting = false
        isWin = true
    }

    fun retryCompletion() {
        if (isAllLevelsCompletedWin && stars == 3) {
            finalizeCompletedRun()
        } else if (placedParts.size == activeFaceSet.parts.size) {
            submitCompletion()
        }
    }

    fun finishCelebration() {
        if (isAllLevelsCompletedWin) {
            finalizeCompletedRun()
            return
        }
        if (isLevelUpWin && levelIndex < 2) {
            levelIndex = (nextLevelAfterCompletion ?: (levelIndex + 1)).coerceIn(levelIndex + 1, 2)
            activeFaceSet = activeCharacterSet.levels.getValue(levelIndex)
        }
        nextLevelAfterCompletion = null
        resetCurrentRound()
    }

    private fun finalizeCompletedRun() {
        if (isCompleting || levelIndex != 2 || stars != 3) return
        val completedCharacterId = activeCharacterSet.id
        val nextCharacterId = pendingNextCharacterId ?: chooseCharacter(excluding = completedCharacterId)
            .id.also { pendingNextCharacterId = it }
        completionError = null
        if (childId == null) {
            val nextCharacter = fullRunCharacterSets.first { it.id == nextCharacterId }
            applyProgress(FaceGameProgressDTO(0, 0, nextCharacter.id), nextCharacter)
            return
        }

        isCompleting = true
        viewModelScope.launch {
            progressRepository.finalizeFaceGameRun(
                childId = childId!!,
                completedCharacterId = completedCharacterId,
                nextCharacterId = nextCharacterId
            ).fold(
                onSuccess = { response ->
                    val progress = response.data
                    val nextCharacter = progress?.activeCharacterId?.let { id ->
                        fullRunCharacterSets.firstOrNull { it.id == id }
                    }
                    if (response.success && progress != null && nextCharacter != null &&
                        progress.levelIndex == 0 && progress.successfulGames == 0
                    ) {
                        isCompleting = false
                        applyProgress(progress, nextCharacter)
                    } else {
                        isCompleting = false
                        completionError = response.message.ifBlank { "شروع مرحله‌ی بعد ثبت نشد؛ دوباره تلاش کن" }
                        finalizationRetryKey++
                    }
                },
                onFailure = {
                    isCompleting = false
                    completionError = "برای شروع بازی بعدی دوباره تلاش کن"
                    finalizationRetryKey++
                }
            )
        }
    }

    fun startDragging() {
        isCurrentlyDragging = true
    }

    fun stopDragging() {
        isCurrentlyDragging = false
    }

    fun addPlacedPart(item: FacePartItem) {
        if (activeFaceSet.parts.none { it == item } || placedParts.any { it.id == item.id }) return
        placedParts.add(item)
        items = items.filter { it.id != item.id }
        if (placedParts.size == activeFaceSet.parts.size) {
            submitCompletion()
        }
    }

    /** Resets progress while keeping the currently selected face set. */
    fun resetCurrentRound() {
        placedParts.clear()
        items = activeFaceSet.parts.toList()
        isWin = false
        isCurrentlyDragging = false
        isCompleting = false
        completionError = null
        isLevelUpWin = false
        isAllLevelsCompletedWin = false
    }
}
