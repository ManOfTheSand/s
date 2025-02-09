package com.sandcore.mmo.gui;

import com.sandcore.mmo.MMOClass;
import com.sandcore.mmo.MMOClassManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassSelectionGUI implements Listener {

    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Select Your Class";

    public static void open(Player player) {
        Map<String, MMOClass> classes = MMOClassManager.getInstance().getClasses();
        if (classes.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No classes available. Please contact an admin.");
            return;
        }
        int size = ((classes.size() - 1) / 9 + 1) * 9; // Round up to nearest multiple of 9
        Inventory classInventory = Bukkit.createInventory(null, size, GUI_TITLE);

        for (MMOClass mmoClass : classes.values()) {
            ItemStack item = new ItemStack(mmoClass.getIcon());
            ItemMeta meta = item.getItemMeta();
            // Translate color codes (e.g., &5 to §5)
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', mmoClass.getDisplayName()));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + mmoClass.getDescription());
            lore.add(ChatColor.AQUA + "Health: " + mmoClass.getBaseHealth());
            lore.add(ChatColor.AQUA + "Mana: " + mmoClass.getBaseMana());
            meta.setLore(lore);
            item.setItemMeta(meta);
            classInventory.addItem(item);
        }
        player.openInventory(classInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }
        String selectedDisplay = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
        Player player = (Player) event.getWhoClicked();

        // Assuming your config keys match the lowercase stripped display names
        MMOClass selectedClass = MMOClassManager.getInstance().getClass(selectedDisplay);
        if (selectedClass != null) {
            player.sendMessage(ChatColor.GREEN + "You selected the " + selectedClass.getDisplayName() + ChatColor.GREEN + " class!");
            // TODO: Add code here to assign the class to the player.
            player.closeInventory();
        } else {
            player.sendMessage(ChatColor.RED + "That class is not available.");
        }
    }
}