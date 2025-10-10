package com.goopey.voidsentflame.client.render;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.TrackedWaypoint.Camera;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class VoidSeaRenderer {
  // Render Triangles
  private static final int SUBDIV = 4;
  private static final float AMPLITUDE = 0.5f;
  private static final float FLOOR_RANGE = 8f;
  private static final float FREQUENCY = 0.5f;
  private static final float SPEED = 0.05f;

  // World Position
  private static final float HEIGHT = -42.5f;
  private static final int WIDTH = 32;
  private static final int OFFSET = 16;
  
  // Sprite Stuff
  private TextureAtlasSprite SPRITE;
  private static String SPRITE_NAME = "void_fluid";
  
  // Shader Stuff
  private RenderType EXAMPLE_RENDER;
  private RenderPipeline distortPipeline;
  private static final int PACKED_LIGHT = 15728880;
  private static final int PACKED_OVERLAY = 655360;

  // Cache Stuff
  private Map<Long, Float> cachedHeight;

  // Dimension Stuff
  private ResourceKey<Level> rubicon;
  private static final String rubiconLocation = "voidsentflame:rubicon";

  public VoidSeaRenderer() {
    // Cache
    this.cachedHeight = new HashMap<>();

    // Dimension Stuff
    this.rubicon = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(rubiconLocation));

    // Sprite
    ResourceLocation res = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/"+ SPRITE_NAME);
    ResourceLocation atlasLocation = Sheets.BLOCKS_MAPPER.apply(res).atlasLocation();
    Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(atlasLocation);
    this.SPRITE = atlas.apply(res);

    // Renderer
    this.EXAMPLE_RENDER = RenderType.create(
      "example2:example2", 4192, 
      false, true, 
      RenderPipelines.SOLID, RenderType.CompositeState.builder()
        .setLightmapState(LightmapStateShard.LIGHTMAP)
        .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
        .createCompositeState(true));
  }

  public void render(RenderLevelStageEvent.AfterEntities event) {
    // Check if in Rubicon
    Level level = event.getLevel();
    if (level.dimension() != this.rubicon) { return; }

    // Level Renderer
    LevelRenderer levelRenderer = event.getLevelRenderer();
    PoseStack poseStack = event.getPoseStack();
    Camera camera = event.getCamera();
    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    VertexConsumer builder = bufferSource.getBuffer(this.EXAMPLE_RENDER);

    // Time
    long gameTime = Minecraft.getInstance().level.getGameTime();
    double t = gameTime + event.getPartialTick().getGameTimeDeltaTicks();
    
    // Start Rendering
    poseStack.pushPose();

    renderSea(t, builder, poseStack, bufferSource, PACKED_LIGHT, PACKED_OVERLAY, camera.position());

    poseStack.popPose();
    // bufferSource.endBatch();
  }

  public void renderSea(double gameTime, @Nonnull VertexConsumer builder, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int packedLight, int packedOverlay, @Nonnull Vec3 cameraPos) {
    // set height to 5 blocks
    poseStack.translate(0, HEIGHT - cameraPos.y(), 0);

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
            
            // compute UV coords (u0,v0 etc)
            // In terms of fractional positions 0..1. We don't want mixels.
            float u0 = this.SPRITE.getU((float)sx / SUBDIV);
            float v0 = this.SPRITE.getV((float)sz / SUBDIV);
            float u1 = this.SPRITE.getU((float)(sx+1) / SUBDIV);
            float v1 = this.SPRITE.getV((float)(sz+1) / SUBDIV);
            
            // top surface
            putVertex(builder, mat, x0, h0, z0, u0, v0, packedLight, packedOverlay, pose);
            putVertex(builder, mat, x0, h3, z1, u0, v1, packedLight, packedOverlay, pose);
            putVertex(builder, mat, x1, h2, z1, u1, v1, packedLight, packedOverlay, pose);
            
            putVertex(builder, mat, x1, h2, z1, u1, v1, packedLight, packedOverlay, pose);
            putVertex(builder, mat, x1, h1, z0, u1, v0, packedLight, packedOverlay, pose);
            putVertex(builder, mat, x0, h0, z0, u0, v0, packedLight, packedOverlay, pose);
            
            // bottom surface
            reversePutVertex(builder, mat, x0, h0, z0, u0, v0, packedLight, packedOverlay, pose);
            reversePutVertex(builder, mat, x0, h3, z1, u0, v1, packedLight, packedOverlay, pose);
            reversePutVertex(builder, mat, x1, h2, z1, u1, v1, packedLight, packedOverlay, pose);
            
            reversePutVertex(builder, mat, x1, h2, z1, u1, v1, packedLight, packedOverlay, pose);
            reversePutVertex(builder, mat, x1, h1, z0, u1, v0, packedLight, packedOverlay, pose);
            reversePutVertex(builder, mat, x0, h0, z0, u0, v0, packedLight, packedOverlay, pose);
          }
        }
      }
    };
  }

  //############################################
  //            HELPER METHODS
  //############################################

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
  
  private float waveHeight(double wx, double wz, double t) {
    double val = Math.sin((wx + wz) * FREQUENCY + t * SPEED);
    return (float)(val * AMPLITUDE);
  }
  
  private void putVertex(VertexConsumer builder, Matrix4f mat, float x, float y, float z, float u, float v, int light, int overlay, PoseStack.Pose pose) {
    builder.addVertex(mat, x, y, z)
    .setColor(1f, 1f, 1f, 1f)
    .setUv(u, v)
    .setOverlay(overlay)
    .setLight(light)
    .setNormal(pose, 0, 1, 0);
  }
  
  private void reversePutVertex(VertexConsumer builder, Matrix4f mat, float x, float y, float z, float u, float v, int light, int overlay, PoseStack.Pose pose) {
    builder.addVertex(mat, x, y, z)
    .setColor(1f, 1f, 1f, 1f)
    .setUv(u, v)
    .setOverlay(overlay)
    .setLight(light)
    .setNormal(pose, 0, -1, 0);
  }
}
