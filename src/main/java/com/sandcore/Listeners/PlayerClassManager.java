package com.sandcore.mmo;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerClassManager {
    private static HashMap<UUID, String> playerClasses = new HashMap<>();
    private static HashMap<UUID, Integer> classChangePoints = new HashMap<>();

    public static String getPlayerClass(Player player) {
        return playerClasses.get(player.getUniqueId());
    }

    public static void setPlayerClass(Player player, String className) {
        playerClasses.put(player.getUniqueId(), className);
    }

    public static int getClassChangePoints(Player player) {
        return classChangePoints.getOrDefault(player.getUniqueId(), 0);
    }

    public static void addClassChangePoints(Player player, int points) {
        classChangePoints.put(player.getUniqueId(), getClassChangePoints(player) + points);
    }

    public static void deductClassChangePoint(Player player) {
        int current = getClassChangePoints(player);
        if (current > 0) {
            classChangePoints.put(player.getUniqueId(), current - 1);
        }
    }
}