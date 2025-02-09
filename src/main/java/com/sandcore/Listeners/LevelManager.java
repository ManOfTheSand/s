package com.sandcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelManager {
    public enum SkillType {
        COMBAT,
        LUMBER,
        MINING,
        FISHING;
    }

    // Stores player level data in memory
    private static Map<UUID, PlayerLevelData> levelData = new HashMap<>();

    // File for persistent storage
    private static File levelDataFile;
    private static FileConfiguration levelConfig;

    // Load level data from disk
    public static void loadData() {
        levelDataFile = new File(SandCore.getInstance().getDataFolder(), "leveldata.yml");
        if (!levelDataFile.exists()) {
            try {
                levelDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        levelConfig = YamlConfiguration.loadConfiguration(levelDataFile);
        for (String key : levelConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            PlayerLevelData data = new PlayerLevelData();
            data.setCombatLevel(levelConfig.getInt(key + ".combatLevel", 1));
            data.setCombatXP(levelConfig.getDouble(key + ".combatXP", 0));
            data.setLumberLevel(levelConfig.getInt(key + ".lumberLevel", 1));
            data.setLumberXP(levelConfig.getDouble(key + ".lumberXP", 0));
            data.setMiningLevel(levelConfig.getInt(key + ".miningLevel", 1));
            data.setMiningXP(levelConfig.getDouble(key + ".miningXP", 0));
            data.setFishingLevel(levelConfig.getInt(key + ".fishingLevel", 1));
            data.setFishingXP(levelConfig.getDouble(key + ".fishingXP", 0));
            levelData.put(uuid, data);
        }
    }

    // Save level data to disk
    public static void saveData() {
        for (Map.Entry<UUID, PlayerLevelData> entry : levelData.entrySet()) {
            String key = entry.getKey().toString();
            PlayerLevelData data = entry.getValue();
            levelConfig.set(key + ".combatLevel", data.getCombatLevel());
            levelConfig.set(key + ".combatXP", data.getCombatXP());
            levelConfig.set(key + ".lumberLevel", data.getLumberLevel());
            levelConfig.set(key + ".lumberXP", data.getLumberXP());
            levelConfig.set(key + ".miningLevel", data.getMiningLevel());
            levelConfig.set(key + ".miningXP", data.getMiningXP());
            levelConfig.set(key + ".fishingLevel", data.getFishingLevel());
            levelConfig.set(key + ".fishingXP", data.getFishingXP());
        }
        try {
            levelConfig.save(levelDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get level data for a player (create if absent)
    public static PlayerLevelData getPlayerLevelData(Player player) {
        return levelData.computeIfAbsent(player.getUniqueId(), k -> new PlayerLevelData());
    }

    // Add XP to a player's skill and handle level-ups
    public static void addXP(Player player, SkillType skill, double xpToAdd) {
        FileConfiguration config = SandCore.getInstance().getConfig();
        PlayerLevelData data = getPlayerLevelData(player);
        double currentXP = 0;
        int currentLevel = 0;
        double xpPerLevel = 1000; // default value

        switch(skill) {
            case COMBAT:
                currentXP = data.getCombatXP();
                currentLevel = data.getCombatLevel();
                xpPerLevel = config.getDouble("leveling.combat.xp_per_level", 1000);
                break;
            case LUMBER:
                currentXP = data.getLumberXP();
                currentLevel = data.getLumberLevel();
                xpPerLevel = config.getDouble("leveling.skills.lumber.xp_per_level", 500);
                break;
            case MINING:
                currentXP = data.getMiningXP();
                currentLevel = data.getMiningLevel();
                xpPerLevel = config.getDouble("leveling.skills.mining.xp_per_level", 500);
                break;
            case FISHING:
                currentXP = data.getFishingXP();
                currentLevel = data.getFishingLevel();
                xpPerLevel = config.getDouble("leveling.skills.fishing.xp_per_level", 300);
                break;
        }

        currentXP += xpToAdd;
        boolean leveledUp = false;
        while (currentXP >= xpPerLevel) {
            currentXP -= xpPerLevel;
            currentLevel++;
            leveledUp = true;
        }

        switch(skill) {
            case COMBAT:
                data.setCombatXP(currentXP);
                data.setCombatLevel(currentLevel);
                break;
            case LUMBER:
                data.setLumberXP(currentXP);
                data.setLumberLevel(currentLevel);
                break;
            case MINING:
                data.setMiningXP(currentXP);
                data.setMiningLevel(currentLevel);
                break;
            case FISHING:
                data.setFishingXP(currentXP);
                data.setFishingLevel(currentLevel);
                break;
        }

        if(leveledUp) {
            player.sendMessage(ChatColor.GOLD + "Congratulations! Your " + skill.name().toLowerCase() + " skill leveled up to " + currentLevel + "!");
        } else {
            player.sendMessage(ChatColor.BLUE + "You gained " + xpToAdd + " XP in " + skill.name().toLowerCase() + " (" + currentXP + "/" + xpPerLevel + " XP)");
        }

        // For example, update the combat level bar
        if(skill == SkillType.COMBAT) {
            LevelBarManager.updateLevelBar(player, currentXP / xpPerLevel, currentLevel);
        }

        // Asynchronously save level data
        new BukkitRunnable() {
            @Override
            public void run() {
                saveData();
            }
        }.runTaskAsynchronously(SandCore.getInstance());
    }
}