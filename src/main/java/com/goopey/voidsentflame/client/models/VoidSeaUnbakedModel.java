package com.goopey.voidsentflame.client.models;

import com.goopey.voidsentflame.client.geometry.VoidSeaUnbakedGeometry;

import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;

public class VoidSeaUnbakedModel extends AbstractUnbakedModel {
  private final VoidSeaUnbakedGeometry geometry;

  public VoidSeaUnbakedModel(StandardModelParameters parameters, VoidSeaUnbakedGeometry geometry) {
    super(parameters);
    this.geometry = geometry;
  }

  /**
   * Used to construct baked quads.
   */
  @Override
  public UnbakedGeometry geometry() {
    return this.geometry();
  }

  /**
   * Used to add additonal properties by calling withParameter(ContextKey<T>, T)
   * They can be accessed in the ContextMap provided in UnbakedGeometry#bake
   */
  @Override
  public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
    super.fillAdditionalProperties(propertiesBuilder);
  }
}
