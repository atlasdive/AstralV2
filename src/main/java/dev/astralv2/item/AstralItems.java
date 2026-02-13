package dev.astralv2.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * サーバー独自アイテムの生成責務。
 */
public final class AstralItems {

    public static final String ASTRAL_CORE_ID = "astral_core";

    private final NamespacedKey itemIdKey;

    public AstralItems(JavaPlugin plugin) {
        this.itemIdKey = new NamespacedKey(plugin, "item_id");
    }

    public ItemStack createAstralCore() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Astral Core");
        meta.setLore(List.of(
            ChatColor.GRAY + "未知のエネルギーが凝縮された核。",
            ChatColor.DARK_GRAY + "Astral専用素材"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, ASTRAL_CORE_ID);
        item.setItemMeta(meta);
        return item;
    }
}
