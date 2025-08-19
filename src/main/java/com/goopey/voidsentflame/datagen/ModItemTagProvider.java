package com.goopey.voidsentflame.datagen;

import java.util.concurrent.CompletableFuture;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

public class ModItemTagProvider extends ItemTagsProvider {
  public ModItemTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
    super(output, lookupProvider, VoidsentFlameMod.MODID);
  }

  @Override
  protected void addTags(Provider provider) {
    // TODO: Add Item Tags
  }
}
