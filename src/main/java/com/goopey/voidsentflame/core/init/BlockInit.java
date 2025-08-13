package com.goopey.voidsentflame.core.init;

import java.util.function.Supplier;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockInit {
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VoidsentFlameMod.MODID);

  public static final DeferredBlock<Block> VOID_STONE = registerBlock("void_stone", () -> new Block(Block.Properties.ofFullCopy(Blocks.STONE)));

  /**
   * Used to register a Block and its BlockItem counterpart
   * 
   * @param name the name of the block
   * @param block the block it's gonna be
   */
  public static DeferredBlock<Block> registerBlock(String name, Supplier<Block> block) {
    DeferredBlock<Block> blockReg = BLOCKS.register(name, block);
    ItemInit.ITEMS.register(name, () -> new BlockItem(blockReg.get(), new Item.Properties()));

    return blockReg;
  }
}