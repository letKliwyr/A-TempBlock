package id.kliwyr;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

    private final TemporaryBlocks plugin;
    private final BlockManager blockManager;
    private final ConfigManager configManager;

    public BlockListener(TemporaryBlocks plugin, BlockManager blockManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.blockManager = blockManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (player == null) {
            Location location = event.getBlock().getLocation();
            if (location.getWorld() != null && configManager.isWorldAllowed(location.getWorld().getName())) {
                blockManager.addTemporaryBlock(location);
            }
            return;
        }

        Location location = event.getBlock().getLocation();
        String worldName = location.getWorld().getName();

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);

        if (claim != null) {
            return;
        }

        if (player.isOp()) {
            return;
        }

        if (!configManager.isWorldAllowed(worldName)) {
            event.setCancelled(true);
            return;
        }

        blockManager.addTemporaryBlock(location);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();

        if (blockManager.isTemporaryBlock(location)) {
            blockManager.removeTemporaryBlock(location);
        }
    }
}
