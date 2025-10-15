package com.goopey.voidsentflame.client.render;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.TrackedWaypoint.Camera;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

public class VoidSeaRenderer {
  // Singleton Instance
  private static final VoidSeaRenderer INSTANCE = new VoidSeaRenderer();

  // Render Triangles
  private static final float AMPLITUDE = 0.5f;
  private static final float FREQUENCY = 0.5f;
  private static final float SPEED = 0.05f;

  // World Position
  private static final float HEIGHT = -42.5f;
  private static final int OFFSET = 4096;
  private static final int VIEW_DISTANCE_SCALE = 16;
  
  // Sprite/Model Stuff
  private TextureAtlasSprite SPRITE;
  private static String SPRITE_NAME = "void_fluid";
  
  // Shader Stuff
  private RenderType EXAMPLE_RENDER;
  private static final int PACKED_LIGHT = 15728880;
  public RenderPipeline.Snippet voidSeaTerrainSnippet;
  public RenderPipeline.Snippet voidSeaFogMatricesSnippet;
  public RenderPipeline.Snippet voidSeaFogSnippet;
  public RenderPipeline.Snippet voidSeaMatricesSnippet;
  // private static final RenderPipeline DISTORT_PIPELINE = RenderPipelines.register(
  //     RenderPipeline.builder(
  //       new RenderPipeline.Snippet[]{RenderPipelines.TERRAIN_SNIPPET})
  //       .withLocation("pipeline/solid")
  //       .withoutStencilTest()
  //       .withCull(false)
  //       .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
  //       .build());
  public RenderPipeline distortPipeline;
  private static final int PACKED_OVERLAY = 655360;

  // Render Pipeline Stuff
  
  // Cache Stuff
  private Map<Float, BakedQuad> cachedQuad;

  static {
    // VOID_SEA_TERRAIN_SNIPPET = 
  }

