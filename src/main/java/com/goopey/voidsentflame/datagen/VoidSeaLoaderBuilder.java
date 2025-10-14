package com.goopey.voidsentflame.datagen;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.goopey.voidsentflame.client.VoidSeaUnbakedModelLoader;

import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;

public class VoidSeaLoaderBuilder extends CustomLoaderBuilder {
  protected VoidSeaLoaderBuilder() {
    super(VoidSeaUnbakedModelLoader.ID, false);
  }

  @Override
  protected CustomLoaderBuilder copyInternal() {
    VoidSeaLoaderBuilder builder = new VoidSeaLoaderBuilder();
    return builder;
  }

  @Override
  public JsonObject toJson(@Nonnull JsonObject json) {
    return super.toJson(json);
  }
}
