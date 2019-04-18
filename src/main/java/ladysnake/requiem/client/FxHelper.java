package ladysnake.requiem.client;

import static java.lang.StrictMath.exp;

public final class FxHelper {
    private FxHelper() { throw new AssertionError(); }

    public static float impulse(float k, float x) {
        float h = k * x;
        return (float) (h * exp(1.0 - h));
    }
}
