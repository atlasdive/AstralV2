package dev.astralv2.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 独自レシピ登録。
 */
public final class AstralRecipeRegistrar {

    private final JavaPlugin plugin;
    private final AstralItems astralItems;

    public AstralRecipeRegistrar(JavaPlugin plugin, AstralItems astralItems) {
        this.plugin = plugin;
        this.astralItems = astralItems;
    }

    public void registerAll() {
        registerAstralCoreRecipe();
    }

    private void registerAstralCoreRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "astral_core_recipe");
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, astralItems.createAstralCore());
        recipe.shape("DED", "ENE", "DED");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('E', Material.ENDER_EYE);
        recipe.setIngredient('N', Material.NETHER_STAR);

        Bukkit.addRecipe(recipe);
    }
}
