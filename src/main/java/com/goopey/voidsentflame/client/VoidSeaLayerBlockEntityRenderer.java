package com.goopey.voidsentflame.client;

import java.util.function.Function;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.goopey.voidsentflame.core.init.ItemInit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MaterialMapper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VoidSeaLayerBlockEntityRenderer implements BlockEntityRenderer<VoidSeaLayerBlockEntity> {
  private static final int SUBDIV = 16;
  private static final float AMPLITUDE = 0.5f;
  private static final float FREQUENCY = 0.5f;
  private static final float SPEED = 0.05f;
  
  public VoidSeaLayerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
  }

  @Override
  public boolean shouldRender(VoidSeaLayerBlockEntity blockEntity, Vec3 cameraPos) {
    // Calculate distance to player/camera
    double dx = blockEntity.getBlockPos().getX() + 8 - cameraPos.x;
    double dz = blockEntity.getBlockPos().getZ() + 8 - cameraPos.z;
    double distSq = dx * dx + dz * dz;

    // 8 chunks = 128 blocks → radius^2 = 16384
    return distSq < 16384;  // within 128 blocks
}

  @Override
  public void render(VoidSeaLayerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPos) {
    long gameTime = Minecraft.getInstance().level.getGameTime();
    double t = gameTime + partialTick;

    BlockPos basePos = blockEntity.getBlockPos();

    // We'll draw only the top surface (you could also optionally draw underside)
    RenderType renderType = RenderType.cutout();  // or solid, depending on textures
    VertexConsumer builder = bufferSource.getBuffer(renderType);

    poseStack.pushPose();
    // translate origin to block entity bottom corner:
    poseStack.translate(basePos.getX(), basePos.getY(), basePos.getZ());

    Matrix4f mat = poseStack.last().pose();
    PoseStack.Pose pose = poseStack.last();

    // iterate across grid
    for (int ix = 0; ix < VoidSeaLayerBlockEntity.WIDTH; ix++) {
      for (int iz = 0; iz < VoidSeaLayerBlockEntity.LENGTH; iz++) {
        // inside each block, subdivide
        for (int sx = 0; sx < SUBDIV; sx++) {
          for (int sz = 0; sz < SUBDIV; sz++) {
            // compute fractional positions 0..1
            float fx0 = ix + (float)sx / SUBDIV;
            float fz0 = iz + (float)sz / SUBDIV;
            float fx1 = ix + (float)(sx+1) / SUBDIV;
            float fz1 = iz + (float)(sz+1) / SUBDIV;

            // world positions for wave sample
            double wx0 = basePos.getX() + fx0;
            double wz0 = basePos.getZ() + fz0;
            double wx1 = basePos.getX() + fx1;
            double wz1 = basePos.getZ() + fz0;
            double wx2 = basePos.getX() + fx1;
            double wz2 = basePos.getZ() + fz1;
            double wx3 = basePos.getX() + fx0;
            double wz3 = basePos.getZ() + fz1;

            // heights = sin wave
            float h0 = waveHeight(wx0, wz0, t);
            float h1 = waveHeight(wx1, wz1, t);
            float h2 = waveHeight(wx2, wz2, t);
            float h3 = waveHeight(wx3, wz3, t);

            // Query the block under each cell to get texture/UV
            BlockState st = Minecraft.getInstance().level.getBlockState(basePos.offset(ix, -1, iz));
            // for this example, assume all below blocks have same top texture
            // More advanced: choose sprite based on st’s model / texture
            ResourceLocation res = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/void_fluid");
            ResourceLocation atlasLocation = Sheets.BLOCKS_MAPPER.apply(res).atlasLocation();
            Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(atlasLocation);
            TextureAtlasSprite sprite = atlas.apply(res);

            // compute UV coords (u0,v0 etc) — here simple full face mapping
            float u0 = sprite.getU0();
            float v0 = sprite.getV0();
            float u1 = sprite.getU1();
            float v1 = sprite.getV1();

            // now build quad for this subdiv cell (two triangles)
            // vertices: (fx0, h0, fz0), (fx1, h1, fz0), (fx1, h2, fz1), (fx0, h3, fz1)
            // note: careful with winding order, normals etc

            // first triangle (v0, v1, v2)
            putVertex(builder, mat, fx0, h0, fz0, u0, v0, packedLight, packedOverlay, pose);
            putVertex(builder, mat, fx1, h1, fz0, u1, v0, packedLight, packedOverlay, pose);
            putVertex(builder, mat, fx1, h2, fz1, u1, v1, packedLight, packedOverlay, pose);

            // second triangle (v2, v3, v0)
            putVertex(builder, mat, fx1, h2, fz1, u1, v1, packedLight, packedOverlay, pose);
            putVertex(builder, mat, fx0, h3, fz1, u0, v1, packedLight, packedOverlay, pose);
            putVertex(builder, mat, fx0, h0, fz0, u0, v0, packedLight, packedOverlay, pose);
          }
        }
      }
    }

    poseStack.popPose();
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
}
