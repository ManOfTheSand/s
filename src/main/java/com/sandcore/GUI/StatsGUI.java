package com.sandcore.GUI;

import com.sandcore.SandCore;
import com.sandcore.SandCore.PlayerStats; // Import the nested PlayerStats class
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

import java.util.ArrayList;
import java.util.List;

public class StatsGUI implements Listener {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&aYour Stats &7(Click to upgrade)"));

        // Retrieve the player's stat data
        PlayerStats stats = SandCore.PlayerStatsManager.getPlayerStats(player);

        // Health item
        ItemStack healthItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta healthMeta = healthItem.getItemMeta();
        String healthName = ChatColor.translateAlternateColorCodes('&', "&cHealth: " + stats.getHealth());
        healthMeta.setDisplayName(healthName);
        List<String> healthLore = new ArrayList<>();
        healthLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to increase Health by 5"));
        healthMeta.setLore(healthLore);
        healthItem.setItemMeta(healthMeta);
        gui.setItem(10, healthItem);

        // Damage item
        ItemStack damageItem = new ItemStack(Material.TNT);
        ItemMeta damageMeta = damageItem.getItemMeta();
        String damageName = ChatColor.translateAlternateColorCodes('&', "&cDamage: " + stats.getDamage());
        damageMeta.setDisplayName(damageName);
        List<String> damageLore = new ArrayList<>();
        damageLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to increase Damage by 1"));
        damageMeta.setLore(damageLore);
        damageItem.setItemMeta(damageMeta);
        gui.setItem(11, damageItem);

        // Speed item
        ItemStack speedItem = new ItemStack(Material.FEATHER);
        ItemMeta speedMeta = speedItem.getItemMeta();
        String speedName = ChatColor.translateAlternateColorCodes('&', "&cSpeed: " + stats.getSpeed());
        speedMeta.setDisplayName(speedName);
        List<String> speedLore = new ArrayList<>();
        speedLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to increase Speed by 0.02"));
        speedMeta.setLore(speedLore);
        speedItem.setItemMeta(speedMeta);
        gui.setItem(12, speedItem);

        // Defense item
        ItemStack defenseItem = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta defenseMeta = defenseItem.getItemMeta();
        String defenseName = ChatColor.translateAlternateColorCodes('&', "&cDefense: " + stats.getDefense());
        defenseMeta.setDisplayName(defenseName);
        List<String> defenseLore = new ArrayList<>();
        defenseLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to increase Defense by 0.5"));
        defenseMeta.setLore(defenseLore);
        defenseItem.setItemMeta(defenseMeta);
        gui.setItem(13, defenseItem);

        // Open the GUI for the player
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the opened inventory is our Stats GUI
        if (!event.getView().getTitle().contains("Your Stats")) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        String clickedItem = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        PlayerStats stats = SandCore.PlayerStatsManager.getPlayerStats(player);

        if(clickedItem.contains("Health")) {
            stats.setHealth(stats.getHealth() + 5.0);
            stats.applyToPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Your Health has been increased by 5.0!");
        } else if(clickedItem.contains("Damage")) {
            stats.setDamage(stats.getDamage() + 1.0);
            stats.applyToPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Your Damage has been increased by 1.0!");
        } else if(clickedItem.contains("Speed")) {
            stats.setSpeed(stats.getSpeed() + 0.02);
            stats.applyToPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Your Speed has been increased by 0.02!");
        } else if(clickedItem.contains("Defense")) {
            stats.setDefense(stats.getDefense() + 0.5);
            stats.applyToPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Your Defense has been increased by 0.5!");
        }

        // Refresh the GUI with updated stats
        open(player);
    }
}