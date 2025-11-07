package com.goopey.voidsentflame.core;

import net.minecraft.client.renderer.MappableRingBuffer;

public class VFGpuBuffers {
  // Vanilla UBOs
  public static MappableRingBuffer DynamicTransformsUbo;
  public static MappableRingBuffer MatrixUbo;
  public static MappableRingBuffer ProjectionUbo;
  public static MappableRingBuffer FogUbo;
  public static MappableRingBuffer GlobalsUbo;
  public static MappableRingBuffer LightUbo;

  // Custom UBOs
  public static MappableRingBuffer WorldPosUbo;

  static {
    
  }

  public enum VFGpuBuffersNames {
    DYNAMIC_TRANSFORM("DynamicTransforms"),
    MATRIX("Matrix"),
    PROJECTION("Projection"),
    FOG("Fog"),
    GLOBALS("Globals"),
    LIGHT("Light"),
    WORLD_POS("WorldPos");

    public String name;

    private VFGpuBuffersNames(String name) {
      this.name = name;
    }
  }
}
