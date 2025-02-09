package com.sandcore.utils;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColorUtil {

    /**
     * Converts hex color codes in the format "&#rrggbb" to actual color codes.
     */
    public static String translateHexColorCodes(String message) {
        if (message == null) return null;
        Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = pattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            // Use the Bungee API's ChatColor.of() method with a fully qualified call.
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}