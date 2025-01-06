package moe.dituon.petpet.core.length;

import java.util.Objects;

public class NumberLength implements Length {
    public static final Length EMPTY = new NumberLength(0, LengthType.PX);

    public final LengthType type;
    public final float value;

    public NumberLength(float length, LengthType type) {
        this.type = type;
        this.value = length;
    }

    @Override
    public LengthType getType() {
        return type;
    }

    @Override
    public boolean isAbsolute() {
        return LengthType.PX == this.type || this.value == 0;
    }

    @Override
    public boolean isDependOnCanvasSize() {
        switch (this.type) {
            case VW:
            case VH:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isDependOnElementSize() {
        switch (this.type) {
            case PERCENT:
            case CW:
            case CH:
                return true;
            default:
                return false;
        }
    }

    @Override
    public float getValue() {
        if (!isAbsolute()) {
            throw new IllegalStateException("getValue with out context must be absolute");
        }
        return value;
    }

    @Override
    public float getRawValue() {
        return value;
    }

    @Override
    public float getValue(LengthContext context) {
        if (type == LengthType.PERCENT) {
            throw new IllegalArgumentException("percentage length are not supported");
        }
        return getValueWithoutCheck(context, this.type);
    }

    protected float getValueWithoutCheck(LengthContext context, LengthType type) {
        return getValueWithoutCheck(context, type, 1.0f);
    }

    protected float getValueWithoutCheck(LengthContext context, LengthType type, float percentageParent) {
        switch (type) {
            case VW:
                return (value * 0.01f) * context.canvasWidth;
            case VH:
                return (value * 0.01f) * context.canvasHeight;
            case CW:
                return (value * 0.01f) * context.elementWidth;
            case CH:
                return (value * 0.01f) * context.elementHeight;
            case PERCENT:
                return (value * 0.01f) * percentageParent;
            case PX:
            default:
                return value;
        }
    }

    public static NumberLength px(float value) {
        if (value == 0) return (NumberLength) EMPTY;
        return new NumberLength(value, LengthType.PX);
    }

    public static NumberLength px(int value) {
        return px((float) value);
    }

    @Override
    public String toString() {
        return value + type.suffix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberLength)) return false;
        NumberLength that = (NumberLength) o;
        return value == that.value && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    public static NumberLength fromString(String str) {
        String numValue = str;
        LengthType valType = LengthType.PX;
        for (LengthType type : LengthType.values()) {
            if (!str.endsWith(type.suffix)) {
                continue;
            }
            numValue = str.substring(0, str.length() - type.suffix.length());
            valType = type;
        }
        return new NumberLength(Float.parseFloat(numValue), valType);
    }
}
