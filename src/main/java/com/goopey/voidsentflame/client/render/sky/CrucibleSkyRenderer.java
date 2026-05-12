package com.goopey.voidsentflame.client.render.sky;

import com.goopey.voidsentflame.util.VFRenderConsts;
import com.goopey.voidsentflame.world.dimension.RubiconDimension;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

public class CrucibleSkyRenderer implements ResourceManagerReloadListener, AutoCloseable {
  private GpuBuffer cloudBoxBuffer;
  private int cloudBoxIndex;

  public CrucibleSkyRenderer() {
  }

  @Override
  public void close() {
    cloudBoxBuffer.close();
  }

  /**
   * Manages creating the buffers and getting the sprites that are needed to make the skybox
   * in the Crucible Biome.
   *
   * @param resourceManager Minecraft's resourceManager. Needed to get resources from the
   *                        game folders into memory
   */
  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    this.createCloudBoxBuffer();
  }

  //###############################################
  //                CREATE BUFFERS
  //###############################################

  /**
   * Helper function used to create the box which portrays a cloud texture.
   */
  private void createCloudBoxBuffer() {
    VertexFormat format = DefaultVertexFormat.BLOCK;
    VertexFormat.Mode mode = VertexFormat.Mode.TRIANGLES;
    GpuBuffer gpuBuffer;

    // for 6 nodes per face and 6 faces
    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.BLOCK.getVertexSize() * 6 * 6)) {
      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, mode, format);

//      putMesh(bufferBuilder, RubiconDimension.VoidSeaConstants.OFFSET, QUAD_SIZE, PADDING);

      // Handle storing the meshdata into the buffer and then closing the MeshData and byteBufferBuilder
      try (MeshData meshData = bufferBuilder.buildOrThrow()) {
        this.cloudBoxIndex = meshData.drawState().indexCount();
        ByteBuffer uploadBuffer = meshData.vertexBuffer();

        gpuBuffer = RenderSystem.getDevice().createBuffer(
          () -> "Crucible Sky Cloud Box",
          GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
          uploadBuffer
        );
      }
    }

    this.cloudBoxBuffer = gpuBuffer;
  }

  //###############################################
  //                BUFFER SHAPES
  //###############################################
  /**
   * Helper method used to create a mesh in the shape of a box
   * @param builder the buffer builder needed to add vertices to a mesh
   * @param size the size of the square
   * @param topHeight how high the top of the square should be
   * @param bottomHeight the low the square should be
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
      {     // Face E - Bottom Face
        {-size, bottomHeight, size}, {size, bottomHeight, size}, {size, bottomHeight, -size}, {-size, bottomHeight, -size}
      },
      {     // Face F - Top Face
        {-size, topHeight, size}, {size, topHeight, size}, {size, topHeight, -size}, {-size, topHeight, -size}
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