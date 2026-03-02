package com.example;

// ЗМІНЕНО: Імпорт GeckoLib замість AzureLib
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class TentacleModel extends GeoModel<TentacleEntity> {

    // Де лежить 3D модель (.geo.json)
    @Override
    public Identifier getModelResource(TentacleEntity animatable) {
        // Identifier.of — це правильний синтаксис для 1.20.6
        return Identifier.of("example", "geo/tentacle.geo.json");
    }

    // Де лежить текстура (.png)
    @Override
    public Identifier getTextureResource(TentacleEntity animatable) {
        return Identifier.of("example", "textures/entity/tentacle.png");
    }

    // Де лежать анімації (.animation.json)
    @Override
    public Identifier getAnimationResource(TentacleEntity animatable) {
        return Identifier.of("example", "animations/tentacle.animation.json");
    }
}