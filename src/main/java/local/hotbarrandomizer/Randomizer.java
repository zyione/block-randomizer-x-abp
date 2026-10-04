package local.hotbarrandomizer;

import net.minecraft.block.AbstractPlantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.ChorusPlantBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;

/** Client-side hotbar selection and optional Accurate Block Placement integration. */
public final class Randomizer implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("hotbar-randomizer");
    public static Settings settings;
    public static boolean enabled;
    private static boolean inInteraction, placed;
    private static Item placedItem;
    private static boolean abpChecked;
    private static Field abpLastItem;
    private static final boolean[] eligible = new boolean[9];
    private static final Object[] types = new Object[9];

    @Override public void onInitializeClient() {
        settings = Settings.load();
        KeyBinding key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hotbar_randomizer.toggle", InputUtil.Type.KEYSYM, 80,
                "category.hotbar_randomizer"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) { enabled = false; inInteraction = false; placed = false; }
            while (key.wasPressed()) {
                if (client.player == null || client.currentScreen != null) continue;
                enabled = !enabled;
                if (enabled) select(client, null);
                message(client, "Hotbar Randomizer: " + (enabled ? "ON" : "OFF"));
            }
        });
    }

    public static void message(MinecraftClient client, String text) {
        if (client.player != null) client.player.sendMessage(Text.literal(text), true);
    }

    public static boolean allowed(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem item)) return false;
        Settings s = settings;
        if (s.excludedItems.contains(Registries.ITEM.getId(item).toString())) return false;
        Block block = item.getBlock();
        boolean plant = block instanceof PlantBlock || block instanceof AbstractPlantBlock || block instanceof ChorusPlantBlock;
        boolean torch = block instanceof TorchBlock;
        boolean falls = block instanceof FallingBlock;
        boolean entity = block instanceof BlockEntityProvider;
        boolean shape = block instanceof SlabBlock || block instanceof StairsBlock || block instanceof FenceBlock
                || block instanceof WallBlock || block instanceof PaneBlock;
        boolean door = block instanceof DoorBlock || block instanceof TrapdoorBlock;
        boolean leaf = block instanceof LeavesBlock;
        if (plant && !s.plants || torch && !s.torches || falls && !s.falling || entity && !s.blockEntities
                || shape && !s.shaped || door && !s.doors || leaf && !s.leaves) return false;
        return plant || torch || falls || entity || shape || door || leaf || s.ordinary;
    }

    public static void begin(Hand hand) {
        inInteraction = enabled && hand == Hand.MAIN_HAND;
        placed = false;
        placedItem = null;
    }

    public static void record(ItemPlacementContext context, ActionResult result, Item item) {
        // BlockItem.place success, not a generic accepted right-click (e.g. opening a chest).
        if (!inInteraction || !enabled || !result.isAccepted()
                || context.getHand() != Hand.MAIN_HAND
                || !context.getWorld().isClient) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (context.getPlayer() != client.player) return;
        placed = true;
        // The stack can already be empty after placing its final item.
        placedItem = item;
    }

    public static void finish() {
        boolean advance = inInteraction && placed && enabled;
        inInteraction = false;
        placed = false;
        // Outer interactBlock has now sent the placement packet. The next interaction
        // synchronizes the newly selected slot, preserving server packet ordering.
        if (advance) select(MinecraftClient.getInstance(), placedItem);
    }

    public static void select(MinecraftClient client, Item previous) {
        if (client.player == null || client.player.isSpectator()) return;
        PlayerInventory inventory = client.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            eligible[i] = allowed(stack);
            types[i] = stack.getItem();
        }
        int next = Selection.choose(eligible, types, previous, settings.avoidRepeats,
                bound -> ThreadLocalRandom.current().nextInt(bound));
        if (next < 0) {
            enabled = false;
            message(client, "Hotbar Randomizer: OFF (no eligible hotbar blocks)");
            return;
        }
        inventory.selectedSlot = next;
        syncAccuratePlacement(client, (Item) types[next]);
    }

    private static void syncAccuratePlacement(MinecraftClient client, Item next) {
        if (!abpChecked) {
            abpChecked = true;
            if (!FabricLoader.getInstance().isModLoaded("accurateblockplacement")) return;
            try {
                // This field is injected by ABP 1.2.1. Cache once; do not reset its
                // placement geometry/cooldown or suppress manual hotbar changes.
                abpLastItem = client.gameRenderer.getClass().getDeclaredField("lastItemInUse");
                if (abpLastItem.getType() != Item.class) throw new NoSuchFieldException("Unexpected ABP field type");
                abpLastItem.setAccessible(true);
            } catch (ReflectiveOperationException | RuntimeException e) {
                LOG.error("Accurate Block Placement compatibility unavailable", e);
                message(client, "Hotbar Randomizer: ABP compatibility unavailable; check latest.log");
            }
        }
        if (abpLastItem != null) try { abpLastItem.set(client.gameRenderer, next); }
        catch (IllegalAccessException e) { LOG.error("Could not synchronize ABP held item", e); abpLastItem = null; }
    }
}
