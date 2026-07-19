package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.util.BufferBuilderHelper;
import com.goopey.voidsentflame.util.VFRenderConsts;
import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

public class FullscreenQuadRenderer implements ResourceManagerReloadListener, AutoCloseable {
  public static final FullscreenQuadRenderer INSTANCE = new FullscreenQuadRenderer();

  private int screenIndex;
  private GpuBuffer screenBuffer;

  private FullscreenQuadRenderer() {
    Tuple<Integer, GpuBuffer> screen = BufferBuilderHelper.buildScreen(VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, 1f);
    this.screenIndex = screen.getA();
    this.screenBuffer = screen.getB();
  }

  public GpuBuffer getQuad() {
    return this.screenBuffer;
  }

  public int getIndex() {
    return this.screenIndex;
  }

  /**
   * This method takes care of automatically closing all skyRenderer classes this main class manages.
   */
  @Override
  public void close() {
    this.screenBuffer.close();
  }

  /**
   * Used to initialize the resources needed by a skyRenderer when the game reloads assets
   * @param resourceManager Minecraft's resource manager. Provides controlled access to the game's files while running.
   */
  @Override
  public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
    Tuple<Integer, GpuBuffer> screen = BufferBuilderHelper.buildScreen(VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY, 1f);
    this.screenIndex = screen.getA();
    this.screenBuffer = screen.getB();
  }
}
