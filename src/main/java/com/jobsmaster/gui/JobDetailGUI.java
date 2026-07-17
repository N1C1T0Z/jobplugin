package com.jobsmaster.gui;

import com.cryptomorin.xseries.XMaterial;
import com.jobsmaster.JobsMaster;
import com.jobsmaster.data.JobLevel;
import com.jobsmaster.JobType;
import com.jobsmaster.data.PlayerJobData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class JobDetailGUI {

    private final JobsMaster plugin;
    private final Player player;
    private final JobType jobType;
    private final Inventory inventory;

    public JobDetailGUI(JobsMaster plugin, Player player, JobType jobType) {
        this.plugin = plugin;
        this.player = player;
        this.jobType = jobType;
        this.inventory = Bukkit.createInventory(null, 36, "§6§l" + plugin.getConfigManager().getDisplayName(jobType));
    }

    public void open() {
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player);
        PlayerJobData.JobProgress progress = data.getJob(jobType);
        int maxLevel = plugin.getConfigManager().getMaxLevel(jobType);

        // Background
        ItemStack bg = XMaterial.matchXMaterial("BLACK_STAINED_GLASS_PANE").map(XMaterial::parseItem).orElse(null);
        if (bg != null) {
            ItemMeta bgMeta = bg.getItemMeta();
            bgMeta.setDisplayName(" ");
            bg.setItemMeta(bgMeta);
            for (int i = 0; i < 36; i++) {
                inventory.setItem(i, bg);
            }
        }

        // Job icon at slot 4
        ItemStack icon = XMaterial.matchXMaterial(plugin.getConfigManager().getMaterial(jobType))
                .map(XMaterial::parseItem).orElse(null);
        if (icon != null) {
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getDisplayName(jobType)));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getDescription(jobType)));
            lore.add("");
            lore.add("§7Niveau: §e" + progress.getLevel() + "§7/§e" + maxLevel);
            lore.add("§7XP: §e" + String.format("%.0f", progress.getXp()));
            lore.add("§7Renaissance: §d" + data.getPrestige());

            double mult = plugin.getBoostManager().getTotalMultiplier(player, jobType);
            if (mult > 1.0) {
                lore.add("§7Multiplicateur: §dx" + String.format("%.2f", mult));
            }

            meta.setLore(lore);
            icon.setItemMeta(meta);
            inventory.setItem(4, icon);
        }

        // Progress bar (row 1, slots 9-17)
        if (progress.getLevel() < maxLevel) {
            double xp = progress.getXp();
            double xpNeeded = 0;
            JobLevel currentLevel = plugin.getConfigManager().getLevel(jobType, progress.getLevel());
            if (currentLevel != null) {
                xpNeeded = currentLevel.getXpRequired();
            }

            if (xpNeeded > 0) {
                double pct = Math.min(1.0, xp / xpNeeded);
                int filled = (int) (pct * 9);
                String greenMat = "LIME_STAINED_GLASS_PANE";
                String grayMat = "GRAY_STAINED_GLASS_PANE";

                for (int i = 0; i < 9; i++) {
                    String mat = i < filled ? greenMat : grayMat;
                    ItemStack barItem = XMaterial.matchXMaterial(mat).map(XMaterial::parseItem).orElse(null);
                    if (barItem != null) {
                        ItemMeta barMeta = barItem.getItemMeta();
                        barMeta.setDisplayName("§a" + String.format("%.1f", pct * 100) + "%");
                        barItem.setItemMeta(barMeta);
                        inventory.setItem(9 + i, barItem);
                    }
                }
            }
        } else {
            ItemStack maxItem = XMaterial.matchXMaterial("GOLD_BLOCK").map(XMaterial::parseItem).orElse(null);
            if (maxItem != null) {
                ItemMeta maxMeta = maxItem.getItemMeta();
                maxMeta.setDisplayName("§6§lNIVEAU MAX ATTEINT !");
                maxItem.setItemMeta(maxMeta);
                inventory.setItem(13, maxItem);
            }
        }

        // Current level rewards (slot 20)
        if (progress.getLevel() <= maxLevel) {
            JobLevel currentLevel = plugin.getConfigManager().getLevel(jobType, progress.getLevel());
            if (currentLevel != null) {
                ItemStack rewardIcon = XMaterial.matchXMaterial("GOLD_INGOT").map(XMaterial::parseItem).orElse(null);
                if (rewardIcon != null) {
                    ItemMeta rewardMeta = rewardIcon.getItemMeta();
                    rewardMeta.setDisplayName("§e§lNiveau Actuel");

                    List<String> rewardLore = new ArrayList<>();
                    rewardLore.add("§7Niveau §e" + currentLevel.getLevel());
                    rewardLore.add("§7XP nécessaire: §e" + String.format("%.0f", currentLevel.getXpRequired()));
                    rewardLore.add("§7Récompense: §e$" + String.format("%.2f", currentLevel.getReward()));

                    if (progress.getLevel() >= maxLevel) {
                        rewardLore.add("");
                        rewardLore.add("§a§lNIVEAU MAX ATTEINT !");
                    }

                    rewardMeta.setLore(rewardLore);
                    rewardIcon.setItemMeta(rewardMeta);
                    inventory.setItem(20, rewardIcon);
                }
            }
        }

        // Next level rewards (slot 22)
        if (progress.getLevel() < maxLevel) {
            int nextLevel = progress.getLevel() + 1;
            JobLevel nextLevelDef = plugin.getConfigManager().getLevel(jobType, nextLevel);
            if (nextLevelDef != null) {
                ItemStack nextIcon = XMaterial.matchXMaterial("EXPERIENCE_BOTTLE").map(XMaterial::parseItem).orElse(null);
                if (nextIcon != null) {
                    ItemMeta nextMeta = nextIcon.getItemMeta();
                    nextMeta.setDisplayName("§b§lProchain Niveau");

                    List<String> nextLore = new ArrayList<>();
                    nextLore.add("§7Niveau §b" + nextLevelDef.getLevel());
                    nextLore.add("§7XP nécessaire: §b" + String.format("%.0f", nextLevelDef.getXpRequired()));
                    nextLore.add("§7Récompense: §b$" + String.format("%.2f", nextLevelDef.getReward()));
                    nextMeta.setLore(nextLore);
                    nextIcon.setItemMeta(nextMeta);
                    inventory.setItem(22, nextIcon);
                }
            }
        }

        // Boost info (slot 24)
        double boost = plugin.getBoostManager().getBoost(player, jobType);
        ItemStack boostIcon = XMaterial.matchXMaterial("BLAZE_POWDER").map(XMaterial::parseItem).orElse(null);
        if (boostIcon != null) {
            ItemMeta boostMeta = boostIcon.getItemMeta();
            boostMeta.setDisplayName("§c§lBoost Actif");

            List<String> boostLore = new ArrayList<>();
            boostLore.add("§7Multiplicateur: §cx" + String.format("%.2f", 1.0 + boost));
            boostLore.add("§7(Renaissance + " + String.format("%.0f", (data.getRewardMultiplier() - 1.0) * 100) + "%)");
            if (boost <= 0) {
                boostLore.add("§7Aucun boost temporaire actif");
            }
            boostMeta.setLore(boostLore);
            boostIcon.setItemMeta(boostMeta);
            inventory.setItem(24, boostIcon);
        }

        // Back button (slot 31)
        ItemStack backIcon = XMaterial.matchXMaterial("BARRIER").map(XMaterial::parseItem).orElse(null);
        if (backIcon != null) {
            ItemMeta backMeta = backIcon.getItemMeta();
            backMeta.setDisplayName("§c§lRetour");
            backMeta.setLore(List.of("§7Retour au menu principal"));
            backIcon.setItemMeta(backMeta);
            inventory.setItem(31, backIcon);
        }

        player.openInventory(inventory);
        plugin.getServer().getPluginManager().registerEvents(new DetailGUIListener(plugin, player, inventory, jobType), plugin);
    }

    private static class DetailGUIListener implements org.bukkit.event.Listener {
        private final JobsMaster plugin;
        private final Player player;
        private final Inventory inv;
        private final JobType jobType;

        DetailGUIListener(JobsMaster plugin, Player player, Inventory inv, JobType jobType) {
            this.plugin = plugin;
            this.player = player;
            this.inv = inv;
            this.jobType = jobType;
        }

        @org.bukkit.event.EventHandler
        public void onClick(org.bukkit.event.inventory.InventoryClickEvent event) {
            if (!event.getInventory().equals(inv)) return;
            if (event.getWhoClicked() != player) return;
            event.setCancelled(true);

            if (event.getSlot() == 31) {
                new JobMainGUI(plugin, player).open();
            }
        }
    }
}