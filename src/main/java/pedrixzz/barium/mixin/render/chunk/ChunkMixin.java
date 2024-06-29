package pedrixzz.barium.mixin.render.chunk;

import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Inject(method = "setLight(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/chunk/light/LightingProvider;Lnet/minecraft/world/chunk/light/LightingProvider;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/light/LightingProvider;updateChunkLight(Lnet/minecraft/world/chunk/light/LightingProvider;Lnet/minecraft/world/chunk/Chunk;)V"))
    private void optimizeLightUpdate(Chunk chunk, LightingProvider skyLight, LightingProvider blockLight, CallbackInfo ci) {
        // Desabilita a atualização de iluminação se o jogador estiver longe do chunk
        if (chunk.getWorld().getDistanceSqToEntity(chunk.getPos().getCenterX(), chunk.getPos().getCenterZ()) > 16 * 16) {
            ci.cancel();
        }
    }

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V",
            at = @At("HEAD"))
    private void optimizeBlockStateUpdate(BlockPos pos, BlockState state, boolean moved, CallbackInfo ci) {
        // Desabilita a atualização do bloco se o jogador estiver longe do chunk
        if (chunk.getWorld().getDistanceSqToEntity(pos.getX(), pos.getZ()) > 16 * 16) {
            ci.cancel();
        }
    }

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    private void optimizeTickBlockEntities(CallbackInfo ci) {
        // Desabilita a atualização de entidades de bloco se o jogador estiver longe do chunk
        if (chunk.getWorld().getDistanceSqToEntity(chunk.getPos().getCenterX(), chunk.getPos().getCenterZ()) > 16 * 16) {
            ci.cancel();
        }
    }

    // Adicione mais métodos de otimização conforme necessário
}
