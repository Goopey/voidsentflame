package com.goopey.voidsentflame.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.util.Tuple;

import static com.goopey.voidsentflame.util.VertexMeshHelper.putBufferVertex;

public class BufferBuilderHelper {
  //###############################################
  //                GENERAL BUFFER
  //###############################################
  /**
   * Builds a screen quad using a specific packedLight or packedOverlay value.
   * @param packedLight the world's packedLight value that the screenBuffer will default to
   * @param packedOverlay the world's packedOverlay value that the screenBuffer will default to
   * @param size the screen size multiplier. Should be 1f to get full screen size.
   * @return a Tuple containing the index and buffer of the screen
   */
  public static Tuple<Integer, GpuBuffer> buildScreen(int packedLight, int packedOverlay, float size) {
    VertexFormat format = DefaultVertexFormat.POSITION_TEX;
    VertexFormat.Mode mode = VertexFormat.Mode.QUADS;
    Tuple<Integer, GpuBuffer> retVal = new Tuple<>(0, (GpuBuffer) null);

    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(6 * format.getVertexSize())) {
      BufferBuilder builder = new BufferBuilder(byteBufferBuilder, mode, format);

      putBufferVertex(builder, packedLight, packedOverlay, -size, -size, 0f, 0f, 0f);
      putBufferVertex(builder, packedLight, packedOverlay, -size, size, 0f, 0f, 1f);
      putBufferVertex(builder, packedLight, packedOverlay, size, size, 0f, 1f, 1f);

      putBufferVertex(builder, packedLight, packedOverlay, size, size, 0f, 1f, 1f);
      putBufferVertex(builder, packedLight, packedOverlay, size, -size, 0f, 1f, 0f);
      putBufferVertex(builder, packedLight, packedOverlay, -size, -size, 0f, 0f, 0f);

      try (MeshData meshdata = builder.buildOrThrow()) {
        retVal.setA(meshdata.drawState().indexCount());
        retVal.setB(RenderSystem.getDevice().createBuffer(
          () -> "Screen Quad",
          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
          meshdata.vertexBuffer()
        ));
      }
    }

    return retVal;
  }
}
