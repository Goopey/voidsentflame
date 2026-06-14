package com.goopey.voidsentflame.client.render;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.*;

import com.goopey.voidsentflame.core.VFGpuBuffers;
import com.goopey.voidsentflame.core.VFGpuBuffers.VFGpuBuffersNames;
import com.goopey.voidsentflame.util.VertexMeshHelper;
import com.goopey.voidsentflame.world.dimension.RubiconDimension;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import net.minecraft.client.renderer.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.VFRenderPipelines;
import com.goopey.voidsentflame.util.VFRenderConsts;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import static com.goopey.voidsentflame.world.dimension.RubiconDimension.VoidSeaConstants;

public class VoidSeaRenderer implements ResourceManagerReloadListener, AutoCloseable {
  // Singleton Instance
  private static final VoidSeaRenderer INSTANCE = new VoidSeaRenderer();

  // ResourceManagerReloadListener event listener
  public static final String NAME = "void_sea";
  public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/" + NAME + ".reload");

  // Render Triangles
  private static final int QUAD_SIZE = 3;
  private static final float PADDING = 1.1f;

  // Sprite/Model Stuff
  private GpuTextureView[] GPU_SPRITE_ANIM_VIEW;
  private final static String SPRITE_NAME = "void_fluid";
  
  // Shader Stuff
  // MAGIC NUMBER, DO NOT CHANGE
  // (int) (((2 * OFFSET * PADDING/ QUAD_SIZE) * (2 * OFFSET * PADDING/ QUAD_SIZE)) * 6);
  private static final int AMOUNT_OF_VERTICES = 155574;
  private GpuBuffer seaMeshBuffer;
  private int seaMeshIndex;
  private GpuBuffer bottomDistortionBuffer;
  private int bottomDistortionIndex;
  // TODO : implement distortionGradient
//  private final GpuBuffer distortionGradientBuffer;
//  private int distortionGradientIndex;
//  private final GpuBuffer distortionGradientLayerBuffer;
//  private int distortionGradientLayerIndex;
  private GpuBuffer screenBuffer;
  private int screenIndex;
  private MappableRingBuffer positionBuffer;
  private MappableRingBuffer positionBuffer2;
  // blend targets
  private final TextureTarget blendTarget;
  private ResourceHandle<TextureTarget> blendTargetHandle;
  private final TextureTarget seaTarget;
  private ResourceHandle<TextureTarget> seaTargetHandle;
  private final RenderTarget mainTarget;
  private ResourceHandle<RenderTarget> mainTargetHandle;
  private final TextureTarget distortionTarget;
  private ResourceHandle<TextureTarget> distortionTargetHandle;
  private final TextureTarget distortionGradientTarget;
  private ResourceHandle<TextureTarget> distortionGradientTargetHandle;
  private GpuTextureView blackTextureView;
  private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);

  // Render Distance tracking
  private final double DEFAULT_RENDER_DISTANCE = 12.;
  private double lastRenderDistance = DEFAULT_RENDER_DISTANCE;

  //#################################################
  //                 INSTANCE
  //#################################################

  private VoidSeaRenderer() {// these values will be resized later
    this.mainTarget = Minecraft.getInstance().getMainRenderTarget();
    this.blendTarget = new TextureTarget(
      "VoidSeaBlendTexture",
      this.mainTarget.width,
      this.mainTarget.height,
      true
    );
    this.blendTarget.copyDepthFrom(this.mainTarget);
    this.distortionTarget = new TextureTarget(
      "VoidSeaDistortionTexture",
      this.mainTarget.width,
      this.mainTarget.height,
      true
    );
    this.distortionTarget.copyDepthFrom(this.mainTarget);
    this.distortionGradientTarget = new TextureTarget(
      "VoidSeaDistortionGradientTexture",
      this.mainTarget.width,
      this.mainTarget.height,
      true
    );
    this.distortionGradientTarget.copyDepthFrom(this.mainTarget);
    this.seaTarget = new TextureTarget(
      "VoidSeaSeaTexture",
      this.mainTarget.width,
      this.mainTarget.height,
      true
    );
    this.seaTarget.copyDepthFrom(this.mainTarget);
  }

  public static VoidSeaRenderer getInstance() {
    return INSTANCE;
  }

  /**
   * This method takes care of automatically closing all skyRenderer classes this main class manages.
   */
  @Override
  public void close() {
    this.closeSprites();
    this.blackTextureView.close();
//    this.distortionGradientBuffer.close();
//    this.distortionGradientLayerBuffer.close();
    this.seaMeshBuffer.close();
    this.screenBuffer.close();
    this.bottomDistortionBuffer.close();
    this.positionBuffer.close();
    this.positionBuffer2.close();
  }

  /**
   * Used to initialize the resources needed by a skyRenderer when the game reloads assets
   * @param resourceManager Minecraft's resource manager. Provides controlled access to the game's files while running.
   */
  @Override
  public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
    this.getSprites();
    buildSea();
    buildScreen();
    buildBottomDistortion();
