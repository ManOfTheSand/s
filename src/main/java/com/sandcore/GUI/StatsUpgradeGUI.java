package com.sandcore.mmo.gui;

import com.sandcore.mmo.PlayerStatPointsManager;
import com.sandcore.mmo.PlayerStats;
import com.sandcore.mmo.PlayerStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsUpgradeGUI implements Listener {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Upgrade Your Stats");

        // Items to represent each stat upgrade.
        ItemStack healthItem = new ItemStack(Material.REDSTONE);
        ItemMeta healthMeta = healthItem.getItemMeta();
        healthMeta.setDisplayName(ChatColor.RED + "Upgrade Health");
        healthItem.setItemMeta(healthMeta);

        ItemStack damageItem = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta damageMeta = damageItem.getItemMeta();
        damageMeta.setDisplayName(ChatColor.DARK_RED + "Upgrade Damage");
        damageItem.setItemMeta(damageMeta);

        ItemStack speedItem = new ItemStack(Material.FEATHER);
        ItemMeta speedMeta = speedItem.getItemMeta();
        speedMeta.setDisplayName(ChatColor.AQUA + "Upgrade Speed");
        speedItem.setItemMeta(speedMeta);

        ItemStack defenseItem = new ItemStack(Material.SHIELD);
        ItemMeta defenseMeta = defenseItem.getItemMeta();
        defenseMeta.setDisplayName(ChatColor.GRAY + "Upgrade Defense");
        defenseItem.setItemMeta(defenseMeta);

        // Show available stat points.
        int points = com.sandcore.mmo.PlayerStatPointsManager.getStatPoints(player);
        ItemStack pointsItem = new ItemStack(Material.EMERALD);
        ItemMeta pointsMeta = pointsItem.getItemMeta();
        pointsMeta.setDisplayName(ChatColor.GREEN + "Available Stat Points: " + points);
        pointsItem.setItemMeta(pointsMeta);

        inv.setItem(1, healthItem);
        inv.setItem(3, damageItem);
        inv.setItem(5, speedItem);
        inv.setItem(7, defenseItem);
        inv.setItem(4, pointsItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Upgrade Your Stats")) {
            return;
        }
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        PlayerStats stats = com.sandcore.mmo.PlayerStatsManager.getPlayerStats(player);
        boolean upgraded = false;
        switch (displayName.toLowerCase()) {
            case "upgrade health":
                if (com.sandcore.mmo.PlayerStatPointsManager.deductStatPoint(player)) {
                    stats.setHealth(stats.getHealth() + 5.0);
                    upgraded = true;
                }
                break;
            case "upgrade damage":
                if (com.sandcore.mmo.PlayerStatPointsManager.deductStatPoint(player)) {
                    stats.setDamage(stats.getDamage() + 1.0);
                    upgraded = true;
                }
                break;
            case "upgrade speed":
                if (com.sandcore.mmo.PlayerStatPointsManager.deductStatPoint(player)) {
                    stats.setSpeed(stats.getSpeed() + 0.02);
                    upgraded = true;
                }
                break;
            case "upgrade defense":
                if (com.sandcore.mmo.PlayerStatPointsManager.deductStatPoint(player)) {
                    stats.setDefense(stats.getDefense() + 0.5);
                    upgraded = true;
                }
                break;
            default:
                break;
        }
        if (upgraded) {
            // Reapply updated stats.
            stats.applyToPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Stat upgraded!");
            // Refresh the GUI to show the new stat points.
            open(player);
        } else {
            player.sendMessage(ChatColor.RED + "You have no stat points available!");
        }
    }
}