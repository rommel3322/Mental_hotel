package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
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

    // Предмети
    public static final Item ALASTOR_CANE = new AlastorCaneItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC));

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
                }
            });
        });
    }
}