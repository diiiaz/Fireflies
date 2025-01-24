package io.github.diiiaz.fireflies.datagen;

import io.github.diiiaz.fireflies.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {


    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                registries.getOrThrow(RegistryKeys.ITEM);
                createShaped(RecipeCategory.TOOLS, ModItems.CATCHING_NET, 1)
                        .pattern(" SS")
                        .pattern(" SS")
                        .pattern("T  ")
                        .input('S', Items.STRING)
                        .input('T', Items.STICK)
                        .criterion("has_string", this.conditionsFromItem(Items.STRING))
                        .offerTo(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "FirefliesRecipeProvider";
    }
}
