package com.goopey.voidsentflame.client.render;

import java.nio.ByteBuffer;
import java.util.*;

import com.goopey.voidsentflame.core.VFGpuBuffers;
import com.goopey.voidsentflame.util.PostChainSerialization;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.*;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
  private static final float HEIGHT = -42.5f;
  private static final int OFFSET = 256;
  private static final int VIEW_DISTANCE_SCALE = 16;
  
  // Sprite/Model Stuff
  private TextureAtlasSprite SPRITE;
  private GpuTextureView[] GPU_SPRITE_ANIM_VIEW;
  private final static String SPRITE_NAME = "void_fluid";
  
  // Shader Stuff
  // Around 35 244 * 6 = 211 464
  private static final int AMOUNT_OF_VERTICES = (int) ((2 * OFFSET * PADDING/ QUAD_SIZE) * (2 * OFFSET * PADDING/ QUAD_SIZE)) * 6;
  private GpuBuffer seaMeshBuffer;
  private MappableRingBuffer positionBuffer;
  private int seaMeshIndex;

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
    this.positionBuffer = VFGpuBuffers.VFWorldPosUbo.get();
//    this.bleedVisualEffect = new BleedVisualEffect();
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
    Level level = Minecraft.getInstance().level;
    LevelRenderer levelRenderer = event.getLevelRenderer();
    if (level.dimension() != RUBICON) { return; }
    // account for which frame of the animation the texture is
    int frame = (int) (level.getGameTime() % 15) / 3;

    // get poseStack to start rendering
    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();

    // FrameGraphBuilder needed to run multiple passes
//    event.get
    FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();

    // scale size depending on render distance
    // TODO : needs readjusting
    float renderDistance = (float) (levelRenderer.getLastViewDistance()/VIEW_DISTANCE_SCALE);
    poseStack.scale(renderDistance, 1f, renderDistance);

    // get cameraPos and lock the wave model at the proper height in the world
    Vec3 cameraPos = Minecraft.getInstance().getCameraEntity().position();

    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    matrix4fStack.pushMatrix();
    matrix4fStack.mul(poseStack.last().pose());

    // avoid crashes if the sprites were cleared 
    if (!this.GPU_SPRITE_ANIM_VIEW[frame].isClosed()) {
      this.renderSea(frameGraphBuilder, cameraPos, matrix4fStack, frame);
    } else {
      this.getSprites();
    }

    matrix4fStack.popMatrix();
    poseStack.popPose();
  }

  //##############################################
  //            RENDER HELPER METHODS
  //##############################################

  private void renderSea(FrameGraphBuilder frameGraphBuilder, Vec3 cameraPos, Matrix4fStack matrix4fStack, int frame) {
    RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
    GpuTextureView colorTextureView = target.getColorTextureView();
    GpuTextureView depthTextureView = target.getDepthTextureView();

    // setup dynamic uniforms
    GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
      matrix4fStack,
      new Vector4f(1f, 1f, 1f, 1f),
      new Vector3f(0f, (float) (HEIGHT - cameraPos.y()), 0f),
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
    RenderPass renderPass = encoder.createRenderPass(() -> {
      return "VoidSea";
    }, colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty());

    try {
      renderPass.setPipeline(VFRenderPipelines.VOID_SEA_DISTORT);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
      // See distort_vert.vsh
      renderPass.setUniform("ChunkOffset", this.positionBuffer.currentBuffer());

      renderPass.bindSampler("Sampler0", this.GPU_SPRITE_ANIM_VIEW[frame]);
      renderPass.bindSampler("Sampler1", this.GPU_SPRITE_ANIM_VIEW[frame]);

      renderPass.setVertexBuffer(0, this.seaMeshBuffer);
      renderPass.setIndexBuffer(this.seaMeshBuffer, RenderSystem.getSequentialBuffer(VertexFormat.Mode.TRIANGLES).type());
      renderPass.draw(0, this.seaMeshIndex);

      // manages closing the renderpass (annoying boilerplate)
    } catch (Throwable err) {
      if (renderPass != null) {
        try {
          renderPass.close();
        } catch (Throwable closeErr) {
          err.addSuppressed(closeErr);
        }
      }
      throw err;
    }

    if (renderPass != null) {
      renderPass.close();
    }

//    PostPass pass = new PostPass(VFRenderPipelines.VOID_SEA_DISTORT);
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
    ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.BLOCK.getVertexSize() * AMOUNT_OF_VERTICES);
    GpuBuffer gpuBuffer;

    try {
      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.BLOCK);
      
      putMesh(bufferBuilder, OFFSET, QUAD_SIZE, PADDING);

      MeshData meshData = bufferBuilder.buildOrThrow();
      // Handle storing the meshdata into the buffer and then closing the MeshData and byteBufferBuilder
      // Last step of making the positions we're rendering stuff in.
      try {
        this.seaMeshIndex = meshData.drawState().indexCount();
        ByteBuffer uploadBuffer = meshData.vertexBuffer();

        gpuBuffer = RenderSystem.getDevice().createBuffer(
          () -> { return "Void Sea Vertex Buffer"; },  
          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX, 
          uploadBuffer
        );
      } catch (Throwable err) {
        if (meshData != null) {
            try { 
              meshData.close(); 
            } catch (Throwable closeErr) { 
              err.addSuppressed(closeErr); 
            }
        }
        throw err;
      }
      if (meshData != null) {
        meshData.close();
      }
    } catch (Throwable err) {
      if (byteBufferBuilder != null) {
        try {
          byteBufferBuilder.close();
        } catch (Throwable closeErr) {
          err.addSuppressed(closeErr);
        }
      }
      throw err;
    }

    if (byteBufferBuilder != null) {
      byteBufferBuilder.close();
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
//    ResourceLocation textureAtlasResLoc = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/void_fluid/" + SPRITE_NAME + "_0");

//    ResourceLocation atlasLocation = Sheets.BLOCKS_MAPPER.apply(textureAtlasResLoc).atlasLocation();
//    Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(atlasLocation);
//    this.SPRITE = atlas.apply(textureAtlasResLoc);
    
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
