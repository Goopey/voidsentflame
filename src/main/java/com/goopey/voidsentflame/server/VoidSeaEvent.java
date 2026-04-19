package com.goopey.voidsentflame.server;

import com.goopey.voidsentflame.client.render.VoidSeaRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class VoidSeaEvent {
  /**
   * This functions manages killing entities who fall underneath the void sea effect.
   * @param event the entityTickEvent
   */
  public static void voidSeaTickKillEntity(EntityTickEvent.Post event) {
    Level level = event.getEntity().level();
    Entity entity = event.getEntity();

    if (level.dimension() == VoidSeaRenderer.RUBICON && !level.isClientSide()) {
      double height = VoidSeaRenderer.HEIGHT + entity.getBbHeight() - 0.2;

      if (entity instanceof Player) {
        double time = ((level.getGameTime() % 24000.0) / 24000.0) * VoidSeaRenderer.SECONDS_PER_DAY;
        double waves = Math.sin(VoidSeaRenderer.WAVE_FREQUENCY * (entity.getX() + entity.getZ() + VoidSeaRenderer.TIME_FREQUENCY * time)) *
          VoidSeaRenderer.WAVE_AMPLITUDE - VoidSeaRenderer.WAVE_AMPLITUDE;
        height += waves;
      }

      if (entity.getY() < height) {
        entity.kill((ServerLevel) level);
      }
    }
  }
}
