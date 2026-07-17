package com.jobsmaster.util;

public class ProgressBar {

    public static String getProgressBar(double current, double max, int totalBars) {
        double percent = Math.min(current / max, 1.0);
        int completedBars = (int) Math.round(percent * totalBars);

        StringBuilder sb = new StringBuilder("&a");
        for (int i = 0; i < completedBars; i++) {
            sb.append("■");
        }
        sb.append("&7");
        for (int i = completedBars; i < totalBars; i++) {
            sb.append("■");
        }
        sb.append(" &f").append((int) (percent * 100)).append("%");

        return MessageUtil.colorize(sb.toString());
    }
}