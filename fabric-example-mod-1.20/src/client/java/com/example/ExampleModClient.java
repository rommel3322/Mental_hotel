package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
    private static final Identifier HUD_TEXTURE = Identifier.of("example", "textures/gui/alastor_hud.png");
    public static int selectedSkill = 0;
    public static boolean isAlastor = false;

    @Override
    public void onInitializeClient() {
        // Реєстрація рендерів
        EntityRendererRegistry.register(ExampleMod.TENTACLE, TentacleRenderer::new);
        EntityRendererRegistry.register(ExampleMod.SHADOW, ShadowRenderer::new);
        EntityRendererRegistry.register(ExampleMod.MINION, MinionRenderer::new);

        KeyBinding switchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.example.switch", GLFW.GLFW_KEY_Z, "category.alastor"));
        KeyBinding useKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.example.use", GLFW.GLFW_KEY_X, "category.alastor"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (switchKey.wasPressed()) {
                selectedSkill = (selectedSkill + 1) % 4;
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.5f);
            }
            if (useKey.wasPressed() && isAlastor) {
                ClientPlayNetworking.send(new SkillPayload(selectedSkill));
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