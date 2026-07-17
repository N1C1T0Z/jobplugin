package com.jobsmaster.manager;

import com.jobsmaster.JobsMaster;
import com.jobsmaster.JobType;
import org.bukkit.entity.Player;

import java.util.*;

public class BoostManager {

    private final JobsMaster plugin;
    private final Map<UUID, Map<JobType, Double>> activeBoosts = new HashMap<>();

    public BoostManager(JobsMaster plugin) {
        this.plugin = plugin;
    }

    public void applyBoost(Player player, JobType job, double multiplier, long durationTicks) {
        UUID uuid = player.getUniqueId();
        Map<JobType, Double> boosts = activeBoosts.computeIfAbsent(uuid, k -> new HashMap<>());
        for (JobType type : JobType.values()) {
            if (job == null || job == type) {
                boosts.put(type, multiplier + boosts.getOrDefault(type, 0.0));
            }
        }

        if (durationTicks > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Map<JobType, Double> b = activeBoosts.get(uuid);
                if (b != null) {
                    for (JobType type : JobType.values()) {
                        if (job == null || job == type) {
                            double val = Math.max(0, b.getOrDefault(type, 0.0) - multiplier);
                            if (val <= 0) b.remove(type);
                            else b.put(type, val);
                        }
                    }
                    if (b.isEmpty()) activeBoosts.remove(uuid);
                }
            }, durationTicks);
        }
    }

    public double getBoost(Player player, JobType job) {
        return activeBoosts.getOrDefault(player.getUniqueId(), new HashMap<>())
                .getOrDefault(job, 0.0);
    }

    public double getTotalMultiplier(Player player, JobType job) {
        double prestigeMult = plugin.getPlayerDataManager().getPlayerData(player).getRewardMultiplier();
        double boostMult = 1.0 + getBoost(player, job);
        return prestigeMult * boostMult;
    }
}