package moe.dituon.petpet.core.transition;

import moe.dituon.petpet.core.utils.math.BezierEasing;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CubicBezierEasingFunction extends EasingFunction {
    public static final String FUNCTION_NAME = "cubic-bezier";
    protected final BezierEasing bezierEasing;

    public CubicBezierEasingFunction(float x1, float y1, float x2, float y2) {
        this.bezierEasing = new BezierEasing(x1, y1, x2, y2);
    }

    @Override
    public float easing(float t) {
        return this.bezierEasing.easing(t);
    }

    @Override
    public float easingFrames(float start, float end, int length, int index) {
        if (index <= 0) return start;
        if (index >= length) return end;
        return start + (end - start) * this.bezierEasing.easing(index / (float) length);
    }

    /**
     * @param str "cubic-bezier(x1, y1, x2, y2)"
     */
    public static CubicBezierEasingFunction fromString(@NotNull String str) {
        var tokensStr = str.substring(FUNCTION_NAME.length() + 1, str.length() - 1).replace(" ", "");
        var tokens = tokensStr.split(",");
        if (tokens.length != 4) throw new IllegalArgumentException("Invalid cubic-bezier function");
        return new CubicBezierEasingFunction(
                Float.parseFloat(tokens[0]),
                Float.parseFloat(tokens[1]),
                Float.parseFloat(tokens[2]),
                Float.parseFloat(tokens[3])
        );
    }

    @Override
    public String toString() {
        var e = this.bezierEasing;
        return FUNCTION_NAME + '(' + e.mX1 + ", " + e.mY1 + ", " + e.mX2 + ", " + e.mY2 + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CubicBezierEasingFunction)) return false;
        CubicBezierEasingFunction that = (CubicBezierEasingFunction) o;
        return Objects.equals(bezierEasing, that.bezierEasing);
    }

    @Override
    public int hashCode() {
        return this.bezierEasing.hashCode() >> 1;
    }
}
