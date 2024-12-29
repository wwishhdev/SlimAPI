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
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadAll(sender);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "=== " + ChatColor.RED + "SlimAPI Help" + ChatColor.GRAY + " ===");
        sender.sendMessage(ChatColor.RED + "/slimapi reload" + ChatColor.GRAY + " - Recarga la configuración");
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
