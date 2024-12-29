package com.wish.api;

import com.wish.API;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ViolationManager {

    private final API plugin;
    private final Map<UUID, Map<String, Integer>> checkViolations;

    public ViolationManager(API plugin) {
        this.plugin = plugin;
        // Usando ConcurrentHashMap para mejor thread-safety en versiones antiguas
        this.checkViolations = new ConcurrentHashMap<>();
    }

    public void addViolation(Player player, String checkName) {
        UUID uuid = player.getUniqueId();

        // Método más compatible para manejar el mapa de violaciones
        if (!checkViolations.containsKey(uuid)) {
            checkViolations.put(uuid, new HashMap<>());
        }

        Map<String, Integer> playerViolations = checkViolations.get(uuid);
        int violations = 0;

        if (playerViolations.containsKey(checkName)) {
            violations = playerViolations.get(checkName);
        }

        violations++;
        playerViolations.put(checkName, violations);

        // Notificar mediante AlertManager
        plugin.getAlertManager().sendAlert(checkName, player, "VL: " + violations);

        // Registrar en la base de datos
        plugin.getDatabaseManager().addViolation(uuid, checkName);
    }

    public int getViolations(Player player, String checkName) {
        UUID uuid = player.getUniqueId();
        if (!checkViolations.containsKey(uuid)) {
            return 0;
        }

        Map<String, Integer> playerViolations = checkViolations.get(uuid);
        return playerViolations.getOrDefault(checkName, 0);
    }

    public void clearViolations(UUID uuid) {
        checkViolations.remove(uuid);
    }
}