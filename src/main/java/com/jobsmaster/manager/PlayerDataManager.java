package com.jobsmaster.manager;

import com.jobsmaster.JobsMaster;
import com.jobsmaster.JobType;
import com.jobsmaster.data.PlayerJobData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final JobsMaster plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerJobData> playerData = new HashMap<>();

    public PlayerDataManager(JobsMaster plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerJobData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), uuid -> {
            PlayerJobData data = load(uuid);
            if (data == null) {
                data = new PlayerJobData(uuid);
            }
            return data;
        });
    }

    public void save(Player player) {
        save(player.getUniqueId(), playerData.get(player.getUniqueId()));
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerJobData> entry : playerData.entrySet()) {
            save(entry.getKey(), entry.getValue());
        }
    }

    private void save(UUID uuid, PlayerJobData data) {
        if (data == null) return;
        File file = new File(dataFolder, uuid.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("prestige", data.getPrestige());

        for (JobType type : JobType.values()) {
            String path = "jobs." + type.name().toLowerCase();
            config.set(path + ".level", data.getLevel(type));
            config.set(path + ".xp", data.getXP(type));
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de sauvegarder les données de " + uuid);
        }
    }

    private PlayerJobData load(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerJobData data = new PlayerJobData(uuid);
        data.setPrestige(config.getInt("prestige", 0));

        for (JobType type : JobType.values()) {
            String path = "jobs." + type.name().toLowerCase();
            if (config.contains(path)) {
                data.setLevel(type, config.getInt(path + ".level", 1));
                data.setXP(type, config.getDouble(path + ".xp", 0.0));
            }
        }

        return data;
    }
}