package com.example;

import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
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

import java.util.List;

public class TentacleEntity extends PathAwareEntity implements GeoEntity {
    // 1. Налаштування кешу та анімацій
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    // ВАЖЛИВО: назви мають збігатися з тими, що ти дав у Blockbench
    protected static final RawAnimation SPAWN_ANIM = RawAnimation.begin()
            .thenPlay("animation.tentacle.spawn")
            .thenLoop("animation.tentacle.idle");
    private static final RawAnimation START_SEQUENCE = RawAnimation.begin()
            .thenPlay("animation.tentacle.spawn") // Граємо 1 раз
            .thenLoop("idle.new");

    public TentacleEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    // 2. ВСТАНОВЛЮЄМО РОЗМІР ТА ХАРАКТЕРИСТИКИ
    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                // ОЦЕ робить їх величезними (2.5 - це в 2.5 рази більше)
                .add(EntityAttributes.GENERIC_SCALE, 2.5);
    }


    // 3. ПІДКЛЮЧАЄМО АНІМАЦІЇ
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            // Просто запускаємо ланцюжок.
            // AzureLib сама зрозуміє, коли закінчиться spawn і треба почати idle.
            return event.setAndContinue(START_SEQUENCE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 4. ЕФЕКТИ ТА АТАКА (Кожен тік гри)
    @Override
    public void tick() {
        super.tick();

        // ВІЗУАЛ (Тільки на клієнті): Чорний дим
        if (this.getWorld().isClient) {
            for (int i = 0; i < 2; i++) {
                this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                        this.getY() + (this.random.nextDouble() * 4),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                        0, 0.05, 0);
            }
        }

        // ЛОГІКА АТАКИ (Тільки на сервері)
        if (!this.getWorld().isClient) {
            // Щупальце б'є ворогів раз на секунду (20 тіків)
            if (this.age % 20 == 0) {
                // Шукаємо всіх істот у радіусі 3 блоків
                List<Entity> targets = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(3.0));

                for (Entity entity : targets) {
                    // Якщо це жива істота і не саме щупальце
                    if (entity instanceof LivingEntity target && !(entity instanceof TentacleEntity)) {

                        // Наносимо шкоду (4 серця)
                        target.damage(this.getWorld().getDamageSources().magic(), 8.0f);

                        // Накладаємо сильне сповільнення (Slowness 5), ніби щупальце схопило жертву
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 4));
                    }
                }
            }

            // Щупальце самознищується через 10 секунд (200 тіків)
            if (this.age > 200) {
                this.discard();
            }
        }
    }
}