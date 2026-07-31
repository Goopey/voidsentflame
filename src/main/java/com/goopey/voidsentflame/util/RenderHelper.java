package com.goopey.voidsentflame.util;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.client.render.FullscreenQuadRenderer;
import com.goopey.voidsentflame.core.VFRenderPipelines;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
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
  public static void clearAndResizeTargetsWhite(ResourceHandle<RenderTarget> mainTargetHandle, List<ResourceHandle<? extends RenderTarget>> targetHandles) {
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
  public static void blitAToB(ResourceHandle<RenderTarget> inTargetHandle, ResourceHandle<RenderTarget> outTargetHandle) {
    RenderTarget inTarget = inTargetHandle.get();
    RenderTarget outTarget = outTargetHandle.get();
    GpuTextureView colorTextureViewI = inTarget.getColorTextureView();
    GpuTextureView colorTextureViewO = outTarget.getColorTextureView();

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    if (colorTextureViewI == null) {
      VoidsentFlameMod.LOGGER.error("VF - blitAToB : THERE WAS NO IN COLOR TEXTURE {}", inTarget);
      return;
    }
    if (colorTextureViewO == null) {
      VoidsentFlameMod.LOGGER.error("VF - blitAToB : THERE WAS NO OUT COLOR TEXTURE {}", outTarget);
      return;
    }

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> "VoidSeaDistort", colorTextureViewO, OptionalInt.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.BLIT_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);

      renderPass.bindSampler("SamplerIn", colorTextureViewI);

      renderPass.setVertexBuffer(0, FullscreenQuadRenderer.INSTANCE.getQuad());
      renderPass.setIndexBuffer(FullscreenQuadRenderer.INSTANCE.getQuad(), VertexFormat.IndexType.SHORT);
      renderPass.draw(0, FullscreenQuadRenderer.INSTANCE.getIndex());
    }
  }
}
