package moe.dituon.petpet.core.transform;

import moe.dituon.petpet.core.length.LengthType;
import moe.dituon.petpet.core.length.NumberPercentageLength;
import moe.dituon.petpet.core.length.PercentageLength;

public enum OffsetKeyword implements OffsetValue {
    LEFT("left", X),
    RIGHT("right", X),
    TOP("top", Y),
    BOTTOM("bottom", Y),
    CENTER("center", XY);

    private static final PercentageLength P0 = new NumberPercentageLength(0, LengthType.PERCENT);
    private static final PercentageLength P50 = new NumberPercentageLength(50, LengthType.PERCENT);
    private static final PercentageLength P100 = new NumberPercentageLength(100, LengthType.PERCENT);

    private static final PercentageLength LEFT_VALUE = P0;
    private static final PercentageLength RIGHT_VALUE = P100;
    private static final PercentageLength TOP_VALUE = P0;
    private static final PercentageLength BOTTOM_VALUE = P100;
    private static final PercentageLength CENTER_X_VALUE = P50;
    private static final PercentageLength CENTER_Y_VALUE = P50;

    public final String keyword;
    public final int type;

    OffsetKeyword(String keyword, int type) {
        this.keyword = keyword;
        this.type = type;
    }

    @Override
    public boolean canLinkWith(OffsetValue value) {
        if (value instanceof OffsetKeyword) {
            return (this.type & ((OffsetKeyword) value).type) != 0;
        }
        return true;
    }

    @Override
    public int getOffsetType() {
        return this.type;
    }

    @Override
    public PercentageLength getX() {
        switch (this) {
            case LEFT:
                return LEFT_VALUE;
            case RIGHT:
                return RIGHT_VALUE;
            case CENTER:
            default:
                return CENTER_X_VALUE;
        }
    }

    @Override
    public PercentageLength getY() {
        switch (this) {
            case TOP:
                return TOP_VALUE;
            case BOTTOM:
                return BOTTOM_VALUE;
            case CENTER:
            default:
                return CENTER_Y_VALUE;
        }
    }
}
