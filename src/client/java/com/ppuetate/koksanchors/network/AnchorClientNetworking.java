package com.ppuetate.koksanchors.network;

import com.ppuetate.koksanchors.KoKsAnchors;
import com.ppuetate.koksanchors.config.AnchorConfigManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Client-only handlers for custom S2C anchor sync payloads.
 */
public final class AnchorClientNetworking {

    private AnchorClientNetworking() {}

    public static void registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(
            AnchorNetworking.AnchorStateConfirmPayload.ID,
            (payload, context) -> context.client().execute(() -> {
                if (context.client().world == null) {
                    return;
                }
                if (!AnchorConfigManager.current().antiGhostEnabled()) {
                    return;
                }

                BlockPos pos = payload.pos();
                BlockState currentState = context.client().world.getBlockState(pos);
                if (!currentState.isOf(Blocks.RESPAWN_ANCHOR)) {
                    return;
                }

                int currentCharges = currentState.get(RespawnAnchorBlock.CHARGES);
                int targetCharges = payload.charges();
                if (currentCharges == targetCharges) {
                    return;
                }

                BlockState correctedState = currentState.with(RespawnAnchorBlock.CHARGES, targetCharges);
                applyCorrectedState(context.client(), pos, correctedState);

                if (AnchorConfigManager.current().fastRenderUpdate()) {
                    forceChunkRebuild(pos);
                }

                KoKsAnchors.LOGGER.debug(
                    "[KoKs Anchors] Sync confirm: {} charges {} -> {}",
                    pos,
                    currentCharges,
                    targetCharges
                );
            })
        );

        ClientPlayNetworking.registerGlobalReceiver(
            AnchorNetworking.AnchorStateRejectPayload.ID,
            (payload, context) -> context.client().execute(() -> {
                if (context.client().world == null) {
                    return;
                }
                if (!AnchorConfigManager.current().antiGhostEnabled()) {
                    return;
                }

                BlockPos pos = payload.pos();
                int correctCharges = payload.correctCharges();

                if (correctCharges == -1) {
                    if (AnchorConfigManager.current().fastRenderUpdate()) {
                        forceChunkRebuild(pos);
                    }

                    KoKsAnchors.LOGGER.debug(
                        "[KoKs Anchors] Reject: anchor en {} no existe en servidor",
                        pos
                    );
                    return;
                }

                BlockState currentState = context.client().world.getBlockState(pos);
                if (!currentState.isOf(Blocks.RESPAWN_ANCHOR)) {
                    return;
                }

                BlockState correctedState = currentState.with(RespawnAnchorBlock.CHARGES, correctCharges);
                applyCorrectedState(context.client(), pos, correctedState);

                if (AnchorConfigManager.current().fastRenderUpdate()) {
                    forceChunkRebuild(pos);
                }

                KoKsAnchors.LOGGER.debug(
                    "[KoKs Anchors] Reject: corregido {} a charges {}",
                    pos,
                    correctCharges
                );
            })
        );

        KoKsAnchors.LOGGER.debug("[KoKs Anchors] Client handlers registrados");
    }

    private static void applyCorrectedState(MinecraftClient client, BlockPos pos, BlockState correctedState) {
        if (client.world == null) {
            return;
        }

        // 3 = notify neighbors + listeners, 2 = listeners only.
        int updateFlags = AnchorConfigManager.current().suppressNeighborUpdates() ? 2 : 3;
        client.world.setBlockState(pos, correctedState, updateFlags);
    }

    private static void forceChunkRebuild(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.worldRenderer != null) {
            int sectionX = pos.getX() >> 4;
            int sectionY = pos.getY() >> 4;
            int sectionZ = pos.getZ() >> 4;
            client.worldRenderer.scheduleBlockRenders(
                sectionX, sectionY, sectionZ,
                sectionX, sectionY, sectionZ
            );
        }
    }
}
