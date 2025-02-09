package com.sandcore.mmo.commands;

import com.sandcore.mmo.SandCore;
import com.sandcore.mmo.PlayerStats;
import com.sandcore.mmo.PlayerStatsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();
        switch (cmdName) {
            case "stats":
                return executeStatsCommand(sender, args);
            case "sandcore":
                return executeSandCoreCommand(sender, args);
            case "classselect":
                return executeClassSelectCommand(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command.");
                return true;
        }
    }

    private boolean executeStatsCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        // Retrieve the player's stats using the public getter getStatsManager() from SandCore
        PlayerStatsManager statsManager = SandCore.getInstance().getStatsManager();
        if (statsManager != null) {
            PlayerStats stats = statsManager.getStats(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Your Stats: " + stats.toString());
        } else {
            player.sendMessage(ChatColor.RED + "Stats not available.");
        }
        return true;
    }

    private boolean executeSandCoreCommand(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "Welcome to SandCore! Use /stats for your stats or /classselect to choose a class.");
        return true;
    }

    private boolean executeClassSelectCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can select a class.");
            return true;
        }
        // Open the class selection GUI (if implemented)
        // For now, we'll let the player know it's under construction.
        sender.sendMessage(ChatColor.GREEN + "Class selection GUI is under construction.");
        return true;
    }
}