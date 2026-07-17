package com.jobsmaster;

import com.jobsmaster.command.JobCommand;
import com.jobsmaster.gui.JobGUI;
import com.jobsmaster.manager.BoostManager;
import com.jobsmaster.manager.ConfigManager;
import com.jobsmaster.manager.JobManager;
import com.jobsmaster.manager.PlayerDataManager;
import com.jobsmaster.listener.JobListener;
import org.bukkit.plugin.java.JavaPlugin;

public class JobsMaster extends JavaPlugin {

    private ConfigManager configManager;
    private JobManager jobManager;
    private JobGUI jobGUI;
    private PlayerDataManager playerDataManager;
    private BoostManager boostManager;

    @Override
    public void onEnable() {
        // Load config
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);

        // Initialize managers
        this.playerDataManager = new PlayerDataManager(this);
        this.boostManager = new BoostManager(this);
        this.jobManager = new JobManager(this, configManager);
        this.jobGUI = new JobGUI(configManager, jobManager);

        // Register command
        JobCommand jobCommand = new JobCommand(configManager, jobManager, jobGUI);
        getCommand("job").setExecutor(jobCommand);
        getCommand("job").setTabCompleter(jobCommand);

        // Register listener
        getServer().getPluginManager().registerEvents(new JobListener(configManager, jobManager), this);

        getLogger().info("JobsMaster enabled successfully!");
        getLogger().info("5 métiers disponibles: Chasseur, Bûcheron, Agriculteur, Builder, Mineur");
        getLogger().info("Utilisez /job pour commencer !");
    }

    @Override
    public void onDisable() {
        if (jobManager != null) {
            jobManager.saveData();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        getLogger().info("JobsMaster disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }
}