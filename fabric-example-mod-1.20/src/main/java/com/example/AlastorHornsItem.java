package com.example;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AlastorHornsItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AlastorHornsItem() {
        super(ArmorMaterials.NETHERITE, Type.HELMET, new Item.Settings().maxCount(1));
    }

    // МИ ВИДАЛИЛИ createRenderer та getRenderProvider, щоб не було конфліктів

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "horns_controller", 5, event -> {
            if (ExampleMod.isUltActive) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("ult_grow").thenLoop("ult_idle"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}