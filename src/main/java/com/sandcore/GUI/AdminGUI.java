package com.sandcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class AdminGUI implements Listener {

    private Inventory inv;

    public AdminGUI() {
        createGUI();
    }

    public void createGUI() {
        ConfigurationSection guiConfig = SandCore.getInstance().getConfig().getConfigurationSection("guis.admin");
        int size = guiConfig.getInt("size", 27);
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("title", "Admin Stats"));
        inv = Bukkit.createInventory(null, size, title);

        // Fill full inventory with default background
        ConfigurationSection bgConfig = SandCore.getInstance().getConfig().getConfigurationSection("guis.default.background");
        if (bgConfig != null) {
            ItemStack background = createItem(bgConfig);
            for (int i = 0; i < size; i++) {
                inv.setItem(i, background);
            }
        }

        // Place dummy admin stat items (e.g., Online Players) in the specified icon slots.
        List<Integer> iconSlots = guiConfig.getIntegerList("icon_slots");
        ItemStack onlinePlayers = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = onlinePlayers.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aOnline Players"));
        onlinePlayers.setItemMeta(meta);

        for (Integer slot : iconSlots) {
            inv.setItem(slot, onlinePlayers);
        }
    }

    private ItemStack createItem(ConfigurationSection section) {
        Material material = Material.getMaterial(section.getString("material", "BLACK_STAINED_GLASS_PANE"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("display_name", " ")));
        List<String> lore = section.getStringList("lore");
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // Open the Admin GUI for a given player.
    public void openGUI(Player player) {
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(inv)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(ChatColor.GREEN + "Admin GUI clicked. More stats coming soon!");
        }
    }
}