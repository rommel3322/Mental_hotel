package com.example;

import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.*;
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
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
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
        // Пріоритет 2: Атака ворога
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.5, true));
        // Пріоритет 3: Слідування за Аластором
        this.goalSelector.add(3, new FollowOwnerGoal(this, 1.3, 4.0f, 1.5f));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));

        // Вибір цілі: Атакувати того, хто вдарив посіпаку
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.ownerUuid != null) {
            PlayerEntity owner = this.getWorld().getPlayerByUuid(this.ownerUuid);
            if (owner != null) {
                // МАГІЯ: Якщо Аластор когось б'є — посіпаки кидаються на ціль миттєво
                if (owner.getAttacking() != null) {
                    this.setTarget(owner.getAttacking());
                }
            }
            // Посіпаки живуть 2 хвилини
            if (this.age > 2400) this.discard();
        }
    }

    public static DefaultAttributeContainer.Builder createMinionAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_SCALE, 0.6); // Маленькі розміри
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 4, event -> {
            // 1. Анімація СПАВНУ (грає перші 2 секунди)
            if (this.age < 40) {
                event.getController().setAnimation(RawAnimation.begin().thenPlay("spawn"));
                return mod.azure.azurelib.core.object.PlayState.CONTINUE;
            }

            // 2. Анімація ХОДЬБИ (тільки якщо рухається)
            if (event.getLimbSwingAmount() > 0.05) {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("walk"));
                return mod.azure.azurelib.core.object.PlayState.CONTINUE;
            }

            // 3. ЯКЩО СТОЇТЬ — ЗУПИНЯЄМО ВСЕ (буде просто моделька)
            return mod.azure.azurelib.core.object.PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // --- КЛАС AI ДЛЯ СЛІДУВАННЯ ЗА ГРАВЦЕМ ---
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
                // Якщо дуже далеко - телепорт до хазяїна
                if (minion.distanceTo(owner) > 20) {
                    minion.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), 0, 0);
                } else {
                    minion.getNavigation().startMovingTo(owner, speed);
                }
            }
        }
    }
}