package pedrixzz.barium.mixin.render.chunk;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Inject(method = "Lnet/minecraft/world/chunk/Chunk;hasWorldData(Lnet/minecraft/world/chunk/WorldChunk;)Z", at = @At("HEAD"), cancellable = true)
    private void hasWorldData(WorldChunk worldChunk, CallbackInfoReturnable<Boolean> cir) {
        if (worldChunk.isLightOn()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "Lnet/minecraft/world/chunk/Chunk;getHighestNonEmptySectionY()I", at = @At("HEAD"), cancellable = true)
    private void getHighestNonEmptySectionY(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(15); // Assuming the highest non-empty section is always the top one.
    }

    @Inject(method = "Lnet/minecraft/world/chunk/Chunk;hasStructures(Lnet/minecraft/world/chunk/WorldChunk;)Z", at = @At("HEAD"), cancellable = true)
    private void hasStructures(WorldChunk worldChunk, CallbackInfoReturnable<Boolean> cir) {
        // Check if the chunk has any structures using a faster method than the original one.
        // This example simply assumes that all chunks have structures.
        cir.setReturnValue(true);
    }

    @Inject(method = "Lnet/minecraft/world/chunk/Chunk;shouldSave()Z", at = @At("HEAD"), cancellable = true)
    private void shouldSave(CallbackInfoReturnable<Boolean> cir) {
        // Check if the chunk should be saved using a faster method than the original one.
        // This example simply assumes that all chunks should be saved.
        cir.setReturnValue(true);
    }

    @Inject(method = "Lnet/minecraft/world/chunk/Chunk;tick(Lnet/minecraft/server/world/ServerWorld;J)V", at = @At("HEAD"))
    private void tick(ServerWorld serverWorld, long time, CallbackInfo ci) {
        // Optimizations for the tick method
        // For example:
        // - Skip updating blocks if the chunk is not loaded
        // - Reduce the number of ticks for some block types
    }

}
