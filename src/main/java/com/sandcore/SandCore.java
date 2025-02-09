package com.sandcore.mmo;

import com.sandcore.mmo.listeners.PlayerStatsListener;
import com.sandcore.mmo.PlayerStatsManager;
import com.sandcore.mmo.commands.Commands;
import com.sandcore.mmo.gui.ClassSelectionGUI;
import com.sandcore.mmo.MMOClassManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SandCore extends JavaPlugin {

    private static SandCore instance;
    private PlayerStatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("\u001B[35mMMOCORE HAS STARTED AND IS READY TO WIN THE BATTLE\u001B[0m");

        saveDefaultConfig();

        // Load MMO classes from config.yml
        MMOClassManager.getInstance().loadClasses(this);

        // Initialize the PlayerStatsManager and register its listener
        statsManager = new PlayerStatsManager(this);
        getServer().getPluginManager().registerEvents(new PlayerStatsListener(statsManager), this);

        // Register commands using our unified command executor.
        Commands commandsExecutor = new Commands();
        getCommand("stats").setExecutor(commandsExecutor);
        getCommand("sandcore").setExecutor(commandsExecutor);
        getCommand("classselect").setExecutor(commandsExecutor);

        // Register GUI listener if needed
        getServer().getPluginManager().registerEvents(new ClassSelectionGUI(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("SandCore disabled!");
        statsManager.saveStats();
    }

    public static SandCore getInstance() {
        return instance;
    }

    // Add this public getter to expose statsManager
    public PlayerStatsManager getStatsManager() {
        return statsManager;
    }
}