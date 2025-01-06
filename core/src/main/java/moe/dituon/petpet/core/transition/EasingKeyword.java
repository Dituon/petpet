package moe.dituon.petpet.core.transition;

public enum EasingKeyword {
    LINEAR("linear", LinearEasingFunction.INSTANCE),
    EASE("ease", new CubicBezierEasingFunction(0.25f, 0.1f, 0.25f, 1f)),
    EASE_IN("ease-in", new CubicBezierEasingFunction(0.42f, 0f, 1f, 1f)),
    EASE_OUT("ease-out", new CubicBezierEasingFunction(0f, 0f, 0.58f, 1f)),
    EASE_IN_OUT("ease-in-out", new CubicBezierEasingFunction(0.42f, 0f, 0.58f, 1f));

    public final String name;
    public final EasingFunction function;
    EasingKeyword(String name, EasingFunction function) {
        this.name = name;
        this.function = function;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
