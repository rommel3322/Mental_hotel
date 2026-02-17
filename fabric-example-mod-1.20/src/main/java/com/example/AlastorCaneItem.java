package com.example;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import java.util.List;

public class AlastorCaneItem extends Item {
    public AlastorCaneItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Працюємо тільки на сервері
        if (!world.isClient && world instanceof ServerWorld serverWorld) {

            if (user.isSneaking()) {
                // 1. Якщо затиснуто Shift — активуємо Промінь
                performRadioBeam(user, serverWorld);
            } else {
                // 2. Якщо просто клік — активуємо Вибух
                performShadowBlast(user, serverWorld);
            }

            // Кулдаун 2 секунди
            user.getItemCooldownManager().set(this, 40);
        }

        return TypedActionResult.success(stack);
    }

    // МЕТОД ВИБУХУ (Shadow Blast)
    private void performShadowBlast(PlayerEntity user, ServerWorld world) {
        double radius = 5.0;
        Box box = user.getBoundingBox().expand(radius);
        List<Entity> entities = world.getOtherEntities(user, box);

        // Візуальний ефект кола часток
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = user.getX() + Math.cos(angle) * 2.5;
            double z = user.getZ() + Math.sin(angle) * 2.5;
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, x, user.getY() + 1, z, 1, 0, 0.1, 0, 0.05);
            world.spawnParticles(ParticleTypes.LARGE_SMOKE, x, user.getY() + 1, z, 1, 0, 0, 0, 0.02);
        }

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity target) {
                // Відкидаємо і б'ємо ворога
                double xDiff = target.getX() - user.getX();
                double zDiff = target.getZ() - user.getZ();
                target.takeKnockback(1.5, -xDiff, -zDiff);
                target.damage(world.getDamageSources().magic(), 6.0f);
            }
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 1.0f, 0.5f);
    }

    // МЕТОД ПРОМЕНЯ (Radio Beam)
    private void performRadioBeam(PlayerEntity user, ServerWorld world) {
        double maxDistance = 25.0; // Тепер 25 блоків
        double step = 0.5; // Крок перевірки (чим менше, тим точніше)

        // Отримуємо напрямок погляду гравця
        net.minecraft.util.math.Vec3d startPos = user.getEyePos();
        net.minecraft.util.math.Vec3d lookVec = user.getRotationVec(1.0F);

        for (double i = 0; i < maxDistance; i += step) {
            // Рахуємо точку в просторі на цій відстані
            net.minecraft.util.math.Vec3d currentPos = startPos.add(lookVec.multiply(i));

            // Малюємо частинки променя (тепер їх видно завжди, навіть якщо не влучив)
            world.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                    currentPos.x, currentPos.y, currentPos.z,
                    2, 0.05, 0.05, 0.05, 0.01);

            // Шукаємо ворога в цій точці (радіус 0.8 блока)
            Box checkArea = new Box(currentPos.x - 0.4, currentPos.y - 0.4, currentPos.z - 0.4,
                    currentPos.x + 0.4, currentPos.y + 0.4, currentPos.z + 0.4);

            List<Entity> targets = world.getOtherEntities(user, checkArea);

            for (Entity entity : targets) {
                if (entity instanceof LivingEntity livingTarget) {
                    // ВЛУЧИЛИ! Наносимо шкоду
                    livingTarget.damage(world.getDamageSources().magic(), 12.0f); // 6 сердець
                    livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
                    livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));

                    // Ефект при влучанні
                    world.spawnParticles(ParticleTypes.LARGE_SMOKE, currentPos.x, currentPos.y, currentPos.z, 10, 0.2, 0.2, 0.2, 0.05);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.PLAYERS, 0.5f, 2.0f);

                    return; // Зупиняємо промінь, бо він влучив у першу ціль
                }
            }
        }

        // Якщо промінь ні в кого не влучив, просто граємо звук "промаху"
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 0.5f, 1.5f);
    }
}