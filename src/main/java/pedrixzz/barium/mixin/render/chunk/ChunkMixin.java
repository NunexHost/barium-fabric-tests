package pedrixzz.barium.mixin,render.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Shadow private WorldChunk worldChunk;

    @Shadow public abstract int getHeight(int x, int z);

    // Lista para armazenar os blocos modificados neste chunk
    private List<BlockPos> modifiedBlocks = new ArrayList<>();

    /**
     * Injeta o código para otimizar o Chunk.setBlockState
     * 
     * @param pos  Posição do bloco a ser modificado
     * @param state Novo estado do bloco
     * @param ci   CallbackInfo
     */
    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", shift = At.Shift.AFTER))
    private void onSetBlockState(BlockPos pos, BlockState state, CallbackInfo ci) {
        // Adiciona o bloco modificado à lista
        modifiedBlocks.add(pos);
    }

    /**
     * Injeta o código para otimizar o Chunk.markDirty
     * 
     * @param ci   CallbackInfo
     */
    @Inject(method = "markDirty", at = @At("TAIL"))
    private void onMarkDirty(CallbackInfo ci) {
        // Verifica se há blocos modificados
        if (!modifiedBlocks.isEmpty()) {
            // Itera sobre os blocos modificados
            for (BlockPos pos : modifiedBlocks) {
                // Verifica se o bloco está dentro do limite do chunk
                if (pos.getX() >= 0 && pos.getX() < 16 && pos.getZ() >= 0 && pos.getZ() < 16) {
                    // Calcula a altura do bloco
                    int height = getHeight(pos.getX(), pos.getZ());
                    // Verifica se o bloco está acima do nível do mar
                    if (pos.getY() >= height) {
                        // Atualiza o WorldChunk com o novo estado do bloco
                        worldChunk.setBlockState(pos, worldChunk.getBlockState(pos), 0);
                    }
                }
            }
            // Limpa a lista de blocos modificados
            modifiedBlocks.clear();
        }
    }
}
