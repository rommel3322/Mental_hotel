package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.server.command.CommandManager.literal;

public class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "example";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Зберігаємо UUID гравців, у яких включена ульта
    public static final Set<UUID> ULT_PLAYERS = new HashSet<>();
    public static boolean isUltActive = false; // Для сумісності з рендерером в одиночній грі

    public static final Item ALASTOR_CANE = new AlastorCaneItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC));
    public static final SimpleParticleType VOODOO_SYMBOL = FabricParticleTypes.simple();

    public static final EntityType<TentacleEntity> TENTACLE = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "tentacle"),
            EntityType.Builder.create(TentacleEntity::new, SpawnGroup.MISC).dimensions(1.5f, 5.0f).build());
    public static final EntityType<ShadowEntity> SHADOW = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "shadow"),
            EntityType.Builder.create(ShadowEntity::new, SpawnGroup.MISC).dimensions(0.6f, 1.8f).build());
    public static final EntityType<MinionEntity> MINION = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "minion"),
            EntityType.Builder.create(MinionEntity::new, SpawnGroup.MISC).dimensions(1.5f, 1.8f).build());

    public static final Map<UUID, ShadowEntity> ACTIVE_SHADOWS = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        // Реєстрація пакетів
        PayloadTypeRegistry.playC2S().register(SkillPayload.ID, SkillPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AlastorPayload.ID, AlastorPayload.CODEC);

        // Реєстрація контенту
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "alastor_cane"), ALASTOR_CANE);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "voodoo_symbol"), VOODOO_SYMBOL);

        FabricDefaultAttributeRegistry.register(TENTACLE, TentacleEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SHADOW, ShadowEntity.createLivingAttributes());
        FabricDefaultAttributeRegistry.register(MINION, MinionEntity.createMinionAttributes());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(ALASTOR_CANE));

        // Пасивний ефект ульти
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (ULT_PLAYERS.contains(player.getUuid())) {
                    applyRadioInterference(player);
                }
            }
        });

        // Реєстрація команд
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("alastor").then(literal("transform").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player != null) {
                    ServerPlayNetworking.send(player, new AlastorPayload(true));
                }
                return 1;
            })));

            dispatcher.register(literal("alastor").then(literal("ult").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player != null) toggleUlt(player);
                return 1;
            })));
        });

        // ГОЛОВНИЙ ОБРОБНИК ВСІХ СКІЛІВ
        ServerPlayNetworking.registerGlobalReceiver(SkillPayload.ID, (payload, context) -> {
            context.player().getServer().execute(() -> {
                ServerPlayerEntity player = context.player();
                ServerWorld world = (ServerWorld) player.getWorld();
                UUID uuid = player.getUuid();
                boolean hasUlt = ULT_PLAYERS.contains(uuid);

                switch (payload.skillId()) {
                    // --- СТАРІ СКІЛИ (СЛОТИ 1, 2, 3) ---
                    case 0:
                        spawnTentacles(player, world);
                        break;
                    case 1:
                        handleShadow(player, world, uuid);
                        break;
                    case 2:
                        spawnMinions(player, world);
                        break;

                    // --- УЛЬТА (КНОПКА R) ---
                    case 4:
                        toggleUlt(player);
                        break;

                    // --- МАГІЧНІ СКІЛИ (КНОПКИ C, V, B, N) ---
                    case 10: // Кнопка C
                        if (hasUlt) { // КУПОЛ
                            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 2f, 0.5f);
                            for (int i = 0; i < 360; i += 12) {
                                double rad = Math.toRadians(i);
                                for (double h = 0; h < 4; h += 0.8) {
                                    world.spawnParticles(VOODOO_SYMBOL, player.getX() + Math.cos(rad) * 6, player.getY() + h, player.getZ() + Math.sin(rad) * 6, 1, 0, 0, 0, 0);
                                }
                            }
                            var targets = world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(7.0), e -> e != player);
                            for (LivingEntity e : targets) {
                                Vec3d push = e.getPos().subtract(player.getPos()).normalize().multiply(2.5);
                                e.addVelocity(push.x, 0.4, push.z);
                                e.velocityModified = true;
                            }
                        } else { // Вуду-атака
                            var hit = player.raycast(15.0, 0, false);
                            world.spawnParticles(VOODOO_SYMBOL, hit.getPos().x, hit.getPos().y, hit.getPos().z, 15, 0.2, 0.2, 0.2, 0.1);
                        }
                        break;

                    case 11: // Кнопка V
                        if (hasUlt) { // ТІНЬОВИЙ РОЗРИВ
                            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.5f, 0.5f);
                            Vec3d ultDash = player.getRotationVector().multiply(4.0);
                            player.addVelocity(ultDash.x, 0.3, ultDash.z);
                            player.velocityModified = true;
                            var victims = world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(6.0), e -> e != player);
                            for (LivingEntity victim : victims) {
                                victim.damage(world.getDamageSources().magic(), 20.0f);
                                victim.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
                                world.spawnParticles(ParticleTypes.LARGE_SMOKE, victim.getX(), victim.getY() + 1, victim.getZ(), 10, 0.2, 0.5, 0.2, 0.1);
                            }
                        } else { // РАДІО-ГЛЮК (Dash + Invisibility)
                            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, SoundCategory.PLAYERS, 1.0f, 1.5f);
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 40, 0, false, false));
                            Vec3d dash = player.getRotationVector().multiply(2.5);
                            player.addVelocity(dash.x, 0.2, dash.z);
                            player.velocityModified = true;
                        }
                        break;

                    case 12: // Кнопка B
                        if (hasUlt) { // РАДІО-ВИБУХ (Beam)
                            Vec3d look = player.getRotationVector();
                            for (int i = 0; i < 25; i++) {
                                Vec3d p = player.getEyePos().add(look.multiply(i));
                                world.spawnParticles(VOODOO_SYMBOL, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.05);
                                if (i % 5 == 0) world.createExplosion(player, p.x, p.y, p.z, 2.0f, World.ExplosionSourceType.NONE);
                            }
                        } else { // Заморозка
                            var targets = world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(5.0), e -> e != player);
                            for (LivingEntity target : targets) {
                                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 5));
                                world.spawnParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 1, target.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                            }
                        }
                        break;

                    case 13: // Кнопка N
                        performExplosion(player, world, hasUlt);
                        break;
                }
            });
        });
    }

    private void toggleUlt(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        if (ULT_PLAYERS.contains(uuid)) {
            ULT_PLAYERS.remove(uuid);
            ServerPlayNetworking.send(player, new AlastorPayload(false));
            player.sendMessage(Text.literal("§7Трансляцію завершено."), true);
        } else {
            ULT_PLAYERS.add(uuid);
            ServerPlayNetworking.send(player, new AlastorPayload(true));
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0f, 0.5f);
            player.sendMessage(Text.literal("§c§lОТРИМАНО ПОВНИЙ ДОСТУП"), true);
        }
    }

    private void applyRadioInterference(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        var targets = world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(5.0), e -> e != player);
        for (LivingEntity target : targets) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0, false, false));
            world.spawnParticles(ParticleTypes.WHITE_ASH, target.getX(), target.getEyeY(), target.getZ(), 4, 0.1, 0.1, 0.1, 0.05);
            world.spawnParticles(ParticleTypes.SQUID_INK, target.getX(), target.getEyeY(), target.getZ(), 2, 0.1, 0.1, 0.1, 0.01);
        }
    }

    private void performExplosion(ServerPlayerEntity player, ServerWorld world, boolean amplified) {
        double radius = amplified ? 12.0 : 8.0;
        float damage = amplified ? 25.0f : 14.0f;
        world.spawnParticles(VOODOO_SYMBOL, player.getX(), player.getY() + 1, player.getZ(), amplified ? 100 : 60, 0.5, 0.5, 0.5, 0.7);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 1.2f, 0.6f);

        var targets = world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), e -> e != player);
        for (LivingEntity target : targets) {
            target.damage(world.getDamageSources().magic(), damage);
            Vec3d launch = target.getPos().subtract(player.getPos()).normalize().multiply( amplified ? 3.5 : 2.0);
            target.addVelocity(launch.x, 0.6, launch.z);
            target.velocityModified = true;
        }
    }

    // Допоміжні методи для чистоти коду
    private void spawnTentacles(ServerPlayerEntity player, ServerWorld world) {
        var hit = player.raycast(12.0, 0, false);
        for (int i = 0; i < 3; i++) {
            TentacleEntity t = new TentacleEntity(TENTACLE, world);
            double ang = Math.toRadians(i * 120);
            t.refreshPositionAndAngles(hit.getPos().x + Math.cos(ang)*1.5, hit.getPos().y, hit.getPos().z + Math.sin(ang)*1.5, 0, 0);
            world.spawnEntity(t);
        }
    }

    private void handleShadow(ServerPlayerEntity player, ServerWorld world, UUID uuid) {
        if (ACTIVE_SHADOWS.containsKey(uuid)) {
            ACTIVE_SHADOWS.get(uuid).startExit();
        } else {
            ShadowEntity s = new ShadowEntity(SHADOW, world);
            s.setOwner(player);
            s.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0);
            world.spawnEntity(s);
            ACTIVE_SHADOWS.put(uuid, s);
        }
    }

    private void spawnMinions(ServerPlayerEntity player, ServerWorld world) {
        for (int i = 0; i < 3; i++) {
            MinionEntity m = new MinionEntity(MINION, world);
            m.refreshPositionAndAngles(player.getX() + (i - 1), player.getY(), player.getZ() + 1, 0, 0);
            m.setOwner(player);
            world.spawnEntity(m);
        }
    }
}