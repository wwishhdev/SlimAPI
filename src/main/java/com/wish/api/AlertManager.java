package com.wish.api;

import com.wish.API;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AlertManager {

    private final API plugin;

    public AlertManager(API plugin) {
        this.plugin = plugin;
    }

    public void sendAlert(String checkName, Player violator, String details) {
        // Usando ChatColor en lugar de códigos de color directos para mayor compatibilidad
        String message = ChatColor.RED + "[AntiCheat] " +
                ChatColor.WHITE + violator.getName() +
                ChatColor.RED + " failed " +
                ChatColor.WHITE + checkName +
                ChatColor.RED + " (" + details + ")";

        // Método más compatible para enviar mensajes
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("slimac.alerts")) {
                player.sendMessage(message);
            }
        }
    }
}