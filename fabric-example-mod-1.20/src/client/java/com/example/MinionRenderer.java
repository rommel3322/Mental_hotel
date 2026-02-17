package com.example;

import mod.azure.azurelib.common.api.client.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;


public class MinionRenderer extends GeoEntityRenderer<MinionEntity> {
    public MinionRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new MinionModel());
    }
}