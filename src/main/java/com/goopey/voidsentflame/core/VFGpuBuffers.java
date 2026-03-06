package com.goopey.voidsentflame.core;

import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.BeanProperty;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.CommandEncoder;

import net.minecraft.client.renderer.MappableRingBuffer;

public class VFGpuBuffers {
  // Vanilla UBOs
  public static Function<Integer, MappableRingBuffer> dynamicTransformsUbo;
  public static Function<Integer, MappableRingBuffer> fogUbo;
  public static Function<Integer, MappableRingBuffer> globalsUbo;
  public static Function<Integer, MappableRingBuffer> lightUbo;
  public static Function<Integer, MappableRingBuffer> matrixUbo;
  public static Function<Integer, MappableRingBuffer> projectionUbo;

  // Custom UBOs
  public static Supplier<MappableRingBuffer> VFWorldPosUbo;
  public static Supplier<MappableRingBuffer> VFHeightPosUbo;

  //#####################################################################
  //#####################################################################
  //                   VANILLA BUFFERS - GET AND USE
  //#####################################################################
  //#####################################################################

  // Vanilla Buffers
  static {
    dynamicTransformsUbo = e -> new MappableRingBuffer(
      () -> GpuBuffersNames.DYNAMIC_TRANSFORM.name, 
      GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 
      new Std140SizeCalculator()
        .putMat4f()
        .putVec4()
        .putVec3()
        .putMat4f()
        .putFloat()
        .get());
    matrixUbo = e -> new MappableRingBuffer(
      () -> GpuBuffersNames.MATRIX.name, 
      GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 
      new Std140SizeCalculator().get());
  }
  // default uniforms
  static {
    fogUbo = e -> new MappableRingBuffer(
      () -> GpuBuffersNames.FOG.name, 
      GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 
      new Std140SizeCalculator()
        .putVec4()
        .putFloat().putFloat().putFloat().putFloat().putFloat().putFloat()
        .get());
    globalsUbo = e -> new MappableRingBuffer(
      () -> GpuBuffersNames.GLOBALS.name, 
      GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 
      new Std140SizeCalculator()
        .putVec2()
        .putFloat().putFloat()
        .putInt()
        .get());
    lightUbo = e -> new MappableRingBuffer(
      () -> GpuBuffersNames.LIGHT.name, 
      GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 
      new Std140SizeCalculator()
        .putVec3().putVec3()
        .get());
    projectionUbo = e -> new MappableRingBuffer(
      () -> GpuBuffersNames.PROJECTION.name, 
      GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 
      new Std140SizeCalculator()
        .putMat4f()
        .get());
  }
  
  /**
   * Manages using a DynamicTransforms UBO. Does the rotation and assigning of values
   * 
   * @param ubo the DynamicTransforms MappabeRingBuffer UBO
   * @param modelViewMat the ModelViewMatrix
   * @param colorModulator the Color Modulator
   * @param modelOffset the modelOffset
   * @param textureMatrix the textureMatrix
   * @param lineWidth the line width
   * @param encoder the Validated Command Encoder to add values to the buffer
   */
  public static void UseDynamicTransforms(MappableRingBuffer ubo, Matrix4fc modelViewMat, Vector4f colorModulator, Vector3f modelOffset, Matrix4fc textureMatrix, float lineWidth, CommandEncoder encoder) {
    ubo.rotate();
    try (GpuBuffer.MappedView bufferView = encoder.mapBuffer(ubo.currentBuffer(), false, true)) {
      Std140Builder.intoBuffer(bufferView.data())
        .putMat4f(modelViewMat)
        .putVec4(colorModulator)
        .putVec3(modelOffset)
        .putMat4f(textureMatrix)
        .putFloat(lineWidth);
    }
  }

  /**
   * No use writing data to UseMatrix. Merely rotates the UBO
   * 
   * @param ubo the Matrix MappableRingBuffer UBO
   * @param encoder the Validated Command Encoder to add values to the buffer
   */
  public static void UseMatrix(MappableRingBuffer ubo, CommandEncoder encoder) {
    ubo.rotate();
    try (GpuBuffer.MappedView bufferView = encoder.mapBuffer(ubo.currentBuffer(), false, true)) {
      Std140Builder.intoBuffer(bufferView.data());
    }
  }

    /**
   * Manages using a Fog UBO. Does the rotation and assigning of values
   * 
   * @param ubo the DynamicTransforms MappabeRingBuffer UBO
   * @param fogColor the color of the fog to be renderer
   * @param fogEnvironmentalStart the start at which the fog starts rolling in
   * @param fogEnvironmentalEnd the end at which the fog reduces vision
   * @param fogRenderDistanceStart the start at which the world fog starts rolling in 
   * @param fogRenderDistanceEnd the end at which the fog completely blocks vision
   * @param fogSkyEnd the end sky fog
   * @param fogCloudsEnd the fog which affects the cloud layer
   * @param encoder the Validated Command Encoder to add values to the buffer
   */
  public static void UseFog(MappableRingBuffer ubo, Vector4f fogColor, float fogEnvironmentalStart, float fogEnvironmentalEnd, float fogRenderDistanceStart, float fogRenderDistanceEnd, float fogSkyEnd, float fogCloudsEnd, CommandEncoder encoder) {
    ubo.rotate();
    try (GpuBuffer.MappedView bufferView = encoder.mapBuffer(ubo.currentBuffer(), false, true)) {
      Std140Builder.intoBuffer(bufferView.data())
        .putVec4(fogColor)
        .putFloat(fogEnvironmentalStart)
        .putFloat(fogEnvironmentalEnd)
        .putFloat(fogRenderDistanceStart)
        .putFloat(fogRenderDistanceEnd)
        .putFloat(fogSkyEnd)
        .putFloat(fogCloudsEnd);
    }
  }
  
  //#####################################################################
  //#####################################################################
  //                   CUSTOM BUFFERS - GET AND USE
  //#####################################################################
  //#####################################################################

  static {
    VFWorldPosUbo = () -> new MappableRingBuffer(
      () -> VFGpuBuffersNames.WORLD_POS.name,
        GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
          new Std140SizeCalculator()
            .putVec3()
            .get());
  }

  /**
   * Manages using a a VFWorldPos UBO. Does the rotation and assigning of values
   * 
   * @param ubo the VFWorldPosUbo MappableRingBuffer
   * @param pos the position to pass to the buffer
   * @param encoder the Validated Encoder to add values to the encoder
   */
  public static void UseWorldPos(MappableRingBuffer ubo, Vector3f pos, CommandEncoder encoder) {
    ubo.rotate();
    try (GpuBuffer.MappedView bufferView = encoder.mapBuffer(ubo.currentBuffer(), false, true)) {
      Std140Builder std140Builder = Std140Builder.intoBuffer(bufferView.data());
      std140Builder.putVec3(pos);
    }
  }

  //#####################################################################
  //#####################################################################
  //                            ENUM NAMES
  //#####################################################################
  //#####################################################################

  // Vanilla UBO names
  public enum GpuBuffersNames {
    DYNAMIC_TRANSFORM("DynamicTransforms"),
    FOG("Fog"),
    GLOBALS("Globals"),
    LIGHT("Lighting"),
    PROJECTION("Projection"),
    MATRIX("Matrix");

    public String name;
    
    private GpuBuffersNames(String name) {
      this.name = name;
    }
  }

  // Custom UBO names
  public enum VFGpuBuffersNames {
    WORLD_POS("VFWorldPosition");

    public String name;

    private VFGpuBuffersNames(String name) {
      this.name = name;
    }
  }
}
