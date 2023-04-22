package moe.dituon.petpet.share;

import kotlin.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class BaseImageMaker {
    protected BaseGifMaker gifMaker;

    /**
     * 同时构造 BaseGifMaker 实例
     * <br/>
     * 推荐直接传入 gifMaker 构造, 共用线程池
     */
    public BaseImageMaker() {
        this.gifMaker = new BaseGifMaker();
    }

    /**
     * 同时构造 BaseGifMaker 实例, 指定线程池容量
     * <br/>
     * 推荐直接传入 gifMaker 构造, 共用一个线程池
     *
     * @param threadPoolSize: BaseGifMaker 线程池容量
     */
    public BaseImageMaker(int threadPoolSize) {
        this.gifMaker = new BaseGifMaker(threadPoolSize);
    }

    /**
     * @param gifMaker: avatar 可能为 Gif 格式, 需要提供 GifMaker 实例
     */
    public BaseImageMaker(BaseGifMaker gifMaker) {
        this.gifMaker = gifMaker;
    }

    public Pair<InputStream, String> makeImage(
            ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
            BufferedImage sticker, GifRenderParams params) {
        for (AvatarModel avatar : avatarList) {
            if (avatar.isGif()) return new Pair<>(
                    gifMaker.makeGIF(avatarList, textList, sticker, params),
                    "gif"
            );
        }
        try {
            return new Pair<>(
                    bufferedImageToInputStream(ImageSynthesis.synthesisImage(
                            sticker, avatarList, textList, params.getAntialias(), true
                    )),
                    "png"
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream bufferedImageToInputStream(BufferedImage bf) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bf, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
