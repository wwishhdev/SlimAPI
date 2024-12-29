package com.wish.commands;

import com.wish.API;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SlimAPICommand implements CommandExecutor, TabCompleter {
    private final API plugin;

    public SlimAPICommand(API plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("slimac.admin")) {
            sender.sendMessage(ChatColor.RED + "§8§l» " + ChatColor.GRAY + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.RED + "§8§l» " + ChatColor.GRAY + "Unknown command. Use:");
            sender.sendMessage(ChatColor.RED + "/slimapi reload " + ChatColor.GRAY + "- Reload configuration");
            sender.sendMessage("");
            return true;
        }

        reloadAll(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "§8§l============[ " + ChatColor.RED + "SlimAPI" + ChatColor.GRAY + " §8§l]============");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "§8§l» " + ChatColor.GRAY + "Commands:");
        sender.sendMessage(ChatColor.RED + "/slimapi reload " + ChatColor.GRAY + "- Reload configuration");
        sender.sendMessage(ChatColor.RED + "/alerts " + ChatColor.GRAY + "- Toggle anticheat alerts");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "§8§l» " + ChatColor.GRAY + "Permissions:");
        sender.sendMessage(ChatColor.GRAY + "slimac.admin " + ChatColor.DARK_GRAY + "- Access to admin commands");
        sender.sendMessage(ChatColor.GRAY + "slimac.alerts " + ChatColor.DARK_GRAY + "- Receive alerts");
        sender.sendMessage(ChatColor.GRAY + "slimac.ping.bypass " + ChatColor.DARK_GRAY + "- Bypass ping checks");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "§8§l» " + ChatColor.GRAY + "Version: " + ChatColor.RED + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "§8§l================================");
        sender.sendMessage("");
    }

    private void reloadAll(CommandSender sender) {
        try {
            // Recargar config.yml
            plugin.reloadConfig();

            // Recargar todos los managers
            plugin.reloadManagers();

            sender.sendMessage(ChatColor.GREEN + "Configuración recargada correctamente.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error al recargar la configuración: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
