package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.fluid.type.VoidFluidType;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class FluidTypesInit {
  public static final DeferredRegister<FluidType> REGISTRY = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, VoidsentFlameMod.MODID);
	public static final DeferredHolder<FluidType, FluidType> VOID_FLUID_TYPE = REGISTRY.register("void_fluid", () -> new VoidFluidType());
}