package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.client.render.sky.CrucibleSkyRenderer;
import com.goopey.voidsentflame.core.VFRenderPipelines;
import com.goopey.voidsentflame.util.VFRenderConsts;
import com.goopey.voidsentflame.util.VertexMeshHelper;
import com.goopey.voidsentflame.world.dimension.RubiconDimension;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.IRenderableSection;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fStack;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class RubiconSkyRenderer implements ResourceManagerReloadListener, AutoCloseable {
  public static final String NAME = "rubicon_sky";
  public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/" + NAME + ".reload");
  public static RubiconSkyRenderer INSTANCE = new RubiconSkyRenderer();

  // all the different sky renderers this main SkyRenderer class manages
  public CrucibleSkyRenderer crucibleSky = new CrucibleSkyRenderer();
  private int screenIndex;
  private GpuBuffer screenBuffer;
  public GpuTextureView importTex;

  private RubiconSkyRenderer() {
    Tuple<Integer, GpuBuffer> screen = VertexMeshHelper.buildScreen(VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY);
    this.screenIndex = screen.getA();
    this.screenBuffer = screen.getB();
  }

  /**
   * This method takes care of automatically closing all skyRenderer classes this main class manages.
   */
  @Override
  public void close() {
    this.crucibleSky.close();
    this.screenBuffer.close();
  }

  /**
   * Used to initialize the resources needed by a skyRenderer when the game reloads assets
   * @param resourceManager Minecraft's resource manager. Provides controlled access to the game's files while running.
   */
  @Override
  public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
    Tuple<Integer, GpuBuffer> screen = VertexMeshHelper.buildScreen(VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY);
    this.screenIndex = screen.getA();
    this.screenBuffer = screen.getB();
    this.crucibleSky.onResourceManagerReload(resourceManager);
  }

  //######################################################
  //                  RENDER STUFF
  //######################################################

  /**
   * Main method. Called when attempting to render the sky in any biome in the Rubicon dimension.
   * @param event the event bus event. Needed to obtain poseStack, matrixStack and other critical objects.
   */
  public void render(RenderLevelStageEvent.AfterOpaqueBlocks event) {
    // Check if in proper biomes
    Minecraft mc = Minecraft.getInstance();
    Level level = mc.level;
    if (!RenderSystem.isOnRenderThread()) { return; }
    if (level == null) { return; }
    if (this.importTex == null) { return; }
    if (level.dimension() != RubiconDimension.RUBICON) { return; }

    // get poseStack to start rendering
    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();
    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    matrix4fStack.pushMatrix();

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
    RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
    GpuTextureView colorTextureViewT = target.getColorTextureView();
    GpuTextureView depthTextureViewT = target.getDepthTextureView();
    LevelRenderer e;
//
//    Iterator<? extends IRenderableSection> a = event.getRenderableSections().iterator();
//    for (Iterator<? extends IRenderableSection> it = a; it.hasNext(); ) {
//      IRenderableSection i = it.next();
//    }

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> "BlitDepthTex", colorTextureViewT, OptionalInt.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.DEPTH_BLIT_PIPELINE);

      renderPass.bindSampler("SamplerIn", colorTextureViewT);
      renderPass.bindSampler("SamplerDepth", depthTextureViewT);

      renderPass.setVertexBuffer(0, this.screenBuffer);
      renderPass.setIndexBuffer(this.screenBuffer, VertexFormat.IndexType.SHORT);
      renderPass.draw(0, this.screenIndex);
    }

    matrix4fStack.popMatrix();
    poseStack.popPose();
  }
}
