package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.util.PostChainSerialization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;
import java.util.*;
import java.util.stream.Stream;

public class BleedVisualEffect extends SimplePreparableReloadListener {
    public static final String FILE_NAME = "void_sea_bleed";
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/post/" + FILE_NAME + ".json");

    public BleedVisualEffect() {}

    @Override
    protected Object prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
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
                VoidsentFlameMod.LOGGER.info(s.replace(" ", ""));
            }

            PostChain chain = PostChainSerialization.serialize(builder.toString(), FILE_NAME, Minecraft.getInstance().getTextureManager(), externalTargets);

            VoidsentFlameMod.LOGGER.info(builder.toString());
            VoidsentFlameMod.LOGGER.info(chain.toString());
        } catch (Exception e) {
            VoidsentFlameMod.LOGGER.error(e.getMessage());
        }

        return null;
    }

    @Override
    protected void apply(Object object, ResourceManager resourceManager, ProfilerFiller profiler) {

    }
}
