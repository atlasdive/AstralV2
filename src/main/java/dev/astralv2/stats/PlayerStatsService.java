package dev.astralv2.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プレイヤーごとのステータス保持（インメモリ）。
 * 永続化自体は PlayerStatsRepository が担当する。
 */
public final class PlayerStatsService {

    private final Map<UUID, PlayerStats> statsByPlayer = new ConcurrentHashMap<>();

    public PlayerStats getOrCreate(UUID playerId) {
        return statsByPlayer.computeIfAbsent(playerId, ignored -> PlayerStats.DEFAULT);
    }

    public void set(UUID playerId, PlayerStats stats) {
        statsByPlayer.put(playerId, stats);
    }

    public void clear(UUID playerId) {
        statsByPlayer.remove(playerId);
    }

    public void clearAll() {
        statsByPlayer.clear();
    }

    public Map<UUID, PlayerStats> snapshot() {
        return new HashMap<>(statsByPlayer);
    }

    public void replaceAll(Map<UUID, PlayerStats> loadedStats) {
        statsByPlayer.clear();
        statsByPlayer.putAll(loadedStats);
    }
}


