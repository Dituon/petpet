package moe.dituon.petpet.share.position;

import kotlinx.serialization.json.JsonArray;
import moe.dituon.petpet.share.AvatarPosType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class PositionCollectionFactory {
    public static PositionCollection<?> createCollection(JsonArray posElements, AvatarPosType type) {
        switch (type) {
            case ZOOM:
                return PositionCollectionFactory.createXYWHCollection(posElements);
            case DEFORM:
                return PositionCollectionFactory.createP4ACollection(posElements);
            default:
                throw new RuntimeException();
        }
    }

    @NotNull
    @Contract("!null -> new")
    public static PositionXYWHCollection createXYWHCollection(JsonArray posElements) {
        try {
            return PositionXYWHCollection.fromJson(posElements);
        } catch (NumberFormatException ignored) {
            return PositionDynamicXYWHCollection.fromJson(posElements);
        }
    }

    @NotNull
    @Contract("!null -> new")
    public static PositionP4ACollection createP4ACollection(JsonArray posElements) {
        return PositionP4ACollection.fromJson(posElements);
    }
}
