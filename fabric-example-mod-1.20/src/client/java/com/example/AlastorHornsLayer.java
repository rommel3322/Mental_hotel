package com.example;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AlastorHornsLayer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private final AlastorHornsModel model;
    private static final Identifier TEXTURE_NORMAL = Identifier.of("example", "textures/entity/horns.png");
    private static final Identifier TEXTURE_ULT = Identifier.of("example", "textures/entity/horns_ult.png");

    public AlastorHornsLayer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, AlastorHornsModel model) {
        super(context);
        this.model = model;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        // УМОВА: Малюємо тільки якщо активована форма Аластора
        if (!ExampleModClient.isAlastor) return;

        matrices.push();

        // Прив'язуємо роги до кістки голови (щоб вони крутилися разом з нею)
        this.getContextModel().getHead().rotate(matrices);

        // ВИБІР ТЕКСТУРИ:
        Identifier texture = ExampleMod.isUltActive ? TEXTURE_ULT : TEXTURE_NORMAL;

        // Якщо ульта, можна ще й трохи збільшити роги кодом:
        if (ExampleMod.isUltActive) {
            matrices.scale(1.2f, 1.4f, 1.2f);
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(model.getLayer(texture));
        this.model.render(matrices, vertexConsumer, light, net.minecraft.client.render.OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}