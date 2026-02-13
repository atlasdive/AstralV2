package dev.astralv2.command;

import dev.astralv2.stats.PlayerStats;
import dev.astralv2.stats.PlayerStatsService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * /astral コマンド（初期実装）
 * - /astral stats : 自分のステータス表示
 */
public final class AstralCommand implements TabExecutor {

    private static final String SUBCOMMAND_STATS = "stats";

    private final PlayerStatsService playerStatsService;

    public AstralCommand(PlayerStatsService playerStatsService) {
        this.playerStatsService = playerStatsService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        if (!SUBCOMMAND_STATS.equalsIgnoreCase(args[0])) {
            sendUsage(sender, label);
            return true;
        }

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMAND_STATS.startsWith(args[0].toLowerCase())
                ? List.of(SUBCOMMAND_STATS)
                : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " stats");
    }
}
