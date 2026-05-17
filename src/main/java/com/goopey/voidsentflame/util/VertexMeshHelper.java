package com.goopey.voidsentflame.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.joml.Vector3f;

public class VertexMeshHelper {
  public static final int[][][] boxCoords = {
    {        // Face A
      {1, 1, -1}, {1, 1, 1}, {1, -1, 1}, {1, -1, -1}
    }, {     // Face B
      {1, 1, 1}, {-1, 1, 1}, {-1, -1, 1}, {1, -1, 1}
    }, {     // Face C
      {-1, 1, 1}, {-1, 1, -1}, {-1, -1, -1}, {-1, -1, 1}
    }, {     // Face D
      {-1, 1, -1}, {1, 1, -1}, {1, -1, -1}, {-1, -1, -1}
    }, {     // Face E - Bottom Face
      {-1, -1, 1}, {1, -1, 1}, {1, -1, -1}, {-1, -1, -1}
    }, {     // Face F - Top Face
      {-1, 1, 1}, {1, 1, 1}, {1, 1, -1}, {-1, 1, -1}
    }
  };

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
   * Helper method used to create a mesh in the shape of a box with a customizable top and bottom height
   * @param builder the buffer builder needed to add vertices to a mesh
   * @param size the size of the square
   * @param topHeight how high the top of the square should be
   * @param bottomHeight the low the square should be
   * @param packedLight the rgb value of the light to be baked in that is hitting the object
   * @param packedOverlay the rgb value of the light to be baked in that is hitting the object
   */
  public static void putCubeMeshVertex(BufferBuilder builder, int size, int topHeight, int bottomHeight, int packedLight, int packedOverlay) {
    cubeMeshLoop(builder, boxCoords, size, topHeight, bottomHeight, packedLight, packedOverlay);
  }

  /**
   * Helper method used to create the bottom box of the distortion effect
   * @param builder the buffer builder needed to add vertices to a mesh
   * @param size the size of the square
   * @param topHeight how high the square should cover the sky
   * @param bottomHeight the low the square should cover the sky
   * @param packedLight the rgb value of the light to be baked in that is hitting the object
   * @param packedOverlay the rgb value of the light to be baked in that is hitting the object
   */
  public static void putOpenCubeMeshVertex(BufferBuilder builder, int size, int topHeight, int bottomHeight, int packedLight, int packedOverlay) {
    int[][][] box = {
      boxCoords[0], boxCoords[1], boxCoords[2], boxCoords[3], boxCoords[4]
    };
    cubeMeshLoop(builder, box, size, topHeight, bottomHeight, packedLight, packedOverlay);
  }

  /**
   * Helper method used to create a simple quad/mesh
   * @param builder the buffer builder needed to add vertices to a mesh
   * @param size the size of the square
   * @param packedLight the rgb value of the light to be baked in that is hitting the object
   * @param packedOverlay the rgb value of the light to be baked in that is hitting the object
   */
  public static void putQuadMeshVertex(BufferBuilder builder, int size, int packedLight, int packedOverlay) {
    putQuadMeshVertex(builder, size, 0, packedLight, packedOverlay);
  }

  /**
   * Helper method used to create a simple quad/mesh
   * @param builder the buffer builder needed to add vertices to a mesh
   * @param size the size of the square
   * @param height how high the square should be in the world
   * @param packedLight the rgb value of the light to be baked in that is hitting the object
   * @param packedOverlay the rgb value of the light to be baked in that is hitting the object
   */
  public static void putQuadMeshVertex(BufferBuilder builder, int size, int height, int packedLight, int packedOverlay) {
    int[][][] box = {
      boxCoords[5]
    };
    cubeMeshLoop(builder, box, size, height, -height, packedLight, packedOverlay);
  }

  //###############################################
  //                GENERAL MESH
  //###############################################

  /**
   * Helper method used to create the boxes
   * @param builder the buffer builder needed to add vertices to a mesh
   * @param size the size of the square
   * @param topHeight how high the square should go
   * @param bottomHeight the low the square should go
   * @param packedLight the rgb value of the light to be baked in that is hitting the object
   * @param packedOverlay the rgb value of the light to be baked in that is hitting the object
   */
  private static void cubeMeshLoop(BufferBuilder builder, int[][][] box, int size, int topHeight, int bottomHeight, int packedLight, int packedOverlay) {
    float u0 = 0, v0 = 0;
    float u1 = 1f, v1 = 1f;

    for (int[][] face : box) {
      Vector3f pos1 = new Vector3f(face[0][0] * size, face[0][1] < 0 ? bottomHeight : topHeight, face[0][2] * size);
      Vector3f pos2 = new Vector3f(face[1][0] * size, face[1][1] < 0 ? bottomHeight : topHeight, face[1][2] * size);
      Vector3f pos3 = new Vector3f(face[2][0] * size, face[2][1] < 0 ? bottomHeight : topHeight, face[2][2] * size);
      Vector3f pos4 = new Vector3f(face[3][0] * size, face[3][1] < 0 ? bottomHeight : topHeight, face[3][2] * size);

      putBufferVertex(builder, packedLight, packedOverlay, pos1.x, pos1.y, pos1.z, u0, v0);
      putBufferVertex(builder, packedLight, packedOverlay, pos2.x, pos2.y, pos2.z, u0, v1);
      putBufferVertex(builder, packedLight, packedOverlay, pos3.x, pos3.y, pos3.z, u1, v1);

      putBufferVertex(builder, packedLight, packedOverlay, pos3.x, pos3.y, pos3.z, u1, v1);
      putBufferVertex(builder, packedLight, packedOverlay, pos4.x, pos4.y, pos4.z, u1, v0);
      putBufferVertex(builder, packedLight, packedOverlay, pos1.x, pos1.y, pos1.z, u0, v0);
    }
  }

  /**
   * Used to add a vertex at specific coordinates with an upwards normal
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
