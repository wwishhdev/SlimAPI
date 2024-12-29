package com.wish.commands;

import com.wish.API;
import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.RED + "§8§l» " + ChatColor.GRAY + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("slimac.alerts")) {
            player.sendMessage(ChatColor.RED + "§8§l» " + ChatColor.GRAY + "You don't have permission to use this command.");
            return true;
        }

        boolean toggledOn = plugin.getAlertManager().toggleAlerts(player);
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "§8§l» " + ChatColor.GRAY + "Alerts: " +
                (toggledOn ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        player.sendMessage("");

        return true;
    }
}