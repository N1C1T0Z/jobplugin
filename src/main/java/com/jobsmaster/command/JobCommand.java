package com.jobsmaster.command;

import com.jobsmaster.JobType;
import com.jobsmaster.data.PlayerJobData;
import com.jobsmaster.gui.JobGUI;
import com.jobsmaster.manager.ConfigManager;
import com.jobsmaster.manager.JobManager;
import com.jobsmaster.util.MessageUtil;
import com.jobsmaster.util.ProgressBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JobCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager;
    private final JobManager jobManager;
    private final JobGUI jobGUI;

    private static final List<String> SUBCOMMANDS = Arrays.asList("info", "prestige", "top");

    public JobCommand(ConfigManager configManager, JobManager jobManager, JobGUI jobGUI) {
        this.configManager = configManager;
        this.jobManager = jobManager;
        this.jobGUI = jobGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.colorize(configManager.getPrefix() + configManager.getMessage("player-only")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            jobGUI.openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                handleInfo(player, args);
                break;
            case "prestige":
                handlePrestige(player);
                break;
            case "top":
                handleTop(player, args);
                break;
            default:
                jobGUI.openMainMenu(player);
                break;
        }

        return true;
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            sendJobList(player);
            return;
        }

        JobType job = JobType.fromString(args[1]);
        if (job == null) {
            sendJobList(player);
            return;
        }

        PlayerJobData data = jobManager.getPlayerData(player.getUniqueId());
        int level = data.getLevel(job);
        double currentXP = data.getXP(job);
        double requiredXP = jobManager.getRequiredXP(level);
        double reward = jobManager.getReward(level + 1, data.getPrestigeLevel());

        player.sendMessage(MessageUtil.colorize("&8&m━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(MessageUtil.format(configManager.getMessage("info-title"), "job", job.getDisplayName()));
        player.sendMessage("");
        player.sendMessage(MessageUtil.format(configManager.getMessage("info-level"),
                "level", String.valueOf(level)));
        player.sendMessage(MessageUtil.format(configManager.getMessage("info-xp"),
                "current", String.valueOf((int) currentXP),
                "required", String.valueOf((int) requiredXP)));
        player.sendMessage(ProgressBar.getProgressBar(currentXP, requiredXP, 20));
        player.sendMessage("");
        player.sendMessage(MessageUtil.format(configManager.getMessage("info-reward"),
                "reward", String.format("%.2f", reward)));
        player.sendMessage(MessageUtil.colorize("&8&m━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handlePrestige(Player player) {
        PlayerJobData data = jobManager.getPlayerData(player.getUniqueId());

        player.sendMessage(MessageUtil.colorize("&8&m━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(MessageUtil.colorize("&d&lRenaissance"));
        player.sendMessage("");
        player.sendMessage(MessageUtil.colorize("&7La renaissance réinitialise tous vos métiers"));
        player.sendMessage(MessageUtil.colorize("&7au niveau 1 mais augmente vos récompenses de x1.5."));
        player.sendMessage(MessageUtil.format(configManager.getMessage("prestige-info"),
                "level", String.valueOf(data.getPrestigeLevel()),
                "multiplier", String.format("%.1f", data.getPrestigeMultiplier())));
        player.sendMessage("");

        if (data.getPrestigeLevel() >= 10) {
            player.sendMessage(MessageUtil.colorize(configManager.getMessage("prestige-fail-max-level")));
        } else if (data.isAllMaxLevel()) {
            player.sendMessage(MessageUtil.colorize("&a&lCliquez sur la renaissance dans le menu &7(/job)"));
            player.sendMessage(MessageUtil.colorize("&7&oOu utilisez &f/job prestige confirm&7&o pour confirmer"));
        } else {
            player.sendMessage(MessageUtil.colorize(configManager.getMessage("prestige-fail-not-max")));
        }
        player.sendMessage(MessageUtil.colorize("&8&m━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleTop(Player player, String[] args) {
        JobType job = null;
        if (args.length >= 2) {
            job = JobType.fromString(args[1]);
        }
        if (job == null) job = JobType.HUNTER;

        final JobType selectedJob = job;
        List<Map.Entry<String, Integer>> top = jobManager.getTopPlayers(job);

        player.sendMessage(MessageUtil.colorize("&8&m━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(MessageUtil.format(configManager.getMessage("top-title"), "job", selectedJob.getDisplayName()));
        player.sendMessage("");

        if (top.isEmpty()) {
            player.sendMessage(MessageUtil.colorize("&7Aucun joueur classé pour l'instant."));
        } else {
            int position = 1;
            for (Map.Entry<String, Integer> entry : top) {
                String prefix = position == 1 ? "&6" : position == 2 ? "&e" : position == 3 ? "&c" : "&7";
                player.sendMessage(MessageUtil.colorize(
                        MessageUtil.format(configManager.getMessage("top-entry"),
                                "position", prefix + position + "&7",
                                "player", entry.getKey(),
                                "level", String.valueOf(entry.getValue()))));
                position++;
            }
        }
        player.sendMessage(MessageUtil.colorize("&8&m━━━━━━━━━━━━━━━━━━━━"));
    }

    private void sendJobList(Player player) {
        StringBuilder jobs = new StringBuilder();
        for (JobType job : JobType.values()) {
            if (jobs.length() > 0) jobs.append("&7, ");
            jobs.append(job.getColor()).append(job.name().toLowerCase());
        }
        player.sendMessage(MessageUtil.colorize(
                configManager.getPrefix() +
                MessageUtil.format(configManager.getMessage("job-not-found"),
                        "jobs", MessageUtil.colorize(jobs.toString()))));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("top"))) {
            return Arrays.stream(JobType.values())
                    .map(j -> j.name().toLowerCase())
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}