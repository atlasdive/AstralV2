package dev.astralv2.world;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.Random;

/**
 * オープンワールド上に定期的な異常地点を生成する最小実装。
 */
public final class WorldAnomalyService {

    private static final int DEFAULT_RADIUS = 2500;

    private final JavaPlugin plugin;
    private final Random random = new Random();
    private Location currentAnomaly;

    public WorldAnomalyService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        rerollAnomaly();

        long periodTicks = 20L * 60L * 10L; // 10分
        Bukkit.getScheduler().runTaskTimer(plugin, this::rerollAnomaly, periodTicks, periodTicks);
    }

    public void rerollAnomaly() {
        World world = Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst()
            .orElse(Bukkit.getWorlds().getFirst());

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
        return location.getWorld().getName() + " ("
            + location.getBlockX() + ", "
            + location.getBlockY() + ", "
            + location.getBlockZ() + ")";
    }
}
