package com.goopey.voidsentflame.datagen;

import java.util.concurrent.CompletableFuture;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

public class ModSpriteSourceProvider extends SpriteSourceProvider {
  public ModSpriteSourceProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
    super(output, lookupProvider, VoidsentFlameMod.MODID);
  }

  @Override
  protected void gather() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'gather'");
  }

}
