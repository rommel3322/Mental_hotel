package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
    private static final Identifier HUD_TEXTURE = Identifier.of("example", "textures/gui/alastor_hud.png");
    public static int selectedSkill = 0;
    public static boolean isAlastor = false;

    // Клавіші
    public static KeyBinding switchKey, useKey, keyR, keyC, keyV, keyB, keyN;

    @Override
    public void onInitializeClient() {
        // 1. РЕНДЕР РОГІВ (Feature Layer)
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                AlastorHornsModel hornsModel = new AlastorHornsModel(AlastorHornsModel.getTexturedModelData().createModel());
                registrationHelper.register(new AlastorHornsLayer(playerRenderer, hornsModel));
            }
        });

        // 2. РЕЄСТРАЦІЯ РЕНДЕРІВ СУТНОСТЕЙ ТА ЧАСТИНОК
        EntityRendererRegistry.register(ExampleMod.TENTACLE, TentacleRenderer::new);
        EntityRendererRegistry.register(ExampleMod.SHADOW, ShadowRenderer::new);
        EntityRendererRegistry.register(ExampleMod.MINION, MinionRenderer::new);
        ParticleFactoryRegistry.getInstance().register(ExampleMod.VOODOO_SYMBOL, VoodooParticle.Factory::new);

        // 3. РЕЄСТРАЦІЯ КЛАВІШ
        switchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.example.switch", GLFW.GLFW_KEY_Z, "category.alastor"));
        useKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.example.use", GLFW.GLFW_KEY_X, "category.alastor"));
        keyR = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.example.ult", GLFW.GLFW_KEY_R, "category.alastor"));

        keyC = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.alastor.magic1", GLFW.GLFW_KEY_C, "category.alastor"));
        keyV = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.alastor.magic2", GLFW.GLFW_KEY_V, "category.alastor"));
        keyB = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.alastor.magic3", GLFW.GLFW_KEY_B, "category.alastor"));
        keyN = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.alastor.magic4", GLFW.GLFW_KEY_N, "category.alastor"));

        // 4. ОБРОБКА НАТИСКАНЬ (Tick)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Перемикання слотів (0, 1, 2, 3)
            while (switchKey.wasPressed()) {
                selectedSkill = (selectedSkill + 1) % 4; // Цикл: 0 -> 1 -> 2 -> 3 -> 0
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.5f);
            }

            if (useKey.wasPressed() && isAlastor) {
                ClientPlayNetworking.send(new SkillPayload(selectedSkill));
            }

            while (keyR.wasPressed() && isAlastor) {
                ClientPlayNetworking.send(new SkillPayload(4)); // ID 4 - перемикач рогів
            }

            if (selectedSkill == 3 && isAlastor) {
                if (keyC.wasPressed()) ClientPlayNetworking.send(new SkillPayload(10));
                if (keyV.wasPressed()) ClientPlayNetworking.send(new SkillPayload(11));
                if (keyB.wasPressed()) ClientPlayNetworking.send(new SkillPayload(12)); // Тепер точно відправить
                if (keyN.wasPressed()) ClientPlayNetworking.send(new SkillPayload(13));
            }
        });

        // 5. МАЛЮВАННЯ HUD
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!isAlastor) return;

            // Малюємо фон HUD
            drawContext.drawTexture(HUD_TEXTURE, 10, 10, 20, 80, 0, 0, 64, 256, 256, 256);

            // Малюємо індикатор вибору
            drawContext.fill(10, 10 + (selectedSkill * 20), 30, 11 + (selectedSkill * 20), 0xFFFFFFFF);

            // Текст-підказка для магії
            if (selectedSkill == 3) {
                String mode = ExampleMod.isUltActive ? "§c§lРЕЖИМ ОВЕРЛОРДА" : "§aРЕЖИМ МАГІЇ";
                drawContext.drawText(MinecraftClient.getInstance().textRenderer, mode, 12, 95, 0xFFFFFF, true);
            }
        });

        // 6. ПРИЙОМ ПАКЕТІВ ВІД СЕРВЕРА
        ClientPlayNetworking.registerGlobalReceiver(AlastorPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                // Сервер присилає true/false. Ми оновлюємо і форму Аластора, і стан ульти.
                isAlastor = true;
                ExampleMod.isUltActive = payload.transform();
            });
        });
    }
}