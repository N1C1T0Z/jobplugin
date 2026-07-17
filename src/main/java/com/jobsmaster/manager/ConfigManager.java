package com.jobsmaster.manager;

import com.jobsmaster.JobsMaster;
import com.jobsmaster.JobType;
import com.jobsmaster.data.JobLevel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final JobsMaster plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final Map<String, Map<String, Double>> xpValues = new HashMap<>();

    public ConfigManager(JobsMaster plugin) {
        this.plugin = plugin;
        loadConfig();
        loadMessages();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadXpValues();
    }

    private void loadXpValues() {
        xpValues.clear();
        for (JobType job : JobType.values()) {
            String path = "jobs." + job.name().toLowerCase();
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section == null) continue;

            Map<String, Double> values = new HashMap<>();
            String xpPath = job == JobType.BUILDER ? "xp-per-place" : "xp-per-" + (job == JobType.HUNTER ? "kill" : "block");
            ConfigurationSection xpSection = section.getConfigurationSection(xpPath);
            if (xpSection != null) {
                for (String key : xpSection.getKeys(false)) {
                    values.put(key.toUpperCase(), xpSection.getDouble(key));
                }
            }
            xpValues.put(job.name(), values);
        }
    }

    public void loadMessages() {
        File msgFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(msgFile);
    }

    public double getXP(JobType job, Material material) {
        Map<String, Double> values = xpValues.get(job.name());
        if (values == null) return 0;
        return values.getOrDefault(material.name(), 0.0);
    }

    public double getXP(JobType job, String entityName) {
        Map<String, Double> values = xpValues.get(job.name());
        if (values == null) return 0;
        return values.getOrDefault(entityName.toUpperCase(), 0.0);
    }

    public double getBuilderXP() {
        return config.getDouble("jobs.builder.xp-per-place", 2);
    }

    public double getRequiredXP(int level) {
        ConfigurationSection tiers = config.getConfigurationSection("level-system.tiers");
        if (tiers == null) return level * 100.0;

        for (String key : tiers.getKeys(false)) {
            ConfigurationSection tier = tiers.getConfigurationSection(key);
            if (tier == null) continue;
            String range = tier.getString("level-range", "1-10");
            String[] parts = range.split("-");
            int min = Integer.parseInt(parts[0]);
            int max = Integer.parseInt(parts[1]);
            double multiplier = tier.getDouble("xp-multiplier", 100);
            if (level >= min && level <= max) {
                return level * multiplier;
            }
        }
        return level * 100.0;
    }

    public double getBaseMoney() {
        return config.getDouble("rewards.base-money", 50.0);
    }

    public double getMoneyPerLevel() {
        return config.getDouble("rewards.money-per-level", 10.0);
    }

    public int getMaxLevel() {
        return config.getInt("level-system.max-level", 100);
    }

    public int getMaxPrestige() {
        return config.getInt("prestige.max-prestige", 10);
    }

    public double getPrestigeMultiplier() {
        return config.getDouble("prestige.multiplier-per-prestige", 1.5);
    }

    public String getMessage(String key) {
        return messages.getString(key, "&cMessage introuvable: " + key);
    }

    public String getPrefix() {
        return getMessage("prefix");
    }

    // ─── GUI helper methods ───

    public String getDisplayName(JobType job) {
        String path = "jobs." + job.name().toLowerCase() + ".display-name";
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, job.getDisplayName()));
    }

    public String getMaterial(JobType job) {
        String path = "jobs." + job.name().toLowerCase() + ".icon";
        return config.getString(path, job.getIcon().name());
    }

    public String getDescription(JobType job) {
        String path = "jobs." + job.name().toLowerCase() + ".description";
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, job.getDescription()));
    }

    public int getMaxLevel(JobType job) {
        return getMaxLevel();
    }

    public int getPrestigeMaxLevel() {
        return getMaxPrestige();
    }

    public JobLevel getLevel(JobType job, int level) {
        if (level > getMaxLevel()) return null;
        double xpRequired = getRequiredXP(level);
        double reward = getBaseMoney() + (level * getMoneyPerLevel());
        return new JobLevel(level, xpRequired, reward);
    }
}