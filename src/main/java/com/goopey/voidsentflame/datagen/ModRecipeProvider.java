package com.goopey.voidsentflame.datagen;

import java.util.List;

import com.goopey.voidsentflame.VoidsentFlameMod;

import com.goopey.voidsentflame.core.init.BlockInit;
import com.goopey.voidsentflame.core.init.ItemInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

public class ModRecipeProvider extends RecipeProvider {
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

  protected ModRecipeProvider(Provider registries, RecipeOutput output) {
    super(registries, output);
  }

  @Override
  protected void buildRecipes() {
    // Copied over from Kaupenjoe's datagen tutorial
    // Kept here as a blueprint of possible recipes to implement

    List<ItemLike> SCRAP_SMELTABLES = List.of(BlockInit.IRON_SCRAP_BLOCK);

    shapeless(RecipeCategory.MISC, ItemInit.CLAYISH_DUST_BALL, 4)
      .requires(BlockInit.CLAYISH_DUST_BLOCK)
      .unlockedBy(getHasName(BlockInit.CLAYISH_DUST_BLOCK), this.has(BlockInit.CLAYISH_DUST_BLOCK))
      .save(output);

    shaped(RecipeCategory.MISC, BlockInit.CLAYISH_DUST_BLOCK, 1)
      .define('#', ItemInit.CLAYISH_DUST_BALL)
      .pattern("##")
      .pattern("##")
      .unlockedBy(getHasName(ItemInit.CLAYISH_DUST_BALL), this.has(ItemInit.CLAYISH_DUST_BALL))
      .save(this.output);

    oreSmelting(this.output, SCRAP_SMELTABLES, RecipeCategory.MISC, Items.IRON_NUGGET, 5,0.25f, 200, "iron_scrap");
    oreBlasting(this.output, SCRAP_SMELTABLES, RecipeCategory.MISC, Items.IRON_NUGGET, 6,0.25f, 100, "iron_scrap");
    oreSmelting(this.output, List.of(ItemInit.CLAYISH_DUST_BALL), RecipeCategory.DECORATIONS, Items.BRICK, 0.3f, 200, "dust_brick");
    oreSmelting(this.output, List.of(BlockInit.CLAYISH_DUST_BLOCK), RecipeCategory.DECORATIONS, Blocks.MAGENTA_TERRACOTTA, 0.3f, 200, "dust_terracotta");
  }

  //#######################################################
  //                  HELPER FUNCTIONS
  //#######################################################

  // basic smelting
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

  // smelting with multiples of a result
  protected void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, int numResult, float pExperience, int pCookingTIme, String pGroup) {
    oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult, numResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
  }

  protected void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, int numResult, float pExperience, int pCookingTime, String pGroup) {
    oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult, numResult, pExperience, pCookingTime, pGroup, "_from_blasting");
  }

  protected <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, int numResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
    for(ItemLike itemlike : pIngredients) {
      ItemStack resultStack = pResult.asItem().getDefaultInstance();
      resultStack.setCount(numResult);
      SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, resultStack, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
        .save(recipeOutput, VoidsentFlameMod.MODID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
    }
  }
}
