package com.goopey.voidsentflame.block;

import com.goopey.voidsentflame.block.blockentity.VoidsentFlameBlockEntity;
import com.goopey.voidsentflame.client.menu.VoidsentFlameCraftingMenu;
import com.goopey.voidsentflame.core.init.BlockEntityInit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VoidsentFlameBlock extends BaseEntityBlock {
  private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");
  public static final MapCodec<VoidsentFlameBlock> CODEC = RecordCodecBuilder.mapCodec(
    (blockInstance) ->
      blockInstance.group(
        Codec.intRange(0, 1000).fieldOf("fire_damage").forGetter((block) -> block.fireDamage),
        Codec.intRange(0, 1000).fieldOf("fire_level").forGetter((block) -> block.flameLevel),
        propertiesCodec())
      .apply(blockInstance, VoidsentFlameBlock::new)
  );
  public final int fireDamage;
  public final int flameLevel;

  public VoidsentFlameBlock(int fireDamage, int level, Properties properties) {
    super(properties.lightLevel(state -> 15));
    this.fireDamage = fireDamage;
    this.flameLevel = level;
  }

  public VoidsentFlameBlock(Properties properties) {
    this(5, 0, properties);
  }

  //#############################################
  //              BLOCK ENTITY
  //#############################################

  @Override
  public MapCodec<VoidsentFlameBlock> codec() { return CODEC; }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    // You can return different tickers here, depending on whatever factors you want. A common use case would be
    // to return different tickers on the client or server, only tick one side to begin with,
    // or only return a ticker for some blockstates (e.g. when using a "my machine is working" blockstate property).
//    return createTickerHelper(type, BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get(), VoidsentFlameBlockEntity::tick);
    if (level instanceof ServerLevel serverlevel) {
      RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> cachedcheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);
      return createTickerHelper(type, BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get(), (lLevel, lPos, lState, lType) -> VoidsentFlameBlockEntity.cookTick(serverlevel, lPos, lState, lType, cachedcheck));
    } else {
      return createTickerHelper(type, BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get(), VoidsentFlameBlockEntity::particleTick);
    }
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
    return new VoidsentFlameBlockEntity(blockPos, blockState);
  }

  //#############################################
  //              CRAFTING TABLE
  //#############################################

  /**
   * Copied over from crafting table code.
   */
  protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (!level.isClientSide()) {
      player.openMenu(blockState.getMenuProvider(level, pos));
      player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
      player.awardStat(Stats.INTERACT_WITH_FURNACE);
    }

    return InteractionResult.SUCCESS;
  }

  /**
   * Copied over from crafting table code.
   */
  protected MenuProvider getMenuProvider(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
    return new SimpleMenuProvider(
      (containerId, inventory, player) ->
        new VoidsentFlameCraftingMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)),
      CONTAINER_TITLE);
  }

  //#############################################
  //                  CAMPFIRE
  //#############################################

  protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interaction, BlockHitResult hitResult) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity instanceof VoidsentFlameBlockEntity voidsentFlameBlockEntity) {
      ItemStack itemstack = player.getItemInHand(interaction);

      if (level.recipeAccess().propertySet(RecipePropertySet.CAMPFIRE_INPUT).test(itemstack)) {
        if (level instanceof ServerLevel) {
          ServerLevel serverlevel = (ServerLevel) level;

          if (voidsentFlameBlockEntity.placeFood(serverlevel, player, itemstack)) {
            player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
            return InteractionResult.SUCCESS_SERVER;
          }
        }
        return InteractionResult.CONSUME;
      }
    }
    return InteractionResult.TRY_WITH_EMPTY_HAND;
  }

  /**
   * Manages setting entities on fire and hurting them when touching the block.
   * @param state the state of the block. Unneeded but might be useful. Conforms with campfire code.
   * @param level the level the block/entity is in.
   * @param pos the position of the block.
   * @param entity the entity that is touching the block
   * @param effectApplier object which manages applying effects from touching blocks onto players/entities.
   * @param intersects whether the entity has to intersect or simply touch the block
   */
  protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean intersects) {
    if (entity instanceof LivingEntity) {
      entity.hurt(level.damageSources().campfire(), this.fireDamage);
    }

    super.entityInside(state, level, pos, entity, effectApplier, intersects);
  }

  /**
   * Static method which randomly spreads and create the particles the campfire makes.
   * @param level the level the particles will be placed in.
   * @param pos the starting position the particles will appear at.
   * @param spawnExtraSmoke whether the campfire will spawn extra smoke particles. Unneeded for now, might be useful later.
   */
  public static void makeParticles(Level level, BlockPos pos, boolean spawnExtraSmoke) {
    RandomSource randomsource = level.getRandom();
    SimpleParticleType simpleparticletype = ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
    level.addAlwaysVisibleParticle(
      simpleparticletype,
      true,
      pos.getX() + 0.5 + randomsource.nextDouble() / 3.0 * (randomsource.nextBoolean() ? 1.0 : -1.0),
      pos.getY() + randomsource.nextDouble() + randomsource.nextDouble(),
      pos.getZ() + 0.5 + randomsource.nextDouble() / 3.0 * (randomsource.nextBoolean() ? 1.0 : -1.0),
      0.0, 0.07, 0.0);
    if (spawnExtraSmoke) {
      level.addParticle(
        ParticleTypes.SMOKE,
        pos.getX() + 0.5 + randomsource.nextDouble() / 4.0 * (randomsource.nextBoolean() ? 1.0 : -1.0),
        pos.getY() + 0.4,
        pos.getZ() + 0.5 + randomsource.nextDouble() / 4.0 * (randomsource.nextBoolean() ? 1.0 : -1.0),
        0.0, 0.005, 0.0
      );
    }
  }

  /**
   * Manages both creating the particles and noise the campfire makes.
   * @param state the state of the block. Unneeded, but might be needed and conforms with campfire code.
   * @param level the level the block is in. Needed to place noise.
   * @param pos the position of the block in the world. Needed to place particles and noise.
   * @param random the randomSource used by the campfire. Allows configuring particular random patterns.
   */
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    if (random.nextInt(10) == 0) {
      level.playLocalSound(
        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5F,
        SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
        0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false
      );
    }

    if (random.nextInt(5) == 0) {
      for(int i = 0; i < random.nextInt(1) + 1; ++i) {
        level.addParticle(
          ParticleTypes.LAVA,
          pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
          random.nextFloat() / 2.0F, 5.0E-5, random.nextFloat() / 2.0F
        );
      }
    }
  }
}
