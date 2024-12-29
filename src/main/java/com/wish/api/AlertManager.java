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
            if (player.hasPermission("slimac.alerts") && alertsEnabled.contains(player.getUniqueId())) {
                player.sendMessage(message);
            }
        }
    }

    public boolean toggleAlerts(Player player) {
        UUID uuid = player.getUniqueId();
        if (alertsEnabled.contains(uuid)) {
            alertsEnabled.remove(uuid);
            return false;
        } else {
            alertsEnabled.add(uuid);
            return true;
        }
    }

    public boolean hasAlertsEnabled(Player player) {
        return alertsEnabled.contains(player.getUniqueId());
    }
}