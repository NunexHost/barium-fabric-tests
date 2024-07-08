package pedrixzz.barium.client.render.chunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;

import static net.minecraft.client.render.RenderLayer.SOLID; // Importando SOLID aqui, pois o acesso é privado

@Environment(EnvType.CLIENT)
public class ChunkRenderer {

    private final ChunkPos chunkPos;
    private final BlockView world;
    private final WorldRenderer worldRenderer;

    public ChunkRenderer(ChunkPos chunkPos, BlockView world, WorldRenderer worldRenderer) {
        this.chunkPos = chunkPos;
        this.world = world;
        this.worldRenderer = worldRenderer;
    }

    public void render(MatrixStack matrixStack, float tickDelta) {
        // Otimização 1: Renderizar apenas chunks visíveis
        if (!MinecraftClient.getInstance().worldRenderer.getChunkRenderDispatcher().getVisibleChunks().contains(chunkPos)) {
            return;
        }

        // Otimização 2: Renderizar apenas blocos que são visíveis
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos blockPos = new BlockPos(chunkPos.x * 16 + x, y, chunkPos.z * 16 + z);
                    BlockState blockState = world.getBlockState(blockPos);

                    // Otimização 3: Usar RenderLayer.SOLID para blocos opacos
                    if (blockState.isOpaque() && MinecraftClient.getInstance().getBlockRenderManager().getRenderLayer(blockState) == SOLID) {
                        worldRenderer.renderBlock(blockState, blockPos, matrixStack, tickDelta);
                    }
                }
            }
        }
    }
}
