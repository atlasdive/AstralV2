package dev.astralv2.world;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

/**
 * ワールドイベント層の最小実装。Anomaly / Dungeon の状態からイベント内容を更新する。
 */
public final class WorldEventService {

    private static final long REROLL_PERIOD_TICKS = 20L * 60L * 10L;
    private static final List<String> EVENT_TYPES = List.of(
        "ボス出現予兆",
        "地域変異",
        "魔力濃度上昇",
        "古代遺物反応"
    );

    private final JavaPlugin plugin;
    private final WorldAnomalyService worldAnomalyService;
    private final DungeonGenerationService dungeonGenerationService;
    private final Random random = new Random();

    private String currentEventName = "未生成";
    private String currentEventDetail = "未生成";
    private BukkitTask rerollTask;

    public WorldEventService(
        JavaPlugin plugin,
        WorldAnomalyService worldAnomalyService,
        DungeonGenerationService dungeonGenerationService
    ) {
        this.plugin = plugin;
        this.worldAnomalyService = worldAnomalyService;
        this.dungeonGenerationService = dungeonGenerationService;
    }

    public void start() {
        stop();
        rerollEvent();

        rerollTask = Bukkit.getScheduler()
            .runTaskTimer(plugin, this::rerollEvent, REROLL_PERIOD_TICKS, REROLL_PERIOD_TICKS);
    }

    public void stop() {
        if (rerollTask != null) {
            rerollTask.cancel();
            rerollTask = null;
        }
    }

    public void rerollEvent() {
        currentEventName = EVENT_TYPES.get(random.nextInt(EVENT_TYPES.size()));

        Optional<Location> anomaly = worldAnomalyService.getCurrentAnomaly();
        String anomalyText = anomaly.map(this::formatLocation).orElse("未生成");
        String dungeonText = dungeonGenerationService.formatCurrentDungeonEntrance();

        currentEventDetail = switch (currentEventName.toLowerCase(Locale.ROOT)) {
            case "ボス出現予兆" -> "異常座標付近で強敵反応を検知: " + anomalyText;
            case "地域変異" -> "周辺環境が変化中。調査推奨地点: " + anomalyText;
            case "魔力濃度上昇" -> "魔力が高密度化。ダンジョン候補: " + dungeonText;
            case "古代遺物反応" -> "遺物信号を追跡中。入口候補: " + dungeonText;
            default -> "イベント情報を更新しました。";
        };

        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[Astral Event] " + ChatColor.GREEN
            + currentEventName + " - " + currentEventDetail);
    }

    public String formatCurrentEvent() {
        return currentEventName + " / " + currentEventDetail;
    }

    private String formatLocation(Location location) {
        if (location.getWorld() == null) {
            return "unknown ("
                + location.getBlockX() + ", "
                + location.getBlockY() + ", "
                + location.getBlockZ() + ")";
        }

        return location.getWorld().getName() + " ("
            + location.getBlockX() + ", "
            + location.getBlockY() + ", "
            + location.getBlockZ() + ")";
    }
}
