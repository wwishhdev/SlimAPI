package com.wish.commands;

import com.wish.API;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AlertsCommand implements CommandExecutor {
    private final API plugin;

    public AlertsCommand(API plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("slimac.alerts")) {
            player.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        boolean toggledOn = plugin.getAlertManager().toggleAlerts(player);
        player.sendMessage(toggledOn ? "§aAlertas activadas." : "§cAlertas desactivadas.");

        return true;
    }
}
