package com.sandcore.mmo.listeners;

import com.sandcore.mmo.PlayerStats;
import com.sandcore.mmo.PlayerStatsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerStatsListener implements Listener {

    private final PlayerStatsManager statsManager;

    public PlayerStatsListener(PlayerStatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Get the player's stats and apply them
        PlayerStats stats = statsManager.getStats(player.getUniqueId());
        stats.applyToPlayer(player);
        player.sendMessage("Your stats: " + stats.toString());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Optionally save stats on quit
        statsManager.saveStats();
    }
}