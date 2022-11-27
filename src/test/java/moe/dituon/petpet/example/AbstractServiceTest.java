package moe.dituon.petpet.example;

import kotlin.Pair;
import moe.dituon.petpet.share.BasePetService;
import moe.dituon.petpet.share.GifAvatarExtraDataProvider;
import moe.dituon.petpet.share.TextData;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public abstract class AbstractServiceTest extends AbstractTest {

    static BasePetService petService = new BasePetService();

    protected void testGeneral(String saveName, String key, List<TextData> additionTextDatas) {
        Pair<InputStream, String> resultStreamAndType = petService.generateImage(
                key,
                new GifAvatarExtraDataProvider(
                        () -> List.of(avatarImage1),
                        () -> List.of(avatarImage2),
                        null,
                        null,
                        null
                ),
                null,
                additionTextDatas
        );
        String finalSaveName = OUTPUT_ROOT + getClass().getSimpleName() + "-" + saveName + "." + resultStreamAndType.getSecond();
        copyInputStreamToFile(resultStreamAndType.getFirst(), new File(finalSaveName));
        System.out.println("test " + key + " done.");
    }

}
