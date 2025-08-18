package com.goopey.voidsentflame.datagen;

import java.util.Map;
import java.util.Set;

import com.goopey.voidsentflame.core.init.BlockInit;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
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
    dropSelf(BlockInit.VOID_STONE.get());
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
