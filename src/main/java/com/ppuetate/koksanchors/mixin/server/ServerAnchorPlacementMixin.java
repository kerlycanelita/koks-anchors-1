package com.ppuetate.koksanchors.mixin.server;

import com.ppuetate.koksanchors.KoKsAnchors;
import com.ppuetate.koksanchors.config.AnchorConfigManager;
import com.ppuetate.koksanchors.network.AnchorNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mixin Server: Validacion y confirmacion de colocacion de Respawn Anchors.
 *
 * TARGET: Block.onPlaced(...)
 *
 * Este mixin intercepta el momento en que un Respawn Anchor es colocado
 * exitosamente en el servidor para:
 *
 * 1. Aplicar debounce server-side de colocacion
 *    - Si un jugador coloca anchors muy rapido, el servidor rechaza
 *    - Previene ghost blocks por spam-placement
 *
 * 2. Enviar confirmacion inmediata al cliente
 *    - El cliente sabe que la colocacion fue aceptada
 *    - Elimina el estado intermedio de "ghost block"
 *
 * NOTA: onPlaced se llama DESPUES de que el bloque ya fue colocado en el mundo.
 * Si el debounce server-side rechaza, necesitamos REVERTIR el bloque.
 *
 * YARN 1.21.10:
 * - RespawnAnchorBlock hereda de Block
 * - Block.onPlaced(World, BlockPos, BlockState, LivingEntity, ItemStack)
 */
@Mixin(Block.class)
public abstract class ServerAnchorPlacementMixin {

    /**
     * Mapa de debounce server-side por jugador para colocacion.
     */
    @Unique
    private static final Map<UUID, Long> koksAnchors_placementDebounce = new ConcurrentHashMap<>();

    @Inject(
        method = "onPlaced",
        at = @At("HEAD"),
        cancellable = true
    )
    private void koksAnchors_validatePlacement(
        World world,
        BlockPos pos,
        BlockState state,
        LivingEntity placer,
        ItemStack itemStack,
        CallbackInfo ci
    ) {
        // Solo actuar en el servidor
        if (world.isClient()) return;

        // Solo aplicar logica para Respawn Anchors
        if (!state.isOf(Blocks.RESPAWN_ANCHOR)) return;

        // Solo para jugadores reales del servidor
        if (!(placer instanceof ServerPlayerEntity serverPlayer)) return;

        // Aplicar debounce server-side de colocacion
        int debounceMs = AnchorConfigManager.current().placementDebounceMs();
        if (debounceMs > 0) {
            long now = System.currentTimeMillis();
            Long lastPlacement = koksAnchors_placementDebounce.get(placer.getUuid());

            if (lastPlacement != null && (now - lastPlacement) < debounceMs) {
                // Debounce activo: el bloque ya fue colocado por vanilla,
                // asi que necesitamos revertirlo
                world.setBlockState(pos, Blocks.AIR.getDefaultState());

                // Devolver el item al jugador
                if (!serverPlayer.isCreative()) {
                    serverPlayer.getInventory().insertStack(new ItemStack(Blocks.RESPAWN_ANCHOR));
                }

                // Notificar al cliente que la colocacion fue rechazada
                AnchorNetworking.sendRejectToClient(serverPlayer, pos, -1);

                KoKsAnchors.LOGGER.debug(
                    "[KoKs Anchors] Placement rejected (debounce) for {} at {}",
                    serverPlayer.getName().getString(), pos
                );

                ci.cancel();
                return;
            }

            koksAnchors_placementDebounce.put(placer.getUuid(), now);
        }

        // Colocacion aceptada: enviar confirmacion
        int charges = state.get(RespawnAnchorBlock.CHARGES);
        AnchorNetworking.sendConfirmToClient(serverPlayer, pos, charges);

        KoKsAnchors.LOGGER.debug(
            "[KoKs Anchors] Placement confirmed for {} at {} (charges: {})",
            serverPlayer.getName().getString(), pos, charges
        );
    }
}
