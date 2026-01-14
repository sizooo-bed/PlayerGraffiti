package fr.sizooo.playergraffiti;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("§aPlayerGraffiti activé ! Prêt à graffiter !");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("§cPlayerGraffiti désactivé !");
    }
}