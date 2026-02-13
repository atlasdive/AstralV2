package dev.astralv2.world;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * オープンワールド上に定期的な異常地点を生成する最小実装。
 */
public final class WorldAnomalyService {

    private static final int DEFAULT_RADIUS = 2500;
    private static final long REROLL_PERIOD_TICKS = 20L * 60L * 10L;

    private final JavaPlugin plugin;
    private final Random random = new Random();
    private Location currentAnomaly;
    private BukkitTask rerollTask;

    public WorldAnomalyService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        rerollAnomaly();

        rerollTask = Bukkit.getScheduler()
            .runTaskTimer(plugin, this::rerollAnomaly, REROLL_PERIOD_TICKS, REROLL_PERIOD_TICKS);
    }

    public void stop() {
        if (rerollTask != null) {
            rerollTask.cancel();
            rerollTask = null;
        }
    }

    public void rerollAnomaly() {
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) {
            currentAnomaly = null;
            plugin.getLogger().warning("No worlds are loaded. Skipping anomaly reroll.");
            return;
        }

        World world = worlds.stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst()
            .orElse(worlds.getFirst());

        int x = random.nextInt(DEFAULT_RADIUS * 2 + 1) - DEFAULT_RADIUS;
        int z = random.nextInt(DEFAULT_RADIUS * 2 + 1) - DEFAULT_RADIUS;
        int y = world.getHighestBlockYAt(x, z) + 1;

        currentAnomaly = new Location(world, x + 0.5, y, z + 0.5);

        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Astral] " + ChatColor.LIGHT_PURPLE
            + "異常座標が発生: " + formatLocation(currentAnomaly));
    }

    public Optional<Location> getCurrentAnomaly() {
        return Optional.ofNullable(currentAnomaly).map(Location::clone);
    }

    public String formatCurrentAnomaly() {
        if (currentAnomaly == null) {
            return "未生成";
        }
        return formatLocation(currentAnomaly);
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
