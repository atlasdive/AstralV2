package dev.astralv2;

import dev.astralv2.combat.CombatListener;
import dev.astralv2.combat.DamageCalculator;
import dev.astralv2.command.AstralCommand;
import dev.astralv2.stats.PlayerStatsService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AstralPlugin extends JavaPlugin {

    private PlayerStatsService playerStatsService;

    @Override
    public void onEnable() {
        playerStatsService = new PlayerStatsService();
        registerCommands();
        registerListeners();
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

    private void registerCommands() {
        PluginCommand astralCommand = getCommand("astral");
        if (astralCommand == null) {
            throw new IllegalStateException("Command 'astral' is not defined in plugin.yml");
        }

        AstralCommand executor = new AstralCommand(playerStatsService);
        astralCommand.setExecutor(executor);
        astralCommand.setTabCompleter(executor);
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(
            new CombatListener(playerStatsService, new DamageCalculator()),
            this
        );
    }
}
