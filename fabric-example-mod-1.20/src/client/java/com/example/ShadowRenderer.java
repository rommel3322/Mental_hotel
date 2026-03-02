package com.example;

// ЗМІНЕНО: Імпорт GeckoLib замість AzureLib
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class ShadowRenderer extends GeoEntityRenderer<ShadowEntity> {
    public ShadowRenderer(EntityRendererFactory.Context renderManager) {
        // Конструктор у GeckoLib 4 приймає ті ж самі параметри
        super(renderManager, new ShadowModel());
    }
}