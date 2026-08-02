package com.goopey.voidsentflame.util;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.client.render.FullscreenQuadRenderer;
import com.goopey.voidsentflame.core.VFGpuBuffers;
import com.goopey.voidsentflame.core.VFRenderPipelines;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.util.ARGB;

import java.util.List;
import java.util.OptionalInt;

public class RenderHelper {
  /**
   * Method used to clear (to white) the content of the list of targets passed and copy the depth buffer of the main
   * target into the other targets.
   * @param mainTargetHandle the main screen's handle. Needed to copy depthBuffers into other RenderTargets.
   * @param targetHandles a list of ResourceHandles to clear, resize and copy new basic data into
   */
  public static void clearAndResizeTargetsWhite(ResourceHandle<? extends RenderTarget> mainTargetHandle, List<ResourceHandle<? extends RenderTarget>> targetHandles) {
    RenderTarget mainTarget = mainTargetHandle.get();
    int width = mainTarget.width;
    int height = mainTarget.height;

    for (ResourceHandle<? extends RenderTarget> targetHandle : targetHandles) {
      RenderTarget target = targetHandle.get();

      // resize
      if (target.width != width || target.height != height) {
        target.resize(width, height);
      }

      // clear textures to white
      if (target.getColorTexture() != null) {
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.getColorTexture(),
          ARGB.color(255, 255, 255, 255)
        );
      }
      if (target.getDepthTexture() != null) {
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(target.getDepthTexture(), 1.0);
        target.copyDepthFrom(mainTarget);
      }
    }
  }

  /**
   * A simple render pass which merely copies data from one target into another.
   * @param inTargetHandle the target that is being read. The A/In target.
   * @param outTargetHandle the target that is being written to. The B/Out target.
   */
  public static void blitAToB(ResourceHandle<? extends RenderTarget> inTargetHandle, ResourceHandle<? extends RenderTarget> outTargetHandle) {
    RenderTarget inTarget = inTargetHandle.get();
    RenderTarget outTarget = outTargetHandle.get();
    GpuTextureView colorTextureViewI = inTarget.getColorTextureView();
    GpuTextureView colorTextureViewO = outTarget.getColorTextureView();

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    if (colorTextureViewI == null) {
      VoidsentFlameMod.LOGGER.error("VF - blitAToB : THERE WAS NO IN COLOR TEXTURE : {}", inTarget);
      return;
    }
    if (colorTextureViewO == null) {
      VoidsentFlameMod.LOGGER.error("VF - blitAToB : THERE WAS NO OUT COLOR TEXTURE : {}", outTarget);
      return;
    }

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> "BlitAToB", colorTextureViewO, OptionalInt.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.BLIT_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);

      renderPass.bindSampler("SamplerIn", colorTextureViewI);

      renderPass.setVertexBuffer(0, FullscreenQuadRenderer.INSTANCE.getQuad());
      renderPass.setIndexBuffer(FullscreenQuadRenderer.INSTANCE.getQuad(), VertexFormat.IndexType.SHORT);
      renderPass.draw(0, FullscreenQuadRenderer.INSTANCE.getIndex());
    }
  }

  //##############################################################################
  //                                DEPTH BLIT
  //##############################################################################6

  /**
   * Copies and linearizes the depth from one target into another. Starts off black near the player and ends white far away.
   * FIXME : blit is incorrectly grabbing data from renderDistance
   * @param renderDistance the MappableRingBuffer needed to transfer the desirable distance (default 128) to the shader.
   * @param fov the MappableRingBuffer needed to transfer data about the player's FOV to the shader.
   * @param inTargetHandle the target whose depth is read.
   * @param outTargetHandle the target whose color gets the depth written to.
   */
  public static void blitDepth(MappableRingBuffer renderDistance, MappableRingBuffer fov, ResourceHandle<? extends RenderTarget> inTargetHandle, ResourceHandle<? extends RenderTarget> outTargetHandle) {
    blitDepth(renderDistance, fov, 128.f, inTargetHandle, outTargetHandle);
  }

  /**
   * Copies and linearizes the depth from one target into another. Starts off black near the player and ends white far away.
   * FIXME : blit is incorrectly grabbing data from renderDistance
   * @param renderDistance the MappableRingBuffer needed to transfer the desirable distance (default 128) to the shader.
   * @param fov the MappableRingBuffer needed to transfer data about the player's FOV to the shader.
   * @param distance the distance to be passed to the shader.
   * @param inTargetHandle the target whose depth is read.
   * @param outTargetHandle the target whose color gets the depth written to.
   */
  public static void blitDepth(MappableRingBuffer renderDistance, MappableRingBuffer fov, float distance, ResourceHandle<? extends RenderTarget> inTargetHandle, ResourceHandle<? extends RenderTarget> outTargetHandle) {
    RenderTarget inTarget = inTargetHandle.get();
    RenderTarget outTarget = outTargetHandle.get();
    GpuTextureView depthTextureViewI = inTarget.getDepthTextureView();
    GpuTextureView colorTextureViewO = outTarget.getColorTextureView();

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    if (depthTextureViewI == null) {
      VoidsentFlameMod.LOGGER.error("VF - blitDepth : THERE WAS NO IN DEPTH TEXTURE : {}", inTarget);
      return;
    }
    if (colorTextureViewO == null) {
      VoidsentFlameMod.LOGGER.error("VF - blitDepth : THERE WAS NO OUT COLOR TEXTURE : {}", outTarget);
      return;
    }
    if (distance <= 0) {
      VoidsentFlameMod.LOGGER.error("VF - blitDepth : DISTANCE WAS SMALLER THAN 0 : {}", distance);
      return;
    }

    VFGpuBuffers.UseFov(
      fov, Minecraft.getInstance().options.fov().get(), encoder
    );
    VFGpuBuffers.UseRenderDistance(
      renderDistance, distance, encoder
    );

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> "BlitDepth", colorTextureViewO, OptionalInt.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.DEPTH_BLIT);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("Fov", fov.currentBuffer());
      renderPass.setUniform("RenderDistance", renderDistance.currentBuffer());

      renderPass.bindSampler("SamplerDepth", depthTextureViewI);

      renderPass.setVertexBuffer(0, FullscreenQuadRenderer.INSTANCE.getQuad());
      renderPass.setIndexBuffer(FullscreenQuadRenderer.INSTANCE.getQuad(), VertexFormat.IndexType.SHORT);
      renderPass.draw(0, FullscreenQuadRenderer.INSTANCE.getIndex());
    }
  }

  /**
   * Copies and linearizes the depth from one target into another. Starts off white near the player and ends black far away.
   * FIXME : blit is incorrectly grabbing data from renderDistance
   * @param renderDistance the MappableRingBuffer needed to transfer the desirable distance (default 128) to the shader.
   * @param fov the MappableRingBuffer needed to transfer data about the player's FOV to the shader.
   * @param inTargetHandle the target whose depth is read.
   * @param outTargetHandle the target whose color gets the depth written to.
   */
  public static void blitInverseDepth(MappableRingBuffer renderDistance, MappableRingBuffer fov, ResourceHandle<? extends RenderTarget> inTargetHandle, ResourceHandle<? extends RenderTarget> outTargetHandle) {
    blitInverseDepth(renderDistance, fov, 128.f, inTargetHandle, outTargetHandle);
  }

  /**
   * Copies and linearizes the depth from one target into another. Starts off white near the player and ends black far away.
   * FIXME : blit is incorrectly grabbing data from renderDistance
   * @param renderDistance the MappableRingBuffer needed to transfer the desirable distance (default 128) to the shader.
   * @param fov the MappableRingBuffer needed to transfer data about the player's FOV to the shader.
   * @param distance the distance to be passed to the shader.
   * @param inTargetHandle the target whose depth is read.
   * @param outTargetHandle the target whose color gets the depth written to.
   */
  public static void blitInverseDepth(MappableRingBuffer renderDistance, MappableRingBuffer fov, float distance, ResourceHandle<? extends RenderTarget> inTargetHandle, ResourceHandle<? extends RenderTarget> outTargetHandle) {
    RenderTarget inTarget = inTargetHandle.get();
    RenderTarget outTarget = outTargetHandle.get();
    GpuTextureView depthTextureViewI = inTarget.getDepthTextureView();
    GpuTextureView colorTextureViewO = outTarget.getColorTextureView();

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    if (depthTextureViewI == null) {
      VoidsentFlameMod.LOGGER.error("VF - blitInverseDepth : THERE WAS NO IN DEPTH TEXTURE : {}", inTarget);
      return;
    }
    if (colorTextureViewO == null) {
      VoidsentFlameMod.LOGGER.error("VF - blitInverseDepth : THERE WAS NO OUT COLOR TEXTURE : {}", outTarget);
      return;
    }
    if (distance <= 0) {
      VoidsentFlameMod.LOGGER.error("VF - blitInverseDepth : DISTANCE WAS SMALLER THAN 0 : {}", distance);
      return;
    }

    VFGpuBuffers.UseFov(
      fov, Minecraft.getInstance().options.fov().get(), encoder
    );
    VFGpuBuffers.UseRenderDistance(
      renderDistance, distance, encoder
    );

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> "BlitInverseDepth", colorTextureViewO, OptionalInt.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.INVERSE_DEPTH_BLIT);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("Fov", fov.currentBuffer());
      renderPass.setUniform("RenderDistance", renderDistance.currentBuffer());

      renderPass.bindSampler("SamplerDepth", depthTextureViewI);

      renderPass.setVertexBuffer(0, FullscreenQuadRenderer.INSTANCE.getQuad());
      renderPass.setIndexBuffer(FullscreenQuadRenderer.INSTANCE.getQuad(), VertexFormat.IndexType.SHORT);
      renderPass.draw(0, FullscreenQuadRenderer.INSTANCE.getIndex());
    }
  }
}
