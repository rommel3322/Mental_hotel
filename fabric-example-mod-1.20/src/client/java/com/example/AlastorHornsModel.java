package com.example;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class AlastorHornsModel extends Model {
    private final ModelPart normalHorns;
    private final ModelPart ultHorns;

    public AlastorHornsModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        // Отримуємо обидві гілки моделі
        this.normalHorns = root.getChild("normal");
        this.ultHorns = root.getChild("ult");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // --- 1. СТАРІ РОГИ (ЗВИЧАЙНІ) ---
        // Створюємо групу "normal", Pivot -8.0F (голова)
        ModelPartData normal = modelPartData.addChild("normal", ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, -8.0F, 0.0F));

        // Твої 6 старих кубів:
        normal.addChild("cube1", ModelPartBuilder.create().uv(0, 0).cuboid(-0.25F, -0.75F, 2.25F, 0.5F, 0.75F, 0.25F), ModelTransform.rotation(0.0F, -1.5708F, 0.0F));
        normal.addChild("cube2", ModelPartBuilder.create().uv(1, 2).cuboid(-0.25F, -1.75F, 2.5F, 0.5F, 1.0F, 0.25F), ModelTransform.rotation(0.0F, -1.5708F, 0.0F));
        normal.addChild("cube3", ModelPartBuilder.create().uv(3, 1).cuboid(-0.25F, -1.0F, 2.0F, 0.5F, 0.25F, 0.25F), ModelTransform.rotation(0.0F, -1.5708F, 0.0F));
        normal.addChild("cube4", ModelPartBuilder.create().uv(1, 4).cuboid(-0.25F, -1.75F, 2.5F, 0.5F, 1.0F, 0.25F), ModelTransform.rotation(0.0F, 1.5708F, 0.0F));
        normal.addChild("cube5", ModelPartBuilder.create().uv(4, 4).cuboid(-0.25F, -0.75F, 2.25F, 0.5F, 0.75F, 0.25F), ModelTransform.rotation(0.0F, 1.5708F, 0.0F));
        normal.addChild("cube6", ModelPartBuilder.create().uv(5, 2).cuboid(-0.25F, -1.0F, 2.0F, 0.5F, 0.25F, 0.25F), ModelTransform.rotation(0.0F, 1.5708F, 0.0F));


        // --- 2. НОВІ РОГИ (УЛЬТА) ---
        // Створюємо групу "ult", Pivot -8.0F (голова)
        ModelPartData ult = modelPartData.addChild("ult", ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, -8.0F, 0.0F));

        // Права частина ульти (bone3)
        ModelPartData bone3 = ult.addChild("bone3", ModelPartBuilder.create(), ModelTransform.of(2.5F, 0.5F, 0.5F, 0.0F, 3.1416F, 0.0F));
        bone3.addChild("cube_r1", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -2.0F, -0.25F, 0.5F, 2.0F, 0.5F), ModelTransform.of(-2.75F, -4.0F, 0.5F, 0.0F, 0.0F, -0.6981F));
        bone3.addChild("cube_r2", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -3.0F, -0.25F, 0.5F, 3.0F, 0.5F), ModelTransform.of(-2.75F, -3.0F, 0.5F, 0.0F, 0.0F, 0.1745F));
        bone3.addChild("cube_r3", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -2.0F, -0.25F, 0.5F, 2.0F, 0.5F), ModelTransform.of(-4.75F, -4.0F, 0.5F, 0.0F, 0.0F, -0.4363F));
        bone3.addChild("cube_r4", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -1.0F, -0.25F, 0.5F, 1.0F, 0.5F), ModelTransform.of(-1.25F, -5.0F, 0.5F, 0.0F, 0.0F, 0.5672F));
        bone3.addChild("cube_r5", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -2.0F, -0.25F, 0.5F, 2.0F, 0.5F), ModelTransform.of(-0.5F, -3.25F, 0.5F, 0.0F, 0.0F, -0.4363F));
        bone3.addChild("cube_r6", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -4.0F, -0.25F, 0.5F, 4.0F, 0.5F), ModelTransform.of(-1.25F, -2.25F, 0.5F, 0.0F, 0.0F, 0.5672F));
        bone3.addChild("cube_r7", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -2.0F, -0.25F, 0.5F, 2.0F, 0.5F), ModelTransform.of(0.25F, -2.0F, 0.5F, 0.0F, 0.0F, -1.3963F));
        bone3.addChild("cube_r8", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -4.0F, -0.25F, 0.5F, 4.0F, 0.5F), ModelTransform.of(-1.5F, -2.25F, 0.5F, 0.0F, 0.0F, -1.0472F));
        bone3.addChild("cube_r9", ModelPartBuilder.create().uv(2, 1).cuboid(0.0F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

        // Ліва частина ульти (bone2)
        ModelPartData bone2 = ult.addChild("bone2", ModelPartBuilder.create(), ModelTransform.pivot(-2.75F, 0.5F, -0.5F));
        bone2.addChild("cube_r10", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -2.0F, -0.25F, 0.5F, 2.0F, 0.5F), ModelTransform.of(-2.75F, -4.0F, 0.5F, 0.0F, 0.0F, -0.6981F));
        bone2.addChild("cube_r11", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -3.0F, -0.25F, 0.5F, 3.0F, 0.5F), ModelTransform.of(-2.75F, -3.0F, 0.5F, 0.0F, 0.0F, 0.1745F));
        bone2.addChild("cube_r12", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -2.0F, -0.25F, 0.5F, 2.0F, 0.5F), ModelTransform.of(-4.75F, -4.0F, 0.5F, 0.0F, 0.0F, -0.4363F));
        bone2.addChild("cube_r13", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -1.0F, -0.25F, 0.5F, 1.0F, 0.5F), ModelTransform.of(-1.25F, -5.0F, 0.5F, 0.0F, 0.0F, 0.5672F));
        bone2.addChild("cube_r14", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -2.0F, -0.25F, 0.5F, 2.0F, 0.5F), ModelTransform.of(-0.5F, -3.25F, 0.5F, 0.0F, 0.0F, -0.4363F));
        bone2.addChild("cube_r15", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -4.0F, -0.25F, 0.5F, 4.0F, 0.5F), ModelTransform.of(-1.25F, -2.25F, 0.5F, 0.0F, 0.0F, 0.5672F));
        bone2.addChild("cube_r16", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -2.0F, -0.25F, 0.5F, 2.0F, 0.5F), ModelTransform.of(0.25F, -2.0F, 0.5F, 0.0F, 0.0F, -1.3963F));
        bone2.addChild("cube_r17", ModelPartBuilder.create().uv(1, 1).cuboid(-0.25F, -4.0F, -0.25F, 0.5F, 4.0F, 0.5F), ModelTransform.of(-1.5F, -2.25F, 0.5F, 0.0F, 0.0F, -1.0472F));
        bone2.addChild("cube_r18", ModelPartBuilder.create().uv(2, 1).cuboid(0.0F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

        return TexturedModelData.of(modelData, 16, 16);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        if (ExampleMod.isUltActive) {
            this.ultHorns.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        } else {
            this.normalHorns.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }
    }
}