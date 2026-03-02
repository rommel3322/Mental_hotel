package com.example;

// ЗМІНЕНО: Імпорт GeckoLib замість AzureLib
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class MinionModel extends GeoModel<MinionEntity> {

    @Override
    public Identifier getModelResource(MinionEntity animatable) {
        // Identifier.of — правильний синтаксис для 1.20.6
        return Identifier.of("example", "geo/minion.geo.json");
    }

    @Override
    public Identifier getTextureResource(MinionEntity animatable) {
        return Identifier.of("example", "textures/entity/minion.png");
    }

    @Override
    public Identifier getAnimationResource(MinionEntity animatable) {
        // Переконайся, що файл називається саме minion.json (без .animation)
        return Identifier.of("example", "animations/minion.json");
    }
}