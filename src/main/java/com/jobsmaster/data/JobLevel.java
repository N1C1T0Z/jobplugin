package com.jobsmaster.data;

public class JobLevel {

    private final int level;
    private final double xpRequired;
    private final double reward;

    public JobLevel(int level, double xpRequired, double reward) {
        this.level = level;
        this.xpRequired = xpRequired;
        this.reward = reward;
    }

    public int getLevel() {
        return level;
    }

    public double getXpRequired() {
        return xpRequired;
    }

    public double getReward() {
        return reward;
    }
}