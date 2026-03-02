package com.example;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class AlastorHornsModel extends GeoModel<AlastorHornsItem> {
    @Override
    public Identifier getModelResource(AlastorHornsItem animatable) {
        return Identifier.of("example", "geo/horns.geo.json");
    }
    @Override
    public Identifier getTextureResource(AlastorHornsItem animatable) {
        return Identifier.of("example", "textures/armor/horns.png");
    }
    @Override
    public Identifier getAnimationResource(AlastorHornsItem animatable) {
        return Identifier.of("example", "animations/horns.json");
    }
}