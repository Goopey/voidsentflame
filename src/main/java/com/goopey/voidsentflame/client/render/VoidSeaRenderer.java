package com.goopey.voidsentflame.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.VFGpuBuffers;
import com.goopey.voidsentflame.core.VFRenderPipelines;
import com.goopey.voidsentflame.core.VFRenderTypes;
import com.goopey.voidsentflame.core.VFGpuBuffers.GpuBuffersNames;
import com.goopey.voidsentflame.core.VFGpuBuffers.VFGpuBuffersNames;
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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

public class VoidSeaRenderer implements AutoCloseable {
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
  private static String SPRITE_NAME = "void_fluid";
  
  // Shader Stuff
  // TODO : Test
  // Around 35244
  private static final int AMOUNT_OF_VERTICES = (int) ((2 * OFFSET * PADDING/ QUAD_SIZE) * (2 * OFFSET * PADDING/ QUAD_SIZE)) * 6;
  private final MappableRingBuffer worldPosUbo;
  private GpuBuffer seaMeshBuffer;
  private int seaMeshIndex;
  
  // Cache Stuff
  private Map<Float, List<BakedQuad>> cachedQuads;
  private GpuBuffer seaBuffer;
  private GpuBuffer vertexBuffer;
  private GpuBuffer indexBuffer;
  // private BufferBuilder bufferBuilder;

