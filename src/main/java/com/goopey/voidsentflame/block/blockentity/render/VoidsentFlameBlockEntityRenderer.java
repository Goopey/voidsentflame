package com.goopey.voidsentflame.block.blockentity.render;

import com.goopey.voidsentflame.block.blockentity.VoidsentFlameBlockEntity;
import com.goopey.voidsentflame.util.VFRenderConsts;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static com.goopey.voidsentflame.util.VertexMeshHelper.putBufferVertex;

public class VoidsentFlameBlockEntityRenderer implements BlockEntityRenderer<VoidsentFlameBlockEntity, VoidsentFlameBlockEntityRenderer.VoidsentFlameBlockEntityRenderState> {
  public VoidsentFlameBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
  }

  //#####################################################
  //                  RENDER METHODS
  //#####################################################
  @Override
  public VoidsentFlameBlockEntityRenderState createRenderState() {
    return new VoidsentFlameBlockEntityRenderState();
  }

  @Override
  public void extractRenderState(VoidsentFlameBlockEntity blockEntity, VoidsentFlameBlockEntityRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
    renderState.age = blockEntity.getLevel().getGameTime() + partialTick;
  }

  @Override
  public void submit(VoidsentFlameBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState) {
    poseStack.pushPose();

    nodeCollector.submitCustomGeometry(
      poseStack,
      RenderType.solid(),
      (pose, consumer) -> {
        int segments = 40;

        for (int i = 0; i < segments; i++) {
          float t0 = i / (float)segments;
          float t1 = (i + 1) / (float)segments;

          Vec3 p0 = spiralPoint(t0, state.age);
          Vec3 p1 = spiralPoint(t1, state.age);

          addRibbonSegment(
            consumer, pose,
            p0, p1, 0.08F,
            getFireColor(t0), getFireColor(t1)
          );
        }
//        for (int i = 0; i < 40; i++) {
//          float t = i / 40.0F;
//          float angle = renderState.age * 0.1F + t * 10.0F;
//          float radius = 0.25F;
//
//          float x = Mth.cos(angle) * radius;
//          float z = Mth.sin(angle) * radius;
//          float y = t * 1.5F;
//          float size = 0.05F;
//
//          float r;
//          float g;
//          float b;
//
//          if (t < 0.33F) {
//            // Purple
//            r = 0.7F;
//            g = 0.2F;
//            b = 1.0F;
//          } else if (t < 0.66F) {
//            // Orange
//            r = 1.0F;
//            g = 0.5F;
//            b = 0.0F;
//          } else {
//            // Gold
//            r = 1.0F;
//            g = 0.85F;
//            b = 0.2F;
//          }
//
//          buffer.addVertex(pose, x - size, y, z)
//            .setColor(r, g, b, 0.8F);
//          buffer.addVertex(pose, x + size, y, z)
//            .setColor(r, g, b, 0.8F);
//          buffer.addVertex(pose, x + size, y + size * 2, z)
//            .setColor(r, g, b, 0.0F);
//          buffer.addVertex(pose, x - size, y + size * 2, z)
//            .setColor(r, g, b, 0.0F);
//        }
      }
    );

    poseStack.popPose();
  }

  //#################################################
  //                  FIRE VFX
  //#################################################

  private static void addRibbonSegment(VertexConsumer consumer, PoseStack.Pose pose, Vec3 p0, Vec3 p1, float width, int color0, int color1) {
    // Direction of this segment
    Vec3 tangent = p1.subtract(p0).normalize();

    // Generate ribbon width direction
    Vec3 side = tangent.cross(new Vec3(0, 1, 0));

    // Handle near-vertical segments
    if (side.lengthSqr() < 0.0001D) {
      side = new Vec3(1, 0, 0);
    }

    side = side.normalize().scale(width * 0.5F);

    Vec3 a = p0.add(side);
    Vec3 b = p0.subtract(side);
    Vec3 c = p1.add(side);
    Vec3 d = p1.subtract(side);

    float r0 = ((color0 >> 16) & 255) / 255F;
    float g0 = ((color0 >> 8) & 255) / 255F;
    float b0 = (color0 & 255) / 255F;
    float r1 = ((color1 >> 16) & 255) / 255F;
    float g1 = ((color1 >> 8) & 255) / 255F;
    float b1 = (color1 & 255) / 255F;
    float a0 = 0.9F;
    float a1 = 0.0F;

    consumer.addVertex(pose, (float)a.x, (float)a.y, (float)a.z).setColor(r0, g0, b0, a0);
    consumer.addVertex(pose, (float)c.x, (float)c.y, (float)c.z).setColor(r1, g1, b1, a1);
    consumer.addVertex(pose, (float)b.x, (float)b.y, (float)b.z).setColor(r0, g0, b0, a0);

    consumer.addVertex(pose, (float)b.x, (float)b.y, (float)b.z).setColor(r0, g0, b0, a0);
    consumer.addVertex(pose, (float)c.x, (float)c.y, (float)c.z).setColor(r1, g1, b1, a1);
    consumer.addVertex(pose, (float)d.x, (float)d.y, (float)d.z).setColor(r1, g1, b1, a1);
  }

  private static Vec3 spiralPoint(float t, float age) {
    double radius = 0.25D;
    double angle = age * 0.12D + t * Math.PI * 8.0D;

    double x = 0.5D + Math.cos(angle) * radius;
    double y = t * 1.5D;
    double z = 0.5D + Math.sin(angle) * radius;

    return new Vec3(x, y, z);
  }

  private static void addQuad(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float size, int color) {
    float r = ((color >> 16) & 0xFF) / 255.0F;
    float g = ((color >> 8) & 0xFF) / 255.0F;
    float b = (color & 0xFF) / 255.0F;

    float alphaBottom = 0.85F;
    float alphaTop = 0.0F;

    float x0 = x - size;
    float x1 = x + size;
    float y0 = y;
    float y1 = y + size * 2.0F;

    consumer.addVertex(pose, x0, y0, z).setColor(r, g, b, alphaBottom);
    consumer.addVertex(pose, x1, y0, z).setColor(r, g, b, alphaBottom);
    consumer.addVertex(pose, x0, y1, z).setColor(r, g, b, alphaTop);

    consumer.addVertex(pose, x0, y1, z).setColor(r, g, b, alphaTop);
    consumer.addVertex(pose, x1, y0, z).setColor(r, g, b, alphaBottom);
    consumer.addVertex(pose, x1, y1, z).setColor(r, g, b, alphaTop);
  }

  private static int getFireColor(float t) {
    // Purple, Orange, Gold, Red
    if (t < 0.33F) { return 0xAA44FF; }
    if (t < 0.66F) { return 0xFF7700; }
    if (t < 0.90F) { return 0xFFD700; }
    return 0xFF2222;
  }

  //#################################################
  //                 RENDER STATES
  //#################################################
  public static class VoidsentFlameBlockEntityRenderState extends BlockEntityRenderState {
    public float age;
  }
}