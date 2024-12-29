package com.wish;

import com.wish.api.*;
import com.wish.api.events.SlimAPIReloadEvent;
import com.wish.commands.AlertsCommand;
import com.wish.commands.SlimAPICommand;
import org.bukkit.ChatColor;
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

        // Mostrar ASCII art
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "\n" +
                "███████╗██╗     ██╗███╗   ███╗ █████╗ ██████╗ ██╗\n" +
                "██╔════╝██║     ██║████╗ ████║██╔══██╗██╔══██╗██║\n" +
                "███████╗██║     ██║██╔████╔██║███████║██████╔╝██║\n" +
                "╚════██║██║     ██║██║╚██╔╝██║██╔══██║██╔═══╝ ██║\n" +
                "███████║███████╗██║██║ ╚═╝ ██║██║  ██║██║     ██║\n" +
                "╚══════╝╚══════╝╚═╝╚═╝     ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝\n");

        getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "Version: " + ChatColor.RED + getDescription().getVersion());
        getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "Author: " + ChatColor.RED + "wwishh");
        getServer().getConsoleSender().sendMessage("");

        // Cargar configuración
        saveDefaultConfig();

        try {
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

            getLogger().info("SlimAPI has been enabled! by wwishh <3");
        } catch (Exception e) {
            getLogger().severe("Error initializing SlimAPI: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
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
        getLogger().info("SlimAPI has been disabled! by wwishh <3");
    }

    private void initializeManagers() {
        this.alertManager = new AlertManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.violationManager = new ViolationManager(this);
        this.pingManager = new PingManager(this);
        this.pingCompensationManager = new PingCompensationManager(this);
    }


    public void reloadManagers() {
        // Recargar configuración primero
        reloadConfig();

        // Detener servicios antes de recargar
        if (databaseManager != null) {
            databaseManager.reload(); // Usar reload en lugar de shutdown
        }
        if (pingManager != null) {
            pingManager.stopPingCheck();
        }
        if (pingCompensationManager != null) {
            pingCompensationManager.shutdown();
        }

        // Reinicializar otros managers
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