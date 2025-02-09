package com.sandcore.mmo;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatsManager {
    private static Map<UUID, PlayerStats> playerStats = new HashMap<>();

    public static PlayerStats getPlayerStats(Player player) {
        return playerStats.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerStats());
    }

    public static void setPlayerStats(Player player, PlayerStats stats) {
        playerStats.put(player.getUniqueId(), stats);
    }

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
}