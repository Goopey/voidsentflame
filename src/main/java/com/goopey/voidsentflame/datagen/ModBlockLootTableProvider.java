package com.goopey.voidsentflame.datagen;

import java.util.Set;

import com.goopey.voidsentflame.core.init.BlockInit;

import com.goopey.voidsentflame.core.init.ItemInit;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
  protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
    super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
  }

  @Override
  protected void generate() {
    // blocks which drop themselves
    dropSelf(BlockInit.VOID_STONE_BLOCK.get());
    dropSelf(BlockInit.VOID_DUST_BLOCK.get());
    dropSelf(BlockInit.IRON_SCRAP_BLOCK.get());

    // extra drops
    add(BlockInit.SPARKLING_DUST_BLOCK.get(), dropOtherAndMultipleOthers(BlockInit.SPARKLING_DUST_BLOCK.get(), BlockInit.VOID_DUST_BLOCK.asItem(), ItemInit.MINERAL_DUST.get(), 2, 6));
  }

  protected LootTable.Builder dropOtherAndMultipleOthers(Block block, Item itemSelf, Item itemOther, float minDrops, float maxDrops) {
    HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
    return this.createSilkTouchDispatchTable(
      block,
      NestedLootTable.inlineLootTable(
        LootTable.lootTable()
          .withPool(LootPool.lootPool().add(LootItem.lootTableItem(itemSelf)))
          .withPool(LootPool.lootPool().add(
            LootItem.lootTableItem(itemOther)
              .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))
              .apply(ApplyBonusCount.addOreBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE)))
          ))
          .build()
      ));
  }

  protected LootTable.Builder createMultipleDrops(Block pBlock, Item pItem, float minDrops, float maxDrops) {
    HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
    return this.createSilkTouchDispatchTable(pBlock, 
      this.applyExplosionDecay(pBlock, LootItem.lootTableItem(pItem)
        .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))
        .apply(ApplyBonusCount.addOreBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE)))
        ));
  }

  @Override
  protected Iterable<Block> getKnownBlocks() {
    return BlockInit.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
  }
}