//    this.distortionGradientBuffer = buildDistortionGradientBox();
//    this.distortionGradientLayerBuffer = buildDistortionGradientLayer();
    this.positionBuffer = VFGpuBuffers.VFWorldPosUbo.get();
    this.positionBuffer2 = VFGpuBuffers.VFWorldPosUbo.get();
  }

  //######################################################
  //                  RENDER STUFF
  //######################################################

  /**
   * Main method. Called when creating the Void Sea visual effect. Manages calling renderPasses, checking dimension,
   * passing textures.
   * @param event the event bus event. Needed to
   */
  public void render(RenderLevelStageEvent.AfterParticles event) {
    // Check if in Rubicon
    Minecraft mc = Minecraft.getInstance();
    Level level = mc.level;
    LevelRenderer levelRenderer = event.getLevelRenderer();
    if (!RenderSystem.isOnRenderThread()) { return; }
    if (level == null) { return; }
    if (level.dimension() != RubiconDimension.RUBICON) { return; }

    double levelRendererDistance = Math.max(1, levelRenderer.getLastViewDistance());
    if (levelRendererDistance != lastRenderDistance) {
      lastRenderDistance = levelRendererDistance;
      buildSea();
    }
    // account for which frame of the animation the texture is
    int frame = (int) (level.getGameTime() % 20) / 4;

    // get poseStack to start rendering
    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();

    // FrameGraphBuilder needed to run multiple passes
    FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
    this.mainTargetHandle = frameGraphBuilder.importExternal("minecraft:main", this.mainTarget);

    // scale size depending on render distance
    // TODO : needs readjusting
    float renderDistanceScale = (float) (Math.max(levelRenderer.getLastViewDistance(), 12) / VoidSeaConstants.VIEW_DISTANCE_SCALE);
    renderDistanceScale = renderDistanceScale * renderDistanceScale * 1.2f;

    // get cameraPos and lock the wave model at the proper height in the world
    Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
    float deltaTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    Vec3 cameraPos = cameraEntity == null ? new Vec3(0, 0, 0) : cameraEntity.getPosition(deltaTick);

    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    matrix4fStack.pushMatrix();
    matrix4fStack.scale(renderDistanceScale, 1, renderDistanceScale);

    // TODO : remove this
    // avoid crashes if the sprites were cleared 
//    if (!this.GPU_SPRITE_ANIM_VIEW[frame].isClosed()) {
      this.blendTargetHandle = frameGraphBuilder.importExternal("VoidSeaBlendTexHandle", this.blendTarget);
      this.seaTargetHandle = frameGraphBuilder.importExternal("VoidSeaSeaTexHandle", this.seaTarget);
      this.distortionTargetHandle = frameGraphBuilder.importExternal("VoidSeaDistortHandle", this.distortionTarget);
      this.distortionGradientTargetHandle = frameGraphBuilder.importExternal("VoidSeaDistortGradientHandle", this.distortionGradientTarget);

      FramePass pass1 = frameGraphBuilder.addPass("resizeClearCopyPass1");
      this.seaTargetHandle = pass1.readsAndWrites(this.seaTargetHandle);
      this.blendTargetHandle = pass1.readsAndWrites(this.blendTargetHandle);
      this.mainTargetHandle = pass1.readsAndWrites(this.mainTargetHandle);
      this.distortionTargetHandle = pass1.readsAndWrites(this.distortionTargetHandle);
      this.distortionGradientTargetHandle = pass1.readsAndWrites(this.distortionGradientTargetHandle);
      pass1.executes(
        () -> clearAndResizeTargets(this.mainTargetHandle, List.of(
          this.blendTargetHandle, this.seaTargetHandle, this.distortionTargetHandle, this.distortionGradientTargetHandle
        ))
      );

      FramePass pass2 = frameGraphBuilder.addPass("VoidSeaMeshPass2");
      pass2.requires(pass1);
      this.seaTargetHandle = pass2.readsAndWrites(this.seaTargetHandle);
      pass2.executes(
        () -> this.renderSea(cameraPos, matrix4fStack, this.GPU_SPRITE_ANIM_VIEW[frame], this.seaTargetHandle)
      );

      FramePass pass3 = frameGraphBuilder.addPass("VoidSeaMeshDistortPass3");
      pass3.requires(pass1);
      this.distortionTargetHandle = pass3.readsAndWrites(this.distortionTargetHandle);
      pass3.executes(
        () -> this.renderDistortion(cameraPos, matrix4fStack, this.blackTextureView, this.distortionTargetHandle)
      );

      //TODO : Fix gradient
//      FramePass pass4 = frameGraphBuilder.addPass("VoidSeaMeshDistortGradientPass4");
//      pass4.requires(pass1);
//      this.distortionGradientTargetHandle = pass4.readsAndWrites(this.distortionGradientTargetHandle);
//      pass4.executes(
//        () -> this.renderDistortionGradient(cameraPos, matrix4fStack, this.distortionGradientTargetHandle)
//      );

      FramePass pass5 = frameGraphBuilder.addPass("VoidSeaBlendPass5");
      pass5.requires(pass2);
      pass5.requires(pass3);
//      pass5.requires(pass4);
      this.seaTargetHandle = pass5.readsAndWrites(this.seaTargetHandle);
      this.mainTargetHandle = pass5.readsAndWrites(this.mainTargetHandle);
      this.blendTargetHandle = pass5.readsAndWrites(this.blendTargetHandle);
      pass5.executes(
        () -> this.renderBlitAndBlend(this.blendTargetHandle, this.seaTargetHandle, this.mainTargetHandle)
      );

      FramePass pass6 = frameGraphBuilder.addPass("VoidSeaDistortPass6");
      pass6.requires(pass5);
      this.mainTargetHandle = pass6.readsAndWrites(this.mainTargetHandle);
      this.blendTargetHandle = pass6.readsAndWrites(this.blendTargetHandle);
      this.seaTargetHandle = pass6.readsAndWrites(this.seaTargetHandle);
      this.distortionTargetHandle = pass6.readsAndWrites(this.distortionTargetHandle);
//      this.distortionGradientTargetHandle = pass6.readsAndWrites(this.distortionGradientTargetHandle);
      pass6.executes(
        () -> this.renderHeatWave(cameraPos, this.mainTargetHandle, this.seaTargetHandle, this.blendTargetHandle, this.distortionTargetHandle)
      );
      //TODO : remove this
//    } else {
//      this.getSprites();
//    }

    frameGraphBuilder.execute(this.resourcePool);
    matrix4fStack.popMatrix();
    poseStack.popPose();
  }

  //##############################################
  //            RENDER HELPER METHODS
  //##############################################

  /**
   * This method manages drawing a textured mesh of the void sea at a fixed height in the world.
   * @param cameraPos the position of the camera. Needed to place the void sea correctly vertically in the world.
   * @param matrix4fStack the stack of matrices to do projections with to correctly portray 3d objects
   * @param frame the frame of the texture to be used by the void sea. Called frame because we suppose it's animated
   *              and changes often.
   * @param targetHandle the resourceHandle which contains the target where the mixed image will be written to
   */
  private void renderSea(Vec3 cameraPos, Matrix4fStack matrix4fStack, GpuTextureView frame, ResourceHandle<? extends RenderTarget> targetHandle) {
    RenderTarget target = targetHandle.get();
    GpuTextureView colorTextureView = target.getColorTextureView();
    GpuTextureView depthTextureView = target.getDepthTextureView();

    // setup dynamic uniforms
    GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
      matrix4fStack,
      new Vector4f(1f, 1f, 1f, 1f),
      new Vector3f(0f, 0f, 0f),
      new Matrix4f(),
      0.0F
    );

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    // setup other special uniforms
    VFGpuBuffers.UseWorldPos(
      this.positionBuffer,
      new Vector3f((float) cameraPos.x, (float) (VoidSeaConstants.HEIGHT - cameraPos.y), (float) cameraPos.z),
      encoder
    );

    if (colorTextureView == null) { return; }
    // setup render pass and actually use it
    try (RenderPass renderPass = encoder.createRenderPass(() -> "VoidSea", colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())) {
      renderPass.setPipeline(VFRenderPipelines.VOID_SEA_MESH_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
      // See void_sea_mesh_vert.vsh
      renderPass.setUniform("ChunkOffset", this.positionBuffer.currentBuffer());

      renderPass.bindSampler("Sampler0", frame);
      renderPass.bindSampler("Sampler1", frame);

      renderPass.setVertexBuffer(0, this.seaMeshBuffer);
      renderPass.setIndexBuffer(this.seaMeshBuffer, VertexFormat.IndexType.SHORT);
      renderPass.draw(0, this.seaMeshIndex);
    }
  }

  /**
   * A method similar to renderSea but without a depthBuffer. Helps build the part of the world distortion effects that will be applied later.
   * @param cameraPos the position of the camera/player. Used to lock the wave at the proper height in the world.
   * @param matrix4fStack the orientation of the camera and other things. Critical for objects to stay in their proper position/orientation.
   * @param texture the white texture
   * @param targetHandle the target stuff will be rendered to
   */
  private void renderDistortion(Vec3 cameraPos, Matrix4fStack matrix4fStack, GpuTextureView texture, ResourceHandle<? extends RenderTarget> targetHandle) {
    RenderTarget target = targetHandle.get();
    GpuTextureView colorTextureView = target.getColorTextureView();
    GpuTextureView depthTextureView = target.getDepthTextureView();

    // setup dynamic uniforms
    GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
      matrix4fStack,
      new Vector4f(1f, 1f, 1f, 1f),
      new Vector3f(0f, 0f, 0f),
      new Matrix4f(),
      0.0F
    );

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    double height = VoidSeaConstants.HEAT_HEIGHT - cameraPos.y;

    // setup other special uniforms
    VFGpuBuffers.UseWorldPos(
      this.positionBuffer,
      new Vector3f((float) cameraPos.x, (float) (height), (float) cameraPos.z),
      encoder
    );

    if (colorTextureView == null) { return; }
    // setup render pass and actually use it
    try (RenderPass renderPass = encoder.createRenderPass(() -> "VoidSeaDistortTop", colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())) {
      if (height > 0) {
        renderPass.setPipeline(VFRenderPipelines.VOID_SEA_MESH_DISTORT_PIPELINE_B);
      } else {
        renderPass.setPipeline(VFRenderPipelines.VOID_SEA_MESH_DISTORT_PIPELINE_T);
      }
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
      // See void_sea_mesh_vert.vsh
      renderPass.setUniform(VFGpuBuffersNames.WORLD_POS.name, this.positionBuffer.currentBuffer());

      renderPass.bindSampler("Sampler0", texture);
      renderPass.bindSampler("Sampler1", texture);

      renderPass.setVertexBuffer(0, this.seaMeshBuffer);
      renderPass.setIndexBuffer(this.seaMeshBuffer, VertexFormat.IndexType.SHORT);
      renderPass.draw(0, this.seaMeshIndex);
    }

    // setup bottom sphere
    try (RenderPass renderPass = encoder.createRenderPass(() -> "VoidSeaDistortBottom", colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())) {
      if (height > 0) {
        renderPass.setPipeline(VFRenderPipelines.VOID_SEA_MESH_DISTORT_PIPELINE_B);
      } else {
        renderPass.setPipeline(VFRenderPipelines.VOID_SEA_MESH_DISTORT_PIPELINE_T);
      }
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
      renderPass.setUniform(VFGpuBuffersNames.WORLD_POS.name, this.positionBuffer.currentBuffer());

      renderPass.bindSampler("Sampler0", texture);
      renderPass.bindSampler("Sampler1", texture);

      renderPass.setVertexBuffer(0, this.bottomDistortionBuffer);
      renderPass.setIndexBuffer(this.bottomDistortionBuffer, VertexFormat.IndexType.SHORT);
      renderPass.draw(0, this.bottomDistortionIndex);
    }
  }

