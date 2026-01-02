package me.tiger.antiDonkeyDupe;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Base64;

public final class InventoryCodec {

    private InventoryCodec() {}

    public static String toBase64(ItemStack[] items) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeInt(items.length);
            for (ItemStack item : items) {
                oos.writeObject(item);
            }
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static ItemStack[] fromBase64(String data) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.getDecoder().decode(data);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        try (BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
            int size = ois.readInt();
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) ois.readObject();
            }
            return items;
        }
    }
}