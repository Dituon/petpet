import moe.dituon.petpet.core.clip.BorderRadius
import moe.dituon.petpet.core.position.FitType
import moe.dituon.petpet.core.transform.Offset
import org.junit.jupiter.api.Test
import utils.ImageCssComparator

class AvatarTest {
    @Test
    fun testImageFitAndPosition() {
        val imageComparator = ImageCssComparator("image-fit-and-position")
        imageComparator.addImagePart(
            "fit-fill-position-center",
            listOf(
                "object-fit: fill",
                "object-position: center"
            )
        ) {
            it.fit(FitType.FILL).position(Offset.CENTER)
        }
        imageComparator.addImagePart(
            "fit-contain-position-center",
            listOf(
                "object-fit: contain",
                "object-position: center"
            )
        ) {
            it.fit(FitType.CONTAIN).position(Offset.CENTER)
        }
        imageComparator.addImagePart(
            "fit-cover-position-center",
            listOf(
                "object-fit: cover",
                "object-position: center"
            )
        ) {
            it.fit(FitType.COVER).position(Offset.CENTER)
        }
        imageComparator.addImagePart(
            "fit-cover-position-20p20px",
            listOf(
                "object-fit: cover",
                "object-position: 20% 20px"
            )
        ) {
            it.fit(FitType.COVER).position(Offset.fromString("20% 20px"))
        }
        imageComparator.addImagePart(
            "fit-cover-position--100px-60p",
            listOf(
                "object-fit: cover",
                "object-position: -100px -60%"
            )
        ) {
            it.fit(FitType.COVER).position(Offset.fromString("-100px -60%"))
        }
        imageComparator.addImagePart(
            "fit-contain-position-20p20px",
            listOf(
                "object-fit: contain",
                "object-position: 20% 20px"
            )
        ) {
            it.fit(FitType.CONTAIN).position(Offset.fromString("20% 20px"))
        }
        imageComparator.addImagePart(
            "fit-contain-position--100px-60p",
            listOf(
                "object-fit: contain",
                "object-position: -100px -60%"
            )
        ) {
            it.fit(FitType.CONTAIN).position(Offset.fromString("-100px -60%"))
        }
        imageComparator.finish()
    }

    @Test
    fun testImageAngleAndOrigin() {
        val imageComparator = ImageCssComparator("image-angle-and-origin")
        imageComparator.addImagePart(
            "angle-45-origin-center",
            listOf(
                "transform: rotate(45deg)",
                "transform-origin: center"
            )
        ) {
            it.angle(45f).origin(Offset.CENTER)
        }
        imageComparator.addImagePart(
            "angle-75-origin-20px20px",
            listOf(
                "transform: rotate(75deg)",
                "transform-origin: 20px 20px"
            )
        ) {
            it.angle(75f).origin(Offset.fromString("20px 20px"))
        }
        imageComparator.addImagePart(
            "angle-45-origin-80p-30px",
            listOf(
                "transform: rotate(45deg)",
                "transform-origin: 80% -30px"
            )
        ) {
            it.angle(45f).origin(Offset.fromString("80% -30px"))
        }
        imageComparator.addImagePart(
            "angle-45-origin--20px-20p",
            listOf(
                "transform: rotate(45deg)",
                "transform-origin: -20px -20%"
            )
        ) {
            it.angle(45f).origin(Offset.fromString("-20px -20%"))
        }
        imageComparator.finish()
    }

    @Test
    fun testImageBorderRadius() {
        val imageComparator = ImageCssComparator("image-border-radius")
        imageComparator.addImagePart(
            "border-radius-50p",
            listOf(
                "border-radius: 50%"
            )
        ) {
            it.borderRadius(BorderRadius.fromString("50%"))
        }
        imageComparator.addImagePart(
            "border-radius-50p20p",
            listOf(
                "border-radius: 50% 20%"
            )
        ) {
            it.borderRadius(BorderRadius.fromString("50% 20%"))
        }
        imageComparator.addImagePart(
            "border-radius-30px020p0",
            listOf(
                "border-radius: 30px 0 20px 0"
            )
        ) {
            it.borderRadius(BorderRadius.fromString("30px 0 20px 0"))
        }
        imageComparator.addImagePart(
            "border-radius-20p40px5p",
            listOf(
                "border-radius: 20% 40px 5%"
            )
        ) {
            it.borderRadius(BorderRadius.fromString("20% 40px 5%"))
        }
        imageComparator.finish()
    }
}
