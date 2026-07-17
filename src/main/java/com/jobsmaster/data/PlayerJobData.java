package com.jobsmaster.data;

import com.jobsmaster.JobType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJobData {

    private final UUID playerId;
    private final Map<String, Integer> levels;
    private final Map<String, Double> xp;
    private int prestigeLevel;

    public PlayerJobData(UUID playerId) {
        this.playerId = playerId;
        this.levels = new HashMap<>();
        this.xp = new HashMap<>();
        for (JobType job : JobType.values()) {
            levels.put(job.name(), 1);
            xp.put(job.name(), 0.0);
        }
        this.prestigeLevel = 0;
    }

    // ─── Compatibility aliases ───

    public int getPrestige() {
        return getPrestigeLevel();
    }

    public void setPrestige(int prestige) {
        setPrestigeLevel(prestige);
    }

    public double getRewardMultiplier() {
        return getPrestigeMultiplier();
    }

    public boolean isAllMaxLevel(int ignored) {
        return isAllMaxLevel();
    }

    public JobProgress getJob(JobType type) {
        return new JobProgress(type);
    }

    public class JobProgress {
        private final JobType type;

        public JobProgress(JobType type) {
            this.type = type;
        }

        public int getLevel() {
            return PlayerJobData.this.getLevel(type);
        }

        public void setLevel(int level) {
            PlayerJobData.this.setLevel(type, level);
        }

        public double getXp() {
            return PlayerJobData.this.getXP(type);
        }

        public void setXp(double xp) {
            PlayerJobData.this.setXP(type, xp);
        }
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getLevel(JobType job) {
        return levels.getOrDefault(job.name(), 1);
    }

    public void setLevel(JobType job, int level) {
        levels.put(job.name(), Math.min(level, 100));
    }

    public double getXP(JobType job) {
        return xp.getOrDefault(job.name(), 0.0);
    }

    public void setXP(JobType job, double amount) {
        xp.put(job.name(), Math.max(0, amount));
    }

    public void addXP(JobType job, double amount) {
        xp.put(job.name(), getXP(job) + amount);
    }

    public int getPrestigeLevel() {
        return prestigeLevel;
    }

    public void setPrestigeLevel(int prestigeLevel) {
        this.prestigeLevel = Math.max(0, Math.min(prestigeLevel, 10));
    }

    public double getPrestigeMultiplier() {
        return Math.pow(1.5, prestigeLevel);
    }

    public int getTotalLevel() {
        int total = 0;
        for (JobType job : JobType.values()) {
            total += getLevel(job);
        }
        return total;
    }

    public int getMaxedJobs() {
        int count = 0;
        for (JobType job : JobType.values()) {
            if (getLevel(job) >= 100) count++;
        }
        return count;
    }

    public boolean isAllMaxLevel() {
        for (JobType job : JobType.values()) {
            if (getLevel(job) < 100) return false;
        }
        return true;
    }

    public Map<String, Integer> getLevelsMap() {
        return new HashMap<>(levels);
    }

    public void setLevelsMap(Map<String, Integer> levels) {
        this.levels.clear();
        this.levels.putAll(levels);
    }

    public Map<String, Double> getXpMap() {
        return new HashMap<>(xp);
    }

    public void setXpMap(Map<String, Double> xp) {
        this.xp.clear();
        this.xp.putAll(xp);
    }
}