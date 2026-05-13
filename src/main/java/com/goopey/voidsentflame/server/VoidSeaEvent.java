package com.goopey.voidsentflame.server;

import com.goopey.voidsentflame.world.dimension.RubiconDimension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.goopey.voidsentflame.world.dimension.RubiconDimension.VoidSeaConstants;

import java.util.Random;

public class VoidSeaEvent {
  /**
   * This functions manages all the events that can happen when a living entity touches or gets close to the void sea.
   * @param event the entityTickEvent
   */
  public static void voidSeaTick(EntityTickEvent.Post event) {
    Level level = event.getEntity().level();
    Entity entity = event.getEntity();

    lowerGrav(entity);
    killEntity(entity, level);

    if (entity instanceof Player) {
      playSfx((Player) entity);
    }
  }

  /**
   * Function which manages killing entities if they touch the void sea in the Rubicon.
   * @param entity the entity that will die if it is too low in the world
   * @param level the level needed to check if the player is in the right dimension
   */
  private static void killEntity(Entity entity, Level level) {
    if (level.dimension() == RubiconDimension.RUBICON && !level.isClientSide()) {
      double height = VoidSeaConstants.HEIGHT + entity.getBbHeight() - 0.2;

      if (entity instanceof Player) {
        double time = ((level.getGameTime() % 24000.0) / 24000.0) * VoidSeaConstants.SECONDS_PER_DAY;
        double waves = Math.sin(VoidSeaConstants.WAVE_FREQUENCY * (entity.getX() + entity.getZ() + VoidSeaConstants.TIME_FREQUENCY * time)) *
          VoidSeaConstants.WAVE_AMPLITUDE - VoidSeaConstants.WAVE_AMPLITUDE;
        height += waves;
      }

      if (entity.getY() < height) {
        if (entity instanceof Player) {
          ((Player) entity).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 999999999, 0, false, false));
        }
        entity.kill((ServerLevel) level);
      }
    }
  }

  /**
   * Function which manages slowing entities as they get closer to the void sea in the Rubicon.
   * @param entity the entity that will be slowed as they get closer to the bottom of the world
   */
  private static void lowerGrav(Entity entity) {
    double a = Math.sqrt(entity.getY() + (VoidSeaConstants.HEIGHT * -1.5));
    double slowStr = Math.clamp(a / 8, 0.5, 1.0);
    double slowStrH = Math.clamp(slowStr * 2, 0.5, 1.0);
    double slowStrV = entity.getDeltaMovement().y() > 0 ? 1.0 : slowStr;

    if (slowStrV < 0.75) { entity.resetFallDistance(); }

    entity.setDeltaMovement(
      entity.getDeltaMovement().multiply(slowStrH, slowStrV, slowStrH)
    );
  }

  /**
   * Plays ambience sound effects when the player is close enough.
   * @param player the player which will have sound effects play.
   */
  private static void playSfx(Player player) {
    double a = Math.sqrt(player.getY() + (VoidSeaConstants.HEIGHT * -1.5));
    double slowStr = Math.clamp(a / 8, 0.25, 1.0);

    player.playSound(null, (float) slowStr, new Random().nextFloat(0.9f, 1.05f));
  }
}
