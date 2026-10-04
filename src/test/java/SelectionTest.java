import local.hotbarrandomizer.Selection;
import java.util.Random;
import java.util.Arrays;

public class SelectionTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        boolean[] eligible = new boolean[9];
        Object[] types = new Object[9];
        Random random = new Random(123456);
        check(Selection.choose(eligible, types, null, true, random::nextInt) == -1, "Empty hotbar");
        Object stone = new Object(), dirt = new Object(), torch = new Object();
        eligible[7] = true; types[7] = stone;
        check(Selection.choose(eligible, types, stone, true, random::nextInt) == 7, "Single block stays available");
        eligible[2] = true; types[2] = stone;
        for (int i = 0; i < 1000; i++) {
            int slot = Selection.choose(eligible, types, stone, true, random::nextInt);
            check(slot == 2 || slot == 7, "Duplicate stacks remain usable");
        }
        eligible[5] = true; types[5] = dirt;
        for (int i = 0; i < 1000; i++) check(Selection.choose(eligible, types, stone, true, random::nextInt) == 5, "Skip repeated type");
        eligible[8] = true; types[8] = torch;
        int[] counts = new int[9];
        for (int i = 0; i < 40000; i++) counts[Selection.choose(eligible, types, stone, false, random::nextInt)]++;
        for (int i = 0; i < 9; i++) check(eligible[i] ? counts[i] > 9300 && counts[i] < 10700 : counts[i] == 0,
                "Uniform eligible slots; never select tool/excluded/empty slot: " + Arrays.toString(counts));
        eligible[5] = false; eligible[8] = false;
        check(Selection.choose(eligible, types, dirt, true, random::nextInt) != 5, "Depleted slot cannot be selected");
        System.out.println("PASS: empty, single, duplicate type, no-repeat, depleted and uniform slot selection (42,003 selections)");
    }
}
