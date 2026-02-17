package com.example;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SkillPayload(int skillId) implements CustomPayload {
    public static final Id<SkillPayload> ID = new Id<>(Identifier.of("example", "use_skill"));

    // Кодек для передачі цілого числа (ID скіла)
    public static final PacketCodec<RegistryByteBuf, SkillPayload> CODEC = PacketCodecs.INTEGER
            .xmap(SkillPayload::new, SkillPayload::skillId).cast();

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}