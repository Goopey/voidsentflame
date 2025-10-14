package com.goopey.voidsentflame.client;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.client.geometry.VoidSeaUnbakedGeometry;
import com.goopey.voidsentflame.client.models.VoidSeaUnbakedModel;

import javax.annotation.Nonnull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class VoidSeaUnbakedModelLoader implements UnbakedModelLoader<VoidSeaUnbakedModel>, ResourceManagerReloadListener {
  // It is highly recommended to use a singleton pattern for unbaked model loaders, as all models can be loaded through one loader.
    public static final VoidSeaUnbakedModelLoader INSTANCE = new VoidSeaUnbakedModelLoader();

    // The id we will use to register this loader. Also used in the loader datagen class.
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "void_sea_custom_model_loader");

    // In accordance with the singleton pattern, make the constructor private.        
    private VoidSeaUnbakedModelLoader() {}

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        // Handle any cache clearing logic
    }

    @Override
    public VoidSeaUnbakedModel read(@Nonnull JsonObject obj, @Nonnull JsonDeserializationContext context) throws JsonParseException {
        // Use the given JsonObject and, if needed, the JsonDeserializationContext to get properties from the model JSON.
        // The MyUnbakedModel constructor may have constructor parameters (see below).

        // Read the data used to create the quads
        VoidSeaUnbakedGeometry geometry = new VoidSeaUnbakedGeometry();

        // For the basic parameters provided by vanilla and NeoForge, you can use the StandardModelParameters
        StandardModelParameters params = StandardModelParameters.parse(obj, context);

        return new VoidSeaUnbakedModel(params, geometry);
    }
}
