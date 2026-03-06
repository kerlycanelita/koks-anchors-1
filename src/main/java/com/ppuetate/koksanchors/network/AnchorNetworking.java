package com.ppuetate.koksanchors.network;

import com.ppuetate.koksanchors.KoKsAnchors;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Common networking utilities shared by client and server code.
 */
public final class AnchorNetworking {

    private AnchorNetworking() {}

    public record AnchorStateConfirmPayload(BlockPos pos, int charges) implements CustomPayload {

        public static final Id<AnchorStateConfirmPayload> ID =
            new Id<>(Identifier.of(KoKsAnchors.MOD_ID, "anchor_confirm"));

        public static final PacketCodec<RegistryByteBuf, AnchorStateConfirmPayload> CODEC =
            PacketCodec.tuple(
                BlockPos.PACKET_CODEC,
                AnchorStateConfirmPayload::pos,
                PacketCodecs.INTEGER,
                AnchorStateConfirmPayload::charges,
                AnchorStateConfirmPayload::new
            );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record AnchorStateRejectPayload(BlockPos pos, int correctCharges) implements CustomPayload {

        public static final Id<AnchorStateRejectPayload> ID =
            new Id<>(Identifier.of(KoKsAnchors.MOD_ID, "anchor_reject"));

        public static final PacketCodec<RegistryByteBuf, AnchorStateRejectPayload> CODEC =
            PacketCodec.tuple(
                BlockPos.PACKET_CODEC,
                AnchorStateRejectPayload::pos,
                PacketCodecs.INTEGER,
                AnchorStateRejectPayload::correctCharges,
                AnchorStateRejectPayload::new
            );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(AnchorStateConfirmPayload.ID, AnchorStateConfirmPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnchorStateRejectPayload.ID, AnchorStateRejectPayload.CODEC);
        KoKsAnchors.LOGGER.debug("[KoKs Anchors] Payloads de red registrados");
    }

    public static void registerServerHandlers() {
        // Vanilla block interaction is used; server mixins send the custom payloads.
        KoKsAnchors.LOGGER.debug("[KoKs Anchors] Server handlers registrados (via mixins)");
    }

    public static void sendConfirmToClient(ServerPlayerEntity player, BlockPos pos, int charges) {
        if (ServerPlayNetworking.canSend(player, AnchorStateConfirmPayload.ID)) {
            ServerPlayNetworking.send(player, new AnchorStateConfirmPayload(pos, charges));
        }
    }

    public static void sendRejectToClient(ServerPlayerEntity player, BlockPos pos, int correctCharges) {
        if (ServerPlayNetworking.canSend(player, AnchorStateRejectPayload.ID)) {
            ServerPlayNetworking.send(player, new AnchorStateRejectPayload(pos, correctCharges));
        }
    }
}