  // Dimension Stuff
  private static final ResourceKey<Level> RUBICON = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("voidsentflame:rubicon"));

  //#################################################
  //                 INSTANCE
  //#################################################

  private VoidSeaRenderer() {
    // Cache
    this.cachedQuad = new HashMap<>();

    // Render Pipeline
    this.voidSeaMatricesSnippet = RenderPipeline.builder(new RenderPipeline.Snippet[0]).withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).buildSnippet();
    this.voidSeaFogSnippet = RenderPipeline.builder(new RenderPipeline.Snippet[0]).withUniform("Fog", UniformType.UNIFORM_BUFFER).buildSnippet();
    this.voidSeaFogMatricesSnippet = RenderPipeline.builder(new RenderPipeline.Snippet[]{this.voidSeaMatricesSnippet, this.voidSeaFogSnippet}).buildSnippet();
    this.voidSeaTerrainSnippet = RenderPipeline.builder(new RenderPipeline.Snippet[]{this.voidSeaFogMatricesSnippet})
      .withVertexShader("core/terrain").withFragmentShader("core/terrain")
      .withSampler("Sampler0").withSampler("Sampler2")
      .withVertexFormat(DefaultVertexFormat.BLOCK, Mode.QUADS)
      .buildSnippet();
    this.distortPipeline = RenderPipelines.register(
      RenderPipeline.builder(
        new RenderPipeline.Snippet[]{this.voidSeaTerrainSnippet})
        .withLocation("pipeline/solid")
        .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "distort_vert"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "distort_frag"))
        .withColorWrite(true, false)
        .withCull(false)
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .build());
    // this.distortPipeline = RenderPipelines.register(RenderPipeline.builder()
    //   .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "distort_pipeline"))
    //   .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/distort_vert"))
    //   .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/distort_frag"))
    //   .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
    //   .withUniform("waveCenter", UniformType.UNIFORM_BUFFER)
    //   .withUniform("radius", UniformType.UNIFORM_BUFFER)
    //   .withUniform("time", UniformType.UNIFORM_BUFFER)
    //   .withUniform("screenSize", UniformType.UNIFORM_BUFFER)
    //   .withSampler("sceneTex")
    //   .withSampler("sceneDepth")
    //   .withBlend(BlendFunction.TRANSLUCENT)
    //   .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
    //   .build()
    // );

    // Renderer
    this.EXAMPLE_RENDER = RenderType.create(
      "example2:example2", 4192, 
      false, false, 
      this.distortPipeline, RenderType.CompositeState.builder()
        .setLightmapState(LightmapStateShard.LIGHTMAP)
        .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
        .createCompositeState(false));
  }

  public static VoidSeaRenderer getInstance() {
    return INSTANCE;
  }

  //######################################################
  //                  RENDER STUFF
  //######################################################

  /**
   * This is the function which actually does all the work to get stuff to appear/render in the world.
   * @param event the event needed to tie into the part of the general rendering pipeline which I want to be at
   */
  public void render(RenderLevelStageEvent.AfterEntities event) {
    // Check if in Rubicon
    Level level = event.getLevel();
    if (level.dimension() != RUBICON) { return; }

    // Level Renderer
    LevelRenderer levelRenderer = event.getLevelRenderer();
    PoseStack poseStack = event.getPoseStack();
    Camera camera = event.getCamera();
    double viewDistance = levelRenderer.getLastViewDistance();
    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    VertexConsumer builder = bufferSource.getBuffer(this.EXAMPLE_RENDER);

    // Time
    long gameTime = Minecraft.getInstance().level.getGameTime();
    double t = gameTime + event.getPartialTick().getGameTimeDeltaTicks();
    
    // Start Rendering
    poseStack.pushPose();
    // Set height to bottom of world
    poseStack.translate(0, HEIGHT-camera.position().y, 0);
    poseStack.scale((float) (viewDistance/VIEW_DISTANCE_SCALE), 1f, (float) (viewDistance/VIEW_DISTANCE_SCALE));

    // red, blue, green and alpha go from 0..1
    builder.putBulkData(poseStack.last(), getCachedQuad(0, this.cachedQuad, poseStack), 1, 1, 1, 1, PACKED_LIGHT, OFFSET);

    poseStack.popPose();
  }

  //############################################
  //            HELPER METHODS
  //############################################

  private BakedQuad preBakeQuad(PoseStack poseStack) {
    PoseStack.Pose pose = poseStack.last();
    TextureAtlasSprite sprite = getSprite();

    QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer();
    builder.setHasAmbientOcclusion(false);
    // set positions of vertices
    float x0 = -OFFSET;
    float z0 = -OFFSET;
    float x1 = OFFSET;
    float z1 = OFFSET;
    
    float h0, h1, h2, h3 = h2 = h1 = h0 = 0;

    float u0 = sprite.getU(0);
    float v0 = sprite.getV(0);
    float u1 = sprite.getU(1);
    float v1 = sprite.getV(1);
    
    putVertex(builder, x0, h0, z0, u0, v0, PACKED_LIGHT, PACKED_OVERLAY, pose);
    putVertex(builder, x0, h1, z1, u0, v1, PACKED_LIGHT, PACKED_OVERLAY, pose);
    putVertex(builder, x1, h2, z1, u1, v1, PACKED_LIGHT, PACKED_OVERLAY, pose);
    putVertex(builder, x1, h3, z0, u1, v0, PACKED_LIGHT, PACKED_OVERLAY, pose);

    return builder.bakeQuad();
  }

  /**
   * Method used to cache bakedQuad to improve performance.
   * 
   * @param key the key needed to recuperate the cached object.
   * @param cache the map which caches a bakedQuad to a specific key
   * @return the BakedQuad
   */
  private BakedQuad getCachedQuad(float key, Map<Float, BakedQuad> cache, PoseStack poseStack) {
    return cache.computeIfAbsent(key, k -> preBakeQuad(poseStack));
  }

  private TextureAtlasSprite getSprite() {
    if (this.SPRITE == null) {
      ResourceLocation res = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/"+ SPRITE_NAME);
      ResourceLocation atlasLocation = Sheets.BLOCKS_MAPPER.apply(res).atlasLocation();
      Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(atlasLocation);
      this.SPRITE = atlas.apply(res);
    }

    return this.SPRITE;
  }

  /**
   * 
   * @param wx the x position in the wave
   * @param wz the z position in the wave
   * @param t the time
   * @return the height of the wave
   */
  private float waveHeight(double wx, double wz, double t) {
    double val = Math.sin((wx + wz) * FREQUENCY + t * SPEED);
    return (float)(val * AMPLITUDE);
  }
  
  /**
   * Used to add a vertex at specific coordinates with an upwards normal
   * 
   * @param builder the builder needed to add the vertices to a mesh
   * @param mat the initial matrix position
   * @param x the 1st position of the vertex
   * @param y the 2nd position of the vertex
   * @param z the 3rd position of the vertex
   * @param u the 1st UV position
   * @param v the 2nd UV position
   * @param light the packedLight value
   * @param overlay the packedOverlay value
   * @param pose the last PoseStack.Pose
   */
  private void putVertex(VertexConsumer builder, float x, float y, float z, float u, float v, int light, int overlay, PoseStack.Pose pose) {
    builder.addVertex(x, y, z)
    .setColor(1f, 1f, 1f, 1f)
    .setUv(u, v)
    .setOverlay(overlay)
    .setLight(light)
    .setNormal(pose, 0, 1, 0);
  }
}
