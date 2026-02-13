package dev.astralv2.command;

import dev.astralv2.item.AstralItems;
import dev.astralv2.stats.PlayerStats;
import dev.astralv2.stats.PlayerStatsService;
import dev.astralv2.world.DungeonGenerationService;
import dev.astralv2.world.WorldAnomalyService;
import dev.astralv2.world.WorldEventService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

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
    private static final String SUBCOMMAND_EVENT = "event";
    private static final String SUBCOMMAND_EVENT_REROLL = "event-reroll";
    private static final String SUBCOMMAND_STATSET = "statset";
    private static final String SUBCOMMAND_STATADD = "statadd";
    private static final String SUBCOMMAND_STATRESET = "statreset";
    private static final String SUBCOMMAND_LEADERBOARD = "leaderboard";
    private static final String ADMIN_PERMISSION = "astral.admin";
    private static final List<String> STAT_KEYS = List.of("atk", "def", "maxhp", "critchance", "critdamage");

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
            return handleStats(sender, args);
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
        if (SUBCOMMAND_STATSET.equals(subCommand)) {
            return handleStatEdit(sender, args, false);
        }
        if (SUBCOMMAND_STATADD.equals(subCommand)) {
            return handleStatEdit(sender, args, true);
        }
        if (SUBCOMMAND_STATRESET.equals(subCommand)) {
            return handleStatReset(sender, args);
        }
        if (SUBCOMMAND_LEADERBOARD.equals(subCommand)) {
            return handleLeaderboard(sender, args);
        }

        sendUsage(sender, label);
        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "コンソールは対象プレイヤーを指定してください: /astral stats <player>");
                return true;
            }
            sendStats(sender, player.getName(), playerStatsService.getOrCreate(player.getUniqueId()));
            return true;
        }

        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "他プレイヤーのステータス閲覧権限がありません。");
            return true;
        }

        UUID targetId = resolvePlayerId(args[1]);
        if (targetId == null) {
            sender.sendMessage(ChatColor.RED + "対象プレイヤーが見つかりません: " + args[1]);
            return true;
        }

        sendStats(sender, args[1], playerStatsService.getOrCreate(targetId));
        return true;
    }

    private void sendStats(CommandSender sender, String playerName, PlayerStats stats) {
        sender.sendMessage(ChatColor.GOLD + "=== Astral Stats: " + playerName + " ===");
        sender.sendMessage(ChatColor.YELLOW + "ATK: " + ChatColor.WHITE + stats.attack());
        sender.sendMessage(ChatColor.YELLOW + "DEF: " + ChatColor.WHITE + stats.defense());
        sender.sendMessage(ChatColor.YELLOW + "MAX HP: " + ChatColor.WHITE + stats.maxHealth());
        sender.sendMessage(ChatColor.YELLOW + "CRIT CHANCE: " + ChatColor.WHITE + String.format("%.2f%%", stats.critChance() * 100.0));
        sender.sendMessage(ChatColor.YELLOW + "CRIT DAMAGE: " + ChatColor.WHITE + String.format("%.2fx", stats.critDamage()));
    }

    private boolean handleStatEdit(CommandSender sender, String[] args, boolean isAddMode) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /astral " + (isAddMode ? "statadd" : "statset")
                + " <player> <atk|def|maxhp|critchance|critdamage> <value>");
            return true;
        }

        UUID targetId = resolvePlayerId(args[1]);
        if (targetId == null) {
            sender.sendMessage(ChatColor.RED + "対象プレイヤーが見つかりません: " + args[1]);
            return true;
        }

        String statName = args[2].toLowerCase(Locale.ROOT);
        double value;
        try {
            value = Double.parseDouble(args[3]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + "値は数値で指定してください。");
            return true;
        }

        PlayerStats current = playerStatsService.getOrCreate(targetId);
        PlayerStats updated = applyStatChange(current, statName, value, isAddMode);
        if (updated == null) {
            sender.sendMessage(ChatColor.RED + "不明なステータスです。atk/def/maxhp/critchance/critdamage から選んでください。");
            return true;
        }

        playerStatsService.set(targetId, updated);
        sender.sendMessage(ChatColor.GREEN + "更新完了: " + ChatColor.WHITE + args[1] + " の " + statName
            + ChatColor.GREEN + " を " + (isAddMode ? "加算" : "設定") + "しました。入力値=" + value);
        return true;
    }

    private PlayerStats applyStatChange(PlayerStats current, String statName, double value, boolean isAddMode) {
        DoubleUnaryOperator transform = isAddMode ? (original -> original + value) : (original -> value);
        switch (statName) {
            case "atk":
                return current.withAttack(Math.max(0.0, transform.applyAsDouble(current.attack())));
            case "def":
                return current.withDefense(Math.max(0.0, transform.applyAsDouble(current.defense())));
            case "maxhp":
                return current.withMaxHealth(Math.max(1.0, transform.applyAsDouble(current.maxHealth())));
            case "critchance":
                return current.withCritChance(Math.max(0.0, Math.min(1.0, transform.applyAsDouble(current.critChance()))));
            case "critdamage":
                return current.withCritDamage(Math.max(1.0, transform.applyAsDouble(current.critDamage())));
            default:
                return null;
        }
    }

    private boolean handleStatReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /astral statreset <player|all>");
            return true;
        }

        if ("all".equalsIgnoreCase(args[1])) {
            int count = 0;
            for (UUID playerId : playerStatsService.snapshot().keySet()) {
                playerStatsService.set(playerId, PlayerStats.DEFAULT);
                count++;
            }
            sender.sendMessage(ChatColor.GREEN + "全プレイヤーのステータスをデフォルトにリセットしました。対象: " + count + " 人");
            return true;
        }

        UUID targetId = resolvePlayerId(args[1]);
        if (targetId == null) {
            sender.sendMessage(ChatColor.RED + "対象プレイヤーが見つかりません: " + args[1]);
            return true;
        }

        playerStatsService.set(targetId, PlayerStats.DEFAULT);
        sender.sendMessage(ChatColor.GREEN + args[1] + " のステータスをデフォルト値にリセットしました。");
        return true;
    }

    private boolean handleLeaderboard(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /astral leaderboard <atk|def|maxhp|critchance|critdamage> [top]");
            return true;
        }

        String stat = args[1].toLowerCase(Locale.ROOT);
        if (!STAT_KEYS.contains(stat)) {
            sender.sendMessage(ChatColor.RED + "不明なステータスです。atk/def/maxhp/critchance/critdamage から選んでください。");
            return true;
        }

        int top = 10;
        if (args.length >= 3) {
            try {
                top = Math.max(1, Math.min(50, Integer.parseInt(args[2])));
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "top は数値で指定してください。");
                return true;
            }
        }

        List<StatsEntry> entries = playerStatsService.snapshot().entrySet().stream()
            .map(entry -> new StatsEntry(resolveName(entry.getKey()), extractStat(entry.getValue(), stat)))
            .sorted(Comparator.comparingDouble(StatsEntry::value).reversed())
            .limit(top)
            .toList();

        if (entries.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "ランキング対象データがありません。");
            return true;
        }

        sender.sendMessage(ChatColor.AQUA + "=== " + stat.toUpperCase(Locale.ROOT) + " Leaderboard Top " + top + " ===");
        for (int i = 0; i < entries.size(); i++) {
            StatsEntry entry = entries.get(i);
            sender.sendMessage(ChatColor.YELLOW + "#" + (i + 1) + " " + ChatColor.WHITE + entry.name()
                + ChatColor.GRAY + " - " + ChatColor.GREEN + String.format(Locale.ROOT, "%.3f", entry.value()));
        }
        return true;
    }

    private double extractStat(PlayerStats stats, String statKey) {
        return switch (statKey) {
            case "atk" -> stats.attack();
            case "def" -> stats.defense();
            case "maxhp" -> stats.maxHealth();
            case "critchance" -> stats.critChance();
            case "critdamage" -> stats.critDamage();
            default -> 0.0;
        };
    }

    private UUID resolvePlayerId(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online.getUniqueId();
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.getName() == null || offlinePlayer.getUniqueId() == null) {
            return null;
        }
        return offlinePlayer.getUniqueId();
    }

    private String resolveName(UUID playerId) {
        Player online = Bukkit.getPlayer(playerId);
        if (online != null) {
            return online.getName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        return offlinePlayer.getName() == null ? playerId.toString() : offlinePlayer.getName();
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
                SUBCOMMAND_ANOMALY,
                SUBCOMMAND_DUNGEON,
                SUBCOMMAND_EVENT,
                SUBCOMMAND_LEADERBOARD
            ));

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                candidates.add(SUBCOMMAND_GIVECORE);
                candidates.add(SUBCOMMAND_ANOMALY_REROLL);
                candidates.add(SUBCOMMAND_DUNGEON_REROLL);
                candidates.add(SUBCOMMAND_EVENT_REROLL);
                candidates.add(SUBCOMMAND_STATSET);
                candidates.add(SUBCOMMAND_STATADD);
                candidates.add(SUBCOMMAND_STATRESET);
            }

            return completePrefix(candidates, args[0]);
        }

        if (args.length == 2 && (SUBCOMMAND_STATSET.equalsIgnoreCase(args[0])
            || SUBCOMMAND_STATADD.equalsIgnoreCase(args[0])
            || SUBCOMMAND_STATRESET.equalsIgnoreCase(args[0])
            || SUBCOMMAND_STATS.equalsIgnoreCase(args[0]))) {
            return completeOnlinePlayerNames(args[1]);
        }

        if (args.length == 3 && (SUBCOMMAND_STATSET.equalsIgnoreCase(args[0]) || SUBCOMMAND_STATADD.equalsIgnoreCase(args[0]))) {
            return completePrefix(STAT_KEYS, args[2]);
        }

        if (args.length == 2 && SUBCOMMAND_LEADERBOARD.equalsIgnoreCase(args[0])) {
            return completePrefix(STAT_KEYS, args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> completeOnlinePlayerNames(String typed) {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        names.add("all");
        return completePrefix(names, typed);
    }

    private List<String> completePrefix(List<String> candidates, String rawInput) {
        String typed = rawInput.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase(Locale.ROOT).startsWith(typed)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.YELLOW
            + "Usage: /" + label + " <stats|anomaly|dungeon|event|leaderboard>");
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.GRAY
                + "Admin: /" + label + " <givecore|anomaly-reroll|dungeon-reroll|event-reroll|statset|statadd|statreset>");
            sender.sendMessage(ChatColor.GRAY
                + "Admin Example: /" + label + " statset Steve atk 120");
        }
    }

    private record StatsEntry(String name, double value) {
    }
}
