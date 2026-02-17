package com.example;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AlastorPayload(boolean transform) implements CustomPayload {
    public static final Id<AlastorPayload> ID = new Id<>(Identifier.of("example", "alastor_transform"));

    // ДОДАЛИ .cast() в кінці, щоб типи зійшлися
    public static final PacketCodec<RegistryByteBuf, AlastorPayload> CODEC = PacketCodecs.BOOL
            .xmap(AlastorPayload::new, AlastorPayload::transform)
            .cast();

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}