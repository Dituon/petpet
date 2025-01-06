package moe.dituon.petpet.core.utils.math;

// by https://github.com/gre/bezier-easing

import java.util.Objects;

public class BezierEasing {
    private static final int NEWTON_ITERATIONS = 4;
    private static final float NEWTON_MIN_SLOPE = 0.001f;
    private static final float SUBDIVISION_PRECISION = 0.0000001f;
    private static final int SUBDIVISION_MAX_ITERATIONS = 10;

    private static final int K_SPLINE_TABLE_SIZE = 11;
    private static final float K_SAMPLE_STEP_SIZE = 1.0f / (K_SPLINE_TABLE_SIZE - 1.0f);

    private final float[] sampleValues = new float[K_SPLINE_TABLE_SIZE];
    public final float mX1;
    public final float mY1;
    public final float mX2;
    public final float mY2;

    public BezierEasing(float mX1, float mY1, float mX2, float mY2) {
        if (!(0 <= mX1 && mX1 <= 1) || !(0 <= mX2 && mX2 <= 1)) {
            throw new IllegalArgumentException("Bezier x values must be in [0, 1] range");
        }
        this.mX1 = mX1;
        this.mY1 = mY1;
        this.mX2 = mX2;
        this.mY2 = mY2;

        for (int i = 0; i < K_SPLINE_TABLE_SIZE; ++i) {
            sampleValues[i] = calcBezier(i * K_SAMPLE_STEP_SIZE, mX1, mX2);
        }
    }

    private static float a(float aA1, float aA2) {
        return 1.0f - 3.0f * aA2 + 3.0f * aA1;
    }

    private static float b(float aA1, float aA2) {
        return 3.0f * aA2 - 6.0f * aA1;
    }

    private static float c(float aA1) {
        return 3.0f * aA1;
    }

    // Returns x(t) or y(t) for a given t, x1, x2 or y1, y2.
    private static float calcBezier(float t, float aA1, float aA2) {
        return ((a(aA1, aA2) * t + b(aA1, aA2)) * t + c(aA1)) * t;
    }

    // Returns dx/dt or dy/dt for a given t, x1, x2 or y1, y2.
    private static float getSlope(float t, float aA1, float aA2) {
        return 3.0f * a(aA1, aA2) * t * t + 2.0f * b(aA1, aA2) * t + c(aA1);
    }

    private float binarySubdivide(float aX, float aA, float aB, float mX1, float mX2) {
        float currentX;
        float currentT;
        int i = 0;

        do {
            currentT = aA + (aB - aA) / 2.0f;
            currentX = calcBezier(currentT, mX1, mX2) - aX;
            if (currentX > 0.0f) {
                aB = currentT;
            } else {
                aA = currentT;
            }
        } while (Math.abs(currentX) > SUBDIVISION_PRECISION && ++i < SUBDIVISION_MAX_ITERATIONS);

        return currentT;
    }

    private float newtonRaphsonIterate(float aX, float aGuessT, float mX1, float mX2) {
        for (int i = 0; i < NEWTON_ITERATIONS; ++i) {
            float currentSlope = getSlope(aGuessT, mX1, mX2);
            if (currentSlope == 0.0f) {
                return aGuessT;
            }
            float currentX = calcBezier(aGuessT, mX1, mX2) - aX;
            aGuessT -= currentX / currentSlope;
        }
        return aGuessT;
    }

    private float getTForX(float aX) {
        int currentSample = 1;
        int lastSample = K_SPLINE_TABLE_SIZE - 1;

        while (currentSample != lastSample && sampleValues[currentSample] <= aX) {
            currentSample++;
        }
        currentSample--;

        float intervalStart = currentSample * K_SAMPLE_STEP_SIZE;
        float dist = (aX - sampleValues[currentSample]) / (sampleValues[currentSample + 1] - sampleValues[currentSample]);
        float guessForT = intervalStart + dist * K_SAMPLE_STEP_SIZE;

        float initialSlope = getSlope(guessForT, mX1, mX2);
        if (initialSlope >= NEWTON_MIN_SLOPE) {
            return newtonRaphsonIterate(aX, guessForT, mX1, mX2);
        } else if (initialSlope == 0.0f) {
            return guessForT;
        } else {
            return binarySubdivide(aX, intervalStart, intervalStart + K_SAMPLE_STEP_SIZE, mX1, mX2);
        }
    }

    public float easing(float t) {
        if (t == 0 || t == 1) {
            return t;
        }
        return calcBezier(getTForX(t), mY1, mY2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BezierEasing)) return false;
        BezierEasing that = (BezierEasing) o;
        return mX1 == that.mX1 && mY1 == that.mY1 && mX2 == that.mX2 && mY2 == that.mY2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mX1, mY1, mX2, mY2);
    }
}

