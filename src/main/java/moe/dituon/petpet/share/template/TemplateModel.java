package moe.dituon.petpet.share.template;

import moe.dituon.petpet.share.ImageSynthesis;
import moe.dituon.petpet.share.element.FrameInfo;
import moe.dituon.petpet.share.element.avatar.AvatarModel;
import moe.dituon.petpet.share.element.text.TextModel;
import moe.dituon.petpet.share.service.GifEncoder;
import moe.dituon.petpet.share.service.GifEncoderParam;
import moe.dituon.petpet.share.template.background.BackgroundModel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TemplateModel {
    protected final List<AvatarModel> avatarList;
    protected final List<TextModel> textList;
    protected final BackgroundModel background;
    protected final ExtraData extraData;


    public TemplateModel(
            ExtraData extraData,
            BackgroundModel background,
            List<AvatarModel> avatarList,
            List<TextModel> textList
    ) {
        this.background = background;
        this.avatarList = avatarList;
        this.textList = textList;
        this.extraData = extraData;
    }

    public ResultImage getResult() throws IOException {
        var topAvatarList = new ArrayList<AvatarModel>(avatarList.size());
        var bottomAvatarList = new ArrayList<AvatarModel>(avatarList.size());

        var bgs = background.getImages();

        int totalLength = bgs.length;
        for (AvatarModel avatar : avatarList) {
            if (avatar.isOnTop()) {
                topAvatarList.add(avatar);
            } else {
                bottomAvatarList.add(avatar);
            }
            totalLength = Math.max(totalLength, avatar.getImageList().size());
        }


        boolean gifFlag = totalLength > 1;
        List<BufferedImage> output;
        if (bottomAvatarList.isEmpty() &&
                (bgs[0].getType() == BufferedImage.TYPE_3BYTE_BGR || !gifFlag)
        ) {
            output = ImageSynthesis.execImageList(totalLength, (i) -> {
                var base = bgs[i % bgs.length];
                var info = new FrameInfo(i, base.getWidth(), base.getHeight());
                var g2d = base.createGraphics();
                topAvatarList.forEach(avatar -> avatar.draw(g2d, info));
                textList.forEach(text -> text.draw(g2d, info));
                return base;
            });
        } else {
            output = ImageSynthesis.execImageList(totalLength, (i) -> {
                var base = bgs[i % bgs.length];
                var info = new FrameInfo(i, base.getWidth(), base.getHeight());
                var canvas = new BufferedImage(
                        base.getWidth(), base.getHeight(),
                        gifFlag ? BufferedImage.TYPE_3BYTE_BGR : base.getType()
                );
                var g2d = canvas.createGraphics();
                bottomAvatarList.forEach(avatar -> avatar.draw(g2d, info));
                g2d.drawImage(base, 0, 0, null);
                topAvatarList.forEach(avatar -> avatar.draw(g2d, info));
                textList.forEach(text -> text.draw(g2d, info));
                return canvas;
            });
        }

        byte[] blob;
        if (gifFlag) {
            blob = GifEncoder.makeGifUseAnimatedLib(output, new GifEncoderParam());
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(16384);
            // TODO other image format
            ImageIO.write(output.get(0), "png", baos);
            blob = baos.toByteArray();
        }

        return new ResultImage(
                blob,
                output.get(0).getWidth(),
                output.get(0).getHeight(),
                totalLength > 1 ? "gif" : "png",
                totalLength > 1 ? "image/gif" : "image/png"
        );
    }
}
