package com.example;

// ЗМІНЕНО: Імпорт GeckoLib замість AzureLib
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class MinionRenderer extends GeoEntityRenderer<MinionEntity> {
    public MinionRenderer(EntityRendererFactory.Context renderManager) {
        // Конструктор залишається таким самим
        super(renderManager, new MinionModel());
    }
}