package dev.astralv2.stats;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プレイヤーごとのステータス保持（インメモリ）。
 * 保存層は後続タスクで分離実装する。
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
}
