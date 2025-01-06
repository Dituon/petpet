package moe.dituon.petpet.core.position;

public class AvatarEmptyCoords extends AvatarCoords {
    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public CoordType getType() {
        return CoordType.EMPTY;
    }
}
