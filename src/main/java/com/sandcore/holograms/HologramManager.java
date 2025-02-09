package com.sandcore.holograms;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.List;  // Import for List
import java.util.UUID;

public class HologramManager {
    private static final HashMap<UUID, Hologram> activeHolograms = new HashMap<>();

    public static void createHologram(Player player, String id, List<String> lines) {
        Hologram hologram = DHAPI.createHologram(
                "skilltree-" + player.getUniqueId(),
                player.getLocation().add(0, 2, 0),
                lines
        );
        activeHolograms.put(player.getUniqueId(), hologram);
    }

    public static void reloadAll() {
        activeHolograms.values().forEach(Hologram::delete);
        activeHolograms.clear();
    }
}