package id.kliwyr;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private final TemporaryBlocks plugin;
    private Set<String> allowedWorlds;
    private long decayTimeSeconds;
    private int checkIntervalTicks;

    public ConfigManager(TemporaryBlocks plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();

        List<String> worldList = config.getStringList("allowed-worlds");
        allowedWorlds = new HashSet<>(worldList);

        decayTimeSeconds = config.getLong("decay-time-seconds", 60);
        checkIntervalTicks = config.getInt("check-interval-ticks", 20);

        plugin.getLogger().info("Configuration loaded:");
        plugin.getLogger().info("- Allowed worlds: " + allowedWorlds.size());
        plugin.getLogger().info("- Decay time: " + decayTimeSeconds + " seconds");
        plugin.getLogger().info("- Check interval: " + checkIntervalTicks + " ticks");
    }

    public boolean isWorldAllowed(String worldName) {
        return allowedWorlds.contains(worldName);
    }

    public long getDecayTimeSeconds() {
        return decayTimeSeconds;
    }

    public long getDecayTimeMillis() {
        return decayTimeSeconds * 1000;
    }

    public int getCheckIntervalTicks() {
        return checkIntervalTicks;
    }

    public Set<String> getAllowedWorlds() {
        return new HashSet<>(allowedWorlds);
    }
}
