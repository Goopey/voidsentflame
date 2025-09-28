package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidsentBonfireBlockEntity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityInit {
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, VoidsentFlameMod.MODID);

  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidsentBonfireBlockEntity>> VOID_SEA_LAYER_BLOCK_ENTITY =
    BLOCK_ENTITY.register("voidsent_bonfire_block_entity", () -> new BlockEntityType<>(VoidsentBonfireBlockEntity::new, false, BlockInit.VOIDSENT_BONFIRE.get()));
}
