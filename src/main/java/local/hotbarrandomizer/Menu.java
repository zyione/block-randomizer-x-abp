package local.hotbarrandomizer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import com.terraformersmc.modmenu.api.*;
import me.shedaniel.clothconfig2.api.*;
import java.util.function.Consumer;

public final class Menu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return Menu::screen; }
    private static Text text(String s) { return Text.literal(s); }
    private static void toggle(ConfigBuilder b, ConfigCategory c, String label, boolean value, Consumer<Boolean> save) {
        c.addEntry(b.entryBuilder().startBooleanToggle(text(label), value).setDefaultValue(true).setSaveConsumer(save).build());
    }
    private static Screen screen(Screen parent) {
        Settings s = Randomizer.settings;
        ConfigBuilder b = ConfigBuilder.create().setParentScreen(parent).setTitle(text("Hotbar Randomizer"))
                .setSavingRunnable(() -> {
                    s.save();
                    if (Randomizer.enabled) Randomizer.select(MinecraftClient.getInstance(), null);
                });
        ConfigCategory general = b.getOrCreateCategory(text("General"));
        general.addEntry(b.entryBuilder().startTextDescription(text("Toggle with P (rebind in Controls). Only hotbar block items are selected. Starts OFF each session.")).build());
        general.addEntry(b.entryBuilder().startBooleanToggle(text("Randomizer enabled now"), Randomizer.enabled)
                .setDefaultValue(false).setSaveConsumer(v -> Randomizer.enabled = v).build());
        toggle(b, general, "Avoid repeating the same block type when possible", s.avoidRepeats, v -> s.avoidRepeats = v);
        general.addEntry(b.entryBuilder().startStrList(text("Excluded item IDs (e.g. minecraft:tnt)"), s.excludedItems)
                .setDefaultValue(java.util.List.of()).setSaveConsumer(v -> s.excludedItems = new java.util.ArrayList<>(v)).build());
        ConfigCategory categories = b.getOrCreateCategory(text("Included categories"));
        categories.addEntry(b.entryBuilder().startTextDescription(text("All enabled by default. A block in multiple categories must pass every category. Tools, buckets and other non-block items are always excluded.")).build());
        toggle(b, categories, "Ordinary / other blocks", s.ordinary, v -> s.ordinary = v);
        toggle(b, categories, "Plants, flowers and crops", s.plants, v -> s.plants = v);
        toggle(b, categories, "Torches (including redstone torches)", s.torches, v -> s.torches = v);
        toggle(b, categories, "Falling blocks (sand, gravel, anvils, etc.)", s.falling, v -> s.falling = v);
        toggle(b, categories, "Block entities (containers, machines, signs, beds, etc.)", s.blockEntities, v -> s.blockEntities = v);
        toggle(b, categories, "Slabs, stairs, fences, walls and panes", s.shaped, v -> s.shaped = v);
        toggle(b, categories, "Doors and trapdoors", s.doors, v -> s.doors = v);
        toggle(b, categories, "Leaves", s.leaves, v -> s.leaves = v);
        return b.build();
    }
}
