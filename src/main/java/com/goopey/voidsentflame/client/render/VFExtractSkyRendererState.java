package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.world.dimension.RubiconDimension;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;

public class VFExtractSkyRendererState {
  /**
   * This method takes care of managing all sorts of vanilla state values that impact how the Skybox looks while in the
   * dimension. EX: gets rid of the black ring when getting low in the world.
   * @param event the ExtractLevelRenderStateEvent that is executed after MC modifies its render states
   */
  public static void VFExtractSkyRenderStateEvent(ExtractLevelRenderStateEvent event) {
    Level level = event.getLevel();
    SkyRenderState state = event.getRenderState().skyRenderState;

    if (level.dimension() == RubiconDimension.RUBICON) {
      state.shouldRenderDarkDisc = false;
    }
  }
}
