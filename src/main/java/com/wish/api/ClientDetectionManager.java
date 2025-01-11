package com.wish.api;

import com.wish.API;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.List;

public class ClientDetectionManager implements Listener, PluginMessageListener, Reloadable {
    private final API plugin;
    private boolean forgeEnabled;
    private boolean fabricEnabled;
    private String forgeKickMessage;
    private String fabricKickMessage;
    private List<String> allowedForgeVersions;
    private List<String> allowedFabricVersions;

    private static final String FORGE_CHANNEL = "FML|HS";
    private static final String FABRIC_CHANNEL = "fabric:registry/sync";

    public ClientDetectionManager(API plugin) {
        this.plugin = plugin;
        loadConfig();

        // Registrar canales de comunicación
        if (forgeEnabled) {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, FORGE_CHANNEL, this);
        }
        if (fabricEnabled) {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, FABRIC_CHANNEL, this);
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadConfig() {
        forgeEnabled = plugin.getConfig().getBoolean("client-detection.forge.enabled", true);
        fabricEnabled = plugin.getConfig().getBoolean("client-detection.fabric.enabled", true);

        forgeKickMessage = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("client-detection.forge.kick-message",
                        "&cForge is not allowed on this server\n&7Please use vanilla client"));

        fabricKickMessage = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("client-detection.fabric.kick-message",
                        "&cFabric is not allowed on this server\n&7Please use vanilla client"));

        allowedForgeVersions = plugin.getConfig().getStringList("client-detection.forge.allowed-versions");
        allowedFabricVersions = plugin.getConfig().getStringList("client-detection.fabric.allowed-versions");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals(FORGE_CHANNEL) && forgeEnabled) {
            handleForgeClient(player);
        } else if (channel.equals(FABRIC_CHANNEL) && fabricEnabled) {
            handleFabricClient(player);
        }
    }

    private void handleForgeClient(Player player) {
        if (!player.hasPermission("slimac.bypass.forge")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.kickPlayer(forgeKickMessage);
                notifyStaff(player.getName(), "Forge");
            });
        }
    }

    private void handleFabricClient(Player player) {
        if (!player.hasPermission("slimac.bypass.fabric")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.kickPlayer(fabricKickMessage);
                notifyStaff(player.getName(), "Fabric");
            });
        }
    }

    private void notifyStaff(String playerName, String clientType) {
        String alertMessage = ChatColor.RED + "[AntiCheat] " +
                ChatColor.WHITE + playerName +
                ChatColor.RED + " was kicked for using " +
                ChatColor.WHITE + clientType;

        for (Player staff : plugin.getServer().getOnlinePlayers()) {
            if (staff.hasPermission("slimac.alerts")) {
                staff.sendMessage(alertMessage);
            }
        }
    }

    @Override
    public void reload() {
        // Desregistrar canales antiguos
        if (forgeEnabled) {
            plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, FORGE_CHANNEL);
        }
        if (fabricEnabled) {
            plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, FABRIC_CHANNEL);
        }

        // Recargar configuración
        loadConfig();

        // Registrar nuevos canales
        if (forgeEnabled) {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, FORGE_CHANNEL, this);
        }
        if (fabricEnabled) {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, FABRIC_CHANNEL, this);
        }
    }
}