package com.goopey.voidsentflame.block.blockentity;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.VoidsentFlameBlock;
import com.goopey.voidsentflame.core.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

public class VoidsentFlameBlockEntity extends BlockEntity {
  private int flameLevel = 0;
  private static final int NUM_SLOTS = 4;
  private final NonNullList<ItemStack> items;
  private final int[] cookingProgress;
  private final int[] cookingTime;

  public VoidsentFlameBlockEntity(BlockPos pos, BlockState blockState) {
    super(BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get(), pos, blockState);
    this.flameLevel = ((VoidsentFlameBlock) blockState.getBlock()).flameLevel;
    // campfire cooking stuff
    this.items = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
    this.cookingProgress = new int[NUM_SLOTS];
    this.cookingTime = new int[NUM_SLOTS];
  }

  public NonNullList<ItemStack> getItems() {
    return this.items;
  }
  public void clearContent() {
    this.items.clear();
  }

  //#################################################
  //                 BLOCK ENTITY
  //#################################################

  @Override
  public BlockEntityType<?> getType() {
    return BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get();
  }

  /**
   * Loads saved data when the block loads in or smth akin
   * @param input the ValueInput needed to save data
   */
  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    this.flameLevel = input.getIntOr("flameLevel", 0);
    // load campfire inventory and cooking data
    this.items.clear();
    ContainerHelper.loadAllItems(input, this.items);
    input.getIntArray("CookingTimes").ifPresentOrElse(
      (cookingTimesArray) -> System.arraycopy(
        cookingTimesArray, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, cookingTimesArray.length)),
      () -> Arrays.fill(
        this.cookingProgress, 0)
    );
    input.getIntArray("CookingTotalTimes").ifPresentOrElse(
      (cookingTotalTimesArray) -> System.arraycopy(
        cookingTotalTimesArray, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, cookingTotalTimesArray.length)),
      () -> Arrays.fill(
        this.cookingTime, 0));

  }

  /**
   * Saves data when the block unloads or smth akin
   * @param output the ValueOutput needed to save data
   */
  @Override
  public void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    output.putInt("flameLevel", this.flameLevel);
    // save campfire cooking data
    ContainerHelper.saveAllItems(output, this.items, true);
    output.putIntArray("CookingTimes", this.cookingProgress);
    output.putIntArray("CookingTotalTimes", this.cookingTime);
  }

  /**
   * Marks the blockEntity as "dirty" or be considered ready to update at a moment's notice.
   */
  private void markUpdated() {
    this.setChanged();
    this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
  }

  /**
   * Things to do before the block is broken
   * @param pos the position of the block in the world
   * @param state the state of the block
   */
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    if (this.level != null) {
      Containers.dropContents(this.level, pos, this.getItems());
    }

  }

  /**
   * Loads data into the internal "items" inventory
   * @param dataComponentGetter the object used to get data stored in the chunk
   */
  protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
    super.applyImplicitComponents(dataComponentGetter);
    (dataComponentGetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).copyInto(this.getItems());
  }

  /**
   * Saves data from the block into the chunk
   * @param dataComponentBuilder the object used to convert the list data into data storable in the chunk
   */
  protected void collectImplicitComponents(DataComponentMap.Builder dataComponentBuilder) {
    super.collectImplicitComponents(dataComponentBuilder);
    dataComponentBuilder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
  }

  /**
   * Deletes the storage data saved in tags.
   * @param output the ValueOutput needed to delete tag data
   */
  public void removeComponentsFromTag(ValueOutput output) {
    output.discard("Items");
  }

  /**
   * @return a dataPacket used to sync data between the client and server
   */
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  /**
   * @param provider used to create compoundTags
   * @return a CompoundTag with the components to be changed
   */
  public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
    try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(this.problemPath(), VoidsentFlameMod.LOGGER)) {
      TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter$scopedcollector, provider);
      ContainerHelper.saveAllItems(tagvalueoutput, this.items, true);
      return tagvalueoutput.buildResult();
    }
  }

  //#############################################
  //                CAMPFIRE
  //#############################################

  /**
   *
   * @param level
   * @param entity
   * @param stack
   * @return
   */
  public boolean placeFood(ServerLevel level, @Nullable LivingEntity entity, ItemStack stack) {
    for(int i = 0; i < this.items.size(); ++i) {
      ItemStack itemstack = this.items.get(i);
      if (itemstack.isEmpty()) {
        Optional<RecipeHolder<CampfireCookingRecipe>> optional = level.recipeAccess().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), level);
        if (optional.isEmpty()) {
          return false;
        }

        this.cookingTime[i] = ((CampfireCookingRecipe)((RecipeHolder)optional.get()).value()).cookingTime();
        this.cookingProgress[i] = 0;
        this.items.set(i, stack.consumeAndReturn(1, entity));
        level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
        this.markUpdated();
        return true;
      }
    }

    return false;
  }

  /**
   *
   * @param level
   * @param pos
   * @param state
   * @param vfCampfire
   * @param check
   */
  public static void cookTick(ServerLevel level, BlockPos pos, BlockState state, VoidsentFlameBlockEntity vfCampfire, RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> check) {
    boolean flag = false;

    for(int i = 0; i < vfCampfire.items.size(); ++i) {
      ItemStack itemstack = vfCampfire.items.get(i);
      if (!itemstack.isEmpty()) {
        flag = true;
        // cooks faster than a regular fire
        vfCampfire.cookingProgress[i] += 2;
        if (vfCampfire.cookingProgress[i] >= vfCampfire.cookingTime[i]) {
          SingleRecipeInput singlerecipeinput = new SingleRecipeInput(itemstack);
          ItemStack itemstack1 = check.getRecipeFor(singlerecipeinput, level).map((campfireRecipeInstance) -> (campfireRecipeInstance.value()).assemble(singlerecipeinput, level.registryAccess())).orElse(itemstack);
          if (itemstack1.isItemEnabled(level.enabledFeatures())) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemstack1);
            vfCampfire.items.set(i, ItemStack.EMPTY);
            level.sendBlockUpdated(pos, state, state, 3);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
          }
        }
      }
    }

    if (flag) {
      setChanged(level, pos, state);
    }
  }

  /**
   *
   * @param level
   * @param pos
   * @param state
   * @param blockEntity
   */
  public static void particleTick(Level level, BlockPos pos, BlockState state, VoidsentFlameBlockEntity blockEntity) {
    RandomSource randomsource = level.random;
    if (randomsource.nextFloat() < 0.11F) {
      for(int i = 0; i < randomsource.nextInt(2) + 2; ++i) {
        VoidsentFlameBlock.makeParticles(level, pos, false);
      }
    }

    for(int j = 0; j < blockEntity.items.size(); ++j) {
      if (!(blockEntity.items.get(j)).isEmpty() && randomsource.nextFloat() < 0.2F) {
        double d0 = pos.getX() + 0.5;
        double d1 = pos.getY() + 0.5;
        double d2 = pos.getZ() + 0.5F;

        for(int k = 0; k < 4; ++k) {
          level.addParticle(ParticleTypes.GLOW, d0, d1, d2, 0.0, 5.0E-4, 0.0);
        }
      }
    }
  }
}
