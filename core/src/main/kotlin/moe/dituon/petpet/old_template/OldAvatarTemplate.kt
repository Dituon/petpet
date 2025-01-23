package moe.dituon.petpet.old_template

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import moe.dituon.petpet.core.clip.BorderRadius
import moe.dituon.petpet.core.filter.ImageFilterList
import moe.dituon.petpet.core.length.LengthType
import moe.dituon.petpet.core.length.NumberLength
import moe.dituon.petpet.core.position.FitType
import moe.dituon.petpet.core.transform.Offset
import moe.dituon.petpet.core.transition.RotateTransition
import moe.dituon.petpet.core.utils.image.ImageCropper
import moe.dituon.petpet.core.utils.image.ImageXYWHCropper
import moe.dituon.petpet.template.element.AvatarTemplate
import moe.dituon.petpet.template.fields.*
import moe.dituon.petpet.template.fields.length.AvatarCoordsListSerializer
import moe.dituon.petpet.uitls.GlobalJson

@Serializable
data class OldAvatarTemplate @JvmOverloads constructor(
    val type: AvatarType,
    var pos: JsonArray,
    var posType: AvatarPosType = AvatarPosType.ZOOM,
    var crop: IntArray? = null,
    var cropType: CropType = CropType.NONE,
    var fit: FitType = FitType.FILL,
    var style: List<AvatarStyle> = emptyList(),
    var filter: ImageFilterListElement = ImageFilterList.EMPTY,
    var angle: Short = 0,
    var origin: TransformOrigin = TransformOrigin.DEFAULT,
    var opacity: Float = 1.0F,
    var round: Boolean = false,
    var rotate: Boolean = false,
    var avatarOnTop: Boolean = true,
    var antialias: Boolean? = null,
    var resampling: Boolean? = null
) {
    fun toTemplate(index: Int): AvatarTemplate {
        fun replacePosElement(array: JsonArray): JsonArray {
            val modifiedArray = JsonArray(array.map { element ->
                when (element) {
                    is JsonArray -> replacePosElement(element)
                    is JsonObject -> element // 保持 JsonObject 不变
                    is JsonPrimitive -> {
                        if (element.isString) {
                            JsonPrimitive(
                                element.content.replace("width", "100cw")
                                    .replace("height", "100ch")
                            )
                        } else {
                            element
                        }
                    }

                    JsonNull -> JsonNull
                    else -> element
                }
            })
            return modifiedArray
        }

        val coords = GlobalJson.decodeFromJsonElement(AvatarCoordsListSerializer, replacePosElement(pos))
        val cropper = if (crop == null) {
            ImageCropper.empty()
        } else {
            when (cropType) {
                CropType.NONE -> ImageCropper.empty()
                CropType.PIXEL -> when (crop!!.size) {
                    2 -> ImageXYWHCropper(
                        NumberLength.px(crop!![0]),
                        NumberLength.px(crop!![1])
                    )

                    4 -> ImageXYWHCropper(
                        NumberLength.px(crop!![0]),
                        NumberLength.px(crop!![1]),
                        NumberLength.px(crop!![2]),
                        NumberLength.px(crop!![3])
                    )

                    else -> ImageCropper.empty()
                }

                CropType.PERCENT -> when (crop!!.size) {
                    2 -> ImageXYWHCropper(
                        NumberLength(
                            crop!![0].toFloat(),
                            LengthType.CW
                        ),
                        NumberLength(
                            crop!![1].toFloat(),
                            LengthType.CH
                        )
                    )

                    4 -> ImageXYWHCropper(
                        NumberLength(
                            crop!![0].toFloat(),
                            LengthType.CW
                        ),
                        NumberLength(
                            crop!![1].toFloat(),
                            LengthType.CH
                        ),
                        NumberLength(
                            crop!![2].toFloat(),
                            LengthType.CW
                        ),
                        NumberLength(
                            crop!![3].toFloat(),
                            LengthType.CH
                        ),
                    )

                    else -> ImageCropper.empty()
                }
            }
        }

        val styleFilterList = style.map {
            when (it) {
                AvatarStyle.MIRROR -> ImageMirrorFilter()
                AvatarStyle.FLIP -> ImageFlipFilter()
                AvatarStyle.GRAY -> ImageGrayFilter()
                AvatarStyle.BINARIZATION -> ImageBinarizeFilter()
            }
        }

        return AvatarTemplate(
            id = "avatar$index",
            key = listOf(type.toString()),
            coords = coords,
            crop = listOf(cropper),
            fit = listOf(fit),
            filter = if (style.isEmpty()) filter else {
                if (filter.isEmpty()) {
                    ImageFilterList(styleFilterList)
                } else {
                    val tf = ArrayList(filter)
                    tf.addAll(styleFilterList)
                    ImageFilterListElement(tf)
                }
            },
            angle = floatArrayOf(angle.toFloat()),
            rotate = if (rotate) RotateTransition.DEFAULT else null,
            origin = listOf(
                when (origin) {
                    TransformOrigin.DEFAULT -> Offset.LEFT_TOP
                    TransformOrigin.CENTER -> Offset.CENTER
                }
            ),
            opacity = floatArrayOf(opacity),
            borderRadius = if (round) listOf(BorderRadius.ROUND) else emptyList(),
            position = listOf(Offset.CENTER),
        )
    }
}