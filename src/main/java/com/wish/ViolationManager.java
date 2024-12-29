package com.wish.api;

import com.wish.API;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ViolationManager {

    private final API plugin;
    private final Map<UUID, Map<String, Integer>> checkViolations;

    public ViolationManager(API plugin) {
        this.plugin = plugin;
        this.checkViolations = new HashMap<>();
    }

    public void addViolation(Player player, String checkName) {
        UUID uuid = player.getUniqueId();
        checkViolations.computeIfAbsent(uuid, k -> new HashMap<>());
        Map<String, Integer> playerViolations = checkViolations.get(uuid);

        int violations = playerViolations.getOrDefault(checkName, 0) + 1;
        playerViolations.put(checkName, violations);

        // Notificar mediante AlertManager
        plugin.getAlertManager().sendAlert(checkName, player, "VL: " + violations);

        // Registrar en la base de datos
        plugin.getDatabaseManager().addViolation(uuid, checkName);
    }

    public int getViolations(Player player, String checkName) {
        return checkViolations
                .getOrDefault(player.getUniqueId(), new HashMap<>())
                .getOrDefault(checkName, 0);
    }
}