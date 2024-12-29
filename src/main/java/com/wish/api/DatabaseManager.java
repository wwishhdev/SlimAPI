package com.wish.api;

import com.wish.API;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DatabaseManager {

    private final API plugin;
    private final Map<UUID, Integer> violations;

    public DatabaseManager(API plugin) {
        this.plugin = plugin;
        // Usando ConcurrentHashMap para mejor thread-safety en versiones antiguas
        this.violations = new ConcurrentHashMap<>();
    }

    public void addViolation(UUID playerUUID, String checkName) {
        int current = violations.getOrDefault(playerUUID, 0);
        violations.put(playerUUID, current + 1);
    }

    public int getViolations(UUID playerUUID) {
        return violations.getOrDefault(playerUUID, 0);
    }

    public void clearViolations(UUID playerUUID) {
        violations.remove(playerUUID);
    }
}