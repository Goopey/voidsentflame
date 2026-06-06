package com.goopey.voidsentflame.core.init;

import java.util.function.Function;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.*;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockInit {
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VoidsentFlameMod.MODID);

  public static final DeferredHolder<Block, Block> VOID_STONE_BLOCK = 
    register("void_stone_block", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5F, 30F).sound(SoundType.STONE).requiresCorrectToolForDrops());

  public static final DeferredBlock<Block> VOID_DUST_BLOCK =
    register("void_dust_block", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.75F, 5F).sound(SoundType.CALCITE));

  public static final DeferredBlock<RubiconPortalBlock> RUBICON_PORTAL_BLOCK = 
    register("rubicon_portal_block", RubiconPortalBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).noLootTable());

  public static final DeferredBlock<RubiconAirBlock> RUBICON_AIR_BLOCK =
    register("rubicon_air_block", RubiconAirBlock::new,
    BlockBehaviour.Properties.of().sound(SoundType.EMPTY).strength(-1, 3600000).noCollision().noOcclusion().isRedstoneConductor((bs, br, bp) -> false).replaceable().instrument(NoteBlockInstrument.WITHER_SKELETON).noLootTable());

  public static final DeferredBlock<VoidFluidBlock> VOID_FLUID_BLOCK =
    register("void_fluid", VoidFluidBlock::new, BlockBehaviour.Properties.of());

  public static final DeferredBlock<Block> VOID_SEA_LAYER_BLOCK =
    register("void_sea_layer_block", VoidSeaLayerBlock::new, BlockBehaviour.Properties.of());

//  public static final DeferredBlock<VoidsentFlameBlock> VOIDSENT_FLAME_BLOCK =
//    register("voidsent_flame_block", VoidsentFlameBlock::new, BlockBehaviour.Properties.of());

  public static final DeferredBlock<Block> CLAYISH_DUST_BLOCK =
    register("clayish_dust_block", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).strength(1.5f, 10f).sound(SoundType.SAND));

  public static final DeferredBlock<Block> IRON_SCRAP_BLOCK =
    register("iron_scrap_block", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(7f, 40f).sound(SoundType.IRON).requiresCorrectToolForDrops());

  //####################################################
  //                  HELPER METHODS
  //####################################################

  /**
   * Default function used to register a block and its item counterpart.
   * This particular variant creates blockItems without any particular properties.
   * 
   * @param <T> type of block that is registered (can be regular Block, SlabBlock, StairBlock, etc...)
   * @param name the name/id of the block that will be registered - it better be unique
   * @param block a function which maps the properties of a block to the new block T
   * @param blockProperties the properties of the new block
   * 
   * @return returns a DeferredBlock<T> to place in the registry
   */
  private static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties blockProperties) {
    return register(name, block, blockProperties, new Item.Properties());
  }

  /**
   * Advanced function used to register a block and its item counterpart.
   * This particular variant creates blockItems and can specify their properties as an item.
   * 
   * @param <T> type of block that is registered (can be regular Block, SlabBlock, StairBlock, etc...)
   * @param name the name/id of the block that will be registered - it better be unique
   * @param block a function which maps the properties of a block to the new block T
   * @param blockProperties the properties of the new block
   * @param itemProperties the properties of the itemBlock
   * 
   * @return returns a DeferredBlock<T> to place in the registry
   */
  private static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties blockProperties, Item.Properties itemProperties) {
    DeferredBlock<T> registryObject = registerWithoutItem(name, block, blockProperties);
    ItemInit.ITEMS.registerItem(name, iProperties -> new BlockItem(registryObject.get(), iProperties), () -> itemProperties);
    return registryObject;
  }

  private static <T extends Block> DeferredBlock<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties) {
    return BLOCKS.registerBlock(name, block, () -> properties);
  }
}