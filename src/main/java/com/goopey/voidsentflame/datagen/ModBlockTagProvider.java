package com.goopey.voidsentflame.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public class ModBlockTagProvider extends BlockTagsProvider {
  public ModBlockTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, String modId) {
    super(output, lookupProvider, modId);
    //TODO Auto-generated constructor stub
  }

  @Override
  protected void addTags(Provider arg0) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addTags'");
  }
}
