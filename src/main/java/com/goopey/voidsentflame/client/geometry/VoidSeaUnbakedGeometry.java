package com.goopey.voidsentflame.client.geometry;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;

public class VoidSeaUnbakedGeometry implements ExtendedUnbakedGeometry {
  /**
   * Store the unbaked quads to bake.
   */
  public VoidSeaUnbakedGeometry() {
  }
  
  /** Method responsible for model baking, returning the quad collection. Parameters in this method are:
   * - The map of texture names to their associated materials.
   * - The model baker. Can be used for getting sub-models to bake and getting sprites from the texture slots.
   * - The model state. This holds the transformations from the blockstate file, typically from rotations and the uvlock.
   * - The name of the model.
   * - A ContextMap of settings provided by NeoForge and your unbaked model. See the 'NeoForgeModelProperties' class for all available properties.
  */
  @Override
  public QuadCollection bake(@Nonnull TextureSlots texSlot, @Nonnull ModelBaker modelBaker, @Nonnull ModelState modelState, @Nonnull ModelDebugName modelDebugName, @Nonnull ContextMap contextMap) {
    QuadCollection.Builder builder = new QuadCollection.Builder();
    
    builder.addUnculledFace(null);

    return builder.build();
  }
}
