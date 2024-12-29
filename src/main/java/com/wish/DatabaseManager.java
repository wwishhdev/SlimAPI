package com.wish.api;

import com.wish.API;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private final API plugin;
    private final Map<UUID, Integer> violations;

    public DatabaseManager(API plugin) {
        this.plugin = plugin;
        this.violations = new HashMap<>();
    }

    public void addViolation(UUID playerUUID, String checkName) {
        violations.merge(playerUUID, 1, Integer::sum);
    }

    public int getViolations(UUID playerUUID) {
        return violations.getOrDefault(playerUUID, 0);
    }
}
