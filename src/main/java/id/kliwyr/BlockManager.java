package id.kliwyr;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockManager {

    private final TemporaryBlocks plugin;
    private final ConfigManager configManager;
    private final DataStorage dataStorage;
    private final Map<Location, Long> temporaryBlocks;
    private BukkitTask decayTask;

    public BlockManager(TemporaryBlocks plugin, ConfigManager configManager, DataStorage dataStorage) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dataStorage = dataStorage;
        this.temporaryBlocks = new ConcurrentHashMap<>();
    }

    public void addTemporaryBlock(Location location) {
        long expiryTime = System.currentTimeMillis() + configManager.getDecayTimeMillis();
        temporaryBlocks.put(location, expiryTime);
    }

    public void removeTemporaryBlock(Location location) {
        temporaryBlocks.remove(location);
    }

    public boolean isTemporaryBlock(Location location) {
        return temporaryBlocks.containsKey(location);
    }

    public int getTemporaryBlockCount() {
        return temporaryBlocks.size();
    }

    public Map<Location, Long> getTemporaryBlocks() {
        return temporaryBlocks;
    }

    public void startDecayTask() {
        int interval = configManager.getCheckIntervalTicks();

        decayTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkAndDecayBlocks();
            }
        }.runTaskTimer(plugin, interval, interval);

        plugin.getLogger().info("Decay task started with interval: " + interval + " ticks");
    }

    private void checkAndDecayBlocks() {
        long currentTime = System.currentTimeMillis();
        List<Location> toRemove = new ArrayList<>();

        for (Map.Entry<Location, Long> entry : temporaryBlocks.entrySet()) {
            if (currentTime >= entry.getValue()) {
                toRemove.add(entry.getKey());
            }
        }

        if (!toRemove.isEmpty()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Location loc : toRemove) {
                        decayBlock(loc);
                    }
                }
            }.runTask(plugin);
        }
    }

    public void decayBlock(Location location) {
        if (location.getWorld() == null) {
            temporaryBlocks.remove(location);
            return;
        }

        var block = location.getBlock();

        if (block.getType() != Material.AIR) {
            block.setType(Material.AIR);

            location.getWorld().spawnParticle(
                Particle.SMOKE,
                location.clone().add(0.5, 0.5, 0.5),
                20,
                0.3, 0.3, 0.3,
                0.02
            );
        }

        temporaryBlocks.remove(location);
    }

    public void shutdown() {
        if (decayTask != null && !decayTask.isCancelled()) {
            decayTask.cancel();
            plugin.getLogger().info("Decay task stopped");
        }

        dataStorage.saveDataSync(this);
        temporaryBlocks.clear();
    }

    public void clearExpiredBlocks() {
        long currentTime = System.currentTimeMillis();
        temporaryBlocks.entrySet().removeIf(entry -> currentTime >= entry.getValue());
    }
}
