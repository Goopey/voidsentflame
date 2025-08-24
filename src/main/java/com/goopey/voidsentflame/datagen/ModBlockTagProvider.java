package com.goopey.voidsentflame.datagen;

import java.util.concurrent.CompletableFuture;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.init.BlockInit;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public class ModBlockTagProvider extends BlockTagsProvider {
  public ModBlockTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
    super(output, lookupProvider, VoidsentFlameMod.MODID);
  }

  @Override
  protected void addTags(Provider provider) {
    tag(BlockTags.MINEABLE_WITH_PICKAXE)
      .add(BlockInit.VOID_STONE_BLOCK.get());
  }
}
