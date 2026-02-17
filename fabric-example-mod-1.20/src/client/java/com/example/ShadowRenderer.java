package com.example;

import mod.azure.azurelib.common.api.client.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class ShadowRenderer extends GeoEntityRenderer<ShadowEntity> {
    public ShadowRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new ShadowModel());
    }
}