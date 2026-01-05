package com.goopey.voidsentflame.client.render;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;

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
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite.Ticker;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

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
  private GpuTextureView GPU_SPRITE_VIEW;
  private final static String SPRITE_NAME = "void_fluid";
  
  // Shader Stuff
  // Around 35 244 * 6 = 211 464
  private static final int AMOUNT_OF_VERTICES = (int) ((2 * OFFSET * PADDING/ QUAD_SIZE) * (2 * OFFSET * PADDING/ QUAD_SIZE)) * 6;
  private GpuBuffer seaMeshBuffer;
  private int seaMeshIndex;

  // Dimension Stuff
  private static final ResourceKey<Level> RUBICON = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("voidsentflame:rubicon"));

  //#################################################
  //                 INSTANCE
  //#################################################

  private VoidSeaRenderer() {
    // Cache
    this.getSprites();
    this.seaMeshBuffer = buildSea();
  }

  public static VoidSeaRenderer getInstance() {
    return INSTANCE;
  }

  //######################################################
  //                  RENDER STUFF
  //######################################################

  public void render(RenderLevelStageEvent.AfterEntities event) {
    // Check if in Rubicon
    Level level = event.getLevel();
    if (level.dimension() != RUBICON) { return; }

    // get poseStack to start rendering
    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();

    Vec3 cameraPos = event.getCamera().position();
    poseStack.translate(0, HEIGHT - cameraPos.y, 0);

    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    matrix4fStack.pushMatrix();
    matrix4fStack.mul(poseStack.last().pose());

    Ticker spriteTicker = this.SPRITE.createTicker();
    GpuTextureView colorTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
    GpuTextureView depthTextureView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

    GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
      matrix4fStack, 
      new Vector4f(1f, 1f, 1f, 1f), 
      new Vector3f((float) cameraPos.x, 0f, (float) cameraPos.z), 
      new Matrix4f(), 
      0.0F
    );

    RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> {
      return "VoidSea";
    }, colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty());
    
    try {
      GpuTexture tickerTextureUpload = this.GPU_SPRITE_VIEW.texture();
      spriteTicker.tickAndUpload(tickerTextureUpload);
      GpuTextureView tickerTextureView = RenderSystem.getDevice().createTextureView(tickerTextureUpload);

      renderPass.setPipeline(VFRenderPipelines.VOID_SEA_DISTORT);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
      
      renderPass.bindSampler("Sampler0", tickerTextureView);
      renderPass.bindSampler("Sampler2", this.GPU_SPRITE_VIEW);

      renderPass.setVertexBuffer(0, this.seaMeshBuffer);
      renderPass.setIndexBuffer(this.seaMeshBuffer, RenderSystem.getSequentialBuffer(VertexFormat.Mode.TRIANGLES).type());
      renderPass.draw(0, this.seaMeshIndex);
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

    spriteTicker.close();
    matrix4fStack.popMatrix();
    poseStack.popPose();
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
   * @param poseStack the PoseStack needed to add vertices to the quad.
   * @param xSubPos the x position within the subdivision. Goes from 0 to 1.
   * @param zSubPos the z position within the subdivision. Goes from 0 to 1.
   * @param increment the amount needed to get the next subdivided coordinate.
   * @param subdivisions int needed to define how many times the quad is divided.
   * @param lodMaxLevelStep the max amount of times the triangles can be combined.
   * @param lodStepSize the amount of triangles that get combined.
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
    float u1 = 1f, v1 = 0.25f;

    putBufferVertex(builder, x0, 0, z0, u0, v0);
    putBufferVertex(builder, x0, 0, z1, u0, v1);
    putBufferVertex(builder, x1, 0, z1, u1, v1);
    
    putBufferVertex(builder, x1, 0, z1, u1, v1);
    putBufferVertex(builder, x1, 0, z0, u1, v0);
    putBufferVertex(builder, x0, 0, z0, u0, v0);
  }

  /**
   * Helper method which gets a sprite from the block TextureAtlas using it's name.
   * 
   * @return TextureAtlasSprite A Sprite in the TextureAtlas
   */
  private void getSprites() {
    ResourceLocation textureAtlasResLoc = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/" + SPRITE_NAME);
    // Gpu ResourceLocation is different from textureAtlasResLoc, I have 0 idea why. 
    // Seems it needs its path more defined than the regular sprite loading method.
    // Doesn't work if I use the regular path description.
    ResourceLocation gpuResLoc = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "textures/block/" + SPRITE_NAME + ".png");

    ResourceLocation atlasLocation = Sheets.BLOCKS_MAPPER.apply(textureAtlasResLoc).atlasLocation();
    Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(atlasLocation);
    this.SPRITE = atlas.apply(textureAtlasResLoc);
    
    AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(gpuResLoc);
    this.GPU_SPRITE_VIEW = abstractTexture.getTextureView();
    // get ticker texture instead. We initialize GPU_SPRITE using AbstractTexture, but we only use the ticker texture.
    Ticker spriteTicker = this.SPRITE.createTicker();
    GpuTexture gpuSpriteTexture = this.GPU_SPRITE_VIEW.texture();
    spriteTicker.tickAndUpload(gpuSpriteTexture);
    this.GPU_SPRITE_VIEW = RenderSystem.getDevice().createTextureView(gpuSpriteTexture);
    spriteTicker.close();
  }
  
  /**
   * Used to add a vertex at specific coordinates with an upwards normal
   * 
   * @param builder the builder needed to add the vertices to a mesh
   * @param mat the initial matrix position
   * @param x the 1st position of the vertex
   * @param y the 2nd position of the vertex
   * @param z the 3rd position of the vertex
   * @param u the 1st UV position
   * @param v the 2nd UV position
   * @param light the packedLight value
   * @param overlay the packedOverlay value
   * @param pose the last PoseStack.Pose
   */
  private void putBufferVertex(BufferBuilder builder, float x, float y, float z, float u, float v) {
    builder.addVertex(x, y, z)
    .setColor(1f, 1f, 1f, 1f)
    .setUv(u, v)
    .setOverlay(VFRenderConsts.RUBICON_PACKED_OVERLAY)
    .setLight(VFRenderConsts.RUBICON_PACKED_LIGHT)
    .setNormal(0, 1, 0);
  }
}
