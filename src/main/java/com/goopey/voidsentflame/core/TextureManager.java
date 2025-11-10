package com.goopey.voidsentflame.core;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;

import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;

public class TextureManager {
  private final GpuTexture texture;

  public TextureManager(ValidationGpuDevice device) {
    this.texture = device.createTexture(
      "", 
      // usage
      GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, 
      TextureFormat.RGBA8, 
      // width
      16, 
      // height
      16, 
      // depth layers
      1,
      // mipmap levels / lods
      1
    );

    this.texture.setAddressMode(AddressMode.CLAMP_TO_EDGE, AddressMode.REPEAT);
    this.texture.setTextureFilter(FilterMode.LINEAR, FilterMode.NEAREST, false);
  }

  public GpuTexture getTexture() {
    return this.texture;
  }

  public void writeToTexture(CommandEncoder encoder, NativeImage image) {
    encoder.writeToTexture(
      this.getTexture(), image, 0, 0, 0, 16, 16, 0, 0, 0);
  }

  public void closeTexture() {
    this.getTexture().close();
  }

  public boolean isTextureClosed() {
    return this.getTexture().isClosed();
  }
}
