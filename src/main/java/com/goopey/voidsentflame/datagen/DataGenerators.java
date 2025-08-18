package com.goopey.voidsentflame.datagen;

import java.util.concurrent.CompletableFuture;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataGenerators {
  public static void gatherDataClient(GatherDataEvent.Client event) {
    try {
      DataGenerator generator = event.getGenerator();
      PackOutput packOutput = generator.getPackOutput();
      CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

      generator.addProvider(true, new ModModelProvider(packOutput));
    } catch(RuntimeException e) {
      VoidsentFlameMod.LOGGER.error("Failed to generate data", e);
    }
  }

  public static void gatherDataServer(GatherDataEvent.Server event) {
    try {
      DataGenerator generator = event.getGenerator();
      PackOutput packOutput = generator.getPackOutput();
      CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

      generator.addProvider(true, new LootTableProvider(packOutput, Collections.emptySet(),
              List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
      // generator.addProvider(true, new ModRecipeProvider(packOutput, lookupProvider));

      // BlockTagsProvider blockTagsProvider = new ModBlockTagProvider(packOutput, lookupProvider);
      // generator.addProvider(true, blockTagsProvider);
      // generator.addProvider(true, new ModItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter()));

      // generator.addProvider(true, new ModDataMapProvider(packOutput, lookupProvider));

      // generator.addProvider(true, new ModDatapackProvider(packOutput, lookupProvider));
    } catch(RuntimeException e) {
      VoidsentFlameMod.LOGGER.error("Failed to generate data", e);
    }
  }
}
