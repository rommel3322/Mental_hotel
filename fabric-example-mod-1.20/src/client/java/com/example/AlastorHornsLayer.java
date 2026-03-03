package com.example;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AlastorHornsLayer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private final AlastorHornsModel model;

    // Шляхи до твоїх текстур 32x32
    private static final Identifier TEXTURE_NORMAL = Identifier.of("example", "textures/entity/horns.png");
    private static final Identifier TEXTURE_ULT = Identifier.of("example", "textures/entity/horns_ult.png");

    public AlastorHornsLayer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, AlastorHornsModel model) {
        super(context);
        this.model = model;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        // Малюємо роги ТІЛЬКИ якщо активована форма Аластора
        if (!ExampleModClient.isAlastor) return;

        matrices.push();

        // 1. ПРИВ'ЯЗКА ДО ГОЛОВИ
        // Цей рядок копіює рух голови гравця (повороти, нахили) на нашу модель рогів
        this.getContextModel().getHead().rotate(matrices);

        // 2. ВИБІР ТЕКСТУРИ
        // Якщо ульта активна — беремо horns_ult, інакше — звичайну
        Identifier texture = ExampleMod.isUltActive ? TEXTURE_ULT : TEXTURE_NORMAL;

        // 3. ПІДГОТОВКА МАЛЮВАННЯ
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(model.getLayer(texture));

        // 4. РЕНДЕР МОДЕЛІ
        // Останні 4 параметри (1.0f, 1.0f, 1.0f, 1.0f) — це Red, Green, Blue, Alpha.
        // Одиниці гарантують, що роги будуть чорними (як на твоїй текстурі).
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}