package com.goopey.voidsentflame.client.render;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.*;

import com.goopey.voidsentflame.core.VFGpuBuffers;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.*;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
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
  private final GpuBuffer seaDistortionBuffer;
  private int seaDistortionIndex;
  private final MappableRingBuffer positionBuffer;
  private final TextureTarget copyTarget;
  private ResourceHandle<TextureTarget> copyTargetHandle;
  private final RenderTarget mainTarget;
  private ResourceHandle<RenderTarget> mainTargetHandle;
  private GpuTextureView seaSprite;
  private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);

  // PostChain
  private BleedVisualEffect bleedVisualEffect;
  private PostPass distortionPass;

  // Dimension Stuff
  private static final ResourceKey<Level> RUBICON = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("voidsentflame:rubicon"));

  //#################################################
  //                 INSTANCE
  //#################################################

  private VoidSeaRenderer() {
    // Cache
    this.getSprites();
    this.seaMeshBuffer = buildSea();
    this.seaDistortionBuffer = buildDistortion();
    // these values will be resized later
    this.copyTarget = new TextureTarget("VoidSeaCopyTexture", 100, 100, true);
    this.mainTarget = Minecraft.getInstance().getMainRenderTarget();
    this.positionBuffer = VFGpuBuffers.VFWorldPosUbo.get();
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
    int frame = (int) (level.getGameTime() % 15) / 3;

    // get poseStack to start rendering
    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();

    // FrameGraphBuilder needed to run multiple passes
    FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
//    RenderTarget mainTarget = mc.getMainRenderTarget();
//    ResourceLocation mainLocation = ResourceLocation.withDefaultNamespace("main");
    this.mainTargetHandle = frameGraphBuilder.importExternal("minecraft:main", this.mainTarget);
//    RenderTargetDescriptor swapTargetDescriptor = new RenderTargetDescriptor(
//      mainTarget.width, mainTarget.height, mainTarget.useDepth, 0
//    );
//    RenderTarget swapTarget = swapTargetDescriptor.allocate();
//    swapTarget.createBuffers(mainTarget.width, mainTarget.height);
//    ResourceLocation swapLocation = ResourceLocation.withDefaultNamespace("swap");
//    ResourceHandle<RenderTarget> swapTargetHandle = frameGraphBuilder.importExternal(swapLocation.toString(), swapTarget);

    // scale size depending on render distance
    // TODO : needs readjusting
    float renderDistance = (float) (levelRenderer.getLastViewDistance()/Math.max(VIEW_DISTANCE_SCALE, 12));

    // get cameraPos and lock the wave model at the proper height in the world
    Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
    float deltaTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    Vec3 cameraPos = cameraEntity.getPosition(deltaTick);
    // get view vector and convert it to radians
    Vec3 cameraRotPos = cameraEntity.getViewVector(deltaTick);
//    cameraEntity.getViewVector(deltaTick)

    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    matrix4fStack.pushMatrix();

    // avoid crashes if the sprites were cleared 
    if (!this.GPU_SPRITE_ANIM_VIEW[frame].isClosed()) {
      this.copyTargetHandle = frameGraphBuilder.importExternal("VoidSeaCopyTexHandle", this.copyTarget);

      FramePass pass1 = frameGraphBuilder.addPass("resizeCopyPass1");
      this.copyTargetHandle = pass1.readsAndWrites(this.copyTargetHandle);
      this.mainTargetHandle = pass1.readsAndWrites(this.mainTargetHandle);
      pass1.executes(
        () -> {
          this.copyTarget.resize(mainTarget.width, mainTarget.height);
          this.copyTarget.copyDepthFrom(this.mainTarget);
        }
      );

      FramePass pass2 = frameGraphBuilder.addPass("VoidSeaMeshPass2");
      pass2.requires(pass1);
      this.mainTargetHandle = pass2.readsAndWrites(this.mainTargetHandle);
      pass2.executes(
        () -> this.renderSea(cameraPos, matrix4fStack, this.GPU_SPRITE_ANIM_VIEW[frame], this.mainTargetHandle)
      );

      FramePass pass3 = frameGraphBuilder.addPass("VoidSeaDistortPass3");
      pass3.requires(pass2);
      this.copyTargetHandle = pass3.readsAndWrites(this.copyTargetHandle);
      this.mainTargetHandle = pass3.readsAndWrites(this.mainTargetHandle);
      pass3.executes(
        () -> this.renderDistortion(cameraRotPos, matrix4fStack, this.mainTargetHandle, this.copyTargetHandle, this.GPU_SPRITE_ANIM_VIEW[frame])
//          this.renderSea(cameraPos, matrix4fStack, this.copyTarget.getColorTextureView(), this.mainTargetHandle)
//          this.renderDistortion(matrix4fStack, this.mainTargetHandle, this.copyTargetHandle, this.GPU_SPRITE_ANIM_VIEW[frame])
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

  private void renderSea(Vec3 cameraPos, Matrix4fStack matrix4fStack, GpuTextureView frame, ResourceHandle<? extends RenderTarget> targetHandle) {
    RenderTarget target = targetHandle.get();
    GpuTextureView colorTextureView = target.getColorTextureView();
    GpuTextureView depthTextureView = target.getDepthTextureView();

    // setup dynamic uniforms
    GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
      matrix4fStack,
      new Vector4f(1f, 1f, 1f, 1f),
      new Vector3f(0f, (float) (HEIGHT - cameraPos.y), 0f),
      new Matrix4f(),
      0.0F
    );

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    // setup other special uniforms
    VFGpuBuffers.UseWorldPos(
      this.positionBuffer,
      new Vector3f((float) cameraPos.x, 0f, (float) cameraPos.z),
      encoder
    );

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

    gpuBufferSlice.buffer().close();
//    VoidsentFlameMod.LOGGER.info(colorTextureView.texture().getDepthOrLayers() + "");

//    RenderSystem.getDevice().createCommandEncoder().presentTexture(colorTextureView);
//    target.resize(50, 50);
//    target.blitToScreen();
//    this.renderPost(new FrameGraphBuilder());
  }

  private void renderDistortion(Vec3 cameraRotPos, Matrix4fStack matrix4fStack, ResourceHandle<RenderTarget> targetHandle, ResourceHandle<TextureTarget> copyHandle, GpuTextureView voidSeaTexture) {
    RenderTarget target = targetHandle.get();
    RenderTarget copy = copyHandle.get();
    GpuTextureView colorTextureViewT = target.getColorTextureView();
    GpuTextureView colorTextureViewC = copy.getColorTextureView();

    // TODO : fix rotation
    matrix4fStack.rotateXYZ(
      new Vector3f((float) cameraRotPos.x, (float) cameraRotPos.y, (float) cameraRotPos.z)
    );

    GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
      matrix4fStack,
      new Vector4f(1f, 1f, 1f, 1f),
      new Vector3f(0f,  0f, 0f),
//      new Vector3f(0f,  (float) (40 - cameraPos.y), 0f),
      new Matrix4f(),
      0.0F
    );

    CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

    RenderSystem.backupProjectionMatrix();
//    RenderSystem.setProjectionMatrix(null, ProjectionType.ORTHOGRAPHIC);

    try (RenderPass renderPass = encoder.createRenderPass(() -> "VoidSeaDistort", colorTextureViewT, OptionalInt.empty())) {
      renderPass.setPipeline(VFRenderPipelines.VOID_SEA_DISTORTION_PIPELINE);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);

//      renderPass.bindSampler("Sampler0", colorTextureViewC);
      renderPass.bindSampler("Sampler0", voidSeaTexture);

      renderPass.setVertexBuffer(0, this.seaDistortionBuffer);
      renderPass.setIndexBuffer(this.seaDistortionBuffer, VertexFormat.IndexType.SHORT);
      renderPass.draw(0, this.seaDistortionIndex);
//      renderPass.draw(0, 3);
    }

    gpuBufferSlice.buffer().close();

    RenderSystem.restoreProjectionMatrix();
  }

  private void renderPost(FrameGraphBuilder frameGraphBuilder) {
    final ProfilerFiller profilerFiller = Profiler.get();
    RenderTarget target = Minecraft.getInstance().getMainRenderTarget();

    Optional<PostChain> postChainOpt = BleedVisualEffect.INSTANCE.prepare(Minecraft.getInstance().getResourceManager(), profilerFiller);
    if (postChainOpt.isEmpty()) {
      return;
    }
    PostChain postChain = postChainOpt.get();

    PostChain.TargetBundle bundle = PostChain.TargetBundle.of(
      PostChain.MAIN_TARGET_ID,
      frameGraphBuilder.importExternal("main", target)
    );

    postChain.addToFrame(
      frameGraphBuilder,
      target.width,
      target.height,
      bundle
    );
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

  private GpuBuffer buildDistortion() {
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
        this.seaDistortionIndex = meshdata.drawState().indexCount();
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
    RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
    GpuDevice device = RenderSystem.getDevice();
    this.seaSprite = device.createTextureView(
      device.createTexture(() -> "seaSpriteSwapTexture", 15, TextureFormat.RGBA8, mainTarget.width, mainTarget.height, 1, 1)
    );
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
