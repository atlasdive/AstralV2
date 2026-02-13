package dev.astralv2.combat;

import dev.astralv2.stats.PlayerStats;

/**
 * ダメージインフレ対応の最小計算器。
 * まずは通常ダメージのみを扱い、将来クリティカル・属性へ拡張する。
 */
public final class DamageCalculator {

    private static final double BASE_MULTIPLIER = 1.35;
    private static final double DEFENSE_SCALING = 0.06;
    private static final double MIN_DAMAGE = 1.0;

    public double calculate(PlayerStats attackerStats, double targetDefense) {
        double inflatedAttack = attackerStats.attack() * BASE_MULTIPLIER;
        double defenseMitigation = Math.max(0.0, 1.0 - (targetDefense * DEFENSE_SCALING / 100.0));
        double result = inflatedAttack * defenseMitigation;
        return Math.max(MIN_DAMAGE, result);
    }
}
