package fr.sizooo.playergraffiti.commands;

import fr.sizooo.playergraffiti.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GraffitiCommand implements CommandExecutor, TabCompleter {

    private Main plugin;

    public GraffitiCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Show help
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
            case "s":
                return handleSet(player, args);

            case "cancel":
            case "c":
                return handleCancel(player);

            case "list":
            case "l":
                return handleList(player);

            case "clear":
            case "cl":
                return handleClear(player);

            default:
                player.sendMessage("§cUnknown subcommand. Use /graffiti for help.");
                return true;
        }
    }

    private boolean handleSet(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: §7/graffiti set <message>");
            player.sendMessage("§7Then right-click a sign with a feather!");
            return false;
        }

        if (!player.hasPermission("playergraffiti.use")) {
            player.sendMessage("§cYou don't have permission to use graffiti!");
            return true;
        }

        // Combine arguments into message
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]);
            if (i < args.length - 1)
                message.append(" ");
        }

        // Strip color codes from message (optional, prevents color abuse)
        String finalMessage = ChatColor.stripColor(message.toString());

        // Inform player about limits
        player.sendMessage("§7Note: Regular signs: §e30 §7chars max (2 lines of 15)");
        player.sendMessage("§7Hanging signs: §e20 §7chars max (2 lines of 10)");

        // Store message
        plugin.getPlayerManager().setPlayerMessage(player, finalMessage);

        player.sendMessage("§a✓ Graffiti message set: §f" + finalMessage);
        player.sendMessage("§7Now right-click a sign with a feather in hand!");

        return true;
    }

    private boolean handleCancel(Player player) {
        if (plugin.getPlayerManager().getPlayerMessage(player) == null) {
            player.sendMessage("§cYou don't have any pending graffiti message!");
            return true;
        }

        plugin.getPlayerManager().clearPlayerMessage(player);
        player.sendMessage("§aYour graffiti message has been cancelled.");

        return true;
    }

    private boolean handleList(Player player) {
        Map<Location, String> activeGraffiti = plugin.getSignManager().getActiveGraffiti();

        if (activeGraffiti.isEmpty()) {
            player.sendMessage("§7No active graffiti found.");
            return true;
        }

        player.sendMessage("§6=== Active Graffiti (" + activeGraffiti.size() + ") ===");
        int index = 1;
        for (Map.Entry<Location, String> entry : activeGraffiti.entrySet()) {
            Location loc = entry.getKey();
            player.sendMessage(String.format("§e%d. §7At §f[%d, %d, %d] §7- Message: §f%s",
                    index++,
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ(),
                    entry.getValue()));
        }

        return true;
    }

    private boolean handleClear(Player player) {
        if (!player.hasPermission("playergraffiti.admin")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        int count = plugin.getSignManager().getActiveGraffiti().size();
        plugin.getSignManager().clearAllGraffiti();

        player.sendMessage("§aCleared §e" + count + " §aactive graffiti.");

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("§6===== Graffiti Commands =====");
        player.sendMessage("§e/g set <message> §7- Set your graffiti message");
        player.sendMessage("§e/g cancel §7- Cancel your message");
        player.sendMessage("§e/g list §7- List active graffiti");

        if (player.hasPermission("playergraffiti.admin")) {
            player.sendMessage("§e/g clear §7- Clear all graffiti (admin)");
        }

        player.sendMessage("§7Aliases: §f/graffiti, /g, /tag");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Subcommands
            List<String> subCommands = Arrays.asList("set", "cancel", "list");

            // Add admin commands if has permission
            if (sender.hasPermission("playergraffiti.admin")) {
                subCommands = Arrays.asList("set", "cancel", "list", "clear");
            }

            StringUtil.copyPartialMatches(args[0], subCommands, completions);
        }

        return completions;
    }
}