package com.sandcore.holograms;

import com.sandcore.SandCore;
import com.sandcore.utils.ChatColorUtil;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkillTreeHologram {

    private static final Map<UUID, Hologram> activeHolograms = new ConcurrentHashMap<>();
    private static int taskId = -1;

    public static void spawnForPlayer(Player player) {
        ConfigurationSection config = SandCore.getInstance().getConfig();
        Location loc = calculateHologramLocation(player);
        List<String> lines = buildHologramContent(player, config);

        String hologramId = "skilltree-" + player.getUniqueId();
        Hologram hologram = DHAPI.createHologram(hologramId, loc, lines);

        setupHologramAnimation(hologram, config);
        activeHolograms.put(player.getUniqueId(), hologram);
        startCleanupTask(player, hologram, config);
    }

    private static Location calculateHologramLocation(Player player) {
        return player.getLocation().add(player.getLocation().getDirection().multiply(3).setY(1.5));
    }

    private static List<String> buildHologramContent(Player player, ConfigurationSection config) {
        List<String> lines = new ArrayList<>();
        String header = ChatColorUtil.translateHexColorCodes(config.getString("ui.header", ""));
        lines.add(header);

        ConfigurationSection categories = config.getConfigurationSection("ui.categories");
        if (categories != null) {
            categories.getKeys(false).forEach(category -> {
                String title = ChatColorUtil.translateHexColorCodes(categories.getString(category + ".title", ""));
                lines.add(title);
                ConfigurationSection items = categories.getConfigurationSection(category + ".items");
                if (items != null) {
                    items.getKeys(false).forEach(item -> {
                        String display = categories.getString(category + ".items." + item + ".display", "")
                                .replace("%level%", String.valueOf(categories.getInt(category + ".items." + item + ".required-level")));
                        lines.add(ChatColorUtil.translateHexColorCodes(display));
                    });
                }
                lines.add(""); // Empty line between categories
            });
        }
        return lines;
    }

    private static void setupHologramAnimation(Hologram hologram, ConfigurationSection config) {
        int speed = config.getInt("hologram.animation-speed", 2);
        // Replace the invalid setHologramLine call with a custom animation scheduler for the header (line 0).
        Bukkit.getScheduler().runTaskTimer(SandCore.getInstance(), new Runnable() {
            int phase = 0;
            @Override
            public void run() {
                String animatedHeader = getAnimatedHeader(phase);
                DHAPI.setHologramLine(hologram, 0, animatedHeader);
                phase++;
            }
        }, 0L, speed * 20L); // speed * 20L converts seconds to ticks; adjust as needed
    }

    private static String getAnimatedHeader(int phase) {
        // A simple wave animation effect: cycle between two color variants.
        if (phase % 2 == 0) {
            return ChatColorUtil.translateHexColorCodes("&#00FF7FSANDCORE SKILLS");
        } else {
            return ChatColorUtil.translateHexColorCodes("&#32CD32SANDCORE SKILLS");
        }
    }

    private static void startCleanupTask(Player player, Hologram hologram, ConfigurationSection config) {
        if (taskId == -1) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(SandCore.getInstance(), () ->
                    activeHolograms.forEach((uuid, h) -> {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p == null || !p.isOnline() || p.getLocation().distance(h.getLocation()) > config.getInt("hologram.view-distance", 5)) {
                            h.delete();
                            activeHolograms.remove(uuid);
                        }
                    }), 0L, config.getLong("hologram.update-interval", 20) * 20L);
        }
    }

    public static void reload() {
        activeHolograms.values().forEach(Hologram::delete);
        activeHolograms.clear();
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}