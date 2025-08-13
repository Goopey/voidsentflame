package com.goopey.voidsentflame.util;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class NameUtility {
  /**
   * This method takes in an Item and extracts its name to then format
   * it appropriately for registry stuff.
   * 
   * @param item the item whose name we will format
   * @return the item name as a string
   */
  public static String getItemName(Item item) {
    return BuiltInRegistries.ITEM.getKey(item)
      .toString().replace(VoidsentFlameMod.MODID + ":", "");
  }

  /**
   * This method takes in a Block and extracts its name to then format 
   * it appropriately for registry stuff.
   * 
   * @param block the block whose name we will format
   * @return the block name as a string
   */
  public static String getBlockName(Block block) {
    return BuiltInRegistries.BLOCK.getKey(block)
      .toString().replace(VoidsentFlameMod.MODID + ":", "");
  }
}
