package com.jobsmaster.manager;

import com.jobsmaster.JobsMaster;
import com.jobsmaster.JobType;
import com.jobsmaster.data.PlayerJobData;
import com.jobsmaster.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JobManager {

    private final JobsMaster plugin;
    private final ConfigManager configManager;
    private final Map<UUID, PlayerJobData> playerData = new HashMap<>();
    private final File dataFile;

    public JobManager(JobsMaster plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadData();
    }

    public PlayerJobData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerJobData(uuid));
    }

    public void addXP(Player player, JobType job, double amount) {
        PlayerJobData data = getPlayerData(player.getUniqueId());
        int currentLevel = data.getLevel(job);
        if (currentLevel >= configManager.getMaxLevel()) {
            data.setXP(job, 0);
            saveData();
            return;
        }

        double newXP = data.getXP(job) + amount;
        int levelsGained = 0;
        double totalReward = 0;

        while (newXP >= configManager.getRequiredXP(currentLevel) && currentLevel < configManager.getMaxLevel()) {
            newXP -= configManager.getRequiredXP(currentLevel);
            currentLevel++;
            levelsGained++;

            double reward = getReward(currentLevel, data.getPrestigeLevel());
            totalReward += reward;

            // Give money via command
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "money give " + player.getName() + " " + String.format("%.2f", reward));

            if (currentLevel >= configManager.getMaxLevel()) {
                newXP = 0;
                break;
            }
        }

        data.setLevel(job, currentLevel);
        data.setXP(job, Math.max(0, newXP));

        if (levelsGained > 0) {
            showLevelUpAnimation(player, job, currentLevel, totalReward);
        }

        saveData();
    }

    private void showLevelUpAnimation(Player player, JobType job, int level, double reward) {
        String title = MessageUtil.format(configManager.getMessage("level-up-title"),
                "job", job.getDisplayName(),
                "level", String.valueOf(level));
        String subtitle = MessageUtil.format(configManager.getMessage("level-up-subtitle"),
                "reward", String.format("%.2f", reward));

        player.sendTitle(MessageUtil.colorize(title), MessageUtil.colorize(subtitle), 10, 40, 10);

        String barTitle = MessageUtil.format(configManager.getMessage("level-up-bar"),
                "job", job.getDisplayName(),
                "level", String.valueOf(level),
                "reward", String.format("%.2f", reward));

        BossBar bar = Bukkit.createBossBar(
                MessageUtil.colorize(barTitle),
                BarColor.YELLOW,
                BarStyle.SOLID
        );
        bar.addPlayer(player);
        bar.setProgress(1.0);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bar.removeAll();
        }, 60L);
    }

    public double getRequiredXP(int level) {
        return configManager.getRequiredXP(level);
    }

    public double getReward(int level, int prestigeLevel) {
        double base = configManager.getBaseMoney() + (level * configManager.getMoneyPerLevel());
        double multiplier = Math.pow(configManager.getPrestigeMultiplier(), prestigeLevel);
        return base * multiplier;
    }

    public boolean prestige(Player player) {
        PlayerJobData data = getPlayerData(player.getUniqueId());

        if (data.getPrestigeLevel() >= configManager.getMaxPrestige()) {
            return false;
        }

        if (!data.isAllMaxLevel()) {
            return false;
        }

        data.setPrestigeLevel(data.getPrestigeLevel() + 1);
        for (JobType job : JobType.values()) {
            data.setLevel(job, 1);
            data.setXP(job, 0);
        }

        saveData();

        player.sendMessage(MessageUtil.colorize(
                configManager.getPrefix() +
                MessageUtil.format(configManager.getMessage("prestige-success"),
                        "level", String.valueOf(data.getPrestigeLevel()))));
        return true;
    }

    public List<Map.Entry<String, Integer>> getTopPlayers(JobType job) {
        Map<String, Integer> levels = new HashMap<>();
        for (Map.Entry<UUID, PlayerJobData> entry : playerData.entrySet()) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (name != null) {
                levels.put(name, entry.getValue().getLevel(job));
            }
        }
        return levels.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        if (!dataFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                PlayerJobData data = new PlayerJobData(uuid);

                Object levelsObj = config.get(key + ".levels");
                if (levelsObj instanceof Map) {
                    Map<String, Object> levelsMap = (Map<String, Object>) levelsObj;
                    for (Map.Entry<String, Object> entry : levelsMap.entrySet()) {
                        if (entry.getValue() instanceof Integer) {
                            JobType job = JobType.fromString(entry.getKey());
                            if (job != null) {
                                data.setLevel(job, (Integer) entry.getValue());
                            }
                        }
                    }
                }

                Object xpObj = config.get(key + ".xp");
                if (xpObj instanceof Map) {
                    Map<String, Object> xpMap = (Map<String, Object>) xpObj;
                    for (Map.Entry<String, Object> entry : xpMap.entrySet()) {
                        if (entry.getValue() instanceof Double) {
                            JobType job = JobType.fromString(entry.getKey());
                            if (job != null) {
                                data.setXP(job, (Double) entry.getValue());
                            }
                        }
                    }
                }

                data.setPrestigeLevel(config.getInt(key + ".prestigeLevel", 0));
                playerData.put(uuid, data);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in playerdata: " + key);
            }
        }

        plugin.getLogger().info("Loaded " + playerData.size() + " player data entries.");
    }

    public void saveData() {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, PlayerJobData> entry : playerData.entrySet()) {
            String path = entry.getKey().toString();
            PlayerJobData data = entry.getValue();

            for (JobType job : JobType.values()) {
                config.set(path + ".levels." + job.name(), data.getLevel(job));
                config.set(path + ".xp." + job.name(), data.getXP(job));
            }
            config.set(path + ".prestigeLevel", data.getPrestigeLevel());
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data: " + e.getMessage());
        }
    }
}