package com.goopey.voidsentflame.core;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

public class VFGpuTextureView extends GpuTextureView {
  public VFGpuTextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
    super(texture, baseMipLevel, mipLevels);
  }

  public VFGpuTextureView(GpuTexture texture) {
    this(texture, 1, 4);
  }

  @Override
  public void close() {
    this.texture().close();
  }

  @Override
  public boolean isClosed() {
    return this.texture().isClosed();
  }
}
