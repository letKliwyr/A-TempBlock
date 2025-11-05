package id.kliwyr;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final TemporaryBlocks plugin;
    private final BlockManager blockManager;
    private final ConfigManager configManager;

    public CommandHandler(TemporaryBlocks plugin, BlockManager blockManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.blockManager = blockManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tempblocks.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "info" -> handleInfo(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.getLogger().info(sender.getName() + " is reloading TemporaryBlocks configuration");

            configManager.loadConfig();

            if (blockManager != null) {
                blockManager.shutdown();
                blockManager.startDecayTask();
            }

            sender.sendMessage(ChatColor.GREEN + "TemporaryBlocks configuration reloaded successfully!");
            plugin.getLogger().info("Configuration reloaded by " + sender.getName());

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== TemporaryBlocks Info ===");
        sender.sendMessage(ChatColor.YELLOW + "Active temporary blocks: " +
            ChatColor.WHITE + blockManager.getTemporaryBlockCount());
        sender.sendMessage(ChatColor.YELLOW + "Decay time: " +
            ChatColor.WHITE + configManager.getDecayTimeSeconds() + " seconds");
        sender.sendMessage(ChatColor.YELLOW + "Allowed worlds: " +
            ChatColor.WHITE + String.join(", ", configManager.getAllowedWorlds()));
        sender.sendMessage(ChatColor.YELLOW + "Check interval: " +
            ChatColor.WHITE + configManager.getCheckIntervalTicks() + " ticks");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== TemporaryBlocks Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/tempblocks reload " +
            ChatColor.WHITE + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/tempblocks info " +
            ChatColor.WHITE + "- Show plugin information");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("tempblocks.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.add("reload");
            completions.add("info");

            String input = args[0].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        }

        return completions;
    }
}
