package com.sandcore.mmo;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerStatPointsManager {
    private static HashMap<UUID, Integer> statPoints = new HashMap<>();

    public static int getStatPoints(Player player) {
        return statPoints.getOrDefault(player.getUniqueId(), 0);
    }

    public static void addStatPoints(Player player, int points) {
        statPoints.put(player.getUniqueId(), getStatPoints(player) + points);
    }

    public static boolean deductStatPoint(Player player) {
        int current = getStatPoints(player);
        if (current > 0) {
            statPoints.put(player.getUniqueId(), current - 1);
            return true;
        }
        return false;
    }
}