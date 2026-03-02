package com.example;

// ЗМІНЕНО: Імпорт GeckoLib замість AzureLib
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class ShadowModel extends GeoModel<ShadowEntity> {

    @Override
    public Identifier getModelResource(ShadowEntity animatable) {
        // Identifier.of — правильний метод для 1.20.6
        return Identifier.of("example", "geo/shadow.geo.json");
    }

    @Override
    public Identifier getTextureResource(ShadowEntity animatable) {
        return Identifier.of("example", "textures/entity/shadow.png");
    }

    @Override
    public Identifier getAnimationResource(ShadowEntity animatable) {
        return Identifier.of("example", "animations/shadow.animation.json");
    }
}