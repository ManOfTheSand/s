package com.sandcore.mmo;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MMOClassManager {

    private static MMOClassManager instance;
    private final Map<String, MMOClass> classes = new HashMap<>();

    private MMOClassManager() {
    }

    public static MMOClassManager getInstance() {
        if (instance == null) {
            instance = new MMOClassManager();
        }
        return instance;
    }

    public void loadClasses(JavaPlugin plugin) {
        classes.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("classes");

        if (section == null) {
            plugin.getLogger().warning("No MMO classes found in config.yml under section 'classes'");
            return;
        }
        for (String key : section.getKeys(false)) {
            String displayName = section.getString(key + ".display");
            String description = section.getString(key + ".description");
            String iconName = section.getString(key + ".icon");
            double baseHealth = section.getDouble(key + ".baseHealth");
            double baseMana = section.getDouble(key + ".baseMana");

            Material icon;
            try {
                icon = Material.valueOf(iconName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid icon (" + iconName + ") for class " + key + ". Using BEDROCK as fallback.");
                icon = Material.BEDROCK;
            }
            MMOClass mmoClass = new MMOClass(key, displayName, description, icon, baseHealth, baseMana);
            classes.put(key, mmoClass);
        }
    }

    public Map<String, MMOClass> getClasses() {
        return classes;
    }

    public MMOClass getClass(String id) {
        return classes.get(id);
    }
}