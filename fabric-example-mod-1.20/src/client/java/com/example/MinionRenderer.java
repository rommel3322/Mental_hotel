package com.example;

// ЗМІНЕНО: Імпорт GeckoLib замість AzureLib
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class MinionRenderer extends GeoEntityRenderer<MinionEntity> {
    public MinionRenderer(EntityRendererFactory.Context renderManager) {
        // Конструктор залишається таким самим
        super(renderManager, new MinionModel());
    }
    @Override
    public void preRender(MatrixStack poseStack, MinionEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        // Зменшуємо вдвічі (0.5)
        poseStack.scale(0.5f, 0.5f, 0.5f);

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}