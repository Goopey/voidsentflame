package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.VFGpuBuffers;
import com.goopey.voidsentflame.core.VFRenderPipelines;
import com.goopey.voidsentflame.util.BufferBuilderHelper;
import com.goopey.voidsentflame.util.RenderHelper;
import com.goopey.voidsentflame.world.dimension.RubiconDimension;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
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
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class RubiconFogRenderer implements ResourceManagerReloadListener, AutoCloseable {
  public static final String NAME = "rubicon_fog";
  public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/" + NAME + ".reload");
  public static final RubiconFogRenderer INSTANCE = new RubiconFogRenderer();
  private static final int BOX_SIZE = 256;

  private final Minecraft mc = Minecraft.getInstance();
  private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);

  private MappableRingBuffer fov;
  private MappableRingBuffer renderDistance;
  private final RenderTarget mainTarget;
  private ResourceHandle<RenderTarget> mainTargetHandle;
  private final TextureTarget depthTarget;
  private ResourceHandle<TextureTarget> depthTargetHandle;
  private final TextureTarget swapTarget;
  private ResourceHandle<TextureTarget> swapTargetHandle;
  private GpuBuffer skyBoxMesh;
  private int skyBoxIndex;

  private RubiconFogRenderer() {
    this.mainTarget = Minecraft.getInstance().getMainRenderTarget();
    this.depthTarget = new TextureTarget(
      "VoidFogDepthTexture",
      this.mainTarget.width,
      this.mainTarget.height,
      true
    );
    this.depthTarget.copyDepthFrom(this.mainTarget);
    this.swapTarget = new TextureTarget(
      "SwapTarget",
      this.mainTarget.width,
      this.mainTarget.height,
      true
    );
    this.swapTarget.copyDepthFrom(this.mainTarget);
  }

  /**
   * This method takes care of automatically closing all GpuBuffers in this class.
   */
  @Override
  public void close() {
    this.fov.close();
    this.renderDistance.close();
    this.skyBoxMesh.close();
  }

  /**
   * Used to initialize the resources needed by the FogRenderer when the game reloads assets
   * @param resourceManager Minecraft's resource manager. Provides controlled access to the game's files while running.
   */
  @Override
  public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
    this.fov = VFGpuBuffers.VFFovUbo.get();
    this.renderDistance = VFGpuBuffers.VFRenderDistanceUbo.get();
    Tuple<Integer, GpuBuffer> skyBox = BufferBuilderHelper.buildBox(
      VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, BOX_SIZE
    );
    this.skyBoxIndex = skyBox.getA();
    this.skyBoxMesh = skyBox.getB();
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
    this.swapTargetHandle = frameGraphBuilder.importExternal(VoidsentFlameMod.MODID + ":VoidFogSwapMainHandle", this.swapTarget);

    FramePass pass1 = frameGraphBuilder.addPass(VoidsentFlameMod.MODID + ":VoidFogClearAndResize");
    this.mainTargetHandle = pass1.readsAndWrites(this.mainTargetHandle);
    this.depthTargetHandle = pass1.readsAndWrites(this.depthTargetHandle);
    this.swapTargetHandle = pass1.readsAndWrites(this.swapTargetHandle);
    pass1.executes(
      () -> RenderHelper.clearAndResizeTargetsWhite(this.mainTargetHandle, List.of(
        this.depthTargetHandle, this.swapTargetHandle
      ))
    );

    FramePass pass2 = frameGraphBuilder.addPass(VoidsentFlameMod.MODID + ":VoidFogGetMainDepth");
    pass2.requires(pass1);
    this.mainTargetHandle = pass2.readsAndWrites(this.mainTargetHandle);
    this.depthTargetHandle = pass2.readsAndWrites(this.depthTargetHandle);
    pass2.executes(
      () -> RenderHelper.blitDepth(this.renderDistance, this.fov, 128.f, this.mainTargetHandle, this.depthTargetHandle)
    );

    FramePass pass3 = frameGraphBuilder.addPass(VoidsentFlameMod.MODID + ":VoidFogSkyBoxBlackout");
    pass3.requires(pass2);
    this.depthTargetHandle = pass3.readsAndWrites(this.depthTargetHandle);
    pass3.executes(
      () -> this.addSkyBoxPass(this.depthTargetHandle, matrix4fStack)
    );

    // this step is necessary to avoid writing conflicts in the final step to create fog.
    FramePass pass4 = frameGraphBuilder.addPass(VoidsentFlameMod.MODID + ":VoidFogSwapMainTarget");
    pass4.requires(pass1);
    this.mainTargetHandle = pass4.readsAndWrites(this.mainTargetHandle);
    this.swapTargetHandle = pass4.readsAndWrites(this.swapTargetHandle);
    pass4.executes(
      () -> RenderHelper.blitAToB(this.mainTargetHandle, this.swapTargetHandle)
    );

    FramePass pass5 = frameGraphBuilder.addPass(VoidsentFlameMod.MODID + ":VoidFogCopyDepthToMain");
    pass5.requires(pass3);
    pass5.requires(pass4);
    this.mainTargetHandle = pass5.readsAndWrites(this.mainTargetHandle);
    this.swapTargetHandle = pass5.readsAndWrites(this.swapTargetHandle);
    this.depthTargetHandle = pass5.readsAndWrites(this.depthTargetHandle);
    pass5.executes(
      () -> this.addFogPass(this.mainTargetHandle, this.swapTargetHandle, this.depthTargetHandle)
    );

    frameGraphBuilder.execute(this.resourcePool);
    matrix4fStack.popMatrix();
    poseStack.popPose();
  }

  //##############################################
  //            RENDER HELPER METHODS
  //##############################################

  /**
   * The depth texture creates a weird circle in the sky at certain lengths. This skybox is meant to cut that
   * circle out and isolate the terrain's depth texture.
   * @param targetHandle the target data is being read from and written to. In this case, the supposed
   *                     colorized/linearized depth buffer.
   * @param matrix4fStack the matrix stack. Needed for matrix transformations.
   */
  public void addSkyBoxPass(ResourceHandle<? extends RenderTarget> targetHandle, Matrix4fStack matrix4fStack) {
    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
    RenderTarget target = targetHandle.get();
    GpuTextureView colorTextureView = target.getColorTextureView();
    GpuTextureView depthTextureView = target.getDepthTextureView();

    GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
      matrix4fStack,
      new Vector4f(1f, 1f, 1f, 1f),
      new Vector3f(0f, 0f, 0f),
      new Matrix4f(),
      0.0F
    );

    if (colorTextureView == null) {
      return;
    }

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> VoidsentFlameMod.MODID + ":VoidFogSkyBox", colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.VOID_FOG_SKYBOX_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);

      renderPass.setVertexBuffer(0, this.skyBoxMesh);
      renderPass.setIndexBuffer(this.skyBoxMesh, VertexFormat.IndexType.SHORT);
      renderPass.draw(0, this.skyBoxIndex);
    }
  }

  /**
   * One of the final passes. Combines the depth texture and the game's color to recreate fog.
   * @param targetHandle the main target. Data will be written to this target.
   * @param colorHandle the target color is being read from. Needs to be distinguished from the writing target
   *                    because it causes conflicts and can potentially write color twice.
   * @param depthHandle the target the linearized/colorized depth texture is taken from.
   */
  public void addFogPass(ResourceHandle<RenderTarget> targetHandle, ResourceHandle<TextureTarget> colorHandle, ResourceHandle<TextureTarget> depthHandle) {
    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
    RenderTarget target = targetHandle.get();
    TextureTarget colorTarget = colorHandle.get();
    TextureTarget depthTarget = depthHandle.get();
    GpuTextureView colorTextureView = target.getColorTextureView();
    GpuTextureView colorTextureViewC = colorTarget.getColorTextureView();
    GpuTextureView depthTextureViewD = depthTarget.getColorTextureView();

    if (colorTextureView == null) {
      return;
    }

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> VoidsentFlameMod.MODID + ":VoidFog", colorTextureView, OptionalInt.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.VOID_FOG_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);

      renderPass.bindSampler("SamplerWorld", colorTextureViewC);
      renderPass.bindSampler("SamplerDepth", depthTextureViewD);

      renderPass.setVertexBuffer(0, FullscreenQuadRenderer.INSTANCE.getQuad());
      renderPass.setIndexBuffer(FullscreenQuadRenderer.INSTANCE.getQuad(), FullscreenQuadRenderer.VERTEX_FORMAT);
      renderPass.draw(0, FullscreenQuadRenderer.INSTANCE.getIndex());
    }
  }
}
