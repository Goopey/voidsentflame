package com.goopey.voidsentflame.core;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.VFGpuBuffers.GpuBuffersNames;
import com.goopey.voidsentflame.core.VFGpuBuffers.VFGpuBuffersNames;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class VFRenderPipelines {
  public static RenderPipeline.Snippet GLOBALS_TERRAIN_SNIPPET;
  public static RenderPipeline.Snippet GLOBALS_TERRAIN_POS_SNIPPET;
  public static RenderPipeline.Snippet POS_SNIPPET;

  public static RenderPipeline BLIT_PIPELINE;
  public static RenderPipeline DEPTH_BLIT;
  public static RenderPipeline INVERSE_DEPTH_BLIT;

  // VOID SEA
  public static RenderPipeline VOID_SEA_MESH_PIPELINE;
  public static RenderPipeline VOID_SEA_MESH_DISTORT_PIPELINE_T;
  public static RenderPipeline VOID_SEA_MESH_DISTORT_PIPELINE_B;
  public static RenderPipeline VOID_SEA_MESH_DISTORTION_GRADIENT_PIPELINE;
  public static RenderPipeline VOID_SEA_BLEND_PIPELINE;
  public static RenderPipeline VOID_SEA_DISTORTION_PIPELINE;

  // RUBICON SKY

  // FOG RENDERER
  public static RenderPipeline VOID_FOG_DEPTH_PIPELINE;
  public static RenderPipeline VOID_FOG_SKYBOX_PIPELINE;

  static {
    GLOBALS_TERRAIN_SNIPPET = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
      .withUniform(GpuBuffersNames.GLOBALS.name, UniformType.UNIFORM_BUFFER)
      .buildSnippet();
    GLOBALS_TERRAIN_POS_SNIPPET = RenderPipeline.builder(GLOBALS_TERRAIN_SNIPPET)
      .withUniform(VFGpuBuffersNames.WORLD_POS.name, UniformType.UNIFORM_BUFFER)
      .buildSnippet();
    POS_SNIPPET = RenderPipeline.builder()
      .withUniform(VFGpuBuffersNames.WORLD_POS.name, UniformType.UNIFORM_BUFFER)
      .buildSnippet();

    BLIT_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/blit"))
        .withVertexShader(ResourceLocation.withDefaultNamespace("core/screenquad"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/blit"))
        .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
        .withSampler("SamplerIn")
        .withColorWrite(true, true)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(false).withoutStencilTest().withoutBlend()
        .build()
    );
    DEPTH_BLIT = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/depth_blit"))
        .withVertexShader(ResourceLocation.withDefaultNamespace("core/screenquad"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/depth_blit"))
        .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
        .withColorWrite(true, true)
        .withDepthWrite(false)
        .withSampler("SamplerDepth")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(false).withoutBlend().withoutStencilTest()
        .build()
    );
    INVERSE_DEPTH_BLIT = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/inverse_depth_blit"))
        .withVertexShader(ResourceLocation.withDefaultNamespace("core/screenquad"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/inverse_depth_blit"))
        .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
        .withColorWrite(true, true)
        .withDepthWrite(false)
        .withSampler("SamplerDepth")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(false).withoutBlend().withoutStencilTest()
        .build()
    );

    VOID_SEA_MESH_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(GLOBALS_TERRAIN_POS_SNIPPET)
        // sets a pipeline name, not an actual file
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/void_sea_mesh"))
        .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_mesh_vert"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_mesh_frag"))
        .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
        .withColorWrite(true, false)
        .withCull(true)
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .build());
    VOID_SEA_MESH_DISTORTION_GRADIENT_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(GLOBALS_TERRAIN_POS_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/void_sea_mesh_distortion_gradient"))
        .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_mesh_distortion_gradient_vert"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_mesh_distortion_gradient_frag"))
        .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
        .withColorWrite(true, true)
        .withCull(true)
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .withBlend(BlendFunction.TRANSLUCENT)
        .build());
    VOID_SEA_MESH_DISTORT_PIPELINE_T = RenderPipelines.register(
      RenderPipeline.builder(GLOBALS_TERRAIN_POS_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/void_sea_distortion_mesh"))
        .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_mesh_vert"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_distort_mesh_frag"))
        .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
        .withColorWrite(true, false)
        .withCull(true)
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .build());
    VOID_SEA_MESH_DISTORT_PIPELINE_B = RenderPipelines.register(
      RenderPipeline.builder(GLOBALS_TERRAIN_POS_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/void_sea_distortion_mesh"))
        .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_mesh_vert"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_distort_mesh_frag"))
        .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
        .withColorWrite(true, false)
        .withCull(true)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .build());
    VOID_SEA_BLEND_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder()
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/void_sea_blend"))
        .withVertexShader(ResourceLocation.withDefaultNamespace("core/screenquad"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_blend_frag"))
        .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
        .withSampler("SamplerSea")
        .withSampler("SamplerWorld")
        .withColorWrite(true, false)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(false)
        .withoutStencilTest()
        .withoutBlend()
        .build()
    );
    VOID_SEA_DISTORTION_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(POS_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/void_sea_distort"))
        .withVertexShader(ResourceLocation.withDefaultNamespace("core/screenquad"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_sea_distort_frag"))
        .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
        .withSampler("SamplerSea")
        .withSampler("SamplerWorld")
        .withSampler("SamplerBlend")
//        .withSampler("SamplerDistortionGradient") TODO : Fix Gradient
        .withSampler("SamplerHeatWave")
        .withColorWrite(true, false)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(true)
        .withoutBlend()
        .build()
    );

    VOID_FOG_DEPTH_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/void_fog_depth"))
        .withVertexShader(ResourceLocation.withDefaultNamespace("core/screenquad"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_fog_depth"))
        .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
        .withColorWrite(true, true)
        .withDepthWrite(false)
        .withSampler("SamplerIn")
        .withSampler("SamplerDepth")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(false).withoutBlend().withoutStencilTest()
        .build()
    );
    VOID_FOG_SKYBOX_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/void_fog_skybox"))
        .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_fog_skybox_vert"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/void_fog_skybox_frag"))
        .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
        .withColorWrite(true, false)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .withCull(false).withoutBlend().withoutStencilTest()
        .build()
    );
  }
}
