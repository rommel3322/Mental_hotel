package com.example;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class AlastorHornsModel extends Model {
    private final ModelPart bb_main;

    public AlastorHornsModel(ModelPart root) {
        // Використовуємо цей шар, щоб чорний колір не залежав від світла
        super(RenderLayer::getEntityCutoutNoCull);
        this.bb_main = root.getChild("bb_main");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Створюємо кістку bb_main. Pivot -8.0F ставить роги на маківку.
        ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, -8.0F, 0.0F));

        // --- ЦЕ ТВОЇ КУБИ З ЕКСПОРТУ ---

        // Куб 1 (Лівий)
        bb_main.addChild("cube_1", ModelPartBuilder.create().uv(0, 0)
                        .cuboid(-0.25F, -0.75F, 2.25F, 0.5F, 0.75F, 0.25F),
                ModelTransform.rotation(0.0F, -1.5708F, 0.0F));

        // Куб 2
        bb_main.addChild("cube_2", ModelPartBuilder.create().uv(1, 2)
                        .cuboid(-0.25F, -1.75F, 2.5F, 0.5F, 1.0F, 0.25F),
                ModelTransform.rotation(0.0F, -1.5708F, 0.0F));

        // Куб 3
        bb_main.addChild("cube_3", ModelPartBuilder.create().uv(3, 1)
                        .cuboid(-0.25F, -1.0F, 2.0F, 0.5F, 0.25F, 0.25F),
                ModelTransform.rotation(0.0F, -1.5708F, 0.0F));

        // Куб 4 (Правий)
        bb_main.addChild("cube_4", ModelPartBuilder.create().uv(1, 4)
                        .cuboid(-0.25F, -1.75F, 2.5F, 0.5F, 1.0F, 0.25F),
                ModelTransform.rotation(0.0F, 1.5708F, 0.0F));

        // Куб 5
        bb_main.addChild("cube_5", ModelPartBuilder.create().uv(4, 4)
                        .cuboid(-0.25F, -0.75F, 2.25F, 0.5F, 0.75F, 0.25F),
                ModelTransform.rotation(0.0F, 1.5708F, 0.0F));

        // Куб 6
        bb_main.addChild("cube_6", ModelPartBuilder.create().uv(5, 2)
                        .cuboid(-0.25F, -1.0F, 2.0F, 0.5F, 0.25F, 0.25F),
                ModelTransform.rotation(0.0F, 1.5708F, 0.0F));

        // Текстура у тебе 16x16
        return TexturedModelData.of(modelData, 16, 16);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        // Переконайся, що red, green, blue = 1.0f для чорного кольору текстури
        this.bb_main.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}