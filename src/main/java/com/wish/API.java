package com.wish;

import org.bukkit.plugin.java.JavaPlugin;
import com.wish.api.AlertManager;
import com.wish.api.DatabaseManager;
import com.wish.api.ViolationManager;

public class API extends JavaPlugin {

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

        getLogger().info("SlimACAPI has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SlimACAPI has been disabled!");
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