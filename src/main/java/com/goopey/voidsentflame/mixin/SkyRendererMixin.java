package com.goopey.voidsentflame.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
//  /**
//   * @author Goopey
//   *
//   * @reason make dimension dark below world height. The dark sky disc would cause issues in the Rubicon.
//   *
//   * @param level needed to get the bottom height of the level
//   * @return a double which tells the game where to draw the dark sky disc.
//   */
//  @Overwrite
//  public boolean shouldRenderDarkDisc(float partialTick, ClientLevel level) {
//    return Minecraft.getInstance().player.getEyePosition(partialTick).y - level.getMinY() < 0.0;
//  }
}