//  /**
//   * This method manages drawing a box which gets darker as it goes down in the world. This makes the distortion effect
//   * taper off as the blocks are lower in the world.
//   * @param cameraPos the position of the camera. Used to position objects vertically correctly.
//   * @param matrix4fStack the stack of matrices to do projections with to correctly portray 3d objects
//   * @param targetHandle the resourceHandle which contains the target where the mixed image will be written to
//   */
//  private void renderDistortionGradient(Vec3 cameraPos, Matrix4fStack matrix4fStack, ResourceHandle<? extends RenderTarget> targetHandle) {
//    RenderTarget target = targetHandle.get();
//    GpuTextureView colorTextureView = target.getColorTextureView();
//    GpuTextureView depthTextureView = target.getDepthTextureView();
//
//    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
//
//    // setup dynamic uniforms
//    GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
//      matrix4fStack,
//      new Vector4f(1f, 1f, 1f, 1f),
//      new Vector3f(0f, 0f, 0f),
//      new Matrix4f(),
//      0.0F
//    );
//
//    int height = (int) (VoidSeaConstants.HEAT_HEIGHT - VoidSeaConstants.HEIGHT);
//    for (int i = 0; i < height; i++) {
//      int val = height - i;
//      // don't render layers above the camera. Distortion shader takes care of the submerging effect.
//      if (cameraPos.y - 0.5 < VoidSeaConstants.HEAT_HEIGHT - val) {
//        break;
//      }
//      VFGpuBuffers.UseWorldPos(
//        this.positionBuffer,
//        new Vector3f(0, (float) (-cameraPos.y - val), 0),
//        encoder
//      );
//
//      if (colorTextureView == null) { return; }
//      final int i2 = val;
//      // setup render pass and actually use it
//      try (RenderPass renderPass = encoder.createRenderPass(() -> "VoidSeaDistortionGradientLayer" + i2, colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())) {
//        renderPass.setPipeline(VFRenderPipelines.VOID_SEA_MESH_DISTORTION_GRADIENT_PIPELINE);
//        RenderSystem.bindDefaultUniforms(renderPass);
//        renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
//        renderPass.setUniform("ChunkOffset", this.positionBuffer.currentBuffer());
//
//        renderPass.setVertexBuffer(0, this.distortionGradientLayerBuffer);
//        renderPass.setIndexBuffer(this.distortionGradientLayerBuffer, VertexFormat.IndexType.SHORT);
//        renderPass.draw(0, this.distortionGradientLayerIndex);
//      }
//    }
//  }

  /**
   * The final pass. Manages mixing most finalized handles and applies a heatwave effect which makes the affected part of
   * the screen wiggle.
   * @param cameraPos the position of the camera. Needed to position objects vertically and across chunks correctly.
   * @param writeTargetHandle the resourceHandle which contains the target where the mixed image will be written to
   * @param blendHandle the resourceHandle which contains a picture with preblended world and sa textures
   * @param seaHandle the resourceHandle which contains the picture of the void sea
   */
  private void renderHeatWave(Vec3 cameraPos,ResourceHandle<RenderTarget> writeTargetHandle, ResourceHandle<TextureTarget> seaHandle, ResourceHandle<TextureTarget> blendHandle, ResourceHandle<TextureTarget> distortionHandle) {
    RenderTarget writeTarget = writeTargetHandle.get();
    RenderTarget blend = blendHandle.get();
    RenderTarget distortion = distortionHandle.get();
    //RenderTarget distortionGradient = distortionGradientHandle.get();
    RenderTarget sea = seaHandle.get();

    GpuTextureView colorTextureViewT = writeTarget.getColorTextureView();
    GpuTextureView depthTextureViewT = writeTarget.getDepthTextureView();
    GpuTextureView colorTextureViewS = sea.getColorTextureView();
    GpuTextureView colorTextureViewB = blend.getColorTextureView();
    GpuTextureView colorTextureViewD = distortion.getColorTextureView();

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    VFGpuBuffers.UseWorldPos(
      this.positionBuffer2,
      new Vector3f(0f, (float) (cameraPos.y()), 0f),
      encoder
    );

    if (colorTextureViewT == null) {
      return;
    }

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> "VoidSeaDistort", colorTextureViewT, OptionalInt.empty(), depthTextureViewT, OptionalDouble.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.VOID_SEA_DISTORTION_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("ChunkOffset", this.positionBuffer2.currentBuffer());

      renderPass.bindSampler("SamplerSea", colorTextureViewS);
      renderPass.bindSampler("SamplerBlend", colorTextureViewB);
      renderPass.bindSampler("SamplerWorld", colorTextureViewT);
      renderPass.bindSampler("SamplerHeatWave", colorTextureViewD);

      renderPass.setVertexBuffer(0, this.screenBuffer);
      renderPass.setIndexBuffer(this.screenBuffer, VertexFormat.IndexType.SHORT);
      renderPass.draw(0, this.screenIndex);
    }
  }

  /**
   * Mixes the seaHandle and worldHandle and writes it to a new blended target.
   * @param writeTargetHandle the resourceHandle which contains the target where the mixed image will be written to
   * @param seaHandle the resourceHandle which contains the picture of the void sea
   * @param worldHandle the resourceHandle which contains the world's appearance
   */
  private void renderBlitAndBlend(@NotNull ResourceHandle<? extends RenderTarget> writeTargetHandle, ResourceHandle<? extends RenderTarget> seaHandle, ResourceHandle<? extends RenderTarget> worldHandle) {
    RenderTarget writeTarget = writeTargetHandle.get();
    RenderTarget sea = seaHandle.get();
    RenderTarget world = worldHandle.get();
    GpuTextureView colorTextureViewT = writeTarget.getColorTextureView();
    GpuTextureView depthTextureViewT = writeTarget.getDepthTextureView();
    GpuTextureView colorTextureViewS = sea.getColorTextureView();
    GpuTextureView colorTextureViewW = world.getColorTextureView();

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    if (colorTextureViewT == null) { return; }

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> "VoidSeaDistort", colorTextureViewT, OptionalInt.empty(), depthTextureViewT, OptionalDouble.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.VOID_SEA_BLEND_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);

      renderPass.bindSampler("SamplerSea", colorTextureViewS);
      renderPass.bindSampler("SamplerWorld", colorTextureViewW);

      renderPass.setVertexBuffer(0, this.screenBuffer);
      renderPass.setIndexBuffer(this.screenBuffer, VertexFormat.IndexType.SHORT);
      renderPass.draw(0, this.screenIndex);
    }
  }

  /**
   * Method used to clear the content of the list of targets passed and copy the depth buffer of the main target into
   * the other targets.
   * @param mainTargetHandle the main screen's handle. Needed to copy depthBuffers into other RenderTargets.
   * @param targetHandles a list of ResourceHandles to clear, resize and copy new basic data into
   */
  private void clearAndResizeTargets(ResourceHandle<RenderTarget> mainTargetHandle, List<ResourceHandle<? extends RenderTarget>> targetHandles) {
    RenderTarget mainTarget = mainTargetHandle.get();
    int width = mainTarget.width;
    int height = mainTarget.height;

    for (ResourceHandle<? extends RenderTarget> targetHandle : targetHandles) {
      RenderTarget target = targetHandle.get();

      // resize
      if (target.width != width || target.height != height) {
        target.resize(width, height);
      }

      // clear textures
      if (target.getColorTexture() != null) {
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.getColorTexture(),
          // do not change this color. Distort Effect depends on replacing whitespace.
          ARGB.color(255, 255, 255, 255)
        );
      }
      if (target.getDepthTexture() != null) {
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(target.getDepthTexture(), 1.0);
        target.copyDepthFrom(mainTarget);
      }
    }
  }


  //############################################
  //                BUILD SEA
  //############################################

  /**
   * Fills a GpuBuffer with a Vertex Mesh to draw a very large grid that can wiggle to draw an ocean.
   */
  private void buildSea() {
    VertexFormat format = DefaultVertexFormat.BLOCK;
    VertexFormat.Mode mode = VertexFormat.Mode.TRIANGLES;
    GpuBuffer gpuBuffer;

    double levelRendererDistance = Math.max(1, Minecraft.getInstance().levelRenderer.getLastViewDistance());
    double sizeMult = levelRendererDistance / DEFAULT_RENDER_DISTANCE;
    int size = (int) Math.ceil(
      1.1 * Math.pow(
        (DefaultVertexFormat.BLOCK.getVertexSize() * 6.0 * sizeMult * VoidSeaConstants.OFFSET) / QUAD_SIZE, 2
      )
    );

    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(size)) {
      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, mode, format);

      putMesh(bufferBuilder);

      // Handle storing the meshdata into the buffer and then closing the MeshData and byteBufferBuilder
      // Last step of making the positions we're rendering stuff in.
      try (MeshData meshData = bufferBuilder.buildOrThrow()) {
        this.seaMeshIndex = meshData.drawState().indexCount();
        ByteBuffer uploadBuffer = meshData.vertexBuffer();

        gpuBuffer = RenderSystem.getDevice().createBuffer(
          () -> "Void Sea Vertex Buffer",
          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
          uploadBuffer
        );
      }
    }

    this.seaMeshBuffer = gpuBuffer;
  }

  /**
   * Makes a box with an open top to cover the environment with distortions when the player is inside the distortion
   * layers. Also used to cover the bottom of the world's terrain.
   */
  private void buildBottomDistortion() {
    VertexFormat format = DefaultVertexFormat.BLOCK;
    VertexFormat.Mode mode = VertexFormat.Mode.TRIANGLES;
    GpuBuffer gpuBuffer;

    // 5 faces out of 6 with 6 vertices each
    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.BLOCK.getVertexSize() * 5 * 6)) {
      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, mode, format);

      VertexMeshHelper.putOpenCubeMeshVertex(
        bufferBuilder, VoidSeaConstants.OFFSET, (int) (VoidSeaConstants.HEAT_HEIGHT + 30), -VoidSeaConstants.OFFSET,
        VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY
      );

      // Handle storing the meshdata into the buffer and then closing the MeshData and byteBufferBuilder
      // Last step of making the positions we're rendering stuff in.
      try (MeshData meshData = bufferBuilder.buildOrThrow()) {
        this.bottomDistortionIndex = meshData.drawState().indexCount();
        ByteBuffer uploadBuffer = meshData.vertexBuffer();

        gpuBuffer = RenderSystem.getDevice().createBuffer(
          () -> "Void Sea Distortion Bottom Buffer",
          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
          uploadBuffer
        );
      }
    }

    this.bottomDistortionBuffer = gpuBuffer;
  }

