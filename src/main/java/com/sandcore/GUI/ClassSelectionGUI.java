package com.sandcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ClassSelectionGUI implements Listener {

    private Inventory inv;

    public ClassSelectionGUI() {
        createGUI();
    }

    public void createGUI() {
        // Load Class Selection GUI configuration
        ConfigurationSection guiConfig = SandCore.getInstance().getConfig().getConfigurationSection("guis.class_selection");
        int size = guiConfig.getInt("size", 27);
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("title", "Select Your Class"));
        inv = Bukkit.createInventory(null, size, title);

        // Fill inventory with filler items from configuration.
        ConfigurationSection fillerConfig = guiConfig.getConfigurationSection("filler");
        if (fillerConfig != null) {
            ItemStack filler = createItem(fillerConfig);
            for (int i = 0; i < size; i++) {
                inv.setItem(i, filler);
            }
        }

        // Load classes from classes.yml
        File classesFile = new File(SandCore.getInstance().getDataFolder(), "classes.yml");
        YamlConfiguration classesConfig = YamlConfiguration.loadConfiguration(classesFile);
        ConfigurationSection classesSection = classesConfig.getConfigurationSection("classes");
        if (classesSection == null) {
            SandCore.getInstance().getLogger().warning("No classes found in classes.yml!");
            return;
        }

        // Retrieve the list of icon slots from configuration.
        List<Integer> iconSlots = guiConfig.getIntegerList("icon_slots");
        Set<String> classKeys = classesSection.getKeys(false);
        int slotIndex = 0;
        for (String key : classKeys) {
            ConfigurationSection classSection = classesSection.getConfigurationSection(key);
            // Create the item representing the class.
            Material mat = Material.getMaterial(classSection.getString("material", "STONE"));
            ItemStack classItem = new ItemStack(mat);
            ItemMeta meta = classItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', classSection.getString("display_name", key)));
            List<String> lore = classSection.getStringList("lore");
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
            }
            meta.setLore(lore);
            classItem.setItemMeta(meta);

            // Place the class item in the configured icon slot, or choose a fallback slot.
            if (slotIndex < iconSlots.size()) {
                int slot = iconSlots.get(slotIndex);
                inv.setItem(slot, classItem);
            } else {
                for (int i = 0; i < inv.getSize(); i++) {
                    if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                        inv.setItem(i, classItem);
                        break;
                    }
                }
            }
            slotIndex++;
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

    // Open the Class Selection GUI for a given player.
    public void openGUI(Player player) {
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(inv)) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                Player player = (Player) event.getWhoClicked();
                player.sendMessage(ChatColor.GREEN + "You selected the class: " + event.getCurrentItem().getItemMeta().getDisplayName());
                // Here you can add code to assign the class to the player.
                player.closeInventory();
            }
        }
    }
}