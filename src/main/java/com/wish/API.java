package com.wish;

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

    @Override
    public void onEnable() {
        instance = this;

        // Inicializar managers
        this.alertManager = new AlertManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.violationManager = new ViolationManager(this);

        // Registrar eventos
        getServer().getPluginManager().registerEvents(this, this);

        // Cargar configuración
        saveDefaultConfig();

        getLogger().info("SlimACAPI has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SlimACAPI has been disabled!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (getConfig().getBoolean("violations.reset-on-disconnect", true)) {
            violationManager.clearViolations(event.getPlayer().getUniqueId());
            databaseManager.clearViolations(event.getPlayer().getUniqueId());
        }
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