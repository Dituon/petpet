package moe.dituon.petpet.share.element.avatar;

import moe.dituon.petpet.share.AvatarData;
import moe.dituon.petpet.share.AvatarPosType;
import moe.dituon.petpet.share.position.PositionCollection;
import moe.dituon.petpet.share.position.PositionCollectionFactory;
import moe.dituon.petpet.share.position.PositionP4ACollection;
import moe.dituon.petpet.share.position.PositionXYWHCollection;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Supplier;

public class AvatarFactory {
    private final AvatarData data;
    private final AvatarPosType posType;
    private final PositionCollection<?> pos;

    public AvatarFactory(AvatarData data) {
        posType = data.getPosType();
        pos = PositionCollectionFactory.createCollection(data.getPos(), posType);
        this.data = data;
    }

    public AvatarModel build(Supplier<List<BufferedImage>> supplier) {
        switch (posType) {
            case ZOOM:
                return new AvatarXYWHModel(data, supplier, (PositionXYWHCollection) pos);
            case DEFORM:
                return new AvatarDeformModel(data, supplier, (PositionP4ACollection) pos);
        }
        throw new RuntimeException();
    }
}
