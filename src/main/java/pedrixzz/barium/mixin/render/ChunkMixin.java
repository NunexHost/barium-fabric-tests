package pedrixzz.barium.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.chunk.Chunk;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Shadow private boolean loaded;
    @Shadow private int[] heightMap;

    // Cache para evitar cálculo repetido
    private int cachedMaxHeight = -1;

    // Método a ser adicionado
    public void printChunkInfo() {
        if (loaded) {
            System.out.println("Chunk is loaded: (" + this.getX() + ", " + this.getZ() + ")");
        } else {
            System.out.println("Chunk is not loaded: (" + this.getX() + ", " + this.getZ() + ")");
        }
    }

    // Método otimizado para calcular a altura máxima uma vez
    public int getMaxHeight() {
        if (cachedMaxHeight == -1) {
            cachedMaxHeight = calculateMaxHeight();
        }
        return cachedMaxHeight;
    }

    private int calculateMaxHeight() {
        int maxHeight = 0;
        for (int height : heightMap) {
            if (height > maxHeight) {
                maxHeight = height;
            }
        }
        return maxHeight;
    }

    // Métodos shadowed
    @Shadow public abstract int getX();
    @Shadow public abstract int getZ();
}
