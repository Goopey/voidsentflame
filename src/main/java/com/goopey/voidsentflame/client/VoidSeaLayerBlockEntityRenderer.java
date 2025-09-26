package com.goopey.voidsentflame.client;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.goopey.voidsentflame.core.init.ItemInit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class VoidSeaLayerBlockEntityRenderer implements BlockEntityRenderer<VoidSeaLayerBlockEntity> {
  public static final ResourceLocation LIGHT = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "block/light_circle_block");

  public VoidSeaLayerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
  }
  
  @Override
  public void render(VoidSeaLayerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPos) {
    ItemStack stack = new ItemStack(ItemInit.RUNIC_FRUIT_ITEM.get());
    if (!stack.isEmpty()) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        long millis = System.currentTimeMillis();

        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(1f, 1f, 1f);
        poseStack.translate(1f, 2.8f, 1f);
        float angle = ((millis / 45) % 360);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, packedOverlay, poseStack, bufferSource, Minecraft.getInstance().level, 0);
        poseStack.popPose();

        poseStack.translate(0, 0.5f, 0);
        renderBillboardQuadBright(poseStack, bufferSource.getBuffer(RenderType.translucentMovingBlock()), 0.5f, LIGHT);
        poseStack.popPose();
    }
  }

  private static void renderBillboardQuadBright(PoseStack matrixStack, VertexConsumer builder, float scale, ResourceLocation texture) {
    int b1 = LightTexture.FULL_BRIGHT >> 16 & 65535;
    int b2 = LightTexture.FULL_BRIGHT & 65535;
    TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    matrixStack.pushPose();
    matrixStack.translate(0.5, 0.95, 0.5);
    Quaternionf rotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
    matrixStack.mulPose(rotation);
    Matrix4f matrix = matrixStack.last().pose();
    builder.addVertex(matrix, -scale, -scale, 0.0f).setColor(255, 255, 255, 255).setUv(sprite.getU0(), sprite.getV0()).setUv2(b1, b2).setNormal(1, 0, 0);
    builder.addVertex(matrix, -scale, scale, 0.0f).setColor(255, 255, 255, 255).setUv(sprite.getU0(), sprite.getV1()).setUv2(b1, b2).setNormal(1, 0, 0);
    builder.addVertex(matrix, scale, scale, 0.0f).setColor(255, 255, 255, 255).setUv(sprite.getU1(), sprite.getV1()).setUv2(b1, b2).setNormal(1, 0, 0);
    builder.addVertex(matrix, scale, -scale, 0.0f).setColor(255, 255, 255, 255).setUv(sprite.getU1(), sprite.getV0()).setUv2(b1, b2).setNormal(1, 0, 0);
    matrixStack.popPose();
  }
}
