package com.goopey.voidsentflame.core;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.VFGpuBuffers.GpuBuffersNames;
import com.goopey.voidsentflame.core.VFGpuBuffers.VFGpuBuffersNames;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class VFRenderPipelines {
  public static RenderPipeline.Snippet GLOBALS_TERRAIN_SNIPPET;
  public static RenderPipeline.Snippet WORLD_POS_SNIPPET;
  public static RenderPipeline VOID_SEA_DISTORT;

  static {
    GLOBALS_TERRAIN_SNIPPET = RenderPipeline.builder(new RenderPipeline.Snippet[]{RenderPipelines.TERRAIN_SNIPPET})
      .withUniform(GpuBuffersNames.GLOBALS.name, UniformType.UNIFORM_BUFFER)
      .buildSnippet();
    WORLD_POS_SNIPPET = RenderPipeline.builder(new RenderPipeline.Snippet[]{GLOBALS_TERRAIN_SNIPPET})
      .withUniform(VFGpuBuffersNames.WORLD_POS.name, UniformType.UNIFORM_BUFFER)
      .buildSnippet();

    VOID_SEA_DISTORT = RenderPipelines.register(
      RenderPipeline.builder(
        new RenderPipeline.Snippet[]{GLOBALS_TERRAIN_SNIPPET})
        // sets a pipeline name, not an actual file
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/distort"))
        .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/main/distort_vert"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/main/distort_frag"))
        .withVertexFormat(
          // VertexFormat.builder().add("UV0", VertexFormatElement.UV0).add("Position", VertexFormatElement.POSITION).build(), 
          DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
        .withColorWrite(true, false)
        .withCull(false)
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .build());
  }
}
