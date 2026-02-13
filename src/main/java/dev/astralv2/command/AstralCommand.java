package dev.astralv2.command;

import dev.astralv2.item.AstralItems;
import dev.astralv2.stats.PlayerStats;
import dev.astralv2.stats.PlayerStatsService;
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
 * - /astral stats : 自分のステータス表示
 * - /astral givecore : Astral Coreを受け取る（管理者向けデバッグ）
 */
public final class AstralCommand implements TabExecutor {

    private static final String SUBCOMMAND_STATS = "stats";
    private static final String SUBCOMMAND_GIVECORE = "givecore";

    private final PlayerStatsService playerStatsService;
    private final AstralItems astralItems;

    public AstralCommand(PlayerStatsService playerStatsService, AstralItems astralItems) {
        this.playerStatsService = playerStatsService;
        this.astralItems = astralItems;
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

        if (!player.hasPermission("astral.admin")) {
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
            List<String> candidates = List.of(SUBCOMMAND_STATS, SUBCOMMAND_GIVECORE);
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
        sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <stats|givecore>");
    }
}
