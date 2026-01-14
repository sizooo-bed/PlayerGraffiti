package fr.sizooo.playergraffiti.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import fr.sizooo.playergraffiti.Main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    
    private Main plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public DataManager(Main plugin) {
        this.plugin = plugin;
        setup();
    }
    
    private void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        
        dataFile = new File(plugin.getDataFolder(), "graffiti_data.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data file!");
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public void saveActiveGraffiti(Map<Location, String> graffitiData) {
        dataConfig.set("active_graffiti", null); // Clear old data
        
        int index = 0;
        for (Map.Entry<Location, String> entry : graffitiData.entrySet()) {
            Location loc = entry.getKey();
            String path = "graffiti." + index + ".";
            
            dataConfig.set(path + "world", loc.getWorld().getName());
            dataConfig.set(path + "x", loc.getBlockX());
            dataConfig.set(path + "y", loc.getBlockY());
            dataConfig.set(path + "z", loc.getBlockZ());
            dataConfig.set(path + "message", entry.getValue());
            
            index++;
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save graffiti data!");
        }
    }
    
    public Map<Location, String> loadActiveGraffiti() {
        Map<Location, String> graffitiData = new HashMap<>();
        
        if (!dataConfig.contains("graffiti")) {
            return graffitiData;
        }
        
        for (String key : dataConfig.getConfigurationSection("graffiti").getKeys(false)) {
            String path = "graffiti." + key + ".";
            
            String worldName = dataConfig.getString(path + "world");
            int x = dataConfig.getInt(path + "x");
            int y = dataConfig.getInt(path + "y");
            int z = dataConfig.getInt(path + "z");
            String message = dataConfig.getString(path + "message");
            
            Location loc = new Location(
                plugin.getServer().getWorld(worldName),
                x, y, z
            );
            
            graffitiData.put(loc, message);
        }
        
        return graffitiData;
    }
}