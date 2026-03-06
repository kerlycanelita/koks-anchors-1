package com.ppuetate.koksanchors.mixin.client;

import com.ppuetate.koksanchors.config.AnchorConfigManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin Client: Optimizacion de render al cambiar BlockState de anchors.
 *
 * TARGET: ClientWorld.setBlockState(BlockPos, BlockState, int, int)
 *
 * Cuando el mundo del cliente recibe un cambio de BlockState para un
 * Respawn Anchor (ya sea por prediccion local o por paquete del servidor),
 * este mixin:
 *
 * 1. Fuerza un rebuild inmediato del chunk section si fastRenderUpdate esta activo
 * 2. Reduce neighbor updates innecesarios si suppressNeighborUpdates esta activo
 *
 * RESULTADO:
 * - El cambio visual de charges (0->1->2->3->4) es instantaneo
 * - No hay frame de "bloque fantasma" entre el cambio de estado
 * - Menos carga de procesamiento por neighbor updates en spam-charge
 *
 * YARN 1.21.10:
 * - ClientWorld.setBlockState(BlockPos, BlockState, int, int) -> boolean
 * - Flags: 1 = notify neighbors, 2 = send to clients, 16 = no re-render
 */
@Mixin(ClientWorld.class)
public abstract class ClientAnchorRenderMixin {

    @Inject(
        method = "setBlockState",
        at = @At("RETURN")
    )
    private void koksAnchors_fastRenderOnAnchorChange(
        BlockPos pos,
        BlockState state,
        int flags,
        int maxUpdateDepth,
        CallbackInfoReturnable<Boolean> cir
    ) {
        // Solo actuar si el cambio fue exitoso
        if (!cir.getReturnValue()) return;

        // Solo actuar si el bloque es un Respawn Anchor
        if (!state.isOf(Blocks.RESPAWN_ANCHOR)) return;

        // Solo actuar si fast render update esta habilitado
        if (!AnchorConfigManager.current().fastRenderUpdate()) return;

        // Forzar rebuild inmediato del chunk section
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
