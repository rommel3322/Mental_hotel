package com.example;

// Спробуй цей шлях
import mod.azure.azurelib.common.api.client.model.GeoModel;
import net.minecraft.util.Identifier;

public class MinionModel extends GeoModel<MinionEntity> {
    @Override
    public Identifier getModelResource(MinionEntity animatable) {
        return Identifier.of("example", "geo/minion.geo.json");
    }

    @Override
    public Identifier getTextureResource(MinionEntity animatable) {
        return Identifier.of("example", "textures/entity/minion.png");
    }

    @Override
    public Identifier getAnimationResource(MinionEntity animatable) {
        return Identifier.of("example", "animations/minion.json");
    }
}