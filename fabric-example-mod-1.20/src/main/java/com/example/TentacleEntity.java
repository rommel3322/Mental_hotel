package com.example;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
// ІМПОРТИ ГЕКОЛІБА (Змінено)
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class TentacleEntity extends PathAwareEntity implements GeoEntity {

    // 1. Налаштування кешу (Змінено AzureLibUtil -> GeckoLibUtil)
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation SPAWN_ANIM = RawAnimation.begin()
            .thenPlay("animation.tentacle.spawn")
            .thenLoop("animation.tentacle.idle");

    private static final RawAnimation START_SEQUENCE = RawAnimation.begin()
            .thenPlay("animation.tentacle.spawn")
            .thenLoop("idle.new");

    public TentacleEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    // Характеристики (без змін, в 1.20.6 SCALE працює чудово)
    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_SCALE, 2.5);
    }

    // 3. ПІДКЛЮЧАЄМО АНІМАЦІЇ (Логіка та сама, але типи GeckoLib)
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            return event.setAndContinue(START_SEQUENCE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 4. ЕФЕКТИ ТА АТАКА (Без змін, логіка Minecraft та сама)
    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            for (int i = 0; i < 2; i++) {
                this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                        this.getY() + (this.random.nextDouble() * 4),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                        0, 0.05, 0);
            }
        }

        if (!this.getWorld().isClient) {
            if (this.age % 20 == 0) {
                List<Entity> targets = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(3.0));
                for (Entity entity : targets) {
                    if (entity instanceof LivingEntity target && !(entity instanceof TentacleEntity)) {
                        target.damage(this.getWorld().getDamageSources().magic(), 8.0f);
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 4));
                    }
                }
            }

            if (this.age > 200) {
                this.discard();
            }
        }
    }
}