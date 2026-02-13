package dev.astralv2.command;

import dev.astralv2.item.AstralItems;
import dev.astralv2.stats.PlayerStats;
import dev.astralv2.stats.PlayerStatsService;
import dev.astralv2.world.DungeonGenerationService;
import dev.astralv2.world.WorldAnomalyService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * /astral コマンド（初期実装）
 */
public final class AstralCommand implements TabExecutor {

    private static final String SUBCOMMAND_STATS = "stats";
    private static final String SUBCOMMAND_GIVECORE = "givecore";
    private static final String SUBCOMMAND_ANOMALY = "anomaly";
    private static final String SUBCOMMAND_ANOMALY_REROLL = "anomaly-reroll";
    private static final String SUBCOMMAND_DUNGEON = "dungeon";
    private static final String SUBCOMMAND_DUNGEON_REROLL = "dungeon-reroll";
    private static final String ADMIN_PERMISSION = "astral.admin";

    private final PlayerStatsService playerStatsService;
    private final AstralItems astralItems;
    private final WorldAnomalyService worldAnomalyService;
    private final DungeonGenerationService dungeonGenerationService;

    public AstralCommand(
        PlayerStatsService playerStatsService,
        AstralItems astralItems,
        WorldAnomalyService worldAnomalyService,
        DungeonGenerationService dungeonGenerationService
    ) {
        this.playerStatsService = playerStatsService;
        this.astralItems = astralItems;
        this.worldAnomalyService = worldAnomalyService;
        this.dungeonGenerationService = dungeonGenerationService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        if (SUBCOMMAND_STATS.equals(subCommand)) {
            return handleStats(sender);
        }
        if (SUBCOMMAND_GIVECORE.equals(subCommand)) {
            return handleGiveCore(sender);
        }
        if (SUBCOMMAND_ANOMALY.equals(subCommand)) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "現在の異常座標: "
                + ChatColor.LIGHT_PURPLE + worldAnomalyService.formatCurrentAnomaly());
            return true;
        }
        if (SUBCOMMAND_ANOMALY_REROLL.equals(subCommand)) {
            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
                return true;
            }
            worldAnomalyService.rerollAnomaly();
            dungeonGenerationService.rerollDungeonEntrance();
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "異常座標を再生成しました。");
            return true;
        }
        if (SUBCOMMAND_DUNGEON.equals(subCommand)) {
            sender.sendMessage(ChatColor.DARK_AQUA + "現在のダンジョン入口候補: "
                + ChatColor.AQUA + dungeonGenerationService.formatCurrentDungeonEntrance());
            return true;
        }
        if (SUBCOMMAND_DUNGEON_REROLL.equals(subCommand)) {
            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
                return true;
            }
            dungeonGenerationService.rerollDungeonEntrance();
            sender.sendMessage(ChatColor.AQUA + "ダンジョン入口候補を再生成しました。");
            return true;
        }

        sendUsage(sender, label);
        return true;
    }

    private boolean handleStats(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return true;
        }

        PlayerStats stats = playerStatsService.getOrCreate(player.getUniqueId());
        sender.sendMessage(ChatColor.GOLD + "=== Astral Stats ===");
        sender.sendMessage(ChatColor.YELLOW + "ATK: " + ChatColor.WHITE + stats.attack());
        sender.sendMessage(ChatColor.YELLOW + "DEF: " + ChatColor.WHITE + stats.defense());
        sender.sendMessage(ChatColor.YELLOW + "MAX HP: " + ChatColor.WHITE + stats.maxHealth());
        sender.sendMessage(ChatColor.YELLOW + "CRIT CHANCE: " + ChatColor.WHITE + String.format("%.2f%%", stats.critChance() * 100.0));
        sender.sendMessage(ChatColor.YELLOW + "CRIT DAMAGE: " + ChatColor.WHITE + String.format("%.2fx", stats.critDamage()));
        return true;
    }

    private boolean handleGiveCore(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ実行できます。");
            return true;
        }

        if (!player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
            return true;
        }

        player.getInventory().addItem(astralItems.createAstralCore());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Astral Core を1個付与しました。");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> candidates = new ArrayList<>(List.of(
                SUBCOMMAND_STATS,
                SUBCOMMAND_GIVECORE,
                SUBCOMMAND_ANOMALY,
                SUBCOMMAND_DUNGEON
            ));

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                candidates.add(SUBCOMMAND_ANOMALY_REROLL);
                candidates.add(SUBCOMMAND_DUNGEON_REROLL);
            }

            String typed = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String candidate : candidates) {
                if (candidate.startsWith(typed)) {
                    result.add(candidate);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.YELLOW
            + "Usage: /" + label + " <stats|givecore|anomaly|dungeon>");
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.GRAY
                + "Admin: /" + label + " <anomaly-reroll|dungeon-reroll>");
        }
    }
}
