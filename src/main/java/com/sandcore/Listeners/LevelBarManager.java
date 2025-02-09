package com.sandcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelBarManager {
    private static Map<UUID, BossBar> bossBars = new HashMap<>();

    // Create or update a player's level bar
    public static void updateLevelBar(Player player, double progress, int level) {
        BossBar bar = bossBars.get(player.getUniqueId());
        if(bar == null) {
            bar = Bukkit.createBossBar(ChatColor.GREEN + "Combat Level: " + level, BarColor.BLUE, BarStyle.SOLID);
            bossBars.put(player.getUniqueId(), bar);
            bar.addPlayer(player);
        } else {
            bar.setTitle(ChatColor.GREEN + "Combat Level: " + level);
        }
        bar.setProgress(Math.min(Math.max(progress, 0.0), 1.0));
    }

    // Remove a player's boss bar (e.g., on logout)
    public static void removeLevelBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if(bar != null) {
            bar.removeAll();
        }
    }
}