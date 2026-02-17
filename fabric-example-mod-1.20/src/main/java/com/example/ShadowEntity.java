package com.example;

import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import java.util.UUID;

public class ShadowEntity extends PathAwareEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    private UUID ownerUuid;
    private int lifeTicks = 0;
    private int exitTimer = 0;

    private static final TrackedData<Boolean> EXITING = DataTracker.registerData(ShadowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public ShadowEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
        this.setInvulnerable(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(EXITING, false);
    }

    public void setOwner(PlayerEntity player) { this.ownerUuid = player.getUuid(); }
    public void startExit() { this.getDataTracker().set(EXITING, true); }

    public static DefaultAttributeContainer.Builder createLivingAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        boolean isExiting = this.getDataTracker().get(EXITING);

        if (this.getWorld().isClient) {
            spawnMovementParticles();
            return;
        }

        PlayerEntity owner = this.getWorld().getPlayerByUuid(ownerUuid);
        if (owner != null) {
            this.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), owner.getYaw(), 0);

            if (!isExiting) {
                owner.setInvisible(true);
                owner.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(0.01f);

                owner.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 10, 0, false, false, false));
                owner.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 10, 10, false, false, false));

                if (lifeTicks < 20) {
                    owner.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 5, 255, false, false));
                }
            } else {
                exitTimer++;
                owner.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 5, 255, false, false));

                if (exitTimer == 5) {
                    owner.setInvisible(false);
                    owner.removeStatusEffect(StatusEffects.INVISIBILITY);
                }

                if (exitTimer >= 20) {
                    owner.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.0f);
                    this.discard();
                    ExampleMod.ACTIVE_SHADOWS.remove(owner.getUuid());
                }
            }
        } else {
            this.discard();
        }
    }

    private void spawnMovementParticles() {
        for (int i = 0; i < 4; i++) {
            this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE,
                    this.getX() + (random.nextDouble() - 0.5) * 1.2,
                    this.getY() + 0.05,
                    this.getZ() + (random.nextDouble() - 0.5) * 1.2,
                    0, 0.02, 0);
        }
        if (random.nextInt(2) == 0) {
            this.getWorld().addParticle(ParticleTypes.REVERSE_PORTAL,
                    this.getX() + (random.nextDouble() - 0.5),
                    this.getY() + 0.1,
                    this.getZ() + (random.nextDouble() - 0.5),
                    0, 0.1, 0);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, event -> {
            if (this.getDataTracker().get(EXITING)) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("Shadow.rise"));
            }
            if (lifeTicks < 20) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("Shadow.sink"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("Shadow.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}