//  /**
//   * Fills a GpuBuffer with a Vertex Mesh to draw a large box. This box has every side and gets darker the further down
//   * it gets. Used to make the distortion stronger as it goes down in the world.
//   * @return a GpuBuffer which contains the data needed to draw a large quad in the world
//   */
  // TODO : fix distortion gradient method
//  private GpuBuffer buildDistortionGradientBox() {
//    VertexFormat format = DefaultVertexFormat.BLOCK;
//    VertexFormat.Mode mode = VertexFormat.Mode.TRIANGLES;
//    GpuBuffer gpuBuffer;
//
//    // 6 faces out of 6 with 6 vertices each
//    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(
//      DefaultVertexFormat.BLOCK.getVertexSize() * 6 * 6)
//    ) {
//      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, mode, format);
//
//      VertexMeshHelper.putCubeMeshVertex(
//        bufferBuilder, VoidSeaConstants.OFFSET, (int) VoidSeaConstants.HEAT_HEIGHT, (int) VoidSeaConstants.HEIGHT,
//        VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY
//      );
//
//      // Handle storing the meshdata into the buffer and then closing the MeshData and byteBufferBuilder
//      // Last step of making the positions we're rendering stuff in.
//      try (MeshData meshData = bufferBuilder.buildOrThrow()) {
//        this.distortionGradientIndex = meshData.drawState().indexCount();
//        ByteBuffer uploadBuffer = meshData.vertexBuffer();
//
//        gpuBuffer = RenderSystem.getDevice().createBuffer(
//          () -> "Void Sea Distortion Gradient Buffer",
//          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
//          uploadBuffer
//        );
//      }
//    }
//
//    return gpuBuffer;
//    return null;
//  }

