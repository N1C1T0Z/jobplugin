package com.jobsmaster.util;

import org.bukkit.ChatColor;

public class MessageUtil {

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String format(String message, String... replacements) {
        String result = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                result = result.replace("%" + replacements[i] + "%", replacements[i + 1]);
            }
        }
        return colorize(result);
    }
}