package com.goopey.voidsentflame.datagen;

import java.util.List;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

public class ModRecipeProvider extends RecipeProvider {
  protected ModRecipeProvider(Provider registries, RecipeOutput output) {
    super(registries, output);
  }

  @Override
  protected void buildRecipes() {
    // Copied over from Kaupenjoe's datagen tutorial
    // Kept here as a blueprint of possible recipes to implement
    // TODO : Add recipes

    // List<ItemLike> BISMUTH_SMELTABLES = List.of(ModItems.RAW_BISMUTH, ModBlocks.BISMUTH_ORE, ModBlocks.BISMUTH_DEEPSLATE_ORE);

    // ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BISMUTH_BLOCK.get())
    //   .pattern("BBB")
    //   .pattern("BBB")
    //   .pattern("BBB")
    //   .define('B', ModItems.BISMUTH.get())
    //   .unlockedBy("has_bismuth", has(ModItems.BISMUTH)).save(recipeOutput);

    // ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BISMUTH.get(), 9)
    //   .requires(ModBlocks.BISMUTH_BLOCK)
    //   .unlockedBy("has_bismuth_block", has(ModBlocks.BISMUTH_BLOCK)).save(recipeOutput);

    // ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BISMUTH.get(), 18)
    //   .requires(ModBlocks.MAGIC_BLOCK)
    //   .unlockedBy("has_magic_block", has(ModBlocks.MAGIC_BLOCK))
    //   .save(this.output, "tutorialmod:bismuth_from_magic_block");

    // oreSmelting(this.output, BISMUTH_SMELTABLES, RecipeCategory.MISC, ModItems.BISMUTH.get(), 0.25f, 200, "bismuth");
    // oreBlasting(this.output, BISMUTH_SMELTABLES, RecipeCategory.MISC, ModItems.BISMUTH.get(), 0.25f, 100, "bismuth");
  }

  protected void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
      oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
  }

  protected void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
    oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
  }

  protected <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
    for(ItemLike itemlike : pIngredients) {
      SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
        .save(recipeOutput, VoidsentFlameMod.MODID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
    }
  }

  /**
   * Used to construct and run a new instance of the ModRecipeProvider
   */
  public static class Run extends RecipeProvider.Runner {
    public Run(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
      super(output, provider);
    }

    @Override
    protected RecipeProvider createRecipeProvider(@Nonnull HolderLookup.Provider provider, @Nonnull RecipeOutput recipeOutput) {
      return new ModRecipeProvider(provider, recipeOutput);
    }

    @Override
    public String getName() {
      return "Voidsent Flame Recipes";
    }
  }
}
