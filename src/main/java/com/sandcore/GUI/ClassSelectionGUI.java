package com.sandcore;

import com.sandcore.utils.ChatColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassSelectionGUI implements Listener {

    public static void open(Player player) {
        // Load the classes configuration from classes.yml
        File classesFile = new File(SandCore.getInstance().getDataFolder(), "classes.yml");
        YamlConfiguration classesConfig = YamlConfiguration.loadConfiguration(classesFile);
        ConfigurationSection classSection = classesConfig.getConfigurationSection("classes");

        // Determine inventory size. For simplicity, we'll use 9 slots (one row)
        int inventorySize = 9;
        // Set the title (use ui.header or a specific title)
        String title = ChatColorUtil.translateHexColorCodes("&#FFD700Select Your Class");
        Inventory gui = Bukkit.createInventory(null, inventorySize, title);

        // Retrieve extra space slot indices from the main config
        List<Integer> extraSpaceSlots = SandCore.getInstance().getConfig().getIntegerList("ui.extra_space_slots");
        Set<Integer> spacerSlots = new HashSet<>(extraSpaceSlots);

        // Prepare the list of class items
        List<ItemStack> classItems = new ArrayList<>();
        if (classSection != null) {
            for (String classKey : classSection.getKeys(false)) {
                ConfigurationSection cs = classSection.getConfigurationSection(classKey);
                if (cs == null) continue;
                String materialStr = cs.getString("material", "PAPER").toUpperCase();
                Material material = Material.getMaterial(materialStr);
                if (material == null) {
                    material = Material.PAPER;
                }
                String displayName = cs.getString("display_name", classKey);
                List<String> lore = cs.getStringList("lore");

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColorUtil.translateHexColorCodes(displayName));
                if (lore != null && !lore.isEmpty()) {
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(ChatColorUtil.translateHexColorCodes(line));
                    }
                    meta.setLore(coloredLore);
                }
                item.setItemMeta(meta);
                classItems.add(item);
            }
        }

        // Create a filler item (spacer) for extra spacing in the GUI. For example, a dark glass pane.
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        // Fill the inventory: for each slot from 0 to inventorySize - 1, if it's in the spacer slots, add a filler; otherwise, use the next class item if available.
        int classIndex = 0;
        for (int slot = 0; slot < inventorySize; slot++) {
            if (spacerSlots.contains(slot)) {
                gui.setItem(slot, filler);
            } else {
                if (classIndex < classItems.size()) {
                    gui.setItem(slot, classItems.get(classIndex));
                    classIndex++;
                }
            }
        }

        player.openInventory(gui);
    }

    // Optionally, add your InventoryClickEvent handler here to process clicks.
    public void onInventoryClick(InventoryClickEvent event) {
        // Your event handling logic
    }
}