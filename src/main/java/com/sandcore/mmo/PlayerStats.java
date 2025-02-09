package com.sandcore.mmo;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class PlayerStats {
    private double health;
    private double damage;
    private double speed;
    private double defense;

    // No-argument constructor (kept for compatibility)
    public PlayerStats() {
        // Default stat values
        this.health = 20.0;
        this.damage = 2.0;
        this.speed = 0.1;
        this.defense = 0.0;
    }

    // New overloaded constructor for initializing with specific values
    public PlayerStats(double health, double damage, double speed, double defense) {
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.defense = defense;
    }

    // Getters and setters

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    /**
     * Apply these stats to the player's attributes.
     */
    public void applyToPlayer(Player player) {
        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        }
        if (player.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            player.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        }
        if (player.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
        }
        if (player.getAttribute(Attribute.ARMOR) != null) {
            player.getAttribute(Attribute.ARMOR).setBaseValue(defense);
        }
    }

    @Override
    public String toString() {
        return "Health: " + health + ", Damage: " + damage + ", Speed: " + speed + ", Defense: " + defense;
    }
}