package com.example;

// ЗМІНЕНО: Імпорт GeckoLib
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class TentacleRenderer extends GeoEntityRenderer<TentacleEntity> {
    public TentacleRenderer(EntityRendererFactory.Context renderManager) {
        // У GeckoLib 4 конструктор такий самий
        super(renderManager, new TentacleModel());
    }
}