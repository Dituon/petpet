package moe.dituon.petpet.core.transition;

public class LinearEasingFunction extends EasingFunction {
    public static final LinearEasingFunction INSTANCE = new LinearEasingFunction();

    protected LinearEasingFunction(){}

    public float easing(float t) {
        return t;
    }

    public float easingFrames(float start, float end, int length, int index) {
        return start + (end - start) * index / (length - 1);
    }

    @Override
    public String toString() {
        return "linear";
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return obj instanceof LinearEasingFunction;
    }
}
