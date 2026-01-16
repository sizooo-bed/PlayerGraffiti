package fr.sizooo.playergraffiti;

import fr.sizooo.playergraffiti.commands.*;
import fr.sizooo.playergraffiti.listeners.SignClickListener;
import fr.sizooo.playergraffiti.managers.PlayerManager;
import fr.sizooo.playergraffiti.managers.SignManager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private PlayerManager playerManager;
    private SignManager signManager;

    @Override
    public void onEnable() {
        getLogger().info("§a🎉 PlayerGraffiti v2.0 enabled!");

        playerManager = new PlayerManager();
        signManager = new SignManager(this);

        PluginCommand graffitiCmd = this.getCommand("graffiti");
        if (graffitiCmd != null) {
            graffitiCmd.setExecutor(new GraffitiCommand(this));
            graffitiCmd.setTabCompleter(new GraffitiCommand(this));
        }

        getServer().getPluginManager().registerEvents(new SignClickListener(this), this);

        getLogger().info("§aAll systems operational!");
    }

    @Override
    public void onDisable() {
        getLogger().info("§cPlayerGraffiti disabled!");
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public SignManager getSignManager() {
        return signManager;
    }
}