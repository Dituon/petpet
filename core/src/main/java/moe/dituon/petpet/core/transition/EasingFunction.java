package moe.dituon.petpet.core.transition;

public abstract class EasingFunction {
    public abstract float easing(float t);

    /**
     * Calculates the interpolated frame value at a specific index within a specified range.
     * This method is intended to be implemented by subclasses to define how to interpolate frame values.
     *
     * @param start The starting value of the interpolation.
     * @param end The ending value of the interpolation.
     * @param length The total number of frames for the interpolation.
     * @param index The index of the current frame.
     * @return The interpolated frame value at the current index.
     */
    public abstract float easingFrames(float start, float end, int length, int index);

    /**
     * Creates a new FramesInterpolator instance for frame-based interpolation.
     * This method provides a way to encapsulate frame-based interpolation logic into an object.
     *
     * @param start The starting value of the interpolation.
     * @param end The ending value of the interpolation.
     * @param length The total number of frames for the interpolation.
     * @return A new instance of FramesInterpolator for frame-based interpolation.
     */
    public FramesInterpolator frames(float start, float end, int length) {
        return new FramesInterpolator(start, end, length);
    }

    public class FramesInterpolator {
        protected float start;
        protected float end;
        protected int length;

        public FramesInterpolator(float start, float end, int length) {
            this.start = start;
            this.end = end;
            this.length = length;
        }

        public float easing(int index) {
            return EasingFunction.this.easingFrames(this.start, this.end, this.length, index);
        }
    }

    public static EasingFunction fromString(String str) {
        str = str.trim();
        for (EasingKeyword keyword : EasingKeyword.values()) {
            if (keyword.name.equals(str)) return keyword.function;
        }
        if (str.startsWith(CubicBezierEasingFunction.FUNCTION_NAME)) {
            return CubicBezierEasingFunction.fromString(str);
        }
        throw new IllegalArgumentException("Unknown easing function: " + str);
    }
}
