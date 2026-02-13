package dev.astralv2.combat;

import dev.astralv2.stats.PlayerStats;
import dev.astralv2.stats.PlayerStatsService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * 戦闘の最小イベントハンドラ。
 * - PvPは完全無効
 * - プレイヤー -> Mob のダメージへ独自計算を適用
 */
public final class CombatListener implements Listener {

    private final PlayerStatsService playerStatsService;
    private final DamageCalculator damageCalculator;

    public CombatListener(PlayerStatsService playerStatsService, DamageCalculator damageCalculator) {
        this.playerStatsService = playerStatsService;
        this.damageCalculator = damageCalculator;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        if (!(damager instanceof Player player)) {
            return;
        }

        if (target instanceof Player) {
            event.setCancelled(true);
            return;
        }

        if (!(target instanceof LivingEntity)) {
            return;
        }

        PlayerStats attackerStats = playerStatsService.getOrCreate(player.getUniqueId());
        double customDamage = damageCalculator.calculate(attackerStats, 0.0);
        event.setDamage(customDamage);
    }
}
