package com.example;

// ІМПОРТИ ГЕКОЛІБА (Змінено)
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import java.util.UUID;

public class MinionEntity extends PathAwareEntity implements GeoEntity {
    // 1. Налаштування кешу (GeckoLibUtil)
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUuid;

    public MinionEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    public void setOwner(PlayerEntity player) {
        if (player != null) {
            this.ownerUuid = player.getUuid();
        }
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.5, true));
        this.goalSelector.add(3, new FollowOwnerGoal(this, 1.3, 4.0f, 1.5f));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.ownerUuid != null) {
            PlayerEntity owner = this.getWorld().getPlayerByUuid(this.ownerUuid);
            if (owner != null) {
                if (owner.getAttacking() != null) {
                    this.setTarget(owner.getAttacking());
                }
            }
            if (this.age > 2400) this.discard();
        }
    }

    public static DefaultAttributeContainer.Builder createMinionAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_SCALE, 0.6);
    }

    // 2. Анімації (Оновлено під синтаксис GeckoLib)
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 4, event -> {
            // Анімація СПАВНУ
            if (this.age < 40) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("spawn"));
            }

            // Анімація ХОДЬБИ (event.isMoving() - зручний метод GeckoLib)
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }

            // ЯКЩО СТОЇТЬ
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // --- КЛАС AI (без змін, він працює на рівні Minecraft) ---
    class FollowOwnerGoal extends Goal {
        private final MinionEntity minion;
        private final double speed;
        private final float maxDist;
        private final float minDist;

        public FollowOwnerGoal(MinionEntity minion, double speed, float maxDist, float minDist) {
            this.minion = minion;
            this.speed = speed;
            this.maxDist = maxDist;
            this.minDist = minDist;
        }

        @Override
        public boolean canStart() {
            if (minion.ownerUuid == null) return false;
            PlayerEntity owner = minion.getWorld().getPlayerByUuid(minion.ownerUuid);
            return owner != null && minion.distanceTo(owner) > maxDist;
        }

        @Override
        public void tick() {
            PlayerEntity owner = minion.getWorld().getPlayerByUuid(minion.ownerUuid);
            if (owner != null) {
                if (minion.distanceTo(owner) > 20) {
                    minion.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), 0, 0);
                } else {
                    minion.getNavigation().startMovingTo(owner, speed);
                }
            }
        }
    }
}