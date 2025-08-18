package com.goopey.voidsentflame.datagen;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.init.BlockInit;
import com.goopey.voidsentflame.core.init.ItemInit;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModModelProvider extends ModelProvider {
  public ModModelProvider(PackOutput output) {
    super(output, VoidsentFlameMod.MODID);
  }

  protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    createBlocks(blockModels);
    createItems(itemModels);
  }

  private void createBlocks(BlockModelGenerators pBModel) {
    pBModel.createTrivialCube(BlockInit.VOID_STONE.get());
  }

  private void createItems(ItemModelGenerators pIModels) {
    pIModels.generateFlatItem(ItemInit.RUNIC_FRUIT.get(), ModelTemplates.FLAT_ITEM);
  }

}