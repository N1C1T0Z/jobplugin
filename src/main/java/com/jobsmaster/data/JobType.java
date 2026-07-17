package com.jobsmaster.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobType {
    HUNTER("chasseur"),
    LUMBERJACK("bûcheron"),
    FARMER("agriculteur"),
    BUILDER("builder"),
    MINER("mineur");

    private final String displayName;

    public static JobType fromString(String s) {
        for (JobType type : values()) {
            if (type.name().equalsIgnoreCase(s) || type.getDisplayName().equalsIgnoreCase(s)) {
                return type;
            }
        }
        return null;
    }
}