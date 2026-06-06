package com.goopey.voidsentflame.block.blockentity.render;

import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.goopey.voidsentflame.block.blockentity.VoidsentFlameBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class VoidsentFlameBlockEntityRenderer implements BlockEntityRenderer<VoidsentFlameBlockEntity, VoidsentFlameBlockEntityRenderer.VoidsentFlameBlockEntityRenderState> {
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
  }

  @Override
  public void submit(VoidsentFlameBlockEntityRenderState voidSeaLayerBlockEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
  }

  //  @Override
  public void render(@Nonnull VoidSeaLayerBlockEntity blockEntity, float partialTick, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int packedLight, int packedOverlay, @Nonnull Vec3 cameraPos) {
  }

  //#################################################
  //                 RENDER STATES
  //#################################################
  public class VoidsentFlameBlockEntityRenderState extends BlockEntityRenderState {}
}