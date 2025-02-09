package com.sandcore.commands;

import com.sandcore.SandCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Use command name (lowercase) to distinguish command actions
        String cmdName = command.getName().toLowerCase();
        if (cmdName.equals("screload")) {
            // Reload command for updating the config on the fly
            if (sender instanceof Player && !sender.hasPermission("sandcore.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission.");
                return true;
            }
            SandCore.getInstance().reloadConfig();
            sender.sendMessage(ChatColor.toMM("#00FF00") + "SandCore configuration reloaded!");
            return true;
        } else if (cmdName.equals("skilltree")) {
            // Spawn the hologram-based skill tree near the player
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            com.sandcore.holograms.SkillTreeHologram.spawnForPlayer((Player) sender);
            return true;
        }
        // Add other commands (class, stats, adminstats, etc.) as needed.
        sender.sendMessage(ChatColor.RED + "Unknown command.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Return tab-complete suggestions based on the command.
        List<String> completions = new ArrayList<>();
        // For instance, you could complete "screload" or "skilltree" commands if necessary.
        return completions;
    }
}