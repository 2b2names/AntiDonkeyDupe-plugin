package me.tiger.antiDonkeyDupe.listeners;

import me.tiger.antiDonkeyDupe.InventoryCodec;
import me.tiger.antiDonkeyDupe.Storage;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class DonkeyDupeListener implements Listener {

    private final JavaPlugin plugin;
    private final Storage storage;

    public DonkeyDupeListener(JavaPlugin plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    private boolean protectDonkey() { return plugin.getConfig().getBoolean("protect.donkey", true); }
    private boolean protectMule()   { return plugin.getConfig().getBoolean("protect.mule", true); }
    private boolean protectLlama()  { return plugin.getConfig().getBoolean("protect.llama", true); }

    private boolean onlyIfChested() { return plugin.getConfig().getBoolean("only-if-carrying-chest", true); }
    private boolean clearOnUnload() { return plugin.getConfig().getBoolean("clear-on-unload", true); }
    private boolean restoreOnLoad() { return plugin.getConfig().getBoolean("restore-on-load", true); }
    private boolean failClosed()    { return plugin.getConfig().getBoolean("fail-closed", true); }

    private boolean isProtected(Entity e) {
        if (e instanceof Donkey) return protectDonkey();
        if (e instanceof Mule) return protectMule();
        if (e instanceof Llama) return protectLlama();
        return false;
    }

    private boolean isChested(Entity e) {
        if (e instanceof ChestedHorse ch) return ch.isCarryingChest();
        if (e instanceof Llama llama) return llama.isCarryingChest();
        return false;
    }

    private Inventory getStorageInventory(Entity e) {
        // Donkey/Mule are AbstractHorse and have getInventory()
        if (e instanceof AbstractHorse horse) return horse.getInventory();
        if (e instanceof Llama llama) return llama.getInventory();
        return null;
    }

    private boolean hasChestItems(Inventory inv) {
        if (inv == null) return false;
        for (var item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) return true;
        }
        return false;
    }

    @EventHandler
    public void onEntitiesUnload(EntitiesUnloadEvent event) {
        for (Entity e : event.getEntities()) {
            if (!isProtected(e)) continue;
            if (onlyIfChested() && !isChested(e)) continue;

            Inventory inv = getStorageInventory(e);
            if (inv == null) continue;

            // Save a snapshot even if empty; but only write if there are items (keeps file smaller)
            if (!hasChestItems(inv)) continue;

            try {
                String b64 = InventoryCodec.toBase64(inv.getContents());
                storage.put(e.getUniqueId(), b64);

                if (clearOnUnload()) {
                    inv.clear();
                }
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to snapshot inventory for " + e.getType() + " " + e.getUniqueId() + ": " + ex.getMessage());

                // If configured fail-closed, clear inventory to avoid potential dupe surfaces
                if (failClosed() && clearOnUnload() && inv != null) {
                    inv.clear();
                }
            }
        }
        // Persist periodically on unloads (simple + reliable)
        storage.save();
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if (!restoreOnLoad()) return;

        for (Entity e : event.getEntities()) {
            if (!isProtected(e)) continue;
            if (onlyIfChested() && !isChested(e)) continue;

            if (!storage.has(e.getUniqueId())) continue;

            Inventory inv = getStorageInventory(e);
            if (inv == null) continue;

            try {
                var items = InventoryCodec.fromBase64(storage.get(e.getUniqueId()));
                inv.setContents(items);

                // keep snapshot (or remove). We keep it so repeated unload/load stays consistent.
                // If you prefer: storage.remove(e.getUniqueId());
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to restore inventory for " + e.getType() + " " + e.getUniqueId() + ": " + ex.getMessage());
                if (failClosed()) {
                    inv.clear();
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();
        if (!isProtected(e)) return;
        storage.remove(e.getUniqueId());
        storage.save();
    }
}