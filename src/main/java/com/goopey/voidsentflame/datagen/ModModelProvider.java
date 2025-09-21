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
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ModModelProvider extends ModelProvider {
  public static final VariantMutator X_ROT_90 = VariantMutator.X_ROT.withValue(Quadrant.R90);
  public static final VariantMutator Y_ROT_90 = VariantMutator.Y_ROT.withValue(Quadrant.R90);
  public static final VariantMutator Y_ROT_180 = VariantMutator.Y_ROT.withValue(Quadrant.R180);

  public ModModelProvider(PackOutput output) {
    super(output, VoidsentFlameMod.MODID);
  }

  protected void registerModels(@Nonnull BlockModelGenerators blockModels, @Nonnull ItemModelGenerators itemModels) {
    createBlocks(blockModels);
    createItems(itemModels);
  }

  private static void createBlocks(BlockModelGenerators pBModel) {
    createPortalBlocks(pBModel);
    createXYRandomOrientationBlocks(pBModel);

    pBModel.createAirLikeBlock(BlockInit.RUBICON_AIR_BLOCK.get(), BlockInit.RUBICON_AIR_BLOCK.asItem());
    pBModel.createNonTemplateModelBlock(BlockInit.VOID_FLUID_BLOCK.get());
  }

  private static void createItems(ItemModelGenerators pIModels) {
    pIModels.generateFlatItem(ItemInit.RUNIC_FRUIT_ITEM.get(), ModelTemplates.FLAT_ITEM);
    pIModels.generateFlatItem(ItemInit.RUBICON_IGNITER_ITEM.get(), ModelTemplates.FLAT_ITEM);
    pIModels.generateFlatItem(ItemInit.VOID_FLUID_BUCKET.get(), ModelTemplates.FLAT_ITEM);
  }

  /**
   * ##################################################################
   *                    BLOCK EXTRA MODEL METHODS
   * ##################################################################
   */

  private static void createPortalBlocks(BlockModelGenerators pBModel) {
    createPortalBlock(pBModel, BlockInit.RUBICON_PORTAL_BLOCK.get());
  }

  private static void createXYRandomOrientationBlocks(BlockModelGenerators pBModel) {
    createXYRandomOrientationBlock(pBModel, BlockInit.VOID_STONE_BLOCK.get());
  }

  // private void createFluidBlocks(BlockModelGenerators pBModel) {
  //   createFluidBlock(pBModel, BlockInit.VOID_FLUID_BLOCK.get());
  // }

  /**
   * ######################################################
   *                    MODEL METHODS
   * ######################################################
   */

  private static void createPortalBlock(BlockModelGenerators pBModel, NetherPortalBlock block) {
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

  private static void createMirrorRandomOrientationBlock(BlockModelGenerators pBModel, Block block) {
    ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);
    ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("voidsentflame", "block/" + blockKey.getPath());
    
    ResourceLocation cubeLoc = TexturedModel.CUBE.updateTemplate(
      template -> template.extend().requiredTextureSlot(TextureSlot.PARTICLE).build()
    ).updateTexture(
      mapping -> mapping.put(TextureSlot.PARTICLE, texture)
    ).create(block, pBModel.modelOutput);
    
    ResourceLocation cubeLocMirrored = TexturedModel.CUBE_MIRRORED.updateTemplate(
      template -> template.extend().requiredTextureSlot(TextureSlot.PARTICLE).build()
    ).updateTexture(
      mapping -> mapping.put(TextureSlot.PARTICLE, texture)
    ).create(block, pBModel.modelOutput);

    Variant variant = plainModel(cubeLoc);
    Variant variant1 = plainModel(cubeLocMirrored);
    
    pBModel.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, createRotatedVariants(variant, variant1)));
    pBModel.registerSimpleItemModel(block, cubeLoc);
  }

  private static void createXYRandomOrientationBlock(BlockModelGenerators pBModel, Block block) {
    ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);
    ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("voidsentflame", "block/" + blockKey.getPath());
    
    ResourceLocation cubeLoc = TexturedModel.CUBE.updateTemplate(
      template -> template.extend().requiredTextureSlot(TextureSlot.PARTICLE).build()
    ).updateTexture(
      mapping -> mapping.put(TextureSlot.PARTICLE, texture)
    ).create(block, pBModel.modelOutput);

    Variant variant = plainModel(cubeLoc);
    pBModel.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, createXYRotatedVariants(variant)));
    pBModel.registerSimpleItemModel(block, cubeLoc);
  }

  /**
   * ######################################################
   *                    HELPER METHODS
   * ######################################################
   */

  public static MultiVariant plainVariant(ResourceLocation id) {
    return BlockModelGenerators.variant(BlockModelGenerators.plainModel(id));
  }

  public static Variant plainModel(ResourceLocation modelLocation) {
    return new Variant(modelLocation);
  }

  public static MultiVariant createRotatedVariants(Variant variant, Variant mirroredVariant) {
    return BlockModelGenerators.variants(variant, mirroredVariant, variant.with(Y_ROT_180), mirroredVariant.with(Y_ROT_180));
  }

  public static MultiVariant createXYRotatedVariants(Variant variant) {
    return BlockModelGenerators.variants(
      variant, 
      variant.with(X_ROT_90), 
      variant.with(Y_ROT_90),
      variant.with(X_ROT_90).with(Y_ROT_90), 
      variant.with(Y_ROT_180),
      variant.with(Y_ROT_180).with(X_ROT_90)
    );
  }
}