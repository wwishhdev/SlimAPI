package com.wish.api;

import com.wish.API;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PingCompensationManager {
    private final API plugin;
    private final Map<UUID, LinkedList<Integer>> pingHistory;
    private final Map<UUID, Long> lastUpdateTime;
    private int taskId = -1;

    public PingCompensationManager(API plugin) {
        this.plugin = plugin;
        this.pingHistory = new ConcurrentHashMap<>();
        this.lastUpdateTime = new ConcurrentHashMap<>();

        if (plugin.getConfig().getBoolean("ping-compensation.enabled", true)) {
            startCompensationTask();
        }
    }

    private void startCompensationTask() {
        int interval = plugin.getConfig().getInt("ping-compensation.update-interval", 1);
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                updateCompensationData();
            }
        }.runTaskTimer(plugin, interval, interval).getTaskId();
    }

    private void updateCompensationData() {
        int historySize = plugin.getConfig().getInt("ping-compensation.history-size", 20);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            int currentPing = plugin.getPingManager().getCurrentPing(player);

            pingHistory.computeIfAbsent(uuid, k -> new LinkedList<>());
            LinkedList<Integer> history = pingHistory.get(uuid);

            history.addLast(currentPing);
            while (history.size() > historySize) {
                history.removeFirst();
            }

            lastUpdateTime.put(uuid, System.currentTimeMillis());
        }
    }

    public int getCompensatedTime(Player player, int time) {
        if (!plugin.getConfig().getBoolean("ping-compensation.enabled", true)) {
            return time;
        }

        double factor = plugin.getConfig().getDouble("ping-compensation.compensation-factor", 0.75);
        int maxCompensation = plugin.getConfig().getInt("ping-compensation.max-compensation", 150);
        String mode = plugin.getConfig().getString("ping-compensation.mode", "ADAPTIVE");

        int compensation;
        if (mode.equalsIgnoreCase("ADAPTIVE")) {
            compensation = calculateAdaptiveCompensation(player);
        } else {
            compensation = plugin.getPingManager().getCurrentPing(player);
        }

        compensation = Math.min(compensation, maxCompensation);
        int compensatedTime = time + (int)(compensation * factor);

        if (plugin.getConfig().getBoolean("ping-compensation.debug", false)) {
            plugin.getLogger().info(String.format(
                    "Compensation for %s: Original=%d, Compensation=%d, Final=%d",
                    player.getName(), time, compensation, compensatedTime
            ));
        }

        return compensatedTime;
    }

    private int calculateAdaptiveCompensation(Player player) {
        UUID uuid = player.getUniqueId();
        LinkedList<Integer> history = pingHistory.getOrDefault(uuid, new LinkedList<>());

        if (history.isEmpty()) {
            return plugin.getPingManager().getCurrentPing(player);
        }

        // Calcular la mediana del historial de ping
        List<Integer> sorted = new ArrayList<>(history);
        Collections.sort(sorted);
        int median = sorted.get(sorted.size() / 2);

        // Calcular la desviación estándar
        double mean = sorted.stream().mapToInt(Integer::intValue).average().orElse(median);
        double variance = sorted.stream()
                .mapToDouble(ping -> Math.pow(ping - mean, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // Ajustar la compensación basada en la variabilidad del ping
        double variabilityFactor = Math.min(1.0, Math.max(0.5, 1.0 - (stdDev / 100.0)));
        return (int)(median * variabilityFactor);
    }

    public double getCompensatedDistance(Player player, double distance) {
        if (!plugin.getConfig().getBoolean("ping-compensation.enabled", true)) {
            return distance;
        }

        int ping = plugin.getPingManager().getCurrentPing(player);
        double factor = plugin.getConfig().getDouble("ping-compensation.compensation-factor", 0.75);
        double compensation = ping * 0.001 * factor; // convertir ping a segundos y aplicar factor

        return distance * (1 + compensation);
    }

    public boolean isCompensationNeeded(Player player, long timeThreshold) {
        if (!plugin.getConfig().getBoolean("ping-compensation.enabled", true)) {
            return false;
        }

        int ping = plugin.getPingManager().getCurrentPing(player);
        return ping > timeThreshold;
    }

    public void clearData(UUID uuid) {
        pingHistory.remove(uuid);
        lastUpdateTime.remove(uuid);
    }

    public void shutdown() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        pingHistory.clear();
        lastUpdateTime.clear();
    }
}