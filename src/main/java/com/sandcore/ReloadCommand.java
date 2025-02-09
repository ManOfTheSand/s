package com.sandcore.mmo.commands;

import com.sandcore.SandCore;
import com.sandcore.utils.ChatColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sandcore.reload")) {
            sender.sendMessage(ChatColorUtil.translateHexColorCodes("&#FF0000You don't have permission to reload the plugin!"));
            return true;
        }
        SandCore.getInstance().reloadConfig();
        sender.sendMessage(ChatColorUtil.translateHexColorCodes(
                SandCore.getInstance().getConfig().getString("ui.reload-message", "&#00FF00Config reloaded!")
        ));
        return true;
    }
}