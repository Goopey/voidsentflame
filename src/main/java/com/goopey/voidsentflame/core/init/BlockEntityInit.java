package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;

import com.goopey.voidsentflame.block.blockentity.VoidsentFlameBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityInit {
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, VoidsentFlameMod.MODID);

  public static final Supplier<BlockEntityType<VoidsentFlameBlockEntity>> VOIDSENT_FLAME_BLOCK_ENTITY =
    BLOCK_ENTITY.register("voidsent_flame_block_entity", () -> new BlockEntityType<>(VoidsentFlameBlockEntity::new, false, BlockInit.VOIDSENT_FLAME_BLOCK.get()));
}
