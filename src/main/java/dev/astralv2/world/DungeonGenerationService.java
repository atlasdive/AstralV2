package dev.astralv2.world;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.Random;

/**
 * Anomaly 座標を入口候補として利用する、ダンジョン生成マネージャー最小プロトタイプ。
 */
public final class DungeonGenerationService {

    private static final int ENTRANCE_OFFSET_RADIUS = 80;
    private static final long REROLL_PERIOD_TICKS = 20L * 60L * 10L;

    private final JavaPlugin plugin;
    private final WorldAnomalyService worldAnomalyService;
    private final Random random = new Random();
    private Location currentDungeonEntrance;
    private BukkitTask rerollTask;

    public DungeonGenerationService(JavaPlugin plugin, WorldAnomalyService worldAnomalyService) {
        this.plugin = plugin;
        this.worldAnomalyService = worldAnomalyService;
    }

    public void start() {
        stop();
        rerollDungeonEntrance();

        rerollTask = Bukkit.getScheduler()
            .runTaskTimer(plugin, this::rerollDungeonEntrance, REROLL_PERIOD_TICKS, REROLL_PERIOD_TICKS);
    }

    public void stop() {
        if (rerollTask != null) {
            rerollTask.cancel();
            rerollTask = null;
        }
    }

    public void rerollDungeonEntrance() {
        Optional<Location> anomaly = worldAnomalyService.getCurrentAnomaly();
        if (anomaly.isEmpty()) {
            currentDungeonEntrance = null;
            return;
        }

        Location base = anomaly.get();
        World world = base.getWorld();
        if (world == null) {
            currentDungeonEntrance = null;
            plugin.getLogger().warning("Anomaly world is unavailable. Skipping dungeon entrance reroll.");
            return;
        }

        int x = base.getBlockX() + random.nextInt(ENTRANCE_OFFSET_RADIUS * 2 + 1) - ENTRANCE_OFFSET_RADIUS;
        int z = base.getBlockZ() + random.nextInt(ENTRANCE_OFFSET_RADIUS * 2 + 1) - ENTRANCE_OFFSET_RADIUS;
        int y = world.getHighestBlockYAt(x, z) + 1;

        currentDungeonEntrance = new Location(world, x + 0.5, y, z + 0.5);

        Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "[Astral] " + ChatColor.AQUA
            + "ダンジョン入口候補を更新: " + formatLocation(currentDungeonEntrance));
    }

    public String formatCurrentDungeonEntrance() {
        if (currentDungeonEntrance == null) {
            return "未生成";
        }
        return formatLocation(currentDungeonEntrance);
    }

    private String formatLocation(Location location) {
        World world = location.getWorld();
        String worldName = world == null ? "unknown" : world.getName();
        return worldName + " ("
            + location.getBlockX() + ", "
            + location.getBlockY() + ", "
            + location.getBlockZ() + ")";
    }
}
