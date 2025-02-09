package com.sandcore.mmo;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatsManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<UUID, PlayerStats> statsMap = new HashMap<>();

    public PlayerStatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadAllStats();
    }

    /**
     * Loads all stored player stats from players.yml.
     */
    public void loadAllStats() {
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double health = config.getDouble(key + ".health", 20.0);
                double damage = config.getDouble(key + ".damage", 2.0);
                double speed = config.getDouble(key + ".speed", 0.1);
                double defense = config.getDouble(key + ".defense", 0.0);
                PlayerStats stats = new PlayerStats(health, damage, speed, defense);
                statsMap.put(uuid, stats);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in players.yml: " + key);
            }
        }
    }

    /**
     * Returns the PlayerStats for this UUID, creating a default entry if needed.
     */
    public PlayerStats getStats(UUID uuid) {
        if (!statsMap.containsKey(uuid)) {
            PlayerStats stats = new PlayerStats();
            statsMap.put(uuid, stats);
        }
        return statsMap.get(uuid);
    }

    /**
     * Saves all players’ stats back to players.yml.
     */
    public void saveStats() {
        for (UUID uuid : statsMap.keySet()) {
            PlayerStats stats = statsMap.get(uuid);
            String key = uuid.toString();
            config.set(key + ".health", stats.getHealth());
            config.set(key + ".damage", stats.getDamage());
            config.set(key + ".speed", stats.getSpeed());
            config.set(key + ".defense", stats.getDefense());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}