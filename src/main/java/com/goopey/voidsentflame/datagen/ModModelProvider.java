package com.goopey.voidsentflame.datagen;

import javax.annotation.Nonnull;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.init.BlockInit;
import com.goopey.voidsentflame.core.init.ItemInit;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

public class ModModelProvider extends ModelProvider {
  public ModModelProvider(PackOutput output) {
    super(output, VoidsentFlameMod.MODID);
  }

  protected void registerModels(@Nonnull BlockModelGenerators blockModels, @Nonnull ItemModelGenerators itemModels) {
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