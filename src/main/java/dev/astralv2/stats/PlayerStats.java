package dev.astralv2.stats;

/**
 * Astral RPGの基本ステータス値。
 * まずは基盤段階のため、純粋なデータ構造として扱う。
 */
public record PlayerStats(
    double attack,
    double defense,
    double maxHealth,
    double critChance,
    double critDamage
) {

    public static final PlayerStats DEFAULT = new PlayerStats(
        10.0,
        5.0,
        20.0,
        0.05,
        1.50
    );

    public PlayerStats withAttack(double value) {
        return new PlayerStats(value, defense, maxHealth, critChance, critDamage);
    }

    public PlayerStats withDefense(double value) {
        return new PlayerStats(attack, value, maxHealth, critChance, critDamage);
    }

    public PlayerStats withMaxHealth(double value) {
        return new PlayerStats(attack, defense, value, critChance, critDamage);
    }

    public PlayerStats withCritChance(double value) {
        return new PlayerStats(attack, defense, maxHealth, value, critDamage);
    }

    public PlayerStats withCritDamage(double value) {
        return new PlayerStats(attack, defense, maxHealth, critChance, value);
    }
}
