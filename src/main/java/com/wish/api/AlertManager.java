package com.wish.api;

import com.wish.API;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.UUID;

public class AlertManager {

    private final API plugin;

    public AlertManager(API plugin) {
        this.plugin = plugin;
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
        boolean newState = !plugin.getDatabaseManager().areAlertsEnabled(uuid);
        plugin.getDatabaseManager().setAlertsEnabled(uuid, newState);
        return newState;
    }

    public boolean hasAlertsEnabled(Player player) {
        return plugin.getDatabaseManager().areAlertsEnabled(player.getUniqueId());
    }
}