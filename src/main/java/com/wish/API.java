package com.wish;

import com.wish.api.*;
import com.wish.api.events.SlimAPIReloadEvent;
import com.wish.commands.AlertsCommand;
import com.wish.commands.SlimAPICommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class API extends JavaPlugin implements Listener {

    private static API instance;
    private AlertManager alertManager;
    private DatabaseManager databaseManager;
    private ViolationManager violationManager;
    private PingManager pingManager;
    private PingCompensationManager pingCompensationManager;

    @Override
    public void onEnable() {
        instance = this;

        // Inicializar managers
        this.alertManager = new AlertManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.violationManager = new ViolationManager(this);
        this.pingManager = new PingManager(this);
        this.pingCompensationManager = new PingCompensationManager(this);

        // Registrar eventos
        getServer().getPluginManager().registerEvents(this, this);

        // Registrar comandos
        getCommand("alerts").setExecutor(new AlertsCommand(this));
        getCommand("slimapi").setExecutor(new SlimAPICommand(this));

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
        if (pingCompensationManager != null) {
            pingCompensationManager.shutdown();
        }
        getLogger().info("SlimAPI has been disabled!");
    }

    private void initializeManagers() {
        this.alertManager = new AlertManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.violationManager = new ViolationManager(this);
        this.pingManager = new PingManager(this);
        this.pingCompensationManager = new PingCompensationManager(this);
    }


    public void reloadManagers() {
        // Detener servicios antes de recargar
        if (databaseManager != null) databaseManager.shutdown();
        if (pingManager != null) pingManager.stopPingCheck();
        if (pingCompensationManager != null) pingCompensationManager.shutdown();

        // Reinicializar managers
        initializeManagers();

        // Notificar a los listeners registrados
        getServer().getPluginManager().callEvent(new SlimAPIReloadEvent());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (getConfig().getBoolean("violations.reset-on-disconnect", true)) {
            violationManager.clearViolations(event.getPlayer().getUniqueId());
            databaseManager.clearViolations(event.getPlayer().getUniqueId());
        }
    }

    public PingCompensationManager getPingCompensationManager() {
        return pingCompensationManager;
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