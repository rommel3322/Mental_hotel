package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "example";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean isUltActive = false;

    // Предмети
    public static final Item ALASTOR_CANE = new AlastorCaneItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC));
    public static final SimpleParticleType VOODOO_SYMBOL = FabricParticleTypes.simple();

    // Сутності
    public static final EntityType<TentacleEntity> TENTACLE = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "tentacle"),
            EntityType.Builder.create(TentacleEntity::new, SpawnGroup.MISC).dimensions(1.5f, 5.0f).build());
    public static final EntityType<ShadowEntity> SHADOW = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "shadow"),
            EntityType.Builder.create(ShadowEntity::new, SpawnGroup.MISC).dimensions(0.6f, 1.8f).build());
    public static final EntityType<MinionEntity> MINION = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "minion"),
            EntityType.Builder.create(MinionEntity::new, SpawnGroup.MISC).dimensions(1.5f, 1.8f).build());

    public static final Map<UUID, ShadowEntity> ACTIVE_SHADOWS = new HashMap<>();

    @Override
    public void onInitialize() {
        // Реєстрація пакетів
        PayloadTypeRegistry.playC2S().register(SkillPayload.ID, SkillPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AlastorPayload.ID, AlastorPayload.CODEC);

        // Реєстрація предметів
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "alastor_cane"), ALASTOR_CANE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "voodoo_symbol"), VOODOO_SYMBOL);
        FabricDefaultAttributeRegistry.register(TENTACLE, TentacleEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SHADOW, ShadowEntity.createLivingAttributes());
        FabricDefaultAttributeRegistry.register(MINION, MinionEntity.createMinionAttributes()); 

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ALASTOR_CANE);
        });

        // Команда
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("alastor").then(literal("transform").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player != null) {
                    context.getSource().sendFeedback(() -> Text.literal("§c§lРадіо-демон прокинувся..."), false);
                    ServerPlayNetworking.send(player, new AlastorPayload(true));
                }
                return 1;
            })));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("alastor").then(literal("ult").executes(context -> {
                // Перемикаємо стан ульти
                ExampleMod.isUltActive = !ExampleMod.isUltActive;

                String status = ExampleMod.isUltActive ? "§c§lУЛЬТА АКТИВОВАНА" : "§7Ульта вимкнена";
                context.getSource().sendFeedback(() -> Text.literal(status), false);

                // ВАЖЛИВО: Оскільки це серверна команда, нам треба сказати Клієнту оновити картинку
                // Якщо ти тестуєш в одиночній грі, це може спрацювати і так,
                // але для мультиплеєра треба шлях через пакети (Networking).
                return 1;
            })));
        });

        // Обробник скілів
        ServerPlayNetworking.registerGlobalReceiver(SkillPayload.ID, (payload, context) -> {
            context.player().getServer().execute(() -> {
                ServerPlayerEntity player = context.player();
                ServerWorld world = (ServerWorld) player.getWorld();

                if (payload.skillId() == 0) { // Щупальця
                    var hit = player.raycast(12.0, 0, false);
                    Vec3d pos = hit.getPos();
                    for (int i = 0; i < 3; i++) {
                        TentacleEntity t = new TentacleEntity(TENTACLE, world);
                        double ang = Math.toRadians(i * 120);
                        t.refreshPositionAndAngles(pos.x + Math.cos(ang)*1.5, pos.y, pos.z + Math.sin(ang)*1.5, 0, 0);
                        world.spawnEntity(t);
                    }
                    world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.5f, 1.2f);
                }
                else if (payload.skillId() == 1) { // Тінь
                    if (ACTIVE_SHADOWS.containsKey(player.getUuid())) {
                        ACTIVE_SHADOWS.get(player.getUuid()).startExit();
                    } else {
                        ShadowEntity s = new ShadowEntity(SHADOW, world);
                        s.setOwner(player);
                        s.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0);
                        world.spawnEntity(s);
                        ACTIVE_SHADOWS.put(player.getUuid(), s);
                    }
                }else if (payload.skillId() == 2) {
                    for (int i = 0; i < 3; i++) {
                        MinionEntity minion = new MinionEntity(MINION, world);

                        minion.refreshPositionAndAngles(player.getX() + (i - 1), player.getY(), player.getZ() + 1, 0, 0);
                        minion.setOwner(player);

                        world.spawnEntity(minion);
                    }
                }// Усередині обробника SkillPayload.ID (context.player().getServer().execute(() -> { ... }))

                else if (payload.skillId() == 10) { // [ C ] - Вуду-прокляття (Атака по цілі)
                    var hit = player.raycast(15.0, 0, false);
                    double x = hit.getPos().x; double y = hit.getPos().y; double z = hit.getPos().z;

                    var entities = world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class,
                            new net.minecraft.util.math.Box(x-1, y-1, z-1, x+1, y+1, z+1), e -> e != player);

                    if (!entities.isEmpty()) {
                        net.minecraft.entity.LivingEntity target = entities.get(0);
                        target.damage(world.getDamageSources().magic(), 12.0f);
                        target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SLOWNESS, 60, 10));

                        // ЕФЕКТ: Вертикальний вихор навколо цілі
                        for (int i = 0; i < 15; i++) {
                            double angle = i * (Math.PI * 2 / 15);
                            world.spawnParticles(ExampleMod.VOODOO_SYMBOL,
                                    target.getX() + Math.cos(angle) * 1.2, target.getY() + (i * 0.15), target.getZ() + Math.sin(angle) * 1.2,
                                    1, 0, 0.05, 0, 0.01);
                        }
                        world.playSound(null, target.getX(), target.getY(), target.getZ(), net.minecraft.sound.SoundEvents.ENTITY_WITCH_CELEBRATE, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.5f);
                    }
                }

                else if (payload.skillId() == 11) { // [ V ] - Радіо-глюк (Ривок)
                    // ЕФЕКТ: Слід із символів у точці старту
                    world.spawnParticles(ExampleMod.VOODOO_SYMBOL, player.getX(), player.getY() + 1, player.getZ(), 15, 0.5, 0.5, 0.5, 0.1);

                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.INVISIBILITY, 30, 0, false, false));
                    net.minecraft.util.math.Vec3d dash = player.getRotationVector().multiply(2.0);
                    player.addVelocity(dash.x, 0.2, dash.z);
                    player.velocityModified = true;

                    // ЕФЕКТ: Символи в точці приземлення (через 5 тіків можна було б, але спавнимо одразу по вектору)
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.5f);
                }

                else if (payload.skillId() == 12) { // [ B ] - Тіньова пастка (Заморозка навколо)
                    var targets = world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, player.getBoundingBox().expand(6.0), e -> e != player);

                    for (net.minecraft.entity.LivingEntity e : targets) {
                        e.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SLOWNESS, 100, 10));

                        // ЕФЕКТ: Символи вилітають з-під ніг кожного ворога
                        world.spawnParticles(ExampleMod.VOODOO_SYMBOL, e.getX(), e.getY() + 0.1, e.getZ(), 8, 0.2, 0.1, 0.2, 0.05);
                    }
                    // ЕФЕКТ: Велике коло символів під ногами Аластора
                    for (int i = 0; i < 360; i += 20) {
                        double rad = Math.toRadians(i);
                        world.spawnParticles(ExampleMod.VOODOO_SYMBOL, player.getX() + Math.cos(rad) * 4, player.getY() + 0.1, player.getZ() + Math.sin(rad) * 4, 1, 0, 0.1, 0, 0.01);
                    }
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ENTITY_SQUID_SQUIRT, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.5f);
                }

                else if (payload.skillId() == 13) { // [ N ] - Зелені знаки (Вибухова аура)
                    // ЕФЕКТ: Хаотичний вибух символів навколо гравця
                    world.spawnParticles(ExampleMod.VOODOO_SYMBOL, player.getX(), player.getY() + 1.5, player.getZ(), 40, 2.0, 2.0, 2.0, 0.2);

                    world.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ENTITY_ENDER_EYE_DEATH, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.8f);

                    // Відкидання всіх ворогів (як вибух магії)
                    var nearby = world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, player.getBoundingBox().expand(5.0), e -> e != player);
                    for (net.minecraft.entity.LivingEntity e : nearby) {
                        net.minecraft.util.math.Vec3d push = e.getPos().subtract(player.getPos()).normalize().multiply(1.5);
                        e.addVelocity(push.x, 0.4, push.z);
                    }
                }
            });
        });
    }
}