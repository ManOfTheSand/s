package com.sandcore.mmo;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatsManager {
    // Map to store each player's stats using their UUID.
    private static Map<UUID, PlayerStats> playerStats = new HashMap<>();

    /**
     * Retrieves the PlayerStats object for a given player.
     * If none exists, creates a default one.
     */
    public static PlayerStats getPlayerStats(Player player) {
        return playerStats.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerStats());
    }

    /**
     * Retrieves the PlayerStats object using a UUID.
     * If none exists, creates a default one.
     */
    public static PlayerStats getStats(UUID uuid) {
        return playerStats.computeIfAbsent(uuid, key -> new PlayerStats());
    }

    /**
     * Updates the player’s stats based on the class name.
     * Applies the class-specific stats to the player.
     */
    public static void updateStatsForClass(Player player, String className) {
        PlayerStats stats;
        switch (className.toLowerCase()) {
            case "warrior":
                stats = new PlayerStats(30.0, 5.0, 0.1, 3.0);
                break;
            case "mage":
                stats = new PlayerStats(20.0, 7.0, 0.1, 1.5);
                break;
            case "archer":
                stats = new PlayerStats(25.0, 4.0, 0.12, 2.0);
                break;
            default:
                stats = new PlayerStats();
                break;
        }
        stats.applyToPlayer(player);
        setPlayerStats(player, stats);
    }

    /**
     * Stores or updates the player's stats in the map.
     */
    public static void setPlayerStats(Player player, PlayerStats stats) {
        playerStats.put(player.getUniqueId(), stats);
    }

    /**
     * Saves the current player stats.
     * You can implement actual persistence logic here, such as writing to a file or database.
     * For now, it's a stub to satisfy the call to statsManager.saveStats() in your listener.
     */
    public static void saveStats() {
        // TODO: Implement persistence logic (e.g., saving to a file, database, etc.)
        System.out.println("PlayerStats have been saved successfully!");
    }
}