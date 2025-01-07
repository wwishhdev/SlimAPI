package com.wish.api;

import com.wish.API;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PingManager {
    private final API plugin;
    private int taskId = -1;

    public PingManager(API plugin) {
        this.plugin = plugin;
        if (plugin.getConfig().getBoolean("ping.enabled", true)) {
            startPingCheck();
        }
    }

    private void startPingCheck() {
        int interval = plugin.getConfig().getInt("ping.check-interval", 5) * 20; // convertir a ticks

        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                checkAllPlayers();
            }
        }.runTaskTimer(plugin, interval, interval).getTaskId();
    }

    public void stopPingCheck() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void checkAllPlayers() {
        int maxPing = plugin.getConfig().getInt("ping.max-ping", 500);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("slimac.ping.bypass")) {
                continue;
            }

            int ping = getPing(player);
            if (ping > maxPing) {
                handleHighPing(player, ping, maxPing);
            }
        }
    }

    private int getPing(Player player) {
        try {
            // Método compatible con versiones antiguas y nuevas
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            return 0;
        }
    }

    private void handleHighPing(Player player, int ping, int maxPing) {
        // Notificar al staff si está habilitado
        if (plugin.getConfig().getBoolean("ping.actions.notify-staff", true)) {
            String notifyMessage = ChatColor.RED + "[AntiCheat] " +
                    ChatColor.WHITE + player.getName() +
                    ChatColor.RED + " has high ping " +
                    ChatColor.WHITE + "(" + ping + "ms)";

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("slimac.alerts")) {
                    staff.sendMessage(notifyMessage);
                }
            }
        }

        // Ejecutar comando personalizado si está configurado
        String command = plugin.getConfig().getString("ping.actions.command", "");
        if (!command.isEmpty()) {
            command = command.replace("{player}", player.getName())
                    .replace("{ping}", String.valueOf(ping))
                    .replace("{max-ping}", String.valueOf(maxPing));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        // Expulsar al jugador si está habilitado
        if (plugin.getConfig().getBoolean("ping.actions.kick", true)) {
            String kickMessage = plugin.getConfig().getString("ping.actions.kick-message",
                            "&cHas sido expulsado por ping alto\n&fPing máximo permitido: &e{max-ping}ms\n&fTu ping: &e{ping}ms")
                    .replace("{ping}", String.valueOf(ping))
                    .replace("{max-ping}", String.valueOf(maxPing))
                    .replace("&", "§");

            // Ejecutar el kick en el siguiente tick para evitar ConcurrentModificationException
            Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(kickMessage));
        }
    }

    public int getCurrentPing(Player player) {
        return getPing(player);
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("ping.enabled", true);
    }

    public void reload() {
        stopPingCheck();
        if (plugin.getConfig().getBoolean("ping.enabled", true)) {
            startPingCheck();
        }
    }
}