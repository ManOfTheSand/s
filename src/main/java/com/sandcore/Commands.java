package com.sandcore.mmo.commands;

import com.sandcore.mmo.PlayerClassManager;
import com.sandcore.mmo.PlayerStatPointsManager;
import com.sandcore.mmo.PlayerStats;
import com.sandcore.mmo.PlayerStatsManager;
import com.sandcore.mmo.gui.ClassSelectionGUI;
import com.sandcore.mmo.gui.StatsUpgradeGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use these commands.");
            return true;
        }
        Player player = (Player) sender;

        // To keep things organized, we'll use two main command names: "class" and "stats"
        if (command.getName().equalsIgnoreCase("class")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.YELLOW + "Usage: /class <select|info|addcp>");
                return true;
            }
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("select")) {
                // Open the class selection GUI.
                ClassSelectionGUI.open(player);
            } else if (subCmd.equals("info")) {
                String currentClass = PlayerClassManager.getPlayerClass(player);
                int cp = PlayerClassManager.getClassChangePoints(player);
                PlayerStats stats = PlayerStatsManager.getPlayerStats(player);
                player.sendMessage(ChatColor.AQUA + "Your class: " + (currentClass == null ? "None" : currentClass));
                player.sendMessage(ChatColor.AQUA + "Class Change Points: " + cp);
                player.sendMessage(ChatColor.AQUA + "Stats: " + stats.toString());
            } else if (subCmd.equals("addcp")) {
                // Usage: /class addcp <amount> (admin only)
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /class addcp <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[1]);
                    if (player.hasPermission("sandcore.admin")) {
                        PlayerClassManager.addClassChangePoints(player, amount);
                        player.sendMessage(ChatColor.GREEN + "Added " + amount + " class change point(s).");
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permission.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Amount must be a number.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Unknown subcommand.");
            }
        } else if (command.getName().equalsIgnoreCase("stats")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.YELLOW + "Usage: /stats <upgrade|add>");
                return true;
            }
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("upgrade")) {
                // Open the stats upgrade GUI.
                StatsUpgradeGUI.open(player);
            } else if (subCmd.equals("add")) {
                // Usage: /stats add <amount> (admin only)
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /stats add <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[1]);
                    if (player.hasPermission("sandcore.admin")) {
                        PlayerStatPointsManager.addStatPoints(player, amount);
                        player.sendMessage(ChatColor.GREEN + "Added " + amount + " stat point(s).");
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permission.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Amount must be a number.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Unknown subcommand.");
            }
        }
        return true;
    }
}