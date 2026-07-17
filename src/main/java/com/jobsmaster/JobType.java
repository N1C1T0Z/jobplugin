package com.jobsmaster;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum JobType {

    HUNTER("&cChasseur", Material.BOW, "&7Tuez des créatures pour gagner de l'XP", ChatColor.RED),
    WOODCUTTER("&6Bûcheron", Material.IRON_AXE, "&7Coupez des arbres pour gagner de l'XP", ChatColor.GOLD),
    FARMER("&aAgriculteur", Material.WHEAT, "&7Récoltez des cultures pour gagner de l'XP", ChatColor.GREEN),
    BUILDER("&bBuilder", Material.BRICK, "&7Placez des blocs pour gagner de l'XP", ChatColor.AQUA),
    MINER("&7Mineur", Material.DIAMOND_PICKAXE, "&7Minez des minerais pour gagner de l'XP", ChatColor.GRAY);

    private final String displayName;
    private final Material icon;
    private final String description;
    private final ChatColor color;

    JobType(String displayName, Material icon, String description, ChatColor color) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.color = color;
    }

    public String getDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public Material getIcon() {
        return icon;
    }

    public String getDescription() {
        return ChatColor.translateAlternateColorCodes('&', description);
    }

    public ChatColor getColor() {
        return color;
    }

    public static JobType fromString(String name) {
        for (JobType job : values()) {
            if (job.name().equalsIgnoreCase(name) ||
                job.name().replace("_", "").equalsIgnoreCase(name)) {
                return job;
            }
        }
        return null;
    }
}