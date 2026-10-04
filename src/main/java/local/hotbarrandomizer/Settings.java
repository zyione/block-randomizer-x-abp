package local.hotbarrandomizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public final class Settings {
    public boolean avoidRepeats = true;
    public boolean ordinary = true, plants = true, torches = true, falling = true;
    public boolean blockEntities = true, shaped = true, doors = true, leaves = true;
    public List<String> excludedItems = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("hotbar-randomizer.json"); }
    public static Settings load() {
        if (Files.exists(path())) try (Reader r = Files.newBufferedReader(path())) {
            Settings s = GSON.fromJson(r, Settings.class);
            if (s != null) { if (s.excludedItems == null) s.excludedItems = new ArrayList<>(); return s; }
        } catch (Exception e) { Randomizer.LOG.warn("Could not read hotbar randomizer settings; using defaults", e); }
        return new Settings();
    }
    public void save() {
        try {
            Path temp = path().resolveSibling("hotbar-randomizer.json.tmp");
            try (Writer w = Files.newBufferedWriter(temp)) { GSON.toJson(this, w); }
            Files.move(temp, path(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { Randomizer.LOG.error("Could not save hotbar randomizer settings", e); }
    }
}
