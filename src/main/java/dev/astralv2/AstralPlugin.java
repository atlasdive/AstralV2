package dev.astralv2;

import dev.astralv2.combat.CombatListener;
import dev.astralv2.combat.DamageCalculator;
import dev.astralv2.command.AstralCommand;
import dev.astralv2.item.AstralItems;
import dev.astralv2.item.AstralRecipeRegistrar;
import dev.astralv2.stats.PlayerStatsService;
import dev.astralv2.world.DungeonGenerationService;
import dev.astralv2.world.WorldAnomalyService;
import dev.astralv2.world.WorldEventService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AstralPlugin extends JavaPlugin {

    private PlayerStatsService playerStatsService;
    private WorldAnomalyService worldAnomalyService;
    private DungeonGenerationService dungeonGenerationService;
    private WorldEventService worldEventService;

    @Override
    public void onEnable() {
        playerStatsService = new PlayerStatsService();

        AstralItems astralItems = new AstralItems(this);
        new AstralRecipeRegistrar(this, astralItems).registerAll();

        worldAnomalyService = new WorldAnomalyService(this);
        worldAnomalyService.start();

        dungeonGenerationService = new DungeonGenerationService(this, worldAnomalyService);
        dungeonGenerationService.start();

        worldEventService = new WorldEventService(this, worldAnomalyService, dungeonGenerationService);
        worldEventService.start();

        registerCommands(astralItems, worldAnomalyService, dungeonGenerationService, worldEventService);
        registerListeners();
        getLogger().info("AstralV2 plugin enabled. Player stats service initialized.");
    }

    @Override
    public void onDisable() {
        if (worldEventService != null) {
            worldEventService.stop();
        }
        if (dungeonGenerationService != null) {
            dungeonGenerationService.stop();
        }
        if (worldAnomalyService != null) {
            worldAnomalyService.stop();
        }
        if (playerStatsService != null) {
            playerStatsService.clearAll();
        }
        getLogger().info("AstralV2 plugin disabled.");
    }

    public PlayerStatsService getPlayerStatsService() {
        return playerStatsService;
    }

    private void registerCommands(
        AstralItems astralItems,
        WorldAnomalyService worldAnomalyService,
        DungeonGenerationService dungeonGenerationService,
        WorldEventService worldEventService
    ) {
        PluginCommand astralCommand = getCommand("astral");
        if (astralCommand == null) {
            throw new IllegalStateException("Command 'astral' is not defined in plugin.yml");
        }

        AstralCommand executor = new AstralCommand(
            playerStatsService,
            astralItems,
            worldAnomalyService,
            dungeonGenerationService,
            worldEventService
        );
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
