package com.goopey.voidsentflame.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.TrackedWaypoint.Camera;
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
  private static String SPRITE_NAME = "void_fluid";
  
  // Shader Stuff
  private static final int PACKED_LIGHT = 15728880;
  private static final int PACKED_OVERLAY = 655360;
  private RenderType distortRender;
  public RenderPipeline.Snippet voidSeaTerrainSnippet;
  public RenderPipeline.Snippet voidSeaFogMatricesSnippet;
  public RenderPipeline.Snippet voidSeaFogSnippet;
  public RenderPipeline.Snippet voidSeaMatricesSnippet;
  public RenderPipeline.Snippet voidSeaGlobalsFogMatricesSnippet;
  public RenderPipeline distortPipeline;
  
  // Cache Stuff
  private Map<Float, List<BakedQuad>> cachedQuads;

  // Dimension Stuff
  private static final ResourceKey<Level> RUBICON = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("voidsentflame:rubicon"));

  //#################################################
  //                 INSTANCE
  //#################################################

  private VoidSeaRenderer() {
    // Cache
    this.cachedQuads = new HashMap<>();

    // Render Pipeline
    this.voidSeaMatricesSnippet = RenderPipeline.builder(new RenderPipeline.Snippet[0]).withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).buildSnippet();
    this.voidSeaFogSnippet = RenderPipeline.builder(new RenderPipeline.Snippet[0]).withUniform("Fog", UniformType.UNIFORM_BUFFER).buildSnippet();
    this.voidSeaFogMatricesSnippet = RenderPipeline.builder(new RenderPipeline.Snippet[]{this.voidSeaMatricesSnippet, this.voidSeaFogSnippet}).buildSnippet();
    this.voidSeaTerrainSnippet = RenderPipeline.builder(new RenderPipeline.Snippet[]{this.voidSeaFogMatricesSnippet})
      .withVertexShader("core/terrain").withFragmentShader("core/terrain")
      .withSampler("Sampler0").withSampler("Sampler2")
      .withVertexFormat(DefaultVertexFormat.BLOCK, Mode.QUADS)
      .buildSnippet();
    this.voidSeaGlobalsFogMatricesSnippet = RenderPipeline.builder(new RenderPipeline.Snippet[]{this.voidSeaTerrainSnippet})
      .withUniform("Globals", UniformType.UNIFORM_BUFFER)
      .buildSnippet();

    // MappableRingBuffer timeUbo = new MappableRingBuffer(
    //   () -> "Time Ubo", 
    //   GpuBuffer.USAGE_UNIFORM, 
    //   new Std140SizeCalculator().putFloat().get()
    // );

    this.distortPipeline = RenderPipelines.register(
      RenderPipeline.builder(
        new RenderPipeline.Snippet[]{this.voidSeaGlobalsFogMatricesSnippet})
        // sets a pipeline name, not an actual file
        .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "pipeline/distort"))
        .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/main/distort_vert"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "core/main/distort_frag"))
        .withVertexFormat(
          VertexFormat.builder().add("UV0", VertexFormatElement.UV0).add("Position", VertexFormatElement.POSITION).build(), 
          VertexFormat.Mode.QUADS)
        .withColorWrite(true, false)
        .withCull(false)
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .build());

    // Renderer
    this.distortRender = RenderType.create(
      VoidsentFlameMod.MODID + ":distort_render", 4192, 
      false, false, 
      this.distortPipeline, RenderType.CompositeState.builder()
        .setLightmapState(LightmapStateShard.LIGHTMAP)
        .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
        .createCompositeState(false));
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
  public void render(RenderLevelStageEvent.AfterEntities event) {
    // Check if in Rubicon
    Level level = event.getLevel();
    if (level.dimension() != RUBICON) { return; }

    // Level Renderer
    LevelRenderer levelRenderer = event.getLevelRenderer();
    PoseStack poseStack = event.getPoseStack();
    Camera camera = event.getCamera();
    double viewDistance = levelRenderer.getLastViewDistance();
    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    VertexConsumer builder = bufferSource.getBuffer(this.distortRender);

    // Time
    long gameTime = Minecraft.getInstance().level.getGameTime();
    double t = gameTime + event.getPartialTick().getGameTimeDeltaTicks();

    // Start Rendering
    poseStack.pushPose();
    // Set height to bottom of world
    poseStack.translate(0, HEIGHT-camera.position().y, 0);
    poseStack.scale((float) (viewDistance/VIEW_DISTANCE_SCALE), 1f, (float) (viewDistance/VIEW_DISTANCE_SCALE));

    // red, blue, green and alpha go from 0..1
    for (BakedQuad quad : getCachedQuads(0, this.cachedQuads, poseStack, SPRITE_NAME, OFFSET, QUAD_SIZE, PADDING)) {
      builder.putBulkData(poseStack.last(), quad, 1, 1, 1, 1, PACKED_LIGHT, OFFSET);
    }

    poseStack.popPose();
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
    
    putVertex(builder, x0, h0, z0, u0, v0, PACKED_LIGHT, PACKED_OVERLAY, pose);
    putVertex(builder, x0, h1, z1, u0, v1, PACKED_LIGHT, PACKED_OVERLAY, pose);
    putVertex(builder, x1, h2, z1, u1, v1, PACKED_LIGHT, PACKED_OVERLAY, pose);
    putVertex(builder, x1, h3, z0, u1, v0, PACKED_LIGHT, PACKED_OVERLAY, pose);

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
      ResourceLocation res = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/"+ spriteName);
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
}
