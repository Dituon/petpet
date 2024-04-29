package moe.dituon.petpet.share.element.avatar;

import moe.dituon.petpet.share.AvatarData;
import moe.dituon.petpet.share.AvatarPosType;
import moe.dituon.petpet.share.position.PositionCollection;
import moe.dituon.petpet.share.position.PositionCollectionFactory;

public class AvatarFactory {
    private AvatarPosType posType;
    private PositionCollection<?> pos;

    public AvatarFactory(AvatarData data) {
        posType = data.getPosType();
        pos = PositionCollectionFactory.createCollection(data.getPos(), posType);

    }
}
