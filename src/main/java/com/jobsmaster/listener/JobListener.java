package com.jobsmaster.listener;

import com.jobsmaster.JobType;
import com.jobsmaster.manager.ConfigManager;
import com.jobsmaster.manager.JobManager;
import com.jobsmaster.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JobListener implements Listener {

    private final ConfigManager configManager;
    private final JobManager jobManager;

    private static final Set<String> CROP_TYPES = new HashSet<>(Arrays.asList(
            "WHEAT", "CARROTS", "POTATOES", "BEETROOTS",
            "MELON", "PUMPKIN", "SUGAR_CANE", "CACTUS",
            "NETHER_WART", "BAMBOO", "COCOA",
            "SWEET_BERRY_BUSH", "KELP", "SEA_PICKLE",
            "MOSS_BLOCK", "MOSS_CARPET",
            "BROWN_MUSHROOM", "RED_MUSHROOM",
            "GLOW_BERRIES", "PITCHER_PLANT", "TORCHFLOWER"
    ));

    private static final Set<String> LOG_TYPES = new HashSet<>(Arrays.asList(
            "OAK_LOG", "SPRUCE_LOG", "BIRCH_LOG", "JUNGLE_LOG",
            "ACACIA_LOG", "DARK_OAK_LOG", "MANGROVE_LOG", "CHERRY_LOG",
            "STRIPPED_OAK_LOG", "STRIPPED_SPRUCE_LOG", "STRIPPED_BIRCH_LOG",
            "STRIPPED_JUNGLE_LOG", "STRIPPED_ACACIA_LOG", "STRIPPED_DARK_OAK_LOG",
            "STRIPPED_MANGROVE_LOG", "STRIPPED_CHERRY_LOG",
            "OAK_WOOD", "SPRUCE_WOOD", "BIRCH_WOOD", "JUNGLE_WOOD",
            "ACACIA_WOOD", "DARK_OAK_WOOD", "MANGROVE_WOOD", "CHERRY_WOOD"
    ));

    public JobListener(ConfigManager configManager, JobManager jobManager) {
        this.configManager = configManager;
        this.jobManager = jobManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;
        if (event.getEntity() instanceof Player) return;

        Player player = event.getEntity().getKiller();
        EntityType entityType = event.getEntityType();

        double xp = configManager.getXP(JobType.HUNTER, entityType.name());
        if (xp > 0) {
            jobManager.addXP(player, JobType.HUNTER, xp);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        String typeName = event.getBlock().getType().name();

        // Woodcutter
        if (LOG_TYPES.contains(typeName)) {
            double xp = configManager.getXP(JobType.WOODCUTTER, event.getBlock().getType());
            if (xp > 0) {
                jobManager.addXP(player, JobType.WOODCUTTER, xp);
            }
        }

        // Farmer
        if (CROP_TYPES.contains(typeName)) {
            double xp = configManager.getXP(JobType.FARMER, event.getBlock().getType());
            if (xp > 0) {
                jobManager.addXP(player, JobType.FARMER, xp);
            }
        }

        // Miner
        double minerXP = configManager.getXP(JobType.MINER, event.getBlock().getType());
        if (minerXP > 0) {
            jobManager.addXP(player, JobType.MINER, minerXP);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        String typeName = event.getBlock().getType().name();

        // Check blacklist
        if (typeName.equals("TNT") || typeName.contains("BEDROCK") || typeName.contains("BARRIER") ||
            typeName.contains("COMMAND_BLOCK") || typeName.contains("STRUCTURE_BLOCK") ||
            typeName.contains("JIGSAW") || typeName.contains("END_PORTAL") ||
            typeName.contains("NETHER_PORTAL") || typeName.equals("LAVA") ||
            typeName.equals("WATER") || typeName.contains("AIR")) {
            return;
        }

        double xp = configManager.getBuilderXP();
        jobManager.addXP(player, JobType.BUILDER, xp);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getView().getTitle();
        String guiTitle = MessageUtil.colorize(configManager.getMessage("gui-title"));

        if (!title.equals(guiTitle)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Job slots (0-4)
        if (slot >= 0 && slot < JobType.values().length) {
            // Close the GUI, the job info is shown in the item lore
            player.closeInventory();
            return;
        }

        // Prestige slot (14)
        if (slot == 14) {
            player.closeInventory();
            jobManager.prestige(player);
            return;
        }

        // Close slot (22)
        if (slot == 22) {
            player.closeInventory();
            return;
        }
    }
}