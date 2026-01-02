# AntiDonkeyDupe 🛡️

AntiDonkeyDupe is a lightweight **Paper-only Minecraft plugin** that prevents classic donkey, mule, and llama chest duplication exploits by safely handling entity inventories during chunk unload and load events.

Designed for **Paper 1.21.4**, with no NMS and no invasive mechanics.

---

## ✨ Features

- Prevents donkey / mule / llama chest dupes
- Uses Paper chunk entity load/unload events
- Inventory snapshot + restore system
- Clears in-entity storage to prevent desync
- Configurable per entity type
- Fail-safe (fail closed) mode
- Lightweight and survival-safe

---

## ❓ How It Works

Many historical duplication exploits rely on **entity inventory desynchronization** during:
- Chunk unloads
- Rollbacks
- Forced entity removal

AntiDonkeyDupe closes this gap by:
1. Saving the entity’s chest inventory when it unloads
2. Clearing the live entity inventory
3. Restoring the inventory safely when the entity reloads

At no point do two copies of the same inventory exist.

---

## 📦 Requirements

- **Paper** (required)
- Minecraft **1.21.4**
- Java **21**

> This plugin will NOT work on Spigot, CraftBukkit, or Forge.

---

## 🛠️ Installation

1. Download or build the plugin JAR
2. Place it into your server’s `plugins/` directory
3. Start or restart the server
4. Edit `config.yml` if needed

---

## ⚙️ Configuration (`config.yml`)

```yml
protect:
  donkey: true
  mule: true
  llama: true

only-if-carrying-chest: true
clear-on-unload: true
restore-on-load: true
fail-closed: true
