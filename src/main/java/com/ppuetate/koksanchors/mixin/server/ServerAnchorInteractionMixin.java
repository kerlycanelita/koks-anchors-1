package com.ppuetate.koksanchors.mixin.server;

import com.ppuetate.koksanchors.KoKsAnchors;
import com.ppuetate.koksanchors.config.AnchorConfigManager;
import com.ppuetate.koksanchors.network.AnchorNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mixin Server: Validacion y confirmacion de carga de Respawn Anchors.
 *
 * TARGET: RespawnAnchorBlock.onUse(...)
 *
 * Este mixin intercepta la interaccion onUse del Respawn Anchor en el servidor
 * para:
 *
 * 1. Aplicar debounce server-side en la carga con glowstone
 *    - Previene spam de paquetes de carga que causan desincronizacion
 *    - Cada jugador tiene su propio tracker de ultima carga
 *
 * 2. Enviar confirmacion al cliente despues de una carga exitosa
 *    - El cliente recibe el estado correcto de charges inmediatamente
 *    - No necesita esperar el chunk update vanilla
 *
 * 3. Enviar rechazo si la carga fallo validacion
 *    - El cliente revierte el estado predicho al estado real del servidor
 *
 * SEGURIDAD:
 * - Solo actua en el servidor (check world.isClient)
 * - Solo modifica comportamiento para ServerPlayerEntity
 * - El debounce server-side es la autoridad final
 * - No modifica la logica vanilla de carga, solo agrega sync + debounce
 *
 * YARN 1.21.10:
 * - RespawnAnchorBlock.onUse(BlockState, World, BlockPos, PlayerEntity, BlockHitResult)
 * - RespawnAnchorBlock.CHARGES -> IntProperty (0-4)
 * - RespawnAnchorBlock.canCharge(BlockState) -> boolean
 */
@Mixin(RespawnAnchorBlock.class)
public abstract class ServerAnchorInteractionMixin {

    /**
     * Mapa de debounce server-side por jugador.
     * Key: UUID del jugador, Value: timestamp de ultima carga.
     */
    @Unique
    private static final Map<UUID, Long> koksAnchors_chargeDebounce = new ConcurrentHashMap<>();

    @Inject(
        method = "onUse",
        at = @At("HEAD"),
        cancellable = true
    )
    private void koksAnchors_debounceCharge(
        BlockState state,
        World world,
        BlockPos pos,
        PlayerEntity player,
        BlockHitResult hit,
        CallbackInfoReturnable<ActionResult> cir
    ) {
        // Solo actuar en el servidor
        if (world.isClient()) return;

        // Solo para jugadores reales del servidor
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        // Solo aplicar debounce cuando el jugador tiene glowstone
        // y el anchor puede ser cargado
        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);

        boolean hasGlowstone = mainHand.isOf(Items.GLOWSTONE) || offHand.isOf(Items.GLOWSTONE);
        boolean canCharge = state.contains(RespawnAnchorBlock.CHARGES)
            && state.get(RespawnAnchorBlock.CHARGES) < 4;

        if (!hasGlowstone || !canCharge) return;

        // Aplicar debounce server-side
        int debounceMs = AnchorConfigManager.current().chargeDebounceMs();
        if (debounceMs > 0) {
            long now = System.currentTimeMillis();
            Long lastCharge = koksAnchors_chargeDebounce.get(player.getUuid());

            if (lastCharge != null && (now - lastCharge) < debounceMs) {
                // Debounce activo: rechazar y enviar estado correcto al cliente
                int currentCharges = state.get(RespawnAnchorBlock.CHARGES);
                AnchorNetworking.sendRejectToClient(serverPlayer, pos, currentCharges);
                cir.setReturnValue(ActionResult.FAIL);
                return;
            }

            koksAnchors_chargeDebounce.put(player.getUuid(), now);
        }
    }

    @Inject(
        method = "onUse",
        at = @At("RETURN")
    )
    private void koksAnchors_confirmChargeResult(
        BlockState state,
        World world,
        BlockPos pos,
        PlayerEntity player,
        BlockHitResult hit,
        CallbackInfoReturnable<ActionResult> cir
    ) {
        // Solo actuar en el servidor
        if (world.isClient()) return;

        // Solo para jugadores reales del servidor
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        // Despues de onUse, leer el estado actual del bloque en el servidor
        BlockState currentState = world.getBlockState(pos);
        if (!currentState.contains(RespawnAnchorBlock.CHARGES)) return;

        int charges = currentState.get(RespawnAnchorBlock.CHARGES);

        // Enviar confirmacion al cliente con el estado real
        AnchorNetworking.sendConfirmToClient(serverPlayer, pos, charges);
    }
}
