package moe.dituon.petpet.core;

import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonArray;
import moe.dituon.petpet.share.AvatarData;
import moe.dituon.petpet.share.AvatarPosType;
import moe.dituon.petpet.share.AvatarType;
import moe.dituon.petpet.share.FitType;
import moe.dituon.petpet.share.element.FrameInfo;
import moe.dituon.petpet.share.element.avatar.AvatarBuilder;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AvatarTest {
    public static final String outputDir = ".test-output/avatar/";
    static final FitType[] textFits = FitType.values();

    public static void saveImage(BufferedImage image, String name) throws IOException {
        var path = outputDir + name + ".png";
        var file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            file.createNewFile();
        }
        ImageIO.write(image, "png", file);
    }

    @Test
    public void testZoom() throws IOException {
        var data = new AvatarData(AvatarType.TO);
        data.setPosType(AvatarPosType.ZOOM);
        data.setPos(jsonArrayFromString("[0, 0, 200, 200]"));
        var builder = new AvatarBuilder(data);
        var avatarRaw = ImageIO.read(new File("example-data/input/avatar1.png"));

        var image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        var g2d = image.createGraphics();

        var avatar = builder.build(() -> List.of(avatarRaw));
        avatar.draw(g2d, FrameInfo.fromImage(image, 0));
        saveImage(image, "avatarZoom");
    }

    @Test
    public void testDeform() throws IOException {
        var data = new AvatarData(AvatarType.TO);
        data.setPosType(AvatarPosType.DEFORM);
        data.setPos(jsonArrayFromString("[[0,0],[0,200],[200,200],[200,0],[0,0]]"));
        var builder = new AvatarBuilder(data);
        var avatarRaw = ImageIO.read(new File("example-data/input/avatar1.png"));

        var image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        var g2d = image.createGraphics();

        var avatar = builder.build(() -> List.of(avatarRaw));
        avatar.draw(g2d, FrameInfo.fromImage(image, 0));
        saveImage(image, "avatarDeform");
    }

    static JsonArray jsonArrayFromString(String str) {
        return Json.Default.decodeFromString(JsonArray.Companion.serializer(), str);
    }
}
