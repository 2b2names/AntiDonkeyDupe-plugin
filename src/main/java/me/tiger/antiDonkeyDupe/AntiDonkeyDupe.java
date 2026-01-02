package me.tiger.antiDonkeyDupe;

import me.tiger.antiDonkeyDupe.listeners.DonkeyDupeListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiDonkeyDupe extends JavaPlugin {

    private Storage storage;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.storage = new Storage(this);
        this.storage.load();

        getServer().getPluginManager().registerEvents(new DonkeyDupeListener(this, storage), this);

        getLogger().info("AntiDonkeyDupe enabled.");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.save();
        }
        getLogger().info("AntiDonkeyDupe disabled.");
    }

    public Storage getStorage() {
        return storage;
    }
}