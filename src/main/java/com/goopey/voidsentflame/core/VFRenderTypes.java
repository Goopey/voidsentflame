package com.goopey.voidsentflame.core;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.RenderType;

public class VFRenderTypes {
  public static RenderType VOID_SEA_DISTORT_RENDER;
  public static int VOID_SEA_RENDER_CAPACITY = 4192;

  static {
    VOID_SEA_DISTORT_RENDER = RenderType.create(
      VoidsentFlameMod.MODID + ":void_sea_distort_render", VOID_SEA_RENDER_CAPACITY, 
      false, false, 
      VFRenderPipelines.VOID_SEA_DISTORT, RenderType.CompositeState.builder()
        .setLightmapState(LightmapStateShard.LIGHTMAP)
        .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
        .createCompositeState(false));
  }
}
