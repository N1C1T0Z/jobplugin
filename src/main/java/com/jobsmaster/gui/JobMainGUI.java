package com.jobsmaster.gui;

import com.cryptomorin.xseries.XMaterial;
import com.jobsmaster.JobsMaster;
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

public class JobMainGUI {

    private final JobsMaster plugin;
    private final Player player;
    private final Inventory inventory;

    public JobMainGUI(JobsMaster plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, "§6§lJobsMaster - Métiers");
    }

    public void open() {
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Fill background
        ItemStack bg = XMaterial.matchXMaterial("BLACK_STAINED_GLASS_PANE").map(XMaterial::parseItem).orElse(null);
        if (bg != null) {
            ItemMeta bgMeta = bg.getItemMeta();
            bgMeta.setDisplayName(" ");
            bg.setItemMeta(bgMeta);
            for (int i = 0; i < 27; i++) {
                inventory.setItem(i, bg);
            }
        }

        // Job icons in slots 10-14
        int slot = 10;
        for (JobType type : JobType.values()) {
            PlayerJobData.JobProgress progress = data.getJob(type);
            int maxLevel = plugin.getConfigManager().getMaxLevel(type);

            ItemStack icon = XMaterial.matchXMaterial(plugin.getConfigManager().getMaterial(type))
                    .map(XMaterial::parseItem).orElse(null);
            if (icon == null) continue;

            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getDisplayName(type)));

            List<String> lore = new ArrayList<>();
            lore.add("§7Niveau: §e" + progress.getLevel() + "§7/§e" + maxLevel);
            lore.add("§7XP: §e" + String.format("%.0f", progress.getXp()));

            double xpNeeded = 0;
            var levelDef = plugin.getConfigManager().getLevel(type, progress.getLevel());
            if (levelDef != null) {
                xpNeeded = levelDef.getXpRequired();
                double progressPct = (progress.getXp() / xpNeeded) * 100;
                lore.add("§7Progression: §e" + String.format("%.1f", progressPct) + "%");

                // Progress bar
                int bars = (int) ((progress.getXp() / xpNeeded) * 10);
                StringBuilder bar = new StringBuilder("§7[");
                for (int i = 0; i < 10; i++) {
                    bar.append(i < bars ? "§a■" : "§7■");
                }
                bar.append("§7]");
                lore.add(bar.toString());
            } else {
                lore.add("§a§lNIVEAU MAX !");
            }

            lore.add("");
            lore.add("§eCliquez pour voir les détails");

            if (progress.getLevel() >= maxLevel) {
                lore.add("§a§l✓ COMPLÉTÉ");
            }

            meta.setLore(lore);
            icon.setItemMeta(meta);
            inventory.setItem(slot, icon);
            slot++;
        }

        // Prestige info at slot 16
        ItemStack prestigeIcon = XMaterial.matchXMaterial("NETHER_STAR").map(XMaterial::parseItem).orElse(null);
        if (prestigeIcon != null) {
            ItemMeta prestigeMeta = prestigeIcon.getItemMeta();
            prestigeMeta.setDisplayName("§d§lRENAISSANCE");

            List<String> prestigeLore = new ArrayList<>();
            prestigeLore.add("§7Niveau de renaissance: §d" + data.getPrestige());
            prestigeLore.add("§7Multiplicateur: §dx" + String.format("%.2f", data.getRewardMultiplier()));
            prestigeLore.add("");
            prestigeLore.add("§7Réinitialise tous les métiers");
            prestigeLore.add("§7pour augmenter vos récompenses !");
            prestigeLore.add("");

            if (data.isAllMaxLevel(plugin.getConfigManager().getPrestigeMaxLevel())) {
                prestigeLore.add("§a§lCliquez pour renaître !");
            } else {
                prestigeLore.add("§cMettez tous les métiers au niveau max");
                prestigeLore.add("§cpour débloquer la renaissance");
            }

            prestigeMeta.setLore(prestigeLore);
            prestigeIcon.setItemMeta(prestigeMeta);
            inventory.setItem(16, prestigeIcon);
        }

        // Stats at slot 22
        ItemStack statsIcon = XMaterial.matchXMaterial("BOOK").map(XMaterial::parseItem).orElse(null);
        if (statsIcon != null) {
            ItemMeta statsMeta = statsIcon.getItemMeta();
            statsMeta.setDisplayName("§6§lStatistiques");

            List<String> statsLore = new ArrayList<>();
            int totalLevels = 0;
            int totalMax = 0;
            for (JobType type : JobType.values()) {
                totalLevels += data.getJob(type).getLevel();
                totalMax += plugin.getConfigManager().getMaxLevel(type);
            }
            statsLore.add("§7Niveaux totaux: §e" + totalLevels + "§7/§e" + totalMax);
            statsLore.add("§7Renaissance: §d" + data.getPrestige());
            statsLore.add("§7Multiplicateur: §dx" + String.format("%.2f", data.getRewardMultiplier()));

            statsMeta.setLore(statsLore);
            statsIcon.setItemMeta(statsMeta);
            inventory.setItem(22, statsIcon);
        }

        player.openInventory(inventory);
        plugin.getServer().getPluginManager().registerEvents(
                new GUIListener(plugin, player, inventory), plugin);
    }

    private static class GUIListener implements org.bukkit.event.Listener {
        private final JobsMaster plugin;
        private final Player player;
        private final Inventory inv;

        GUIListener(JobsMaster plugin, Player player, Inventory inv) {
            this.plugin = plugin;
            this.player = player;
            this.inv = inv;
        }

        @org.bukkit.event.EventHandler
        public void onClick(org.bukkit.event.inventory.InventoryClickEvent event) {
            if (!event.getInventory().equals(inv)) return;
            if (event.getWhoClicked() != player) return;
            event.setCancelled(true);

            int slot = event.getSlot();
            if (slot >= 10 && slot <= 14) {
                int jobIndex = slot - 10;
                JobType[] types = JobType.values();
                if (jobIndex < types.length) {
                    new JobDetailGUI(plugin, player, types[jobIndex]).open();
                }
            }

            // Prestige slot 16
            if (slot == 16) {
                event.getWhoClicked().closeInventory();
                plugin.getJobManager().prestige((Player) event.getWhoClicked());
            }
        }
    }
}