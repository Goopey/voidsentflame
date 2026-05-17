package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SoundInit {
  public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, VoidsentFlameMod.MODID);

  public static final DeferredHolder<SoundEvent, SoundEvent> VOID_SEA_AMBIENT = registerSoundEvent("void_sea_ambient_sound");

  public static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, name);
    return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
  }
}
