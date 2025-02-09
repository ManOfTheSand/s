package com.sandcore.mmo;

import org.bukkit.Material;

public class MMOClass {
    private final String id;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final double baseHealth;
    private final double baseMana;

    public MMOClass(String id, String displayName, String description, Material icon, double baseHealth, double baseMana) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.baseHealth = baseHealth;
        this.baseMana = baseMana;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public double getBaseHealth() {
        return baseHealth;
    }

    public double getBaseMana() {
        return baseMana;
    }
}