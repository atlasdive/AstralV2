package dev.astralv2;

import org.bukkit.plugin.java.JavaPlugin;

public final class AstralPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("AstralV2 plugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstralV2 plugin disabled.");
    }
}
