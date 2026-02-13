package dev.astralv2.command;

import dev.astralv2.item.AstralItems;
import dev.astralv2.stats.PlayerStats;
import dev.astralv2.stats.PlayerStatsService;
import dev.astralv2.world.DungeonGenerationService;
import dev.astralv2.world.WorldAnomalyService;
import dev.astralv2.world.WorldEventService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * /astral コマンド（初期実装）
 */
public final class AstralCommand implements TabExecutor {

    private static final String SUBCOMMAND_STATS = "stats";
    private static final String SUBCOMMAND_GIVECORE = "givecore";
    private static final String SUBCOMMAND_SETSTAT = "setstat";
    private static final String SUBCOMMAND_RESETSTATS = "resetstats";
    private static final String SUBCOMMAND_ANOMALY = "anomaly";
    private static final String SUBCOMMAND_ANOMALY_REROLL = "anomaly-reroll";
    private static final String SUBCOMMAND_DUNGEON = "dungeon";
    private static final String SUBCOMMAND_DUNGEON_REROLL = "dungeon-reroll";
    private static final String SUBCOMMAND_EVENT = "event";
    private static final String SUBCOMMAND_EVENT_REROLL = "event-reroll";
    private static final String ADMIN_PERMISSION = "astral.admin";

    private final PlayerStatsService playerStatsService;
    private final AstralItems astralItems;
    private final WorldAnomalyService worldAnomalyService;
    private final DungeonGenerationService dungeonGenerationService;
    private final WorldEventService worldEventService;

    public AstralCommand(
        PlayerStatsService playerStatsService,
        AstralItems astralItems,
        WorldAnomalyService worldAnomalyService,
        DungeonGenerationService dungeonGenerationService,
        WorldEventService worldEventService
    ) {
        this.playerStatsService = playerStatsService;
        this.astralItems = astralItems;
        this.worldAnomalyService = worldAnomalyService;
        this.dungeonGenerationService = dungeonGenerationService;
        this.worldEventService = worldEventService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        if (SUBCOMMAND_STATS.equals(subCommand)) {
            return handleStats(sender);
        }
        if (SUBCOMMAND_GIVECORE.equals(subCommand)) {
            return handleGiveCore(sender);
        }
        if (SUBCOMMAND_SETSTAT.equals(subCommand)) {
            return handleSetStat(sender, label, args);
        }
        if (SUBCOMMAND_RESETSTATS.equals(subCommand)) {
            return handleResetStats(sender, label, args);
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

        if (SUBCOMMAND_EVENT.equals(subCommand)) {
            sender.sendMessage(ChatColor.DARK_GREEN + "現在のワールドイベント: "
                + ChatColor.GREEN + worldEventService.formatCurrentEvent());
            return true;
        }
        if (SUBCOMMAND_EVENT_REROLL.equals(subCommand)) {
            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
                return true;
            }
            worldEventService.rerollEvent();
            sender.sendMessage(ChatColor.GREEN + "ワールドイベントを再生成しました。");
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

    private boolean handleSetStat(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " setstat <player> <stat> <value>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "対象プレイヤーが見つかりません（オンラインのみ対応）。");
            return true;
        }

        double value;
        try {
            value = Double.parseDouble(args[3]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + "value は数値で指定してください。");
            return true;
        }

        PlayerStats current = playerStatsService.getOrCreate(target.getUniqueId());
        PlayerStats updated = applyStat(current, args[2], value);
        if (updated == null) {
            sender.sendMessage(ChatColor.RED + "stat は attack|defense|maxhealth|critchance|critdamage を指定してください。");
            return true;
        }

        playerStatsService.set(target.getUniqueId(), updated);
        sender.sendMessage(ChatColor.GREEN + "ステータスを更新しました: " + target.getName());
        target.sendMessage(ChatColor.AQUA + "管理者によりステータスが更新されました。");
        return true;
    }

    private boolean handleResetStats(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " resetstats <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "対象プレイヤーが見つかりません（オンラインのみ対応）。");
            return true;
        }

        playerStatsService.set(target.getUniqueId(), PlayerStats.DEFAULT);
        sender.sendMessage(ChatColor.GREEN + "ステータスを初期化しました: " + target.getName());
        target.sendMessage(ChatColor.AQUA + "管理者によりステータスが初期化されました。");
        return true;
    }

    private PlayerStats applyStat(PlayerStats current, String key, double value) {
        return switch (key.toLowerCase(Locale.ROOT)) {
            case "attack", "atk" -> current.withAttack(value);
            case "defense", "def" -> current.withDefense(value);
            case "maxhealth", "hp" -> current.withMaxHealth(value);
            case "critchance", "crit" -> current.withCritChance(value);
            case "critdamage", "critdmg" -> current.withCritDamage(value);
            default -> null;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> candidates = new ArrayList<>(List.of(
                SUBCOMMAND_STATS,
                SUBCOMMAND_ANOMALY,
                SUBCOMMAND_DUNGEON,
                SUBCOMMAND_EVENT
            ));

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                candidates.add(SUBCOMMAND_GIVECORE);
                candidates.add(SUBCOMMAND_SETSTAT);
                candidates.add(SUBCOMMAND_RESETSTATS);
                candidates.add(SUBCOMMAND_ANOMALY_REROLL);
                candidates.add(SUBCOMMAND_DUNGEON_REROLL);
                candidates.add(SUBCOMMAND_EVENT_REROLL);
            }

            return filterByPrefix(candidates, args[0]);
        }

        if (args.length == 2 && sender.hasPermission(ADMIN_PERMISSION)
            && (SUBCOMMAND_SETSTAT.equalsIgnoreCase(args[0]) || SUBCOMMAND_RESETSTATS.equalsIgnoreCase(args[0]))) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
            return filterByPrefix(playerNames, args[1]);
        }

        if (args.length == 3 && sender.hasPermission(ADMIN_PERMISSION)
            && SUBCOMMAND_SETSTAT.equalsIgnoreCase(args[0])) {
            return filterByPrefix(List.of("attack", "defense", "maxhealth", "critchance", "critdamage"), args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> filterByPrefix(List<String> candidates, String input) {
        String typed = input.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.startsWith(typed)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.YELLOW
            + "Usage: /" + label + " <stats|anomaly|dungeon|event>");
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.GRAY
                + "Admin: /" + label
                + " <givecore|setstat|resetstats|anomaly-reroll|dungeon-reroll|event-reroll>");
        }
    }
}
