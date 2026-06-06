package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;

import com.goopey.voidsentflame.block.blockentity.VoidsentFlameBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityInit {
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, VoidsentFlameMod.MODID);

  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidSeaLayerBlockEntity>> VOID_SEA_LAYER_BLOCK_ENTITY =
    BLOCK_ENTITY.register("void_sea_layer_block_entity", () -> new BlockEntityType<>(VoidSeaLayerBlockEntity::new, false, BlockInit.VOID_SEA_LAYER_BLOCK.get()));

//  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidsentFlameBlockEntity>> VOIDSENT_FLAME_BLOCK_ENTITY =
//    BLOCK_ENTITY.register("voidsent_flame_block_entity", () -> new BlockEntityType<>(VoidsentFlameBlockEntity::new, false, BlockInit.VOIDSENT_FLAME_BLOCK.get()));
}
