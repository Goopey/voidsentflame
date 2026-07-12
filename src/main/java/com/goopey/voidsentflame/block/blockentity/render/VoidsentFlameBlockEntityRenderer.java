package com.goopey.voidsentflame.block.blockentity.render;

import com.goopey.voidsentflame.block.blockentity.VoidsentFlameBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoidsentFlameBlockEntityRenderer implements BlockEntityRenderer<VoidsentFlameBlockEntity, VoidsentFlameBlockEntityRenderer.VoidsentFlameBlockEntityRenderState> {
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
  }

  //#################################################
  //                 RENDER STATES
  //#################################################
  public static class VoidsentFlameBlockEntityRenderState extends BlockEntityRenderState {
    public float age;
    public List<ItemStackRenderState> items = Collections.emptyList();
  }
}