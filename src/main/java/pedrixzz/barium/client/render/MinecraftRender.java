package pedrixzz.barium.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.light.LightSourceView;

import java.util.*;

public class MinecraftRender {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final int RENDER_DISTANCE = 10; // Distância de renderização

    private final Set<ChunkPos> renderedChunks = Sets.newHashSet();
    private final Map<ChunkPos, Chunk> chunks = Maps.newHashMap(); 

    public void renderWorld(MatrixStack matrices, float tickDelta) {
        World world = CLIENT.world;
        if (world == null) return;

        // Calcula o centro do jogador
        BlockPos playerPos = CLIENT.player.getBlockPos();
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;

        // Limpa os chunks renderizados
        renderedChunks.clear();

        // Cria um buffer para guardar os chunks a renderizar
        List<ChunkPos> chunksToRender = Lists.newArrayList();

        // Percorre os chunks na distância de renderização
        for (int x = playerChunkX - RENDER_DISTANCE; x <= playerChunkX + RENDER_DISTANCE; x++) {
            for (int z = playerChunkZ - RENDER_DISTANCE; z <= playerChunkZ + RENDER_DISTANCE; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);
                chunksToRender.add(chunkPos);
            }
        }

        // Ordena os chunks por distância ao jogador
        chunksToRender.sort(Comparator.comparingDouble(chunkPos -> {
            double distanceSquared = Math.pow(chunkPos.x - playerChunkX, 2) + Math.pow(chunkPos.z - playerChunkZ, 2);
            return distanceSquared;
        }));

        // Renderiza os chunks em ordem de distância
        for (ChunkPos chunkPos : chunksToRender) {
            renderChunk(matrices, chunkPos, tickDelta);
        }
    }

    private void renderChunk(MatrixStack matrices, ChunkPos chunkPos, float tickDelta) {
        if (renderedChunks.contains(chunkPos)) return;

        // Obtém o Chunk do mundo
        Chunk chunk = CLIENT.world.getChunkManager().getChunk(chunkPos.x, chunkPos.z);
        if (chunk == null) return;

        // Adiciona o Chunk ao mapa
        chunks.put(chunkPos, chunk);

        // Renderiza o chunk
        renderChunk(matrices, chunk, tickDelta);
        renderedChunks.add(chunkPos);
    }

    // Método para renderizar o chunk usando WorldRenderer
    private void renderChunk(MatrixStack matrices, Chunk chunk, float tickDelta) {
        World world = CLIENT.world;
        VertexConsumerProvider.Immediate immediate = CLIENT.getBufferBuilders().getEntityVertexConsumers();
        WorldRenderer.renderChunk(world, matrices, immediate, chunk, tickDelta, false, false, false);
    }
}
