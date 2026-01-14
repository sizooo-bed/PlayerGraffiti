package fr.sizooo.playergraffiti.managers;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    
    // Stocke temporairement les messages des joueurs
    private Map<UUID, String> playerMessages = new HashMap<>();
    
    // Ajoute un message pour un joueur
    public void setPlayerMessage(Player player, String message) {
        playerMessages.put(player.getUniqueId(), message);
    }
    
    // Récupère le message d'un joueur
    public String getPlayerMessage(Player player) {
        return playerMessages.get(player.getUniqueId());
    }
    
    // Supprime le message d'un joueur (après usage)
    public void clearPlayerMessage(Player player) {
        playerMessages.remove(player.getUniqueId());
    }
}