package id.kliwyr;

import org.bukkit.plugin.java.JavaPlugin;

public final class TemporaryBlocks extends JavaPlugin {

    private ConfigManager configManager;
    private DataStorage dataStorage;
    private BlockManager blockManager;
    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        if (!checkDependencies()) {
            getLogger().severe("Missing required dependencies! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        dataStorage = new DataStorage(this);
        blockManager = new BlockManager(this, configManager, dataStorage);

        dataStorage.loadData(blockManager);

        getServer().getPluginManager().registerEvents(new BlockListener(this, blockManager, configManager), this);

        commandHandler = new CommandHandler(this, blockManager, configManager);
        var command = getCommand("tempblocks");
        if (command != null) {
            command.setExecutor(commandHandler);
            command.setTabCompleter(commandHandler);
        }

        blockManager.startDecayTask();

        getLogger().info("TemporaryBlocks enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (blockManager != null) {
            blockManager.shutdown();
        }
        if (dataStorage != null) {
            dataStorage.saveData(blockManager);
        }
        getLogger().info("TemporaryBlocks disabled successfully!");
    }

    private boolean checkDependencies() {
        var pluginManager = getServer().getPluginManager();
        boolean hasGriefPrevention = pluginManager.getPlugin("GriefPrevention") != null;
        boolean hasWorldGuard = pluginManager.getPlugin("WorldGuard") != null;

        if (!hasGriefPrevention) {
            getLogger().severe("GriefPrevention not found!");
        }
        if (!hasWorldGuard) {
            getLogger().severe("WorldGuard not found!");
        }

        return hasGriefPrevention && hasWorldGuard;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }
}

