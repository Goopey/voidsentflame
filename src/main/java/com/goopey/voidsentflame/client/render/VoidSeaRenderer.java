package com.goopey.voidsentflame.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.goopey.voidsentflame.core.StandaloneModelRegistry;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.TrackedWaypoint.Camera;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public class VoidSeaRenderer {
  // Singleton Instance
  private static final VoidSeaRenderer INSTANCE = new VoidSeaRenderer();

  // Render Triangles
  private static final int SUBDIV = 4;
  private static final float AMPLITUDE = 0.5f;
  private static final float FLOOR_RANGE = 8f;
  private static final float FREQUENCY = 0.5f;
  private static final float SPEED = 0.05f;

  // World Position
  private static final float HEIGHT = -42.5f;
  private static final int WIDTH = 128;
  private static final int OFFSET = 64;
  
  // Sprite/Model Stuff
  private TextureAtlasSprite SPRITE;
  private static String SPRITE_NAME = "void_fluid";
  private static StandaloneModelKey<QuadCollection> MODEL_KEY = StandaloneModelRegistry.VOID_SEA;
  
  // Shader Stuff
  private RenderType EXAMPLE_RENDER;
  private RenderPipeline distortPipeline;
  private static final int PACKED_LIGHT = 15728880;
  private static final int PACKED_OVERLAY = 655360;
  
  // Cache Stuff
  private Map<Long, Float> cachedHeight;
  private Map<Float, List<BakedQuad>> cachedQuads;
  private Map<Float, BakedQuad> cachedQuad;

  // Dimension Stuff
  private static final ResourceKey<Level> RUBICON = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("voidsentflame:rubicon"));

  //#################################################
  //                 INSTANCE
  //#################################################

  private VoidSeaRenderer() {
    // Cache
    this.cachedHeight = new HashMap<>();
    this.cachedQuads = new HashMap<>();
    this.cachedQuad = new HashMap<>();

    // Render Pipeline
    //  this.distortPipeline = RenderPipelines.register(RenderPipeline.builder()
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
      false, true, 
      RenderPipelines.SOLID, RenderType.CompositeState.builder()
        .setLightmapState(LightmapStateShard.LIGHTMAP)
        .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
        .createCompositeState(true));
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
    // LevelRenderer levelRenderer = event.getLevelRenderer();
    PoseStack poseStack = event.getPoseStack();
    Camera camera = event.getCamera();
    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    VertexConsumer builder = bufferSource.getBuffer(this.EXAMPLE_RENDER);

    // Time
    long gameTime = Minecraft.getInstance().level.getGameTime();
    double t = gameTime + event.getPartialTick().getGameTimeDeltaTicks();
    
    // Start Rendering
    poseStack.pushPose();
    // Set height to bottom of world
    poseStack.translate(0, HEIGHT-camera.position().y, 0);

    // red, blue, green and alpha go from 0..1
    // List<BakedQuad> quads = getCachedQuads(0, this.cachedQuads, poseStack);
    // for (BakedQuad quad : quads) {
    builder.putBulkData(poseStack.last(), getCachedQuad(0, this.cachedQuad, poseStack), 1, 1, 1, 1, PACKED_LIGHT, OFFSET);
    // }
    // renderSea(t, builder, poseStack, bufferSource, PACKED_LIGHT, PACKED_OVERLAY, camera.position());

    poseStack.popPose();
  }

  /**
   * 
   * @param gameTime the gameTime needed
   * @param builder
   * @param poseStack
   * @param bufferSource
   * @param packedLight
   * @param packedOverlay
   * @param cameraPos
   */
  public void renderSea(double gameTime, @Nonnull VertexConsumer builder, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int packedLight, int packedOverlay, @Nonnull Vec3 cameraPos) {
    // set height to 5 blocks
    // poseStack.translate(0, HEIGHT - cameraPos.y(), 0);
    TextureAtlasSprite sprite = this.getSprite();

    Matrix4f mat = poseStack.last().pose();
    PoseStack.Pose pose = poseStack.last();

    // iterate across grid
    for (int ix = -OFFSET; ix < WIDTH - OFFSET; ix++) {
      for (int iz = -OFFSET; iz < WIDTH - OFFSET; iz++) {
        // inside each block, subdivide
        for (int sx = 0; sx < SUBDIV; sx++) {
          for (int sz = 0; sz < SUBDIV; sz++) {
            // compute fractional positions 0..1
            float x0 = ix + (float)sx / SUBDIV;
            float z0 = iz + (float)sz / SUBDIV;
            float x1 = ix + (float)(sx+1) / SUBDIV;
            float z1 = iz + (float)(sz+1) / SUBDIV;
            
            // world positions for wave sample
            double wx0 = cameraPos.x() + x0;
            double wz0 = cameraPos.z() + z0;
            double wx1 = cameraPos.x() + x1;
            double wz1 = cameraPos.z() + z0;
            double wx2 = cameraPos.x() + x1;
            double wz2 = cameraPos.z() + z1;
            double wx3 = cameraPos.x() + x0;
            double wz3 = cameraPos.z() + z1;
            
            // heights = sin wave
            float h0 = getCachedHeight(wx0, wz0, gameTime, this.cachedHeight);
            float h1 = getCachedHeight(wx1, wz1, gameTime, this.cachedHeight);
            float h2 = getCachedHeight(wx2, wz2, gameTime, this.cachedHeight);
            float h3 = getCachedHeight(wx3, wz3, gameTime, this.cachedHeight);
            
            // float h0, h1, h2, h3 = h2 = h1 = h0 = 0;

            // compute UV coords (u0,v0 etc)
            // In terms of fractional positions 0..1. We don't want mixels.
            float u0 = sprite.getU((float)sx / SUBDIV);
            float v0 = sprite.getV((float)sz / SUBDIV);
            float u1 = sprite.getU((float)(sx+1) / SUBDIV);
            float v1 = sprite.getV((float)(sz+1) / SUBDIV);
            
            // // top surface
            putVertex(builder, x0, h0, z0, u0, v0, packedLight, packedOverlay, pose);
            putVertex(builder, x0, h1, z1, u0, v1, packedLight, packedOverlay, pose);
            putVertex(builder, x1, h2, z1, u1, v1, packedLight, packedOverlay, pose);
            putVertex(builder, x1, h3, z0, u1, v0, packedLight, packedOverlay, pose);
          }
        }
      }
    };
  }

  //############################################
  //            HELPER METHODS
  //############################################

  private BakedQuad preBakeQuad(PoseStack poseStack) {
    PoseStack.Pose pose = poseStack.last();
    TextureAtlasSprite sprite = getSprite();

    QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer();
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

  private List<BakedQuad> preBakeQuads(PoseStack poseStack) {
    List<BakedQuad> bakedQuads = new ArrayList<BakedQuad>();
    Matrix4f mat = poseStack.last().pose();
    PoseStack.Pose pose = poseStack.last();
    TextureAtlasSprite sprite = getSprite();

    // iterate across grid
    for (int ix = -OFFSET; ix < WIDTH - OFFSET; ix++) {
      for (int iz = -OFFSET; iz < WIDTH - OFFSET; iz++) {
        // inside each block, subdivide
        for (int sx = 0; sx < SUBDIV; sx++) {
          for (int sz = 0; sz < SUBDIV; sz++) {
            QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer();
            // compute fractional positions 0..1
            float x0 = ix + (float)sx / SUBDIV;
            float z0 = iz + (float)sz / SUBDIV;
            float x1 = ix + (float)(sx+1) / SUBDIV;
            float z1 = iz + (float)(sz+1) / SUBDIV;
            
            float h0, h1, h2, h3 = h2 = h1 = h0 = 0;

            // compute UV coords (u0,v0 etc)
            // In terms of fractional positions 0..1. We don't want mixels.
            float u0 = sprite.getU((float)sx / SUBDIV);
            float v0 = sprite.getV((float)sz / SUBDIV);
            float u1 = sprite.getU((float)(sx+1) / SUBDIV);
            float v1 = sprite.getV((float)(sz+1) / SUBDIV);
            
            // top surface
            putVertex(builder, x0, h0, z0, u0, v0, PACKED_LIGHT, PACKED_OVERLAY, pose);
            putVertex(builder, x0, h1, z1, u0, v1, PACKED_LIGHT, PACKED_OVERLAY, pose);
            putVertex(builder, x1, h2, z1, u1, v1, PACKED_LIGHT, PACKED_OVERLAY, pose);
            putVertex(builder, x1, h3, z0, u1, v0, PACKED_LIGHT, PACKED_OVERLAY, pose);

            bakedQuads.add(builder.bakeQuad());
          }
        }
      }
    };
    
    return bakedQuads;
  }

  /**
   * Method used to cache height of specific sets of coordinates to improve performance.
   * 
   * @param wx the x position of the wave in the grid
   * @param wz the z position of the wave in the grid
   * @param t time in ticks. Needed for waveheight to determine the height at the current moment.
   * @param cache the map which caches a set of coordinates to a height value
   * @return
   */
  private float getCachedHeight(double wx, double wz, double t, Map<Long, Float> cache) {
    long key = (long) (Math.floor((wx + wz) * FLOOR_RANGE) / FLOOR_RANGE);
    return cache.computeIfAbsent(key, k -> waveHeight(wx, wz, t));
  }
  
  /**
   * Method used to cache bakedQuad to improve performance.
   * 
   * @param key the key needed to recuperate the cached object.
   * @param cache the map which caches a bakedQuad to a specific key
   * @return the BakedQuad
   */
  private List<BakedQuad> getCachedQuads(float key, Map<Float, List<BakedQuad>> cache, PoseStack poseStack) {
    return cache.computeIfAbsent(key, k -> preBakeQuads(poseStack));
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
  
  
  /**
   * Used to add a vertex at specific coordinates with a downwards normal
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
  private void reversePutVertex(VertexConsumer builder, Matrix4f mat, float x, float y, float z, float u, float v, int light, int overlay, PoseStack.Pose pose) {
    builder.addVertex(mat, x, y, z)
    .setColor(1f, 1f, 1f, 1f)
    .setUv(u, v)
    .setOverlay(overlay)
    .setLight(light)
    .setNormal(pose, 0, -1, 0);
  }
}
