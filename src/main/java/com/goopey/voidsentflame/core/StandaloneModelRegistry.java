package com.goopey.voidsentflame.core;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public class StandaloneModelRegistry {
  public static final StandaloneModelKey<QuadCollection> VOID_SEA = new StandaloneModelKey<>(
    new ModelDebugName() {
      @Override
      public String debugName() {
        return VoidsentFlameMod.MODID + ":void_sea";
      }
    }
  );

  public static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
    event.register(VOID_SEA, SimpleUnbakedStandaloneModel.quadCollection(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "void_sea")));
  }
}
