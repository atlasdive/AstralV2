package dev.astralv2;

import dev.astralv2.stats.PlayerStatsService;
import org.bukkit.plugin.java.JavaPlugin;

public final class AstralPlugin extends JavaPlugin {

    private PlayerStatsService playerStatsService;

    @Override
    public void onEnable() {
        playerStatsService = new PlayerStatsService();
        getLogger().info("AstralV2 plugin enabled. Player stats service initialized.");
    }

    @Override
    public void onDisable() {
        if (playerStatsService != null) {
            playerStatsService.clearAll();
        }
        getLogger().info("AstralV2 plugin disabled.");
    }

    public PlayerStatsService getPlayerStatsService() {
        return playerStatsService;
    }
}
