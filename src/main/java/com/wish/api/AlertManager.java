package com.wish.api;

import com.wish.API;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AlertManager {

    private final API plugin;
    private final Set<UUID> alertsEnabled;

    public AlertManager(API plugin) {
        this.plugin = plugin;
        this.alertsEnabled = new HashSet<>();
        loadAlertStates();
    }

    private void loadAlertStates() {
        // Cargar estados de alertas de la base de datos al iniciar
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getDatabaseManager().getAlertsStatus(player.getUniqueId())) {
                    alertsEnabled.add(player.getUniqueId());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error al cargar estados de alertas: " + e.getMessage());
        }
    }

    public void sendAlert(String checkName, Player violator, String details) {
        if (!plugin.getConfig().getBoolean("alerts.enabled", true)) {
            return;
        }

        String message = ChatColor.RED + "[AntiCheat] " +
                ChatColor.WHITE + violator.getName() +
                ChatColor.RED + " failed " +
                ChatColor.WHITE + checkName +
                ChatColor.RED + " (" + details + ")";

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("slimac.alerts") &&
                    plugin.getDatabaseManager().areAlertsEnabled(player.getUniqueId())) {
                player.sendMessage(message);
            }
        }
    }

    public boolean toggleAlerts(Player player) {
        UUID uuid = player.getUniqueId();
        boolean newState = !alertsEnabled.contains(uuid);

        if (plugin.getDatabaseManager().updateAlertsStatus(uuid, newState)) {
            if (newState) {
                alertsEnabled.add(uuid);
            } else {
                alertsEnabled.remove(uuid);
            }
            return newState;
        }
        return alertsEnabled.contains(uuid);
    }

    public boolean hasAlertsEnabled(Player player) {
        return alertsEnabled.contains(player.getUniqueId());
    }
}