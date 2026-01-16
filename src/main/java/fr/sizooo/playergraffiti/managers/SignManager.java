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
    private Map<Location, String> activeGraffiti = new HashMap<>();
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

    public void backupAndScheduleRestoration(Sign sign, int delaySeconds, String playerName, String message) {
        Location loc = sign.getLocation();

        SignSide side = sign.getSide(org.bukkit.block.sign.Side.FRONT);

        String[] originalLines = new String[4];
        for (int i = 0; i < 4; i++) {
            originalLines[i] = side.getLine(i);
        }

        activeGraffiti.put(loc, message);
        signOriginals.put(loc, originalLines);

        dataManager.saveActiveGraffiti(activeGraffiti);

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
        for (Map.Entry<Location, String> entry : activeGraffiti.entrySet()) {
            Location loc = entry.getKey();
            if (loc.getBlock().getState() instanceof Sign) {
                Sign sign = (Sign) loc.getBlock().getState();
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, "");
                }
                sign.update();
            }
        }

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