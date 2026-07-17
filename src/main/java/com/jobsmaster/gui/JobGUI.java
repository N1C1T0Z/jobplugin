package com.jobsmaster.gui;

import com.jobsmaster.JobType;
import com.jobsmaster.data.PlayerJobData;
import com.jobsmaster.manager.ConfigManager;
import com.jobsmaster.manager.JobManager;
import com.jobsmaster.util.MessageUtil;
import com.jobsmaster.util.ProgressBar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class JobGUI {

    private final ConfigManager configManager;
    private final JobManager jobManager;

    public JobGUI(ConfigManager configManager, JobManager jobManager) {
        this.configManager = configManager;
        this.jobManager = jobManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MessageUtil.colorize(configManager.getMessage("gui-title")));
        PlayerJobData data = jobManager.getPlayerData(player.getUniqueId());

        // Job items (slots 0-4)
        for (int i = 0; i < JobType.values().length; i++) {
            JobType job = JobType.values()[i];
            inv.setItem(i, createJobItem(data, job));
        }

        // Border (glass panes)
        ItemStack border = createBorderItem();
        for (int i = 5; i < 9; i++) inv.setItem(i, border);
        for (int i = 9; i < 13; i++) inv.setItem(i, border);
        for (int i = 16; i < 18; i++) inv.setItem(i, border);
        for (int i = 18; i < 22; i++) inv.setItem(i, border);
        for (int i = 23; i < 27; i++) inv.setItem(i, border);

        // Stats item (slot 13)
        inv.setItem(13, createStatsItem(data));

        // Prestige item (slot 14)
        inv.setItem(14, createPrestigeItem(data));

        // Close button (slot 22)
        inv.setItem(22, createCloseItem());

        player.openInventory(inv);
    }

    private ItemStack createJobItem(PlayerJobData data, JobType job) {
        int level = data.getLevel(job);
        double currentXP = data.getXP(job);
        double requiredXP = jobManager.getRequiredXP(level);
        double reward = jobManager.getReward(level + 1, data.getPrestigeLevel());

        ItemStack item = new ItemStack(job.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(MessageUtil.colorize(
                MessageUtil.format(configManager.getMessage("gui-job-info"), "job", job.getDisplayName())));

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(MessageUtil.format(configManager.getMessage("gui-description"), "description", job.getDescription()));
        lore.add("");
        lore.add(MessageUtil.format(configManager.getMessage("gui-level"),
                "level", String.valueOf(level)));
        lore.add(MessageUtil.format(configManager.getMessage("gui-xp"),
                "current", String.valueOf((int) currentXP),
                "required", String.valueOf((int) requiredXP)));
        lore.add(ProgressBar.getProgressBar(currentXP, requiredXP, 20));
        lore.add("");
        lore.add(MessageUtil.format(configManager.getMessage("gui-reward"),
                "reward", String.format("%.2f", reward)));

        if (level >= 100) {
            lore.add("");
            lore.add(MessageUtil.colorize("&a&l✔ MÉTIER MAXÉ !"));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createStatsItem(PlayerJobData data) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(MessageUtil.colorize(configManager.getMessage("gui-stats-title")));

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(MessageUtil.format(configManager.getMessage("gui-stats-total-level"),
                "total", String.valueOf(data.getTotalLevel())));
        lore.add(MessageUtil.format(configManager.getMessage("gui-stats-maxed"),
                "maxed", String.valueOf(data.getMaxedJobs())));
        lore.add("");
        lore.add(MessageUtil.format(configManager.getMessage("gui-stats-prestige"),
                "level", String.valueOf(data.getPrestigeLevel()),
                "multiplier", String.format("%.1f", data.getPrestigeMultiplier())));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPrestigeItem(PlayerJobData data) {
        ItemStack item = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(MessageUtil.colorize(configManager.getMessage("gui-prestige-title")));

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(MessageUtil.colorize(configManager.getMessage("gui-prestige-line1")));
        lore.add(MessageUtil.colorize(configManager.getMessage("gui-prestige-line2")));
        lore.add(MessageUtil.colorize(configManager.getMessage("gui-prestige-line3")));
        lore.add("");
        lore.add(MessageUtil.format(configManager.getMessage("gui-prestige-level"),
                "level", String.valueOf(data.getPrestigeLevel())));
        lore.add(MessageUtil.format(configManager.getMessage("gui-prestige-multiplier"),
                "multiplier", String.format("%.1f", data.getPrestigeMultiplier())));
        lore.add("");

        if (data.getPrestigeLevel() >= 10) {
            lore.add(MessageUtil.colorize(configManager.getMessage("gui-prestige-maxed")));
        } else if (data.isAllMaxLevel()) {
            lore.add(MessageUtil.colorize(configManager.getMessage("gui-prestige-available")));
        } else {
            lore.add(MessageUtil.colorize(configManager.getMessage("gui-prestige-unavailable")));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(MessageUtil.colorize(configManager.getMessage("gui-close")));
        item.setItemMeta(meta);
        return item;
    }
}