  // Dimension Stuff
  private static final ResourceKey<Level> RUBICON = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("voidsentflame:rubicon"));

  //#################################################
  //                 INSTANCE
  //#################################################

  private VoidSeaRenderer() {
    // Cache
    this.cachedQuads = new HashMap<>();
    this.worldPosUbo = VFGpuBuffers.VFWorldPosUbo.apply(0);
    this.SPRITE = getSprite(SPRITE_NAME);
    this.seaMeshBuffer = buildSea();
  }

  public static VoidSeaRenderer getInstance() {
    return INSTANCE;
  }

  //######################################################
  //                  RENDER STUFF
  //######################################################

  
  /**
   * This is the function which actually does all the work to get stuff to appear/render in the world.
   * @param event the event needed to tie into the part of the general rendering pipeline which I want to be at
   */
  /**
  public void render(RenderLevelStageEvent.AfterEntities event) {
    // Check if in Rubicon
    Level level = event.getLevel();
    if (level.dimension() != RUBICON) { return; }
    
    // LevelRenderer levelRenderer = event.getLevelRenderer();
    Vec3 cameraPos = event.getCamera().position();
    TextureAtlasSprite sprite = getSprite(SPRITE_NAME);
    Matrix4f modelViewMatrix = event.getModelViewMatrix();
    RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
    // double viewDistance = levelRenderer.getLastViewDistance();
    // MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    // VertexConsumer builder = bufferSource.getBuffer(VFRenderTypes.VOID_SEA_DISTORT_RENDER);
      
    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();
    poseStack.translate(0, HEIGHT-cameraPos.y, 0);
    
    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    matrix4fStack.pushMatrix();
    matrix4fStack.mul(poseStack.last().pose());
    
    RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
    GpuBuffer vertexBuffer = RenderSystem.getQuadVertexBuffer();
    GpuBuffer indexBuffer = indices.getBuffer(6);
    
    ValidationGpuDevice vDevice = new ValidationGpuDevice(RenderSystem.getDevice(), false);
    CommandEncoder encoder = vDevice.createCommandEncoder();
    TextureManager manager = new TextureManager(vDevice);
    manager.writeToTexture(encoder, sprite.contents().getOriginalImage());
    

    // TODO: remove these useless references
    // GlCommandEncoder e;
    // ValidationCommandEncoder e2;
    SkyRenderer e;  
    GpuTextureView view = vDevice.createTextureView(manager.getTexture());
  
    GpuTextureView colorView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView(); 
    GpuTextureView depthView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView(); 

    // TODO: fix error, get all transform method inputs
    GpuBufferSlice dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(matrix4fStack, new Vector4f(0, 0, 0, 0), new Vector3f(), modelViewMatrix, 0); 

    VFGpuBuffers.UseWorldPos(worldPosUbo, cameraPos.toVector3f(), encoder);

    // createRenderPass takes the name, the colorTextureView, an OptionalInt for clearColor, a depthTextureView and an OptionalDouble for clearing the Depth Texture
    try (RenderPass pass = encoder.createRenderPass(() -> "testPass3", colorView, OptionalInt.empty(), depthView, OptionalDouble.of(1.0))) {
      BufferBuilder builder2 = new BufferBuilder(new ByteBufferBuilder(BUFFER_SIZE, BUFFER_SIZE), VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
      RenderSystem.bindDefaultUniforms(pass);
      
      pass.setPipeline(VFRenderPipelines.VOID_SEA_DISTORT);
      pass.setIndexBuffer(indexBuffer, indices.type());
      // sprite texture?
      pass.bindSampler("Sampler0", view);
      // lightmap texture?
      pass.bindSampler("Sampler2", view);
      
      pass.setUniform(GpuBuffersNames.DYNAMIC_TRANSFORM.name, dynamicUniforms);
      // pass.setUniform(VFGpuBuffersNames.WORLD_POS.name, this.worldPosUbo.currentBuffer());
      
      // for (BakedQuad quad : getCachedQuads(0, this.cachedQuads, poseStack, SPRITE_NAME, OFFSET, QUAD_SIZE, PADDING)) {
        //   builder2.putBulkData(poseStack.last(), quad, 1, 1, 1, 1, VFRenderConsts.RUBICON_PACKED_LIGHT, OFFSET);
        // }
      
      putVertex(builder2, 0, 0, 0, sprite.getU(0), sprite.getV(0), VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, poseStack.last());
      putVertex(builder2, 0, 0f, 0, sprite.getU(0), sprite.getV(1), VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, poseStack.last());
      putVertex(builder2, 0f, 0, 0, sprite.getU(1), sprite.getV(0), VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, poseStack.last());
      putVertex(builder2, 0f, 0f, 0, sprite.getU(1), sprite.getV(1), VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, poseStack.last());
        
      GpuBuffer uploadBuffer = RenderSystem.getDevice().createBuffer(() -> { return "VoidSeaRender MeshData upload buffer";}, 16, builder2.buildOrThrow().vertexBuffer());

      pass.setVertexBuffer(0, uploadBuffer);
      pass.draw(0, 4);

      // pass.draw(0, BUFFER_SIZE);
      pass.close();
    }
    
    manager.closeTexture();
    // managerSampler2.closeTexture();
    // funnyMap.close();
    view.close();
    matrix4fStack.popMatrix();
    poseStack.popPose();
  }
  */

  /**
   * This is the function which actually does all the work to get stuff to appear/render in the world.
   * @param event the event needed to tie into the part of the general rendering pipeline which I want to be at
   */
  /**
  public void render2(RenderLevelStageEvent.AfterEntities event) {
    // Check if in Rubicon
    Level level = event.getLevel();
    if (level.dimension() != RUBICON) { return; }

    // Level Renderer
    LevelRenderer levelRenderer = event.getLevelRenderer();
    PoseStack poseStack = event.getPoseStack();
    Vec3 cameraPos = event.getCamera().position();
    double viewDistance = levelRenderer.getLastViewDistance();
    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    
    // Start Rendering
    poseStack.pushPose();
    matrix4fStack.pushMatrix();

    // Set height to bottom of world
    poseStack.translate(0, HEIGHT-cameraPos.y, 0);
    poseStack.scale((float) (viewDistance/VIEW_DISTANCE_SCALE), 1f, (float) (viewDistance/VIEW_DISTANCE_SCALE));

    // pass in modelOffset value
    // #########################
    GpuTextureView colorTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
    GpuTextureView depthTextureView = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
    
    GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
      matrix4fStack, 
      new Vector4f(1f, 1f, 1f, 1f), 
      new Vector3f((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z), 
      new Matrix4f(), 
      0.0F
    );
    RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> {
      return "VoidSea";
    }, colorTextureView, OptionalInt.empty(), depthTextureView, OptionalDouble.empty());

    try {
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
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
    // #########################
    //close renderpass (needed for modelOffset)

    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    VertexConsumer builder = bufferSource.getBuffer(VFRenderTypes.VOID_SEA_DISTORT_RENDER);

    for (BakedQuad quad : getCachedQuads(0, this.cachedQuads, poseStack, SPRITE_NAME, OFFSET, QUAD_SIZE, PADDING)) {
      builder.putBulkData(poseStack.last(), quad, 1, 1, 1, 1, VFRenderConsts.RUBICON_PACKED_LIGHT, OFFSET);
    }

    // close rendering
    bufferSource.endBatch();
    matrix4fStack.popMatrix();
    poseStack.popPose();
  }*/

  public void render(RenderLevelStageEvent.AfterEntities event) {
    // Check if in Rubicon
    Level level = event.getLevel();
    if (level.dimension() != RUBICON) { return; }

    // get poseStack to start rendering
    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();

    Vec3 cameraPos = event.getCamera().position();
    poseStack.translate(0, HEIGHT - cameraPos.y, 0);
    // poseStack.mulPose(Axis.XP.rotationDegrees(90));

    TextureManager textureManager = Minecraft.getInstance().getTextureManager();
    ResourceLocation res = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/" + SPRITE_NAME);
    AbstractTexture abstractTexture = textureManager.getTexture(res);

    Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
    matrix4fStack.pushMatrix();
    matrix4fStack.mul(poseStack.last().pose());

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
      renderPass.setPipeline(VFRenderPipelines.VOID_SEA_DISTORT);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
      
      renderPass.bindSampler("Sampler0", abstractTexture.getTextureView());
      renderPass.bindSampler("Sampler2", abstractTexture.getTextureView());

      // VFGpuBuffers.UseWorldPos(this.worldPosUbo, cameraPos.toVector3f(), RenderSystem.getDevice().createCommandEncoder());
      // GpuTextureView view = RenderSystem.getDevice().createTextureView(textureManager.getTexture());
      // renderPass.setUniform(VFGpuBuffers.VFGpuBuffersNames.WORLD_POS.name, this.worldPosUbo.currentBuffer());

      renderPass.setVertexBuffer(0, this.seaMeshBuffer);
      renderPass.setIndexBuffer(this.seaMeshBuffer, RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).type());
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
      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

      // putBufferVertex(bufferBuilder, -OFFSET, 0, -OFFSET, this.SPRITE.getU(0), this.SPRITE.getV(0));
      // putBufferVertex(bufferBuilder, -OFFSET, 0, OFFSET, this.SPRITE.getU(0), this.SPRITE.getV(1));
      // putBufferVertex(bufferBuilder, OFFSET, 0, OFFSET, this.SPRITE.getU(1), this.SPRITE.getV(1));
      // putBufferVertex(bufferBuilder, OFFSET, 0, -OFFSET, this.SPRITE.getU(1), this.SPRITE.getV(0));
      
      putMesh(bufferBuilder, OFFSET, QUAD_SIZE, PADDING);

      MeshData meshData = bufferBuilder.buildOrThrow();

      // Handle storing the meshdata into the buffer and then closing the MeshData and byteBufferBuilder
      // Last step of making the positions we're rendering stuff in.
      try {
        this.seaMeshIndex = meshData.drawState().indexCount();
        gpuBuffer = RenderSystem.getDevice().createBuffer(
          () -> { return "Void Sea Vertex Buffer"; }, 
          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX, 
          meshData.vertexBuffer()
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
   * Helper method to generate a single quad at a specific position within a subdivided grid.
   * 
   * @param poseStack the PoseStack needed to add vertices to the quad.
   * @param xSubPos the x position within the subdivision. Goes from 0 to 1.
   * @param ySubPos the z position within the subdivision. Goes from 0 to 1.
   * @param increment the amount needed to get the next subdivided coordinate.
   * @param subdivisions int needed to define how many times the quad is divided.
   * @param lodMaxLevelStep the max amount of times the quads can be combined.
   * @param lodStepSize the amount of quads that get combined.
   * 
   * @return BakedQuad a single BakedQuad which will be rendered in the world.
   */
  private BakedQuad preBakeQuad(PoseStack poseStack, String name, int size, float xSubPos, float ySubPos) {
    PoseStack.Pose pose = poseStack.last();
    TextureAtlasSprite sprite = getSprite(name);

    QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer();
    builder.setHasAmbientOcclusion(false);

    // set positions of vertices
    float x0 = xSubPos;
    float z0 = ySubPos;
    float x1 = xSubPos + size;
    float z1 = ySubPos + size;
    
    float h0, h1, h2, h3 = h2 = h1 = h0 = 0;

    float u0 = sprite.getU(0);
    float v0 = sprite.getV(0);
    float u1 = sprite.getU(1);
    float v1 = sprite.getV(1);
    
    putVertex(builder, x0, h0, z0, u0, v0, VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, pose);
    putVertex(builder, x0, h1, z1, u0, v1, VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, pose);
    putVertex(builder, x1, h2, z1, u1, v1, VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, pose);
    putVertex(builder, x1, h3, z0, u1, v0, VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, pose);

    return builder.bakeQuad();
  }
  

  /**
   * Helper method to generate a large, subdivided grid of quads.
   * 
   * @param poseStack the PoseStack needed to add vertices to the quad.
   * @param spriteName the name needed to get the sprite to texture the quad.
   * @param size int which defines how large a quad can potentially be. The resulting quad will be 2x in length and height.
   * @param subdivisions int needed to define how many times the quad is divided.
   * @param lodMaxLevelStep the max amount of times the quads can be combined.
   * @param quadSize the amount of quads that get combined.
   * 
   * @return List<BakedQuad> A list of BakedQuads to render.
   */
  private List<BakedQuad> preBakeQuads(PoseStack poseStack, String spriteName, int size, int quadSize, float padding) {
    List<BakedQuad> list = new ArrayList<BakedQuad>();
    
    for (int x = -size; x < size; x+=quadSize) {
      for (int y = -size; y < size; y+=quadSize) {
        if (Math.sqrt(x * x + y * y) < size * padding) {
          list.add(preBakeQuad(poseStack, spriteName, quadSize, x - 0.5f, y - 0.5f));
        }
      }
    }

    return list;
  }

  /**
   * Helper method to generate a large, subdivided grid of quads.
   * 
   * @param builder the BufferBuilder to which vertices will be added.
   * @param size int which defines how large the grid can potentially be. The resulting grid will be 2x in length and height.
   * @param quadSize int needed to define how large the subdisivions of the quad will be.
   * @param padding a margin of space which makes the grid slightly larger.
   */
  private void putMesh(BufferBuilder builder, int size, int quadSize, float padding) {
    for (int x = -size; x < size; x+=quadSize) {
      for (int y = -size; y < size; y+=quadSize) {
        if (Math.sqrt(x * x + y * y) < size * padding) {
          putMeshVertex(builder, quadSize, x - 0.5f, y - 0.5f);
        }
      }
    }
  }

  /**
   * Helper method to generate a single quad at a specific position within a subdivided grid.
   * 
   * @param poseStack the PoseStack needed to add vertices to the quad.
   * @param xSubPos the x position within the subdivision. Goes from 0 to 1.
   * @param ySubPos the z position within the subdivision. Goes from 0 to 1.
   * @param increment the amount needed to get the next subdivided coordinate.
   * @param subdivisions int needed to define how many times the quad is divided.
   * @param lodMaxLevelStep the max amount of times the quads can be combined.
   * @param lodStepSize the amount of quads that get combined.
   * 
   * @return BakedQuad a single BakedQuad which will be rendered in the world.
   */
  private void putMeshVertex(BufferBuilder builder, int size, float xSubPos, float ySubPos) {
    // set positions of vertices
    float x0 = xSubPos;
    float z0 = ySubPos;
    float x1 = xSubPos + size;
    float z1 = ySubPos + size;
    
    float u0 = this.SPRITE.getU(0);
    float v0 = this.SPRITE.getV(0);
    float u1 = this.SPRITE.getU(1);
    float v1 = this.SPRITE.getV(1);

    putBufferVertex(builder, x0, 0, z0, u0, v0);
    putBufferVertex(builder, x0, 0, z1, u0, v1);
    putBufferVertex(builder, x1, 0, z1, u1, v1);
    
    putBufferVertex(builder, x1, 0, z1, u1, v1);
    putBufferVertex(builder, x1, 0, z0, u1, v0);
    putBufferVertex(builder, x0, 0, z0, u0, v0);
}

  /**
   * Caches quads using a given key and returns them.
   * 
   * @param key the key to get the object from the cache.
   * @param cache the map which contains variables mapped to keys.
   * @param poseStack the PoseStack needed to add vertices to the quads.
   * @param spriteName the name of the Sprite needed to texture the quads.
   * @param size int which defines how large the quad is supposed to be. Forms a square 2x in length.
   * @param subdivisions int which defines how many times the quad is divided.
   * @param lodMaxLevelStep the max amount of times the quads can be combined.
   * @param quadSize the amount of quads that get combined.
   * 
   * @return the cached list of quads.
   */
  private List<BakedQuad> getCachedQuads(float key, Map<Float, List<BakedQuad>> cache, PoseStack poseStack, String spriteName, int size, int quadSize, float padding) {
    return cache.computeIfAbsent(key, k -> preBakeQuads(poseStack, spriteName, size, quadSize, padding));
  }

  /**
   * Helper method which gets a sprite from the block TextureAtlas using it's name.
   * 
   * @return TextureAtlasSprite A Sprite in the TextureAtlas
   */
  private TextureAtlasSprite getSprite(String spriteName) {
    if (this.SPRITE == null) {
      ResourceLocation res = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/" + spriteName);
      ResourceLocation atlasLocation = Sheets.BLOCKS_MAPPER.apply(res).atlasLocation();
      Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(atlasLocation);
      this.SPRITE = atlas.apply(res);
    }

    return this.SPRITE;
  }

  /**
   * TODO comment
   * @param wx the x position in the wave
   * @param wz the z position in the wave
   * @param t the time
   * @return the height of the wave
   */
  private float waveHeight(double wx, double wz, double t) {
    double val = Math.sin((wx + wz) * FREQUENCY + t * SPEED);
    return (float)(val * AMPLITUDE);
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
  private void putVertex(VertexConsumer builder, float x, float y, float z, float u, float v, int light, int overlay, PoseStack.Pose pose) {
    builder.addVertex(x, y, z)
    .setColor(1f, 1f, 1f, 1f)
    .setUv(u, v)
    .setOverlay(overlay)
    .setLight(light)
    .setNormal(pose, 0, 1, 0);
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

  @Override
  public void close() throws Exception {
    this.seaMeshBuffer.close();
  }
}
