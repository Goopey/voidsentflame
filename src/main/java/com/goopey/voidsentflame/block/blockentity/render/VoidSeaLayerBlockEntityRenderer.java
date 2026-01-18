package com.goopey.voidsentflame.block.blockentity.render;

import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class VoidSeaLayerBlockEntityRenderer implements BlockEntityRenderer<VoidSeaLayerBlockEntity, VoidSeaLayerBlockEntityRenderState> {
  // Render Triangles
  private static final int SUBDIV = 4;
  private static final float AMPLITUDE = 0.5f;
  private static final float FLOOR_RANGE = 8f;
  private static final float FREQUENCY = 0.5f;
  private static final float SPEED = 0.05f;
  private static final float HEIGHT = 21.5f;
  private TextureAtlasSprite SPRITE;
  private static String SPRITE_NAME = "void_fluid";

  // Render Distance
  private static int RENDER_DISTANCE = 128;

  // Shader Stuff
  private RenderType EXAMPLE_RENDER;
  private static String GPU_TEXTURE_NAME = "void_waves";
  private GpuDevice gpu;
  private RenderPipeline distortPipeline;
  private RenderPipeline wavePipeline;
  private GpuBuffer quadBuffer;

  public VoidSeaLayerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    ResourceLocation res = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/"+ SPRITE_NAME);
    ResourceLocation atlasLocation = Sheets.BLOCKS_MAPPER.apply(res).atlasLocation();
  }

  //#####################################################
  //                  FRUSTUM CULLING
  //#####################################################

  @Override
  public int getViewDistance() {
    return RENDER_DISTANCE;
  }

  @Override
  public AABB getRenderBoundingBox(VoidSeaLayerBlockEntity blockEntity) {
    return new AABB(
      -RENDER_DISTANCE, Double.NEGATIVE_INFINITY, -RENDER_DISTANCE,
      RENDER_DISTANCE, Double.POSITIVE_INFINITY, RENDER_DISTANCE
    );
  }

  @Override
  public boolean shouldRender(@Nonnull VoidSeaLayerBlockEntity blockEntity, @Nonnull Vec3 cameraPos) {
    BlockPos pos = blockEntity.getBlockPos();
    float x = pos.getX() - (float) cameraPos.x;
    float z = pos.getZ() - (float) cameraPos.z;
    float distSq = x * x + z * z;

    return distSq < (this.getViewDistance() * this.getViewDistance());
  }


  @Override
  public boolean shouldRenderOffScreen() {
    return true;
  }

  //#####################################################
  //                  RENDER METHODS
  //#####################################################

    @Override
    public VoidSeaLayerBlockEntityRenderState createRenderState() {
        return new VoidSeaLayerBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(VoidSeaLayerBlockEntity blockEntity, VoidSeaLayerBlockEntityRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
    }

    @Override
    public void submit(VoidSeaLayerBlockEntityRenderState voidSeaLayerBlockEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {

    }


    //  @Override
    public void render(@Nonnull VoidSeaLayerBlockEntity blockEntity, float partialTick, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int packedLight, int packedOverlay, @Nonnull Vec3 cameraPos) {
        //TODO : Cleanup Code

        // Map<Long, Float> heightCache = new HashMap<>();

        // // VoidsentFlameMod.LOGGER.info("Packed Light:["+packedLight+"]      Packed Overlay:["+packedOverlay+"]");

        // long gameTime = Minecraft.getInstance().level.getGameTime();
        // double t = gameTime + partialTick;

        // BlockPos basePos = blockEntity.getBlockPos();
        // VertexConsumer builder = bufferSource.getBuffer(this.EXAMPLE_RENDER);

        // poseStack.pushPose();

        // // set height to 5 blocks
        // poseStack.translate(0, HEIGHT, 0);

        // Matrix4f mat = poseStack.last().pose();
        // PoseStack.Pose pose = poseStack.last();

        // // iterate across grid
        // for (int ix = 0; ix < VoidSeaLayerBlockEntity.WIDTH; ix++) {
        //   for (int iz = 0; iz < VoidSeaLayerBlockEntity.LENGTH; iz++) {
        //     // inside each block, subdivide
        //     for (int sx = 0; sx < SUBDIV; sx++) {
        //       for (int sz = 0; sz < SUBDIV; sz++) {
        //         // compute fractional positions 0..1
        //         float x0 = ix + (float)sx / SUBDIV;
        //         float z0 = iz + (float)sz / SUBDIV;
        //         float x1 = ix + (float)(sx+1) / SUBDIV;
        //         float z1 = iz + (float)(sz+1) / SUBDIV;

        //         // world positions for wave sample
        //         double wx0 = basePos.getX() + x0;
        //         double wz0 = basePos.getZ() + z0;
        //         double wx1 = basePos.getX() + x1;
        //         double wz1 = basePos.getZ() + z0;
        //         double wx2 = basePos.getX() + x1;
        //         double wz2 = basePos.getZ() + z1;
        //         double wx3 = basePos.getX() + x0;
        //         double wz3 = basePos.getZ() + z1;

        //         // heights = sin wave
        //         float h0 = getCachedHeight(wx0, wz0, t, heightCache);
        //         float h1 = getCachedHeight(wx1, wz1, t, heightCache);
        //         float h2 = getCachedHeight(wx2, wz2, t, heightCache);
        //         float h3 = getCachedHeight(wx3, wz3, t, heightCache);

        //         // compute UV coords (u0,v0 etc)
        //         float u0 = this.SPRITE.getU0();
        //         float v0 = this.SPRITE.getV0();
        //         float u1 = this.SPRITE.getU1();
        //         float v1 = this.SPRITE.getV1();

        //         // top surface
        //         putVertex(builder, mat, x0, h0, z0, u0, v0, packedLight, packedOverlay, pose);
        //         putVertex(builder, mat, x0, h3, z1, u0, v1, packedLight, packedOverlay, pose);
        //         putVertex(builder, mat, x1, h2, z1, u1, v1, packedLight, packedOverlay, pose);

        //         putVertex(builder, mat, x1, h2, z1, u1, v1, packedLight, packedOverlay, pose);
        //         putVertex(builder, mat, x1, h1, z0, u1, v0, packedLight, packedOverlay, pose);
        //         putVertex(builder, mat, x0, h0, z0, u0, v0, packedLight, packedOverlay, pose);

        //         // bottom surface
        //         reversePutVertex(builder, mat, x0, h0, z0, u0, v0, packedLight, packedOverlay, pose);
        //         reversePutVertex(builder, mat, x0, h3, z1, u0, v1, packedLight, packedOverlay, pose);
        //         reversePutVertex(builder, mat, x1, h2, z1, u1, v1, packedLight, packedOverlay, pose);

        //         reversePutVertex(builder, mat, x1, h2, z1, u1, v1, packedLight, packedOverlay, pose);
        //         reversePutVertex(builder, mat, x1, h1, z0, u1, v0, packedLight, packedOverlay, pose);
        //         reversePutVertex(builder, mat, x0, h0, z0, u0, v0, packedLight, packedOverlay, pose);
        //       }
        //     }
        //   }
        // };

        // poseStack.popPose();
    }

  /**
   * Method used to cache height of specific sets of coordinates to improve performance.
   *
   * @param wx the x position of the wave in the grid
   * @param wz the z position of the wave in the grid
   * @param t time in ticks. Needed for waveheight to determine the height at the current moment.
   * @param cache the map which caches a set of coordinates to a height value
   * @return
   */
  private float getCachedHeight(double wx, double wz, double t, Map<Long, Float> cache) {
    long key = (long) (Math.floor((wx + wz) * FLOOR_RANGE) / FLOOR_RANGE);
    return cache.computeIfAbsent(key, k -> waveHeight(wx, wz, t));
  }

  private float waveHeight(double wx, double wz, double t) {
    double val = Math.sin((wx + wz) * FREQUENCY + t * SPEED);
    return (float)(val * AMPLITUDE);
  }

  private void putVertex(VertexConsumer builder, Matrix4f mat, float x, float y, float z, float u, float v, int light, int overlay, PoseStack.Pose pose) {
    builder.addVertex(mat, x, y, z)
    .setColor(1f, 1f, 1f, 1f)
    .setUv(u, v)
    .setOverlay(overlay)
    .setLight(light)
    .setNormal(pose, 0, 1, 0);
  }

  private void reversePutVertex(VertexConsumer builder, Matrix4f mat, float x, float y, float z, float u, float v, int light, int overlay, PoseStack.Pose pose) {
    builder.addVertex(mat, x, y, z)
    .setColor(1f, 1f, 1f, 1f)
    .setUv(u, v)
    .setOverlay(overlay)
    .setLight(light)
    .setNormal(pose, 0, -1, 0);
  }

  //#####################################################
  //                  RENDER PIPELINE
  //#####################################################

  private void setupPipelines() {
    // Build wave pipeline
    this.wavePipeline = RenderPipelines.register(RenderPipeline.builder()
      .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "wave_pipeline"))
      .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "wave"))
      .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "wave"))
      .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
      .withUniform("modelViewProj", UniformType.UNIFORM_BUFFER)
      .withUniform("time", UniformType.UNIFORM_BUFFER)
      .withSampler("tex")
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .build()
    );

    // Build distortion pipeline
    this.distortPipeline = RenderPipelines.register(RenderPipeline.builder()
      .withLocation(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "distort_pipeline"))
      .withVertexShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "distort"))
      .withFragmentShader(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "distort"))
      .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
      .withUniform("waveCenter", UniformType.UNIFORM_BUFFER)
      .withUniform("radius", UniformType.UNIFORM_BUFFER)
      .withUniform("time", UniformType.UNIFORM_BUFFER)
      .withUniform("screenSize", UniformType.UNIFORM_BUFFER)
      .withSampler("sceneTex")
      .withSampler("sceneDepth")
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .build()
    );
  }

  private void setupQuad() {
    float[] quadVerts = new float[] {
        -1f, -1f,  0f, 1f,
        -1f,  1f,  0f, 0f,
        1f,  1f,  1f, 0f,
        -1f, -1f,  0f, 1f,
        1f,  1f,  1f, 0f,
        1f, -1f,  1f, 1f
    };
  }
}
