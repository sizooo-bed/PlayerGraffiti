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
        // Check if it's a right click on a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        
        // Check if clicked block is a sign or hanging sign
        if (!isSign(clickedBlock.getType())) return;
        
        Player player = event.getPlayer();
        
        // Check permission
        if (!player.hasPermission("playergraffiti.use")) {
            player.sendMessage("§cYou don't have permission to use graffiti!");
            return;
        }
        
        PlayerManager playerManager = plugin.getPlayerManager();
        
        // Check if player has a saved message
        String message = playerManager.getPlayerMessage(player);
        if (message == null || message.isEmpty()) {
            player.sendMessage("§cYou don't have a saved message!");
            player.sendMessage("§7Use §e/graffiti set <message> §7first.");
            return;
        }
        
        // Check if player is holding a feather
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.FEATHER) {
            player.sendMessage("§cYou must hold a feather to graffiti!");
            return;
        }
        
        // Cancel event to avoid opening sign editor
        event.setCancelled(true);
        
        try {
            Sign sign = (Sign) clickedBlock.getState();
            
            // Get sign type and max characters
            boolean isHangingSign = isHangingSign(clickedBlock.getType());
            int maxCharsPerLine = isHangingSign ? 10 : 15;
            int maxTotalChars = maxCharsPerLine * 2; // 2 lignes pour le message
            
            // Vérifier si le message dépasse la limite totale
            if (message.length() > maxTotalChars) {
                player.sendMessage("§cMessage too long for this sign type!");
                player.sendMessage("§7Maximum: §e" + maxTotalChars + " §7characters for this sign.");
                return;
            }
            
            // Format message for the sign (nouveau format)
            String[] formattedLines = formatGraffitiLinesNew(message, player.getName(), maxCharsPerLine);
            
            // Save original lines and schedule restoration
            plugin.getSignManager().backupAndScheduleRestoration(sign, 300, player.getName(), message);
            
            // Get the front side of the sign
            SignSide side = sign.getSide(Side.FRONT);
            
            // Apply formatted text to the sign
            for (int i = 0; i < 4; i++) {
                side.setLine(i, formattedLines[i]);
            }
            
            // Update the sign
            sign.update();
            
            // Clear player's message
            playerManager.clearPlayerMessage(player);
            
            // Consume one feather
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            
            // Success message
            player.sendMessage("§a✓ Graffiti applied! It will disappear in §e5 minutes§a.");
            
            // Play sound effect
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            
            // Spawn particles around the sign
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
    
    // Helper method to check if a material is a sign
    private boolean isSign(Material material) {
        String name = material.name();
        return name.contains("SIGN") && !name.contains("WALL_SIGN");
    }
    
    // Helper method to check if a material is a hanging sign
    private boolean isHangingSign(Material material) {
        String name = material.name();
        return name.contains("HANGING_SIGN");
    }
    
    // Nouvelle méthode de formatage selon le format demandé
    private String[] formatGraffitiLinesNew(String message, String playerName, int maxCharsPerLine) {
        String[] lines = new String[4];
        
        // Diviser le message en deux lignes si nécessaire
        List<String> messageLines = splitMessage(message, maxCharsPerLine);
        
        // Ligne 0: première partie du message (ou vide)
        lines[0] = messageLines.size() > 0 ? messageLines.get(0) : "";
        
        // Ligne 1: deuxième partie du message (ou vide)
        lines[1] = messageLines.size() > 1 ? messageLines.get(1) : "";
        
        // Ligne 2: nom du joueur avec préfixe
        String playerLine = "§7- " + playerName;
        if (getVisibleLength(playerLine) > maxCharsPerLine) {
            playerLine = truncateToVisibleLength(playerLine, maxCharsPerLine);
        }
        lines[2] = playerLine;
        
        // Ligne 3: label Graffiti
        String graffitiLabel = "§8» §7Graffiti";
        if (getVisibleLength(graffitiLabel) > maxCharsPerLine) {
            graffitiLabel = truncateToVisibleLength(graffitiLabel, maxCharsPerLine);
        }
        lines[3] = graffitiLabel;
        
        return lines;
    }
    
    // Diviser un message en plusieurs lignes selon la limite de caractères
    private List<String> splitMessage(String message, int maxCharsPerLine) {
        List<String> lines = new ArrayList<>();
        String remaining = message.trim();
        
        while (!remaining.isEmpty() && lines.size() < 2) {
            if (remaining.length() <= maxCharsPerLine) {
                lines.add(remaining);
                break;
            }
            
            // Essayer de couper à un espace
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
    
    // Obtenir la longueur visible (sans codes couleur)
    private int getVisibleLength(String text) {
        // Enlever les codes couleur (§ + un caractère)
        String visible = text.replaceAll("§[0-9a-fk-or]", "");
        return visible.length();
    }
    
    // Tronquer une chaîne à une longueur visible maximale
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