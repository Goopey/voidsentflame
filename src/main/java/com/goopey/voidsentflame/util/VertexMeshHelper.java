package com.goopey.voidsentflame.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.joml.Vector3f;

public class VertexMeshHelper {
  //###############################################
  //                BUFFER CUBES
  //###############################################
  /**
   * Helper method used to create a mesh in the shape of a box.
   * @param builder the buffer builder needed to add vertices to the mesh
   * @param size the size of the cube
   * @param packedLight the rgb value of the light to be baked in that is hitting the object
   * @param packedOverlay the rgb value of the light to be baked in that is hitting the object
   */
  public static void putCubeMeshVertex(BufferBuilder builder, int size, int packedLight, int packedOverlay) {
    putCubeMeshVertex(builder, size, size, -size, packedLight, packedOverlay);
  }

  /**
   * Helper method used to create a mesh in the shape of a box
   * @param builder the buffer builder needed to add vertices to a mesh
   * @param size the size of the square
   * @param topHeight how high the top of the square should be
   * @param bottomHeight the low the square should be
   * @param packedLight the rgb value of the light to be baked in that is hitting the object
   * @param packedOverlay the rgb value of the light to be baked in that is hitting the object
   */
  public static void putCubeMeshVertex(BufferBuilder builder, int size, int topHeight, int bottomHeight, int packedLight, int packedOverlay) {
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

      putBufferVertex(builder, packedLight, packedOverlay, pos1.x, pos1.y, pos1.z, u0, v0);
      putBufferVertex(builder, packedLight, packedOverlay, pos2.x, pos2.y, pos2.z, u0, v1);
      putBufferVertex(builder, packedLight, packedOverlay, pos3.x, pos3.y, pos3.z, u1, v1);

      putBufferVertex(builder, packedLight, packedOverlay, pos3.x, pos3.y, pos3.z, u1, v1);
      putBufferVertex(builder, packedLight, packedOverlay, pos4.x, pos4.y, pos4.z, u1, v0);
      putBufferVertex(builder, packedLight, packedOverlay, pos1.x, pos1.y, pos1.z, u0, v0);
    }
  }


  //###############################################
  //                GENERAL MESH
  //###############################################

  /**
   * Used to add a vertex at specific coordinates with an upwards normal
   *
   * @param builder the builder needed to add the vertices to a mesh
   * @param packedLight the rgb value of the light to be baked in that is hitting the object
   * @param packedOverlay the rgb value of the light to be baked in that is hitting the object
   * @param x the 1st position of the vertex
   * @param y the 2nd position of the vertex
   * @param z the 3rd position of the vertex
   * @param u the 1st UV position
   * @param v the 2nd UV position
   */
  public static void putBufferVertex(BufferBuilder builder, int packedLight, int packedOverlay, float x, float y, float z, float u, float v) {
    builder.addVertex(x, y, z)
      .setColor(1f, 1f, 1f, 1f)
      .setUv(u, v)
      .setLight(packedLight)
      .setOverlay(packedOverlay)
      .setNormal(0, 1f, 0);
  }
}
