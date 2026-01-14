package fr.sizooo.playergraffiti.managers;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.scheduler.BukkitRunnable;
import fr.sizooo.playergraffiti.Main;
import java.util.HashMap;
import java.util.Map;

public class SignManager {

    private Main plugin;
    private Map<Location, String> activeGraffiti = new HashMap<>(); // Location -> Message
    private DataManager dataManager;

    public SignManager(Main plugin) {
        this.plugin = plugin;
        this.dataManager = new DataManager(plugin);
        loadActiveGraffiti();
    }

    private void loadActiveGraffiti() {
        activeGraffiti = dataManager.loadActiveGraffiti();
        plugin.getLogger().info("Loaded " + activeGraffiti.size() + " active graffiti from file.");
    }

    // Dans SignManager.java, modifie la méthode backupAndScheduleRestoration :

    public void backupAndScheduleRestoration(Sign sign, int delaySeconds, String playerName, String message) {
        Location loc = sign.getLocation();

        // Get the front side of the sign
        SignSide side = sign.getSide(org.bukkit.block.sign.Side.FRONT);

        // Save original lines BEFORE modification
        String[] originalLines = new String[4];
        for (int i = 0; i < 4; i++) {
            originalLines[i] = side.getLine(i);
        }

        // Store in maps
        activeGraffiti.put(loc, message);
        signOriginals.put(loc, originalLines);

        // Save to file
        dataManager.saveActiveGraffiti(activeGraffiti);

        // Schedule restoration
        new BukkitRunnable() {
            @Override
            public void run() {
                restoreSign(loc);
            }
        }.runTaskLater(plugin, delaySeconds * 20L);
    }

    public Map<Location, String> getActiveGraffiti() {
        return new HashMap<>(activeGraffiti);
    }

    public void clearAllGraffiti() {
        // Pour chaque graffiti actif, on restaure le panneau
        for (Map.Entry<Location, String> entry : activeGraffiti.entrySet()) {
            Location loc = entry.getKey();
            if (loc.getBlock().getState() instanceof Sign) {
                Sign sign = (Sign) loc.getBlock().getState();
                // On restaure avec des lignes vides (ou tu pourrais sauvegarder l'original)
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, "");
                }
                sign.update();
            }
        }

        // Puis on vide la liste
        activeGraffiti.clear();
        dataManager.saveActiveGraffiti(activeGraffiti);
    }

    private void restoreSign(Location loc) {
        if (signOriginals.containsKey(loc)) {
            if (loc.getBlock().getState() instanceof Sign) {
                Sign sign = (Sign) loc.getBlock().getState();
                String[] originalLines = signOriginals.get(loc);
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, originalLines[i]);
                }
                sign.update();
            }
            signOriginals.remove(loc);
            activeGraffiti.remove(loc);
            dataManager.saveActiveGraffiti(activeGraffiti);
        }
    }

    private Map<Location, String[]> signOriginals = new HashMap<>();
}