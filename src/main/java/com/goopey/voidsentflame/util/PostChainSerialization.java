package com.goopey.voidsentflame.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.goopey.voidsentflame.VoidsentFlameMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class PostChainSerialization {
    public static PostChain serialize(String strData, String name, TextureManager textureManager, Set<ResourceLocation> externalTargets) {
        JsonElement element = JsonParser.parseString(strData);
        DataResult<PostChainConfig> config = PostChainConfig.CODEC.parse(JsonOps.INSTANCE, element);

        CachedOrthoProjectionMatrixBuffer projectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer(
            name + "||orthoProjectionMatrixBuffer",
            0,
            0,
            false
        );

        PostChain retVal = null;
        try {
            retVal = PostChain.load(
                    config.getOrThrow(),
                    textureManager,
                    externalTargets,
                    ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/postchain/" + name),
                    projectionMatrixBuffer
            );
        } catch (IllegalStateException e) {
            VoidsentFlameMod.LOGGER.error("Failed to load CONFIG in {}! Try contacting the author to fix this issue!", name);
        } catch (ShaderManager.CompilationException e) {
            VoidsentFlameMod.LOGGER.error("Failed to COMPILE SHADER {}! Try reloading and contacting the author to fix this issue!", name);
        }

        return retVal;
    }
}
