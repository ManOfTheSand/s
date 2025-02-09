package com.sandcore;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LevelListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Hide vanilla XP bar
        player.setLevel(0);
        player.setExp(0);
        // Update the combat level bar using saved data
        PlayerLevelData data = LevelManager.getPlayerLevelData(player);
        double progress = data.getCombatXP() / SandCore.getInstance().getConfig().getDouble("leveling.combat.xp_per_level", 1000);
        LevelBarManager.updateLevelBar(player, progress, data.getCombatLevel());
    }
}