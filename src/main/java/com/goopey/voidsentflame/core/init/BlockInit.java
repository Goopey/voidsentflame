package com.goopey.voidsentflame.core.init;

import java.util.function.Function;
import java.util.function.Supplier;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockInit {
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VoidsentFlameMod.MODID);

  public static final DeferredHolder<Block, Block> VOID_STONE = 
    register("void_stone", blockProperties -> new Block(blockProperties), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.5F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops());

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
    ItemInit.ITEMS.registerItem(name, iProperties -> new BlockItem(registryObject.get(), iProperties), itemProperties);
    return registryObject;
  }

  private static <T extends Block> DeferredBlock<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> block, BlockBehaviour.Properties properties) {
    return BLOCKS.registerBlock(name, block, properties);
  }
}