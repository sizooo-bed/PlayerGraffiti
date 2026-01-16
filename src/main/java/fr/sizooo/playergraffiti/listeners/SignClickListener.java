package fr.sizooo.playergraffiti.listeners;

import fr.sizooo.playergraffiti.Main;
import fr.sizooo.playergraffiti.managers.PlayerManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SignClickListener implements Listener {
    
    private Main plugin;
    
    public SignClickListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();

        if (!isSign(clickedBlock.getType())) return;
        
        Player player = event.getPlayer();

        if (!player.hasPermission("playergraffiti.use")) {
            player.sendMessage("§cYou don't have permission to use graffiti!");
            return;
        }
        
        PlayerManager playerManager = plugin.getPlayerManager();

        String message = playerManager.getPlayerMessage(player);
        if (message == null || message.isEmpty()) {
            player.sendMessage("§cYou don't have a saved message!");
            player.sendMessage("§7Use §e/graffiti set <message> §7first.");
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.FEATHER) {
            player.sendMessage("§cYou must hold a feather to graffiti!");
            return;
        }

        event.setCancelled(true);
        
        try {
            Sign sign = (Sign) clickedBlock.getState();

            boolean isHangingSign = isHangingSign(clickedBlock.getType());
            int maxCharsPerLine = isHangingSign ? 10 : 15;
            int maxTotalChars = maxCharsPerLine * 2;

            if (message.length() > maxTotalChars) {
                player.sendMessage("§cMessage too long for this sign type!");
                player.sendMessage("§7Maximum: §e" + maxTotalChars + " §7characters for this sign.");
                return;
            }

            String[] formattedLines = formatGraffitiLinesNew(message, player.getName(), maxCharsPerLine);

            plugin.getSignManager().backupAndScheduleRestoration(sign, 300, player.getName(), message);

            SignSide side = sign.getSide(Side.FRONT);

            for (int i = 0; i < 4; i++) {
                side.setLine(i, formattedLines[i]);
            }

            sign.update();

            playerManager.clearPlayerMessage(player);

            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            player.sendMessage("§a✓ Graffiti applied! It will disappear in §e5 minutes§a.");

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

            Location signLoc = clickedBlock.getLocation();
            World world = signLoc.getWorld();
            for (int i = 0; i < 10; i++) {
                world.spawnParticle(Particle.HEART, 
                    signLoc.getX() + 0.5, 
                    signLoc.getY() + 1, 
                    signLoc.getZ() + 0.5, 
                    3, 0.3, 0.3, 0.3, 0);
            }
            
        } catch (ClassCastException e) {
            player.sendMessage("§cError: This is not a valid sign!");
        }
    }

    private boolean isSign(Material material) {
        String name = material.name();
        return name.contains("SIGN") && !name.contains("WALL_SIGN");
    }

    private boolean isHangingSign(Material material) {
        String name = material.name();
        return name.contains("HANGING_SIGN");
    }

    private String[] formatGraffitiLinesNew(String message, String playerName, int maxCharsPerLine) {
        String[] lines = new String[4];

        List<String> messageLines = splitMessage(message, maxCharsPerLine);

        lines[0] = messageLines.size() > 0 ? messageLines.get(0) : "";

        lines[1] = messageLines.size() > 1 ? messageLines.get(1) : "";

        String playerLine = "§7- " + playerName;
        if (getVisibleLength(playerLine) > maxCharsPerLine) {
            playerLine = truncateToVisibleLength(playerLine, maxCharsPerLine);
        }
        lines[2] = playerLine;

        String graffitiLabel = "§8» §7Graffiti";
        if (getVisibleLength(graffitiLabel) > maxCharsPerLine) {
            graffitiLabel = truncateToVisibleLength(graffitiLabel, maxCharsPerLine);
        }
        lines[3] = graffitiLabel;
        
        return lines;
    }

    private List<String> splitMessage(String message, int maxCharsPerLine) {
        List<String> lines = new ArrayList<>();
        String remaining = message.trim();
        
        while (!remaining.isEmpty() && lines.size() < 2) {
            if (remaining.length() <= maxCharsPerLine) {
                lines.add(remaining);
                break;
            }

            int cutIndex = maxCharsPerLine;
            if (remaining.charAt(cutIndex) != ' ') {
                int lastSpace = remaining.lastIndexOf(' ', cutIndex);
                if (lastSpace > 0) {
                    cutIndex = lastSpace;
                }
            }
            
            lines.add(remaining.substring(0, cutIndex).trim());
            remaining = remaining.substring(cutIndex).trim();
        }
        
        return lines;
    }

    private int getVisibleLength(String text) {
        String visible = text.replaceAll("§[0-9a-fk-or]", "");
        return visible.length();
    }

    private String truncateToVisibleLength(String text, int maxVisibleLength) {
        if (getVisibleLength(text) <= maxVisibleLength) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        int visibleCount = 0;
        boolean inColorCode = false;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (inColorCode) {
                result.append(c);
                inColorCode = false;
            } else if (c == '§') {
                result.append(c);
                inColorCode = true;
            } else {
                if (visibleCount < maxVisibleLength) {
                    result.append(c);
                    visibleCount++;
                } else {
                    break;
                }
            }
        }
        
        return result.toString();
    }
}