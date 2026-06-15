package com.goopey.voidsentflame.block.blockentity.render;

import com.goopey.voidsentflame.block.blockentity.VoidsentFlameBlockEntity;
import com.goopey.voidsentflame.util.VFRenderConsts;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.goopey.voidsentflame.util.VertexMeshHelper.putBufferVertex;

public class VoidsentFlameBlockEntityRenderer implements BlockEntityRenderer<VoidsentFlameBlockEntity, VoidsentFlameBlockEntityRenderer.VoidsentFlameBlockEntityRenderState> {
  private static final float SIZE = 0.375F;
  private final ItemModelResolver itemModelResolver;

  public VoidsentFlameBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    this.itemModelResolver = context.itemModelResolver();
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
    int i = (int) blockEntity.getBlockPos().asLong();
    renderState.items = new ArrayList();

    for(int j = 0; j < blockEntity.getItems().size(); ++j) {
      ItemStackRenderState itemstackrenderstate = new ItemStackRenderState();
      this.itemModelResolver.updateForTopItem(
        itemstackrenderstate, blockEntity.getItems().get(j),
        ItemDisplayContext.FIXED, blockEntity.getLevel(),
        null, i + j
      );
      renderState.items.add(itemstackrenderstate);
    }
  }

  @Override
  public void submit(VoidsentFlameBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState) {
    // render item
    List<ItemStackRenderState> list = state.items;

    for (int i = 0; i < list.size(); ++i) {
      ItemStackRenderState itemstackrenderstate = list.get(i);
      if (!itemstackrenderstate.isEmpty()) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.44921875F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(i * 45.f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.translate(-0.3125F, -0.3125F, 0.0F);
        poseStack.scale(0.375F, 0.375F, 0.375F);
        itemstackrenderstate.submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
      }
    }

    // render fire
    poseStack.pushPose();
    nodeCollector.submitCustomGeometry(
      poseStack,
      RenderType.solid(),
      (pose, consumer) -> {
        int segments = 40;
        int segments2 = 20;

        for (int i = 0; i < segments; i++) {
          float t0 = i / (float) segments;
          float t1 = (i + 1) / (float) segments;

          Vec3 p0 = spiralPoint(t0, state.age, (2.0 * (segments - i)) / segments);
          Vec3 p1 = spiralPoint(t1, state.age, (2.0 * (segments - i)) / segments);

          addRibbonSegment(
            consumer, pose,
            p0, p1, 0.08F,
            getFireColor(t0), getFireColor(t1)
          );
        }
        for (int i = 0; i < segments2; i++) {
          float t0 = i / (float) segments2;
          float t1 = (i + 1) / (float) segments2;

          Vec3 p0 = spiralPoint(t0, state.age, (double) (segments2 - i) / segments2);
          Vec3 p1 = spiralPoint(t1, state.age, (double) (segments2 - i) / segments2);

          addRibbonSegment(
            consumer, pose,
            p0, p1, 0.08F,
            getFireColor(t0), getFireColor(t1)
          );
        }
      }
    );
    poseStack.popPose();
  }

  //#################################################
  //                  FIRE VFX
  //#################################################

  private static void addRibbonSegment(VertexConsumer consumer, PoseStack.Pose pose, Vec3 p0, Vec3 p1, float width, int color0, int color1) {
    Vec3 tangent = p1.subtract(p0).normalize();
    Vec3 side = tangent.cross(new Vec3(0, 1, 0));

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

//    consumer.addVertex(pose, (float) a.x, (float) a.y, (float) a.z).setColor(r0, g0, b0, a0)
//      .setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);
//    consumer.addVertex(pose, (float) b.x, (float) b.y, (float) b.z).setColor(r0, g0, b0, a0)
//      .setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);
//    consumer.addVertex(pose, (float) c.x, (float) c.y, (float) c.z).setColor(r1, g1, b1, a1)
//      .setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);
//    consumer.addVertex(pose, (float) d.x, (float) d.y, (float) d.z).setColor(r1, g1, b1, a1)
//      .setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);

    consumer.addVertex(pose, (float) a.x, (float) a.y, (float) a.z).setColor(r0, g0, b0, a0).setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);
    consumer.addVertex(pose, (float) c.x, (float) c.y, (float) c.z).setColor(r1, g1, b1, a1).setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);
    consumer.addVertex(pose, (float) b.x, (float) b.y, (float) b.z).setColor(r0, g0, b0, a0).setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);

    consumer.addVertex(pose, (float) b.x, (float) b.y, (float) b.z).setColor(r0, g0, b0, a0).setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);
    consumer.addVertex(pose, (float) c.x, (float) c.y, (float) c.z).setColor(r1, g1, b1, a1).setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);
    consumer.addVertex(pose, (float) d.x, (float) d.y, (float) d.z).setColor(r1, g1, b1, a1).setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);
  }

  private static Vec3 spiralPoint(float t, float age, double radMult) {
    double radius = 0.5 * Math.sqrt(radMult);
    double angle = age * 0.12 + t * Math.PI * 8.0;

    double x = 0.5 + Math.cos(angle) * radius;
    double y = t * 1.5;
    double z = 0.5 + Math.sin(angle) * radius;

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

    consumer.addVertex(pose, x0, y0, z).setColor(r, g, b, alphaBottom).setUv(0, 0).setUv2(0, 0).setNormal(0, 1, 0);
    consumer.addVertex(pose, x0, y1, z).setColor(r, g, b, alphaBottom).setUv(0, 1).setUv2(0, 1).setNormal(0, 1, 0);
    consumer.addVertex(pose, x1, y1, z).setColor(r, g, b, alphaTop).setUv(1, 1).setUv2(1, 1).setNormal(0, 1, 0);
    consumer.addVertex(pose, x1, y0, z).setColor(r, g, b, alphaBottom).setUv(1, 0).setUv2(1, 0).setNormal(0, 1, 0);
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
    public List<ItemStackRenderState> items = Collections.emptyList();
  }
}