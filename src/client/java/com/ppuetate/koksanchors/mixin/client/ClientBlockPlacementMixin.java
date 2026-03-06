package com.ppuetate.koksanchors.mixin.client;

import com.ppuetate.koksanchors.config.AnchorConfigManager;
import com.ppuetate.koksanchors.network.AnchorDebounceTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin Client: Debounce de colocacion y carga de Respawn Anchors.
 *
 * TARGET: ClientPlayerInteractionManager.interactBlock(...)
 *
 * Este mixin intercepta interacciones de bloque en el lado del cliente
 * ANTES de que se envie el paquete al servidor. Aplica debounce para:
 *
 * 1. Colocacion de anchor: Si el jugador tiene un Respawn Anchor en mano
 *    y hace spam click, el debounce evita enviar multiples paquetes de
 *    colocacion que causarian ghost blocks.
 *
 * 2. Carga con glowstone: Si el jugador interactua con un anchor existente
 *    con glowstone en mano, el debounce evita cargas multiples.
 *
 * SEGURIDAD:
 * - Solo cancela la interaccion client-side (retorna PASS para evitar envio)
 * - El servidor siempre tiene la autoridad final
 * - No modifica ningun dato del mundo, solo la decision de enviar el packet
 *
 * YARN 1.21.10:
 * - ClientPlayerInteractionManager.interactBlock(ClientPlayerEntity, Hand, BlockHitResult)
 * - Retorna ActionResult
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientBlockPlacementMixin {

    @Inject(
        method = "interactBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private void koksAnchors_debounceAnchorInteraction(
        ClientPlayerEntity player,
        Hand hand,
        BlockHitResult hitResult,
        CallbackInfoReturnable<ActionResult> cir
    ) {
        if (player == null || hitResult == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        ItemStack heldStack = player.getStackInHand(hand);
        BlockPos targetPos = hitResult.getBlockPos();
        BlockState targetState = client.world.getBlockState(targetPos);

        // ── Caso 1: Carga de anchor existente con glowstone ──
        if (targetState.isOf(Blocks.RESPAWN_ANCHOR) && heldStack.isOf(Items.GLOWSTONE)) {
            if (!AnchorDebounceTracker.canCharge()) {
                // Debounce activo: cancelar envio del paquete
                cir.setReturnValue(ActionResult.PASS);
                return;
            }
            // Permitir la interaccion, el tracker ya marco el timestamp
            return;
        }

        // ── Caso 2: Colocacion de nuevo anchor ──
        if (heldStack.isOf(Items.RESPAWN_ANCHOR)) {
            if (!AnchorDebounceTracker.canPlace()) {
                // Debounce activo: cancelar envio del paquete
                cir.setReturnValue(ActionResult.PASS);
                return;
            }
            // Permitir la colocacion, el tracker ya marco el timestamp
        }
    }
}
