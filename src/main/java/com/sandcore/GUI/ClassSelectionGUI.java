package com.sandcore.mmo.gui;

import com.sandcore.mmo.PlayerClassManager;
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

public class ClassSelectionGUI implements Listener {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Select Your Class");

        // Create sample class items.
        ItemStack warrior = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta warriorMeta = warrior.getItemMeta();
        warriorMeta.setDisplayName(ChatColor.RED + "Warrior");
        warrior.setItemMeta(warriorMeta);

        ItemStack mage = new ItemStack(Material.BLAZE_ROD);
        ItemMeta mageMeta = mage.getItemMeta();
        mageMeta.setDisplayName(ChatColor.BLUE + "Mage");
        mage.setItemMeta(mageMeta);

        ItemStack archer = new ItemStack(Material.BOW);
        ItemMeta archerMeta = archer.getItemMeta();
        archerMeta.setDisplayName(ChatColor.GREEN + "Archer");
        archer.setItemMeta(archerMeta);

        // Put items in designated slots.
        gui.setItem(2, warrior);
        gui.setItem(4, mage);
        gui.setItem(6, archer);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Select Your Class")) {
            return;
        }
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        String className = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        // Check if the player has a class change point.
        if (PlayerClassManager.getClassChangePoints(player) <= 0) {
            player.sendMessage(ChatColor.RED + "You don't have any class change points!");
            player.closeInventory();
            return;
        }

        // Update the player's class, deduct one change point, and update stats.
        PlayerClassManager.setPlayerClass(player, className);
        PlayerClassManager.deductClassChangePoint(player);
        PlayerStatsManager.updateStatsForClass(player, className);

        player.sendMessage(ChatColor.GREEN + "Your class has been updated to " + className + "!");
        player.closeInventory();
    }
}