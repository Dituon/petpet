package moe.dituon.petpet.core.transition;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RotateTransition {
    public static final RotateTransition LINER = createDefaultInstance(EasingKeyword.LINEAR.function);
    public static final RotateTransition EASE = createDefaultInstance(EasingKeyword.EASE.function);
    public static final RotateTransition EASE_IN = createDefaultInstance(EasingKeyword.EASE_IN.function);
    public static final RotateTransition EASE_OUT = createDefaultInstance(EasingKeyword.EASE_OUT.function);
    public static final RotateTransition EASE_IN_OUT = createDefaultInstance(EasingKeyword.EASE_IN_OUT.function);
    public static final RotateTransition DEFAULT = LINER;

    public final float start;
    public final float end;
    public final EasingFunction easing;
    public final int rotateCount;

    public RotateTransition(float start, float end, EasingFunction easing, int rotateCount) {
        this.start = start;
        this.end = end + 360 * rotateCount;
        this.easing = easing;
        this.rotateCount = (int) ((end - start) / 360f);
    }

    public boolean isEmpty() {
        return false;
    }

    public float getAngle(int length, int index) {
        return this.easing.easingFrames(start, end, length + 1, index);
    }

    public static RotateTransition createDefaultInstance(EasingFunction function) {
        return RotateTransition.builder()
                .start(0).end(0)
                .rotateCount(1)
                .easing(function)
                .build();
    }

    public static RotateTransition empty() {
        return EmptyRotateTransition.INSTANCE;
    }

    @Override
    public String toString() {
        return "RotateTransition{" +
                "start=" + start +
                ", end=" + end +
                ", easing=" + easing +
                '}';
    }

    protected static class EmptyRotateTransition extends RotateTransition {
        public static final EmptyRotateTransition INSTANCE = new EmptyRotateTransition();

        public EmptyRotateTransition() {
            super(0, 0, LinearEasingFunction.INSTANCE, 0);
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public float getAngle(int length, int index) {
            return 0;
        }
    }
}
