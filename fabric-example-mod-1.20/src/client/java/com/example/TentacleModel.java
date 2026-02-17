package com.example;

import mod.azure.azurelib.common.api.client.model.GeoModel;
import net.minecraft.util.Identifier;

public class TentacleModel extends GeoModel<TentacleEntity> {

    // Кажемо, де лежить 3D модель (.geo.json)
    @Override
    public Identifier getModelResource(TentacleEntity animatable) {
        return Identifier.of("example", "geo/tentacle.geo.json");
    }

    // Кажемо, де лежить текстура (.png)
    @Override
    public Identifier getTextureResource(TentacleEntity animatable) {
        return Identifier.of("example", "textures/entity/tentacle.png");
    }

    // Кажемо, де лежать анімації (.animation.json)
    @Override
    public Identifier getAnimationResource(TentacleEntity animatable) {
        // Перевір, щоб назва файлу була точно такою, як у папці animations
        return Identifier.of("example", "animations/tentacle.animation.json");
    }
}