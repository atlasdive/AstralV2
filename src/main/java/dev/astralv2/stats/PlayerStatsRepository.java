package dev.astralv2.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * プレイヤーステータスの JSON 永続化。
 */
public final class PlayerStatsRepository {

    private static final Type FILE_TYPE = new TypeToken<Map<String, PlayerStats>>() {
    }.getType();

    private final JavaPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public PlayerStatsRepository(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void save(Map<UUID, PlayerStats> statsByPlayer) {
        try {
            Files.createDirectories(plugin.getDataFolder().toPath());
            Path filePath = resolveFilePath();

            Map<String, PlayerStats> serializable = statsByPlayer.entrySet().stream()
                .collect(Collectors.toMap(
                    e -> e.getKey().toString(),
                    Map.Entry::getValue
                ));

            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                gson.toJson(serializable, FILE_TYPE, writer);
            }
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save player stats: " + exception.getMessage());
        }
    }

    public Map<UUID, PlayerStats> load() {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return Map.of();
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            Map<String, PlayerStats> raw = gson.fromJson(reader, FILE_TYPE);
            if (raw == null || raw.isEmpty()) {
                return Map.of();
            }

            return raw.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> UUID.fromString(entry.getKey()),
                    Map.Entry::getValue
                ));
        } catch (Exception exception) {
            plugin.getLogger().severe("Failed to load player stats: " + exception.getMessage());
            return Map.of();
        }
    }

    private Path resolveFilePath() {
        return plugin.getDataFolder().toPath().resolve("player-stats.json");
    }
}