//  /**
//   * Fills a GpuBuffer with a Vertex Mesh to draw a plane. These planes are made transparent in a later step and are
//   * stacked multiple times.
//   * Used to make the distortion stronger as it goes down.
//   * @return a GpuBuffer which contains the data needed to draw a large quad in the world
//   */
//  private GpuBuffer buildDistortionGradientLayer() {
//    VertexFormat format = DefaultVertexFormat.BLOCK;
//    VertexFormat.Mode mode = VertexFormat.Mode.TRIANGLES;
//    GpuBuffer gpuBuffer;
//
//    // 6 faces out of 6 with 6 vertices each
//    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(
//      DefaultVertexFormat.BLOCK.getVertexSize() * 6)
//    ) {
//      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, mode, format);
//
//      VertexMeshHelper.putQuadMeshVertex(
//        bufferBuilder, VoidSeaConstants.OFFSET, (int) VoidSeaConstants.HEAT_HEIGHT,
//        VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY
//      );
//
//      // Handle storing the meshdata into the buffer and then closing the MeshData and byteBufferBuilder
//      // Last step of making the positions we're rendering stuff in.
//      try (MeshData meshData = bufferBuilder.buildOrThrow()) {
//        this.distortionGradientLayerIndex = meshData.drawState().indexCount();
//        ByteBuffer uploadBuffer = meshData.vertexBuffer();
//
//        gpuBuffer = RenderSystem.getDevice().createBuffer(
//          () -> "Void Sea Distortion Gradient Layer Buffer",
//          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
//          uploadBuffer
//        );
//      }
//    }
//
//    return gpuBuffer;
//  }

  /**
   * Fills a GpuBuffer with a Vertex Mesh shaped like a quad. Used to draw things straight across the screen.
   */
  private void buildScreen() {
    VertexFormat format = DefaultVertexFormat.POSITION_TEX;
    VertexFormat.Mode mode = VertexFormat.Mode.QUADS;
    GpuBuffer gpuBuffer;

    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(6 * format.getVertexSize())) {
      BufferBuilder builder = new BufferBuilder(byteBufferBuilder, mode, format);

      putBufferVertex(builder, -1f, -1f, 0f, 0f, 0f);
      putBufferVertex(builder, -1f, 1f, 0f, 0f, 1f);
      putBufferVertex(builder, 1f, 1f, 0f, 1f, 1f);

      putBufferVertex(builder, 1f, 1f, 0f, 1f, 1f);
      putBufferVertex(builder, 1f, -1f, 0f, 1f, 0f);
      putBufferVertex(builder, -1f, -1f, 0f, 0f, 0f);

      try (MeshData meshdata = builder.buildOrThrow()) {
        this.screenIndex = meshdata.drawState().indexCount();
        gpuBuffer = RenderSystem.getDevice().createBuffer(
          () -> "Distort quad",
          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
          meshdata.vertexBuffer()
        );
      }
    }

    this.screenBuffer = gpuBuffer;
  }

  //############################################
  //            HELPER METHODS
  //############################################

  /**
   * Helper method to generate a large, subdivided grid of triangles.
   * @param builder the BufferBuilder to which vertices will be added.
   */
  private void putMesh(BufferBuilder builder) {
    double levelRendererDistance = Math.max(1, Minecraft.getInstance().levelRenderer.getLastViewDistance());
    int size = (int) (VoidSeaConstants.OFFSET * (levelRendererDistance / DEFAULT_RENDER_DISTANCE));

    for (int x = -size; x < size; x+= VoidSeaRenderer.QUAD_SIZE) {
      for (int y = -size; y < size; y+= VoidSeaRenderer.QUAD_SIZE) {
        if (Math.sqrt(x * x + y * y) < size * VoidSeaRenderer.PADDING) {
          putMeshVertex(builder, x - 0.5f, y - 0.5f);
        }
      }
    }
  }

  /**
   * Helper method to generate a single quad at a specific position within a subdivided grid.
   *
   * @param xSubPos the x position within the subdivision. Goes from 0 to 1.
   * @param zSubPos the z position within the subdivision. Goes from 0 to 1.
   */
  private void putMeshVertex(BufferBuilder builder, float xSubPos, float zSubPos) {
    // set positions of vertices
    float x1 = xSubPos + VoidSeaRenderer.QUAD_SIZE;
    float z1 = zSubPos + VoidSeaRenderer.QUAD_SIZE;

    float u0 = 0, v0 = 0;
    float u1 = 1f, v1 = 1f;

    putBufferVertex(builder, xSubPos, 0, zSubPos, u0, v0);
    putBufferVertex(builder, xSubPos, 0, z1, u0, v1);
    putBufferVertex(builder, x1, 0, z1, u1, v1);
    
    putBufferVertex(builder, x1, 0, z1, u1, v1);
    putBufferVertex(builder, x1, 0, zSubPos, u1, v0);
    putBufferVertex(builder, xSubPos, 0, zSubPos, u0, v0);
  }

  /**
   * Helper method which gets a sprite from the block TextureAtlas using its name and the animated sprites for the gpu texture.
   * Initializes textures for VoidSeaRenderer.
   */
  private void getSprites() {
    GpuTextureView[] spriteAnim = new GpuTextureView[5];

    for (int i = 0; i < 5; i++) {
      // Gpu ResourceLocation is different from textureAtlasResLoc, I have 0 idea why. 
      // Seems it needs its path more defined than the regular sprite loading method.
      // Doesn't work if I use the regular path description.
      ResourceLocation gpuResLoc = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "textures/block/void_fluid/" + SPRITE_NAME + "_" + i + ".png");
      AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(gpuResLoc);
      GpuTextureView textView = abstractTexture.getTextureView();

      spriteAnim[i] = textView;
    }

    this.GPU_SPRITE_ANIM_VIEW = spriteAnim;
    ResourceLocation gpuResLoc = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "textures/black.png");
    AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(gpuResLoc);
    this.blackTextureView = abstractTexture.getTextureView();
  }

  /**
   * Manages closing the sprites generated by getSprites() at the end of the lifecycle.
   */
  private void closeSprites() {
    for (GpuTextureView sprite : GPU_SPRITE_ANIM_VIEW){
      sprite.close();
    }
    this.blackTextureView.close();
  }

  /**
   * Used to add a vertex at specific coordinates with an upwards normal
   * @param builder the builder needed to add the vertices to a mesh
   * @param x the 1st position of the vertex
   * @param y the 2nd position of the vertex
   * @param z the 3rd position of the vertex
   * @param u the 1st UV position
   * @param v the 2nd UV position
   */
  private void putBufferVertex(BufferBuilder builder, float x, float y, float z, float u, float v) {
    VertexMeshHelper.putBufferVertex(
      builder,
      VFRenderConsts.RUBICON_PACKED_LIGHT,
      VFRenderConsts.RUBICON_PACKED_OVERLAY,
      x, y, z, u, v
    );
  }
}
