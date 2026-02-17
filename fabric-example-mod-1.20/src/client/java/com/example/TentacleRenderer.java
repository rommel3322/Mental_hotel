package com.example;

import mod.azure.azurelib.common.api.client.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class TentacleRenderer extends GeoEntityRenderer<TentacleEntity> {
    public TentacleRenderer(EntityRendererFactory.Context randerManager){
        super(randerManager, new TentacleModel());
    }
    {}
}
