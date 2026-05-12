package com.goopey.voidsentflame.client.render.sky;

import com.goopey.voidsentflame.util.VFRenderConsts;
import com.goopey.voidsentflame.util.VertexMeshHelper;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

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
  public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
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

      VertexMeshHelper.putCubeMeshVertex(bufferBuilder, 10, VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY);

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
}