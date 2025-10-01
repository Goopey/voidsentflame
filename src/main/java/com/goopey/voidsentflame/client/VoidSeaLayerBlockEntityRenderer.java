package com.goopey.voidsentflame.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.joml.Matrix4f;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class VoidSeaLayerBlockEntityRenderer implements BlockEntityRenderer<VoidSeaLayerBlockEntity> {
  private static final int SUBDIV = 4;
  private static final float AMPLITUDE = 0.5f;
  private static final float FLOOR_RANGE = 4f;
  private static final float FREQUENCY = 0.5f;
  private static final float SPEED = 0.05f;
  private TextureAtlasSprite SPRITE;
  
  public VoidSeaLayerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    ResourceLocation res = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/void_fluid");
    ResourceLocation atlasLocation = Sheets.BLOCKS_MAPPER.apply(res).atlasLocation();
    Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(atlasLocation);
    this.SPRITE = atlas.apply(res);
  }

  @Override
  public boolean shouldRender(VoidSeaLayerBlockEntity blockEntity, Vec3 cameraPos) {
    // Calculate distance to player/camera
    double dx = blockEntity.getBlockPos().getX() + 8 - cameraPos.x;
    double dz = blockEntity.getBlockPos().getZ() + 8 - cameraPos.z;
    double distSq = dx * dx + dz * dz;

    return true;
  }

  @Override
  public void render(VoidSeaLayerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPos) {    
    Map<Long, Float> heightCache = new HashMap<>();
    
    long gameTime = Minecraft.getInstance().level.getGameTime();
    double t = gameTime + partialTick;

    BlockPos basePos = blockEntity.getBlockPos();

    RenderType renderType = RenderType.solid();
    VertexConsumer builder = bufferSource.getBuffer(renderType);

    poseStack.pushPose();
    // translate origin to block entity bottom corner
    // set height to 5 blocks
    poseStack.translate(0, 5, 0);

    Matrix4f mat = poseStack.last().pose();
    PoseStack.Pose pose = poseStack.last();

    // iterate across grid
    for (int ix = 0; ix < VoidSeaLayerBlockEntity.WIDTH; ix++) {
      for (int iz = 0; iz < VoidSeaLayerBlockEntity.LENGTH; iz++) {
        // inside each block, subdivide
        for (int sx = 0; sx < SUBDIV; sx++) {
          for (int sz = 0; sz < SUBDIV; sz++) {
            // compute fractional positions 0..1
            float x0 = ix + (float)sx / SUBDIV;
            float z0 = iz + (float)sz / SUBDIV;
            float x1 = ix + (float)(sx+1) / SUBDIV;
            float z1 = iz + (float)(sz+1) / SUBDIV;

            // world positions for wave sample
            double wx0 = basePos.getX() + x0;
            double wz0 = basePos.getZ() + z0;
            double wx1 = basePos.getX() + x1;
            double wz1 = basePos.getZ() + z0;
            double wx2 = basePos.getX() + x1;
            double wz2 = basePos.getZ() + z1;
            double wx3 = basePos.getX() + x0;
            double wz3 = basePos.getZ() + z1;

            // heights = sin wave
            float h0 = getCachedHeight(wx0, wz0, t, heightCache);
            float h1 = getCachedHeight(wx1, wz1, t, heightCache);
            float h2 = getCachedHeight(wx2, wz2, t, heightCache);
            float h3 = getCachedHeight(wx3, wz3, t, heightCache);

            // compute UV coords (u0,v0 etc)
            float u0 = this.SPRITE.getU0();
            float v0 = this.SPRITE.getV0();
            float u1 = this.SPRITE.getU1();
            float v1 = this.SPRITE.getV1();

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
    }

    poseStack.popPose();
  }

  private float getCachedHeight(double wx, double wz, double t, Map<Long, Float> cache) {
    // long key = (((long)Double.doubleToRawLongBits(wx)) << 32) ^ Double.doubleToRawLongBits(wz);
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
