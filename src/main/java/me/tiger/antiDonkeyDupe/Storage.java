package me.tiger.antiDonkeyDupe;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Storage {

    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration cfg;

    // entityUuid -> base64( ItemStack[] )
    private final Map<UUID, String> snapshots = new HashMap<>();

    public Storage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create data.yml: " + e.getMessage());
            }
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);

        snapshots.clear();
        if (cfg.isConfigurationSection("snapshots")) {
            for (String key : Objects.requireNonNull(cfg.getConfigurationSection("snapshots")).getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String b64 = cfg.getString("snapshots." + key, "");
                    if (b64 != null && !b64.isEmpty()) {
                        snapshots.put(uuid, b64);
                    }
                } catch (IllegalArgumentException ignored) {
                    // skip bad keys
                }
            }
        }
    }

    public void save() {
        if (cfg == null) cfg = new YamlConfiguration();

        cfg.set("snapshots", null);
        for (Map.Entry<UUID, String> e : snapshots.entrySet()) {
            cfg.set("snapshots." + e.getKey(), e.getValue());
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data.yml: " + e.getMessage());
        }
    }

    public void put(UUID entityUuid, String base64) {
        snapshots.put(entityUuid, base64);
    }

    public String get(UUID entityUuid) {
        return snapshots.get(entityUuid);
    }

    public boolean has(UUID entityUuid) {
        return snapshots.containsKey(entityUuid);
    }

    public void remove(UUID entityUuid) {
        snapshots.remove(entityUuid);
    }
}