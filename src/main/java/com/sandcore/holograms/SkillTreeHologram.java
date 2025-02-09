package com.sandcore.holograms;


import com.sandcore.SandCore;
import com.sandcore.utils.ChatColorUtil;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SkillTreeHologram implements Listener {

    private static final Map<UUID, Hologram> activeHolograms = new HashMap<>();

    public static void spawnForPlayer(Player player) {
        // Determine a location in front of the player to spawn the hologram
        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(3));
        List<String> hologramLines = new ArrayList<>();

        FileConfiguration config = SandCore.getInstance().getConfig();
        ConfigurationSection skilltreeSection = config.getConfigurationSection("skilltree");
        if (skilltreeSection == null) {
            player.sendMessage(ChatColor.RED + "Skill tree is not configured!");
            return;
        }

        // Create hologram lines reading from config using hex colors
        for (String key : skilltreeSection.getKeys(false)) {
            String display = skilltreeSection.getString(key + ".display_name", key);
            int reqLevel = skilltreeSection.getInt(key + ".required_level", 1);
            // Use the ChatColorUtil to convert hex colors
            String line = ChatColorUtil.translateHexColorCodes(display) + ChatColor.GRAY
                    + " (Req Level: " + reqLevel + ")";
            hologramLines.add(line);
        }

        // Provide a unique ID for the hologram (API expects a string id as the first parameter)
        String hologramId = "skilltree_" + player.getUniqueId().toString();
        // Create the hologram using DecentHolograms API: now with a unique id, location, and list of strings
        Hologram hologram = DHAPI.createHologram(hologramId, loc, hologramLines);
        activeHolograms.put(player.getUniqueId(), hologram);

        // Remove the hologram if the player moves away (run every second)
        Bukkit.getScheduler().runTaskTimer(SandCore.getInstance(), () -> {
            if (player.isOnline() && activeHolograms.containsKey(player.getUniqueId())) {
                if (player.getLocation().distance(loc) > 5) {
                    hologram.delete();
                    activeHolograms.remove(player.getUniqueId());
                }
            }
        }, 20L, 20L);
    }
}