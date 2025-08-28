package com.goopey.voidsentflame.datagen;

import javax.annotation.Nonnull;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.init.BlockInit;
import com.goopey.voidsentflame.core.init.ItemInit;
import com.mojang.math.Quadrant;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction.Axis;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ModModelProvider extends ModelProvider {
  public ModModelProvider(PackOutput output) {
    super(output, VoidsentFlameMod.MODID);
  }

  protected void registerModels(@Nonnull BlockModelGenerators blockModels, @Nonnull ItemModelGenerators itemModels) {
    createBlocks(blockModels);
    createItems(itemModels);
  }

  private void createBlocks(BlockModelGenerators pBModel) {
    createPortalBlocks(pBModel);
    pBModel.createTrivialCube(BlockInit.VOID_STONE_BLOCK.get());
    pBModel.createAirLikeBlock(BlockInit.RUBICON_AIR_BLOCK.get(), BlockInit.RUBICON_AIR_BLOCK.asItem());
    pBModel.createAirLikeBlock(BlockInit.VOID_FLUID_BLOCK.get(), BlockInit.VOID_FLUID_BLOCK.asItem());
  }

  private void createItems(ItemModelGenerators pIModels) {
    pIModels.generateFlatItem(ItemInit.RUNIC_FRUIT_ITEM.get(), ModelTemplates.FLAT_ITEM);
    pIModels.generateFlatItem(ItemInit.RUBICON_IGNITER_ITEM.get(), ModelTemplates.FLAT_ITEM);
    pIModels.generateFlatItem(ItemInit.VOID_FLUID_BUCKET.get(), ModelTemplates.FLAT_ITEM);
  }

  /**
   * ##################################################################
   *                    BLOCK EXTRA MODEL METHODS
   * ##################################################################
   */

  private void createPortalBlocks(BlockModelGenerators pBModel) {
    createPortalBlock(pBModel, BlockInit.RUBICON_PORTAL_BLOCK.get());
  }

  private void createFluidBlocks(BlockModelGenerators pBModel) {

  }

  /**
   * ######################################################
   *                    MODEL METHODS
   * ######################################################
   */

  private void createPortalBlock(BlockModelGenerators pBModel, NetherPortalBlock block) {
    pBModel.blockStateOutput.accept(
      MultiVariantGenerator
        .dispatch(block)
        .with(PropertyDispatch
          .initial(BlockStateProperties.HORIZONTAL_AXIS)
          .select(
            Axis.X, 
            plainVariant(ModelLocationUtils.getModelLocation(block, "_ns"))
          ).select(
            Axis.Z, 
            plainVariant(ModelLocationUtils.getModelLocation(block, "_ew"))
          )));
  }

  /**
   * ######################################################
   *                    HELPER METHODS
   * ######################################################
   */

  public static MultiVariant plainVariant(ResourceLocation id) {
    return BlockModelGenerators.variant(BlockModelGenerators.plainModel(id));
  }
}