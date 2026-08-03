package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.VFGpuBuffers;
import com.goopey.voidsentflame.core.VFRenderPipelines;
import com.goopey.voidsentflame.util.RenderHelper;
import com.goopey.voidsentflame.world.dimension.RubiconDimension;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

import java.util.List;
import java.util.OptionalInt;

public class RubiconFogRenderer implements ResourceManagerReloadListener, AutoCloseable {
  public static final String NAME = "rubicon_fog";
  public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/" + NAME + ".reload");
  public static final RubiconFogRenderer INSTANCE = new RubiconFogRenderer();

  private final Minecraft mc = Minecraft.getInstance();
  private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);

  private MappableRingBuffer fov;
  private MappableRingBuffer renderDistance;
  private final RenderTarget mainTarget;
  private ResourceHandle<RenderTarget> mainTargetHandle;
  private final TextureTarget depthTarget;
  private ResourceHandle<TextureTarget> depthTargetHandle;
  private final TextureTarget skyBoxTarget;
  private ResourceHandle<TextureTarget> skyBoxTargetHandle;

  private RubiconFogRenderer() {
    this.mainTarget = Minecraft.getInstance().getMainRenderTarget();
    this.depthTarget = new TextureTarget(
      "VoidFogDepthTexture",
      this.mainTarget.width,
      this.mainTarget.height,
      true
    );
    this.depthTarget.copyDepthFrom(this.mainTarget);
    this.skyBoxTarget = new TextureTarget(
      "VoidFogDepthTexture",
      this.mainTarget.width,
      this.mainTarget.height,
      true
    );
    this.skyBoxTarget.copyDepthFrom(this.mainTarget);
  }

  /**
   * This method takes care of automatically closing all GpuBuffers in this class.
   */
  @Override
  public void close() {
    this.fov.close();
    this.renderDistance.close();
  }

  /**
   * Used to initialize the resources needed by the FogRenderer when the game reloads assets
   * @param resourceManager Minecraft's resource manager. Provides controlled access to the game's files while running.
   */
  @Override
  public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
    this.fov = VFGpuBuffers.VFFovUbo.get();
    this.renderDistance = VFGpuBuffers.VFRenderDistanceUbo.get();
  }

  //######################################################
  //                  RENDER STUFF
  //######################################################

  /**
   * Main method. Called when attempting to render the sky in any biome in the Rubicon dimension.
   * @param event the event bus event. Needed to obtain poseStack, matrixStack and other critical objects.
   */
  public void render(RenderLevelStageEvent.AfterTranslucentBlocks event) {
    // Check if in proper biomes
    Minecraft mc = Minecraft.getInstance();
    Level level = mc.level;
    if (!RenderSystem.isOnRenderThread()) { return; }
    if (level == null) { return; }
    if (level.dimension() != RubiconDimension.RUBICON) { return; }

    // get poseStack to start rendering
    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();
    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    matrix4fStack.pushMatrix();

    FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
    this.mainTargetHandle = frameGraphBuilder.importExternal("minecraft:main", this.mainTarget);
    this.depthTargetHandle = frameGraphBuilder.importExternal(VoidsentFlameMod.MODID + ":VoidFogDepthTexHandle", this.depthTarget);
    this.skyBoxTargetHandle = frameGraphBuilder.importExternal(VoidsentFlameMod.MODID + ":SkyBoxTargetHandle", this.skyBoxTarget);

    FramePass pass1 = frameGraphBuilder.addPass(VoidsentFlameMod.MODID + ":VoidFogClearAndResize");
    this.mainTargetHandle = pass1.readsAndWrites(this.mainTargetHandle);
    this.depthTargetHandle = pass1.readsAndWrites(this.depthTargetHandle);
    pass1.executes(
      () -> RenderHelper.clearAndResizeTargetsWhite(this.mainTargetHandle, List.of(
        this.depthTargetHandle
      ))
    );

//    FramePass pass2 = frameGraphBuilder.addPass(VoidsentFlameMod.MODID + ":VoidFogSkyBoxBlackout");
//    this.mainTargetHandle = pass2.readsAndWrites(this.mainTargetHandle);

    FramePass pass3 = frameGraphBuilder.addPass(VoidsentFlameMod.MODID + ":VoidFogGetMainDepth");
    pass3.requires(pass1);
    this.mainTargetHandle = pass3.readsAndWrites(this.mainTargetHandle);
    this.depthTargetHandle = pass3.readsAndWrites(this.depthTargetHandle);
//    pass3.executes(
//      () -> RenderHelper.blitInverseDepth(this.renderDistance, this.fov, 4, this.mainTargetHandle, this.depthTargetHandle)
//    );
    pass3.executes(
      () -> this.addDepthPass(this.mainTargetHandle, this.depthTargetHandle)
    );

    FramePass pass4 = frameGraphBuilder.addPass(VoidsentFlameMod.MODID + ":VoidFogCopyDepthToMain");
    pass4.requires(pass3);
    this.mainTargetHandle = pass4.readsAndWrites(this.mainTargetHandle);
    this.depthTargetHandle = pass4.readsAndWrites(this.depthTargetHandle);
    pass4.executes(
      () -> RenderHelper.blitAToB(this.depthTargetHandle, this.mainTargetHandle)
    );

    frameGraphBuilder.execute(this.resourcePool);
    matrix4fStack.popMatrix();
    poseStack.popPose();
  }

  //##############################################
  //            RENDER HELPER METHODS
  //##############################################

  /**
   * Manages reading the depth information from the game and converting it into a texture that'll be used later.
   * @param targetInHandle The main game's render target. We're reading data from the terrain here.
   * @param targetOutHandle The out target to be used in another process.
   */
  public void addDepthPass(ResourceHandle<? extends RenderTarget> targetInHandle, ResourceHandle<? extends RenderTarget> targetOutHandle) {
    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
    RenderTarget target = targetInHandle.get();
    GpuTextureView depthTextureViewT = target.getDepthTextureView();
    RenderTarget outTarget = targetOutHandle.get();
    GpuTextureView colorTextureViewO = outTarget.getColorTextureView();

    // setup special uniforms
    VFGpuBuffers.UseFov(
      this.fov, this.mc.options.fov().get(), encoder
    );

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> VoidsentFlameMod.MODID + ":VoidDepthFog", colorTextureViewO, OptionalInt.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.VOID_FOG_DEPTH_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);

      renderPass.bindSampler("SamplerDepth", depthTextureViewT);
      renderPass.setUniform("Fov", this.fov.currentBuffer());

      renderPass.setVertexBuffer(0, FullscreenQuadRenderer.INSTANCE.getQuad());
      renderPass.setIndexBuffer(FullscreenQuadRenderer.INSTANCE.getQuad(), VertexFormat.IndexType.SHORT);
      renderPass.draw(0, FullscreenQuadRenderer.INSTANCE.getIndex());
    }
  }
}
