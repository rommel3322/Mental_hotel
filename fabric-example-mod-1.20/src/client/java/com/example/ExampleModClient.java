package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
    private static final Identifier HUD_TEXTURE = Identifier.of("example", "textures/gui/alastor_hud.png");
    public static int selectedSkill = 0;
    public static boolean isAlastor = false;
    public static boolean isUltActive = false; // Змінна для ульти
    public static KeyBinding keyC,keyV,keyB,keyN;

    @Override
    public void onInitializeClient() {

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                // Створюємо модель рогів
                AlastorHornsModel hornsModel = new AlastorHornsModel(AlastorHornsModel.getTexturedModelData().createModel());

                // Додаємо шар до рендерера гравця
                registrationHelper.register(new AlastorHornsLayer(playerRenderer, hornsModel));
            }
        });

        // Реєстрація рендерів
        EntityRendererRegistry.register(ExampleMod.TENTACLE, TentacleRenderer::new);
        EntityRendererRegistry.register(ExampleMod.SHADOW, ShadowRenderer::new);
        EntityRendererRegistry.register(ExampleMod.MINION, MinionRenderer::new);
        ParticleFactoryRegistry.getInstance().register(ExampleMod.VOODOO_SYMBOL, VoodooParticle.Factory::new);


        KeyBinding switchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.example.switch", GLFW.GLFW_KEY_Z, "category.alastor"));
        KeyBinding useKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.example.use", GLFW.GLFW_KEY_X, "category.alastor"));
        keyC = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.alastor.magic1", GLFW.GLFW_KEY_C, "category.alastor"));
        keyV = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.alastor.magic2", GLFW.GLFW_KEY_V, "category.alastor"));
        keyB = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.alastor.magic3", GLFW.GLFW_KEY_B, "category.alastor"));
        keyN = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.alastor.magic4", GLFW.GLFW_KEY_N, "category.alastor"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (switchKey.wasPressed()) {
                selectedSkill = (selectedSkill + 1) % 4;
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.5f);
            }
            if (useKey.wasPressed() && isAlastor) {
                ClientPlayNetworking.send(new SkillPayload(selectedSkill));
            }
            if (selectedSkill == 3 && isAlastor) { // 4-й слот
                if (keyC.wasPressed()) ClientPlayNetworking.send(new SkillPayload(10));
                if (keyV.wasPressed()) ClientPlayNetworking.send(new SkillPayload(11));
                if (keyB.wasPressed()) ClientPlayNetworking.send(new SkillPayload(12));
                if (keyN.wasPressed()) ClientPlayNetworking.send(new SkillPayload(13));
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!isAlastor) return;
            drawContext.drawTexture(HUD_TEXTURE, 10, 10, 20, 80, 0, 0, 64, 256, 256, 256);
            drawContext.fill(10, 10 + (selectedSkill * 20), 30, 11 + (selectedSkill * 20), 0xFFFFFFFF);
        });

        ClientPlayNetworking.registerGlobalReceiver(AlastorPayload.ID, (payload, context) -> {
            context.client().execute(() -> isAlastor = payload.transform());
        });
    }
}