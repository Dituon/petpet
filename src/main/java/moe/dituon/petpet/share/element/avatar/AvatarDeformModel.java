package moe.dituon.petpet.share.element.avatar;

import moe.dituon.petpet.share.AvatarData;
import moe.dituon.petpet.share.ImageDeformer;
import moe.dituon.petpet.share.element.FrameInfo;
import moe.dituon.petpet.share.position.PositionP4ACollection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Supplier;

public class AvatarDeformModel extends AvatarModel {
    final PositionP4ACollection pos;

    AvatarDeformModel(
            AvatarData data,
            Supplier<List<BufferedImage>> imageSupplier,
            PositionP4ACollection pos
    ) {
        super(data, imageSupplier);
        this.pos = pos;
    }

    @Override
    public void draw(Graphics2D g2d, FrameInfo info) {
        var pos = this.pos.getPosition(info.index);
        var anchorPos = this.pos.getAnchor(info.index);
        var avatarImage = super.imageList.get(info.index % super.imageList.size());
        BufferedImage result = ImageDeformer.computeImage(avatarImage, pos);
        g2d.drawImage(result, anchorPos[0], anchorPos[1], null);
    }

    @Override
    public int getPosLength() {
        return pos.size();
    }
}
