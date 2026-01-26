package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.util.PostChainSerialization;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;
import java.util.*;
import java.util.stream.Stream;

public class BleedVisualEffect extends SimplePreparableReloadListener<Optional<PostChain>> {
    public PostChain bleedEffectPostChain;
    public static final String FILE_NAME = "void_sea_bleed";
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/post/" + FILE_NAME + ".json");
    public static BleedVisualEffect INSTANCE = new BleedVisualEffect();

    private BleedVisualEffect() {}

    public boolean shouldPrepare() {
        return this.bleedEffectPostChain == null;
    }

    @Override
    protected Optional<PostChain> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Optional<PostChain> retVal = Optional.empty();
        if (!RenderSystem.isOnRenderThread()) {
            return retVal;
        }

        try {
            Optional<Resource> optRes = resourceManager.getResource(LOCATION);

            if (optRes.isEmpty()) { throw new IllegalStateException("FAILED TO LOAD BLEED EFFECT."); }

            Resource res = optRes.get();
            BufferedReader reader = res.openAsReader();
            Stream<String> content = reader.lines();
            List<String> stringList = content.toList();

            Set<ResourceLocation> externalTargets = new HashSet<ResourceLocation>();
            externalTargets.add(ResourceLocation.withDefaultNamespace("main"));

            StringBuilder builder = new StringBuilder();

            for (String s : stringList) {
                builder.append(s.replace(" ", ""));
            }

            PostChain chain = PostChainSerialization.serialize(builder.toString(), FILE_NAME, Minecraft.getInstance().getTextureManager(), externalTargets);

            retVal = Optional.of(chain);

            // TODO : remove test LOGGING methods
            VoidsentFlameMod.LOGGER.info(builder.toString());
            VoidsentFlameMod.LOGGER.info(chain.toString());
        } catch (Exception e) {
            VoidsentFlameMod.LOGGER.error("Bleed Visual Effect Failed to load! {}", e.getMessage());
        }

        return retVal;
    }

    @Override
    protected void apply(Optional<PostChain> chain, ResourceManager resourceManager, ProfilerFiller profiler) {
        bleedEffectPostChain = (PostChain) chain.orElse(null);
    }
}
