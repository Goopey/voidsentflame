package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.core.VFReloadableTexture;
import com.goopey.voidsentflame.core.VFTextureManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

public class AbstractTextureInit {
  public static void registerAbstractTextures(FMLClientSetupEvent event) {
    ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
    VFTextureManager textureManager = VFTextureManager.getInstance();

    registerAbstractTexture(resourceManager, textureManager, "void_fluid", "block");
  
    Minecraft.getInstance().reloadResourcePacks();
  }

  /**
   * Helper method which registers and loads AbstractTextures.
   * 
   * @param resourceManager
   * @param textureManager
   * @param name
   * @param location
   */
  private static void registerAbstractTexture(ResourceManager resourceManager, TextureManager textureManager, String name, String location) {
    String totalLocation = location + "/" + name;
    ResourceLocation res = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, totalLocation);
    VFReloadableTexture reloadableTexture = new VFReloadableTexture(res);
    try {
      // reloadableTexture.
      reloadableTexture.loadContents(resourceManager);
      
      textureManager.registerForNextReload(res);
    } catch (IOException e) {
      VoidsentFlameMod.LOGGER.error("Failed to reload texture : " + totalLocation);
      VoidsentFlameMod.LOGGER.error(e.getMessage());
    }
  }
}
