package fr.sizooo.playergraffiti.managers;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private Map<UUID, String> playerMessages = new HashMap<>();

    public void setPlayerMessage(Player player, String message) {
        playerMessages.put(player.getUniqueId(), message);
    }

    public String getPlayerMessage(Player player) {
        return playerMessages.get(player.getUniqueId());
    }

    public void clearPlayerMessage(Player player) {
        playerMessages.remove(player.getUniqueId());
    }
}