package com.example;

// ЗМІНЕНО: Імпорт GeckoLib
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class TentacleRenderer extends GeoEntityRenderer<TentacleEntity> {
    public TentacleRenderer(EntityRendererFactory.Context renderManager) {
        // У GeckoLib 4 конструктор такий самий
        super(renderManager, new TentacleModel());
    }
    @Override
    public void preRender(MatrixStack poseStack, TentacleEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        // Збільшуємо в 3 рази (3.0)
        poseStack.scale(3.0f, 3.0f, 3.0f);

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}