package local.hotbarrandomizer;

import java.util.function.IntUnaryOperator;

/** Pure slot selection, also exercised without launching Minecraft. */
public final class Selection {
    public static int choose(boolean[] eligible, Object[] types, Object previous, boolean avoidRepeats,
                             IntUnaryOperator random) {
        boolean alternative = false;
        for (int i = 0; i < 9; i++) if (eligible[i] && types[i] != previous) alternative = true;
        int count = 0, result = -1;
        for (int i = 0; i < 9; i++) {
            if (!eligible[i] || (avoidRepeats && alternative && types[i] == previous)) continue;
            if (random.applyAsInt(++count) == 0) result = i;
        }
        return result;
    }
}
