package com.wish;

import com.wish.api.PingManager;
import com.wish.commands.AlertsCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import com.wish.api.AlertManager;
import com.wish.api.DatabaseManager;
import com.wish.api.ViolationManager;

public class API extends JavaPlugin implements Listener {

    private static API instance;
    private AlertManager alertManager;
    private DatabaseManager databaseManager;
    private ViolationManager violationManager;
    private PingManager pingManager;

    @Override
    public void onEnable() {
        instance = this;

        // Inicializar managers
        this.alertManager = new AlertManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.violationManager = new ViolationManager(this);
        this.pingManager = new PingManager(this);

        // Registrar eventos
        getServer().getPluginManager().registerEvents(this, this);

        // Registrar comandos
        getCommand("alerts").setExecutor(new AlertsCommand(this));

        // Cargar configuración
        saveDefaultConfig();

        getLogger().info("SlimAPI has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        if (pingManager != null) {
            pingManager.stopPingCheck();
        }
        getLogger().info("SlimAPI has been disabled!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (getConfig().getBoolean("violations.reset-on-disconnect", true)) {
            violationManager.clearViolations(event.getPlayer().getUniqueId());
            databaseManager.clearViolations(event.getPlayer().getUniqueId());
        }
    }

    public PingManager getPingManager() {
        return pingManager;
    }

    public static API getInstance() {
        return instance;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ViolationManager getViolationManager() {
        return violationManager;
    }
}