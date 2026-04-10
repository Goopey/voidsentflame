package com.goopey.voidsentflame.client.render;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.*;

import com.goopey.voidsentflame.core.VFGpuBuffers;
import com.goopey.voidsentflame.core.VFGpuBuffers.VFGpuBuffersNames;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import net.minecraft.client.renderer.*;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class VoidSeaRenderer {
  // Singleton Instance
  private static final VoidSeaRenderer INSTANCE = new VoidSeaRenderer();

  // Render Waves
  private static final float AMPLITUDE = 0.5f;
  private static final float FREQUENCY = 0.5f;
  private static final float SPEED = 0.05f;
  
  // Render Triangles
  private static final int QUAD_SIZE = 3;
  private static final float PADDING = 1.1f;

  // World Position
  private static final double HEIGHT = -42.5;
  private static final double HEAT_HEIGHT = -26.5;
  private static final int OFFSET = 256;
  private static final int VIEW_DISTANCE_SCALE = 16;
  
  // Sprite/Model Stuff
  private TextureAtlasSprite SPRITE;
  private GpuTextureView[] GPU_SPRITE_ANIM_VIEW;
  private final static String SPRITE_NAME = "void_fluid";
  
  // Shader Stuff
  // MAGIC NUMBER, DO NOT CHANGE
  // (int) (((2 * OFFSET * PADDING/ QUAD_SIZE) * (2 * OFFSET * PADDING/ QUAD_SIZE)) * 6);
  private static final int AMOUNT_OF_VERTICES = 155574;
  private final GpuBuffer seaMeshBuffer;
  private int seaMeshIndex;
  private final GpuBuffer bottomDistortionBuffer;
  private int bottomDistortionIndex;
  private final GpuBuffer screenBuffer;
  private int screenIndex;
  private final MappableRingBuffer positionBuffer;
  private final MappableRingBuffer lookAngleBuffer;
  // blend targets
  private final TextureTarget blendTarget;
  private ResourceHandle<TextureTarget> blendTargetHandle;
  private final TextureTarget seaTarget;
  private ResourceHandle<TextureTarget> seaTargetHandle;
  private final RenderTarget mainTarget;
  private ResourceHandle<RenderTarget> mainTargetHandle;
  private final TextureTarget distortionTarget;
  private ResourceHandle<TextureTarget> distortionTargetHandle;
  private GpuTextureView heatWaveTextureView;
  private GpuTextureView blackTextureView;
  private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);

  // Dimension Stuff
  private static final ResourceKey<Level> RUBICON = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("voidsentflame:rubicon"));

  //#################################################
  //                 INSTANCE
  //#################################################

  private VoidSeaRenderer() {
    // Cache
    this.getSprites();
    this.seaMeshBuffer = buildSea();
    this.screenBuffer = buildScreen();
    this.bottomDistortionBuffer = buildBottomDistortion();
    this.positionBuffer = VFGpuBuffers.VFWorldPosUbo.get();
    this.lookAngleBuffer = VFGpuBuffers.VFLookAngleUbo.get();

    // these values will be resized later
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

  //######################################################
  //                  RENDER STUFF
  //######################################################

  /**
   * Main method. Called when creating the Void Sea visual effect. Manages calling renderPasses, checking dimension,
   * passing textures.
   *
   * @param event the event bus event. Needed to
   */
  public void render(RenderLevelStageEvent.AfterEntities event) {
    // Check if in Rubicon
    Minecraft mc = Minecraft.getInstance();
    Level level = mc.level;
    LevelRenderer levelRenderer = event.getLevelRenderer();
    if (level.dimension() != RUBICON) { return; }
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
    float renderDistance = (float) (levelRenderer.getLastViewDistance()/Math.max(VIEW_DISTANCE_SCALE, 12));

    // get cameraPos and lock the wave model at the proper height in the world
    Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
    float deltaTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    Vec3 cameraPos = cameraEntity.getPosition(deltaTick);
    // convert to ranges from -1 to 1.
    Vector2f cameraRot = new Vector2f(
      cameraEntity.getXRot(deltaTick) / 90,
      cameraEntity.getYRot(deltaTick) / 180
    );

    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    matrix4fStack.pushMatrix();

    // avoid crashes if the sprites were cleared 
    if (!this.GPU_SPRITE_ANIM_VIEW[frame].isClosed()) {
      this.blendTargetHandle = frameGraphBuilder.importExternal("VoidSeaBlendTexHandle", this.blendTarget);
      this.seaTargetHandle = frameGraphBuilder.importExternal("VoidSeaSeaTexHandle", this.seaTarget);
      this.distortionTargetHandle = frameGraphBuilder.importExternal("VoidSeaDistortHandle", this.distortionTarget);

      FramePass pass1 = frameGraphBuilder.addPass("resizeClearCopyPass1");
      this.seaTargetHandle = pass1.readsAndWrites(this.seaTargetHandle);
      this.blendTargetHandle = pass1.readsAndWrites(this.blendTargetHandle);
      this.mainTargetHandle = pass1.readsAndWrites(this.mainTargetHandle);
      this.distortionTargetHandle = pass1.readsAndWrites(this.distortionTargetHandle);
      pass1.executes(
        () -> clearAndResizeTargets(this.mainTargetHandle, List.of(this.blendTargetHandle, this.seaTargetHandle, this.distortionTargetHandle))
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

      FramePass pass4 = frameGraphBuilder.addPass("VoidSeaBlendPass4");
      pass4.requires(pass2);
      pass4.requires(pass3);
      this.seaTargetHandle = pass4.readsAndWrites(this.seaTargetHandle);
      this.mainTargetHandle = pass4.readsAndWrites(this.mainTargetHandle);
      this.blendTargetHandle = pass4.readsAndWrites(this.blendTargetHandle);
      pass4.executes(
        () -> this.renderBlend(this.blendTargetHandle, this.seaTargetHandle, this.mainTargetHandle)
      );

      FramePass pass5 = frameGraphBuilder.addPass("VoidSeaDistortPass5");
      pass5.requires(pass4);
      this.blendTargetHandle = pass5.readsAndWrites(this.blendTargetHandle);
      this.seaTargetHandle = pass5.readsAndWrites(this.seaTargetHandle);
      this.mainTargetHandle = pass5.readsAndWrites(this.mainTargetHandle);
      this.distortionTargetHandle = pass5.readsAndWrites(this.distortionTargetHandle);
      pass5.executes(
        () -> this.renderHeatWave(cameraRot, this.mainTargetHandle, this.seaTargetHandle, this.blendTargetHandle, this.distortionTargetHandle)
      );
    } else {
      this.getSprites();
    }

    frameGraphBuilder.execute(this.resourcePool);
    matrix4fStack.popMatrix();
    poseStack.popPose();
  }

  //##############################################
  //            RENDER HELPER METHODS
  //##############################################

  /**
   * TODO : comment
   * @param cameraPos
   * @param matrix4fStack
   * @param frame
   * @param targetHandle
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
      new Vector3f((float) cameraPos.x, (float) (HEIGHT - cameraPos.y), (float) cameraPos.z),
      encoder
    );

    // setup render pass and actually use it
    try (RenderPass renderPass = encoder.createRenderPass(() -> "VoidSea", colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty())) {
      renderPass.setPipeline(VFRenderPipelines.VOID_SEA_MESH_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
      // See void_sea_mesh_vert.vsh
      renderPass.setUniform("ChunkOffset", this.positionBuffer.currentBuffer());

      renderPass.bindSampler("Sampler0", this.blackTextureView);
      renderPass.bindSampler("Sampler1", this.blackTextureView);

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

    double height = HEAT_HEIGHT - cameraPos.y;

    // setup other special uniforms
    VFGpuBuffers.UseWorldPos(
      this.positionBuffer,
      new Vector3f((float) cameraPos.x, (float) (height), (float) cameraPos.z),
      encoder
    );

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

  /**
   * TODO : comment
   * @param cameraRot
   * @param writeTargetHandle
   * @param blendHandle
   * @param seaHandle
   */
  private void renderHeatWave(Vector2f cameraRot,ResourceHandle<RenderTarget> writeTargetHandle, ResourceHandle<TextureTarget> seaHandle, ResourceHandle<TextureTarget> blendHandle, ResourceHandle<TextureTarget> distortionHandle) {
    RenderTarget writeTarget = writeTargetHandle.get();
    RenderTarget blend = blendHandle.get();
    RenderTarget distortion = distortionHandle.get();
    RenderTarget sea = seaHandle.get();
    GpuTextureView colorTextureViewT = writeTarget.getColorTextureView();
    GpuTextureView depthTextureViewT = writeTarget.getDepthTextureView();
    GpuTextureView colorTextureViewS = sea.getColorTextureView();
    GpuTextureView colorTextureViewB = blend.getColorTextureView();
    GpuTextureView colorTextureViewD = distortion.getColorTextureView();

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    VFGpuBuffers.UseLookAngle(
      this.lookAngleBuffer, cameraRot, encoder
    );

    try (RenderPass renderPass = encoder.createRenderPass(
      () -> "VoidSeaDistort", colorTextureViewT, OptionalInt.empty(), depthTextureViewT, OptionalDouble.empty())
    ) {
      renderPass.setPipeline(VFRenderPipelines.VOID_SEA_DISTORTION_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform(VFGpuBuffersNames.LOOK_ANGLE.name, this.lookAngleBuffer.currentBuffer());

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
   * TODO : comment
   * @param writeTargetHandle
   * @param seaHandle
   * @param worldHandle
   */
  private void renderBlend(ResourceHandle<? extends RenderTarget> writeTargetHandle, ResourceHandle<? extends RenderTarget> seaHandle, ResourceHandle<? extends RenderTarget> worldHandle) {
    RenderTarget writeTarget = writeTargetHandle.get();
    RenderTarget sea = seaHandle.get();
    RenderTarget world = worldHandle.get();
    GpuTextureView colorTextureViewT = writeTarget.getColorTextureView();
    GpuTextureView depthTextureViewT = writeTarget.getDepthTextureView();
    GpuTextureView colorTextureViewS = sea.getColorTextureView();
    GpuTextureView colorTextureViewW = world.getColorTextureView();

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

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
   * TODO : comment
   * @param mainTargetHandle
   * @param targetHandles
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

  private GpuBuffer buildSea() {
    VertexFormat format = DefaultVertexFormat.BLOCK;
    VertexFormat.Mode mode = VertexFormat.Mode.TRIANGLES;
    GpuBuffer gpuBuffer;

    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.BLOCK.getVertexSize() * AMOUNT_OF_VERTICES)) {
      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, mode, format);
      
      putMesh(bufferBuilder, OFFSET, QUAD_SIZE, PADDING);

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

    return gpuBuffer;
  }

  private GpuBuffer buildBottomDistortion() {
    VertexFormat format = DefaultVertexFormat.BLOCK;
    VertexFormat.Mode mode = VertexFormat.Mode.TRIANGLES;
    GpuBuffer gpuBuffer;

    // 5 faces out of 6 with 6 vertices each
    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.BLOCK.getVertexSize() * 5 * 6)) {
      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, mode, format);

      putCubeMeshVertex(bufferBuilder, OFFSET, (int) (HEAT_HEIGHT + 30), -OFFSET);

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

    return gpuBuffer;
  }

  private GpuBuffer buildScreen() {
    VertexFormat format = DefaultVertexFormat.POSITION_TEX;
    VertexFormat.Mode mode = VertexFormat.Mode.QUADS;
    GpuBuffer gpuBuffer;

    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(6 * format.getVertexSize())) {
      BufferBuilder builder = new BufferBuilder(byteBufferBuilder, mode, format);
      // TODO : hardcode values to improve performance
      float x0 = -1f, x1 = 1f;
      float y0 = -1f, y1 = 1f;
      float z0 = 0f, z1 = 0f;
      float v0 = 0f, v1 = 1f;
      float u0 = 0f, u1 = 1f;

      putBufferVertex(builder, x0, y0, z0, u0, v0);
      putBufferVertex(builder, x0, y1, z1, u0, v1);
      putBufferVertex(builder, x1, y1, z1, u1, v1);

      putBufferVertex(builder, x1, y1, z1, u1, v1);
      putBufferVertex(builder, x1, y0, z0, u1, v0);
      putBufferVertex(builder, x0, y0, z0, u0, v0);

      try (MeshData meshdata = builder.buildOrThrow()) {
        this.screenIndex = meshdata.drawState().indexCount();
        gpuBuffer = RenderSystem.getDevice().createBuffer(
          () -> "Distort quad",
          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
          meshdata.vertexBuffer()
        );
      }
    }

    return gpuBuffer;
  }

  //############################################
  //            HELPER METHODS
  //############################################

  /**
   * Helper method to generate a large, subdivided grid of tris.
   * 
   * @param builder the BufferBuilder to which vertices will be added.
   * @param size int which defines how large the grid can potentially be. The resulting grid will be 2x in length and height.
   * @param triSize int needed to define how large the subdisivions of the quad will be.
   * @param padding a margin of space which makes the grid slightly larger.
   */
  private void putMesh(BufferBuilder builder, int size, int triSize, float padding) {
    for (int x = -size; x < size; x+=triSize) {
      for (int y = -size; y < size; y+=triSize) {
        if (Math.sqrt(x * x + y * y) < size * padding) {
          putMeshVertex(builder, triSize, x - 0.5f, y - 0.5f);
        }
      }
    }
  }

  /**
   * Helper method to generate a single quad at a specific position within a subdivided grid.
   *
   * @param xSubPos the x position within the subdivision. Goes from 0 to 1.
   * @param zSubPos the z position within the subdivision. Goes from 0 to 1.
   * 
   * @return BakedQuad a single BakedQuad which will be rendered in the world.
   */
  private void putMeshVertex(BufferBuilder builder, int size, float xSubPos, float zSubPos) {
    // set positions of vertices
    float x0 = xSubPos;
    float z0 = zSubPos;
    float x1 = xSubPos + size;
    float z1 = zSubPos + size;

    float u0 = 0, v0 = 0;
    float u1 = 1f, v1 = 1f;

    putBufferVertex(builder, x0, 0, z0, u0, v0);
    putBufferVertex(builder, x0, 0, z1, u0, v1);
    putBufferVertex(builder, x1, 0, z1, u1, v1);
    
    putBufferVertex(builder, x1, 0, z1, u1, v1);
    putBufferVertex(builder, x1, 0, z0, u1, v0);
    putBufferVertex(builder, x0, 0, z0, u0, v0);
  }

  /**
   * Helper method used to create the bottom box of the distortion effect
   * @param builder the buffer builder needed to add vertices to a mesh
   * @param size the size of the square
   * @param topHeight how high the square should cover the sky
   * @param bottomHeight the low the square should cover the sky
   */
  private void putCubeMeshVertex(BufferBuilder builder, int size, int topHeight, int bottomHeight) {
    float u0 = 0, v0 = 0;
    float u1 = 1f, v1 = 1f;

    int[][][] box = {
      {     // Face A
        {size, topHeight, -size}, {size, topHeight, size}, {size, bottomHeight, size}, {size, bottomHeight, -size}
      },
      {     // Face B
        {size, topHeight, size}, {-size, topHeight, size}, {-size, bottomHeight, size}, {size, bottomHeight, size}
      },
      {     // Face C
        {-size, topHeight, size}, {-size, topHeight, -size}, {-size, bottomHeight, -size}, {-size, bottomHeight, size}
      },
      {     // Face D
        {-size, topHeight, -size}, {size, topHeight, -size}, {size, bottomHeight, -size}, {-size, bottomHeight, -size}
      },
      {     // Face E
        {-size, bottomHeight, size}, {size, bottomHeight, size}, {size, bottomHeight, -size}, {-size, bottomHeight, -size}
      }
    };

    for (int[][] face : box) {
      Vector3f pos1 = new Vector3f(face[0][0], face[0][1], face[0][2]);
      Vector3f pos2 = new Vector3f(face[1][0], face[1][1], face[1][2]);
      Vector3f pos3 = new Vector3f(face[2][0], face[2][1], face[2][2]);
      Vector3f pos4 = new Vector3f(face[3][0], face[3][1], face[3][2]);

      putBufferVertex(builder, pos1.x, pos1.y, pos1.z, u0, v0);
      putBufferVertex(builder, pos2.x, pos2.y, pos2.z, u0, v1);
      putBufferVertex(builder, pos3.x, pos3.y, pos3.z, u1, v1);

      putBufferVertex(builder, pos3.x, pos3.y, pos3.z, u1, v1);
      putBufferVertex(builder, pos4.x, pos4.y, pos4.z, u1, v0);
      putBufferVertex(builder, pos1.x, pos1.y, pos1.z, u0, v0);
    }
  }

  /**
   * Helper method which gets a sprite from the block TextureAtlas using it's name and the animated sprites for the gpu texture.
   * Initializes textures for VoidSeaRenderer.
   * 
   * @return TextureAtlasSprite A Sprite in the TextureAtlas
   */
  private void getSprites() {
    GpuTextureView[] spriteAnim = new GpuTextureView[5];

    for (int i = 0; i < 5; i++) {
      // Gpu ResourceLocation is different from textureAtlasResLoc, I have 0 idea why. 
      // Seems it needs its path more defined than the regular sprite loading method.
      // Doesn't work if I use the regular path description.
      ResourceLocation gpuResLoc = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "textures/block/void_fluid/" + SPRITE_NAME + "_" + i + ".png");
      AbstractTexture abstText = Minecraft.getInstance().getTextureManager().getTexture(gpuResLoc);
      GpuTextureView textView = abstText.getTextureView();

      spriteAnim[i] = textView;
    }

    this.GPU_SPRITE_ANIM_VIEW = spriteAnim;
    ResourceLocation gpuResLoc = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "textures/heat_wave.png");
    AbstractTexture abstText = Minecraft.getInstance().getTextureManager().getTexture(gpuResLoc);
    this.heatWaveTextureView = abstText.getTextureView();
    gpuResLoc = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "textures/black.png");
    abstText = Minecraft.getInstance().getTextureManager().getTexture(gpuResLoc);
    this.blackTextureView = abstText.getTextureView();
  }

  /**
   * Used to add a vertex at specific coordinates with an upwards normal
   * 
   * @param builder the builder needed to add the vertices to a mesh
   * @param x the 1st position of the vertex
   * @param y the 2nd position of the vertex
   * @param z the 3rd position of the vertex
   * @param u the 1st UV position
   * @param v the 2nd UV position
   */
  private void putBufferVertex(BufferBuilder builder, float x, float y, float z, float u, float v) {
    builder.addVertex(x, y, z)
    .setColor(1f, 1f, 1f, 1f)
    .setUv(u, v)
    .setOverlay(VFRenderConsts.RUBICON_PACKED_OVERLAY)
    .setLight(VFRenderConsts.RUBICON_PACKED_LIGHT)
    .setNormal(0, 1f, 0);
  }
}
