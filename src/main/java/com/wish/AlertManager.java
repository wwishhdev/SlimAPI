package com.wish.api;

import com.wish.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AlertManager {

    private final API plugin;

    public AlertManager(API plugin) {
        this.plugin = plugin;
    }

    public void sendAlert(String checkName, Player violator, String details) {
        String message = String.format("§c[AntiCheat] §f%s §cfailed §f%s §c(%s)",
                violator.getName(), checkName, details);

        // Enviar alerta a todos los jugadores con permiso
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission("slimac.alerts"))
                .forEach(player -> player.sendMessage(message));
    }
}
