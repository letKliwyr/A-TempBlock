package id.kliwyr;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataStorage {

    private final TemporaryBlocks plugin;
    private final File dataFile;

    public DataStorage(TemporaryBlocks plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public void loadData(BlockManager blockManager) {
        if (!dataFile.exists()) {
            plugin.getLogger().info("No existing data file found, starting fresh");
            return;
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        List<String> blockList = data.getStringList("temporary-blocks");

        if (blockList.isEmpty()) {
            plugin.getLogger().info("No temporary blocks to load");
            return;
        }

        int loaded = 0;
        int expired = 0;
        long currentTime = System.currentTimeMillis();
        List<Location> expiredBlocks = new ArrayList<>();

        for (String entry : blockList) {
            try {
                String[] parts = entry.split(";");
                if (parts.length != 5) continue;

                String worldName = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                long expiryTime = Long.parseLong(parts[4]);

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("World not found: " + worldName + ", skipping block");
                    continue;
                }

                Location location = new Location(world, x, y, z);

                if (expiryTime <= currentTime) {
                    expiredBlocks.add(location);
                    expired++;
                    continue;
                }

                blockManager.getTemporaryBlocks().put(location, expiryTime);
                loaded++;

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load block data: " + entry);
            }
        }

        if (!expiredBlocks.isEmpty()) {
            for (Location loc : expiredBlocks) {
                blockManager.decayBlock(loc);
            }
            plugin.getLogger().info("Removed " + expired + " expired blocks on startup");
        }

        if (loaded > 0) {
            plugin.getLogger().info("Loaded " + loaded + " active temporary blocks from storage");
        }

        if (expired > 0) {
            saveDataSync(blockManager);
            plugin.getLogger().info("Data file cleaned and saved");
        }
    }

    public void saveData(BlockManager blockManager) {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveDataInternal(blockManager);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void saveDataSync(BlockManager blockManager) {
        saveDataInternal(blockManager);
    }

    private void saveDataInternal(BlockManager blockManager) {
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }

            FileConfiguration data = new YamlConfiguration();
            List<String> blockList = new ArrayList<>();

            long currentTime = System.currentTimeMillis();
            int saved = 0;

            for (Map.Entry<Location, Long> entry : blockManager.getTemporaryBlocks().entrySet()) {
                Location loc = entry.getKey();
                long expiryTime = entry.getValue();

                if (expiryTime <= currentTime) {
                    continue;
                }

                String serialized = String.format("%s;%.0f;%.0f;%.0f;%d",
                    loc.getWorld().getName(),
                    loc.getX(),
                    loc.getY(),
                    loc.getZ(),
                    expiryTime
                );
                blockList.add(serialized);
                saved++;
            }

            data.set("temporary-blocks", blockList);
            data.save(dataFile);

            if (saved > 0) {
                plugin.getLogger().info("Saved " + saved + " temporary blocks to storage");
            }

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
