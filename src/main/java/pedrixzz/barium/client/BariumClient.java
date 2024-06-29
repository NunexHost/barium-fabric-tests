package pedrixzz.barium.client;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.ClientWorldTickCallback;
import net.fabricmc.fabric.api.client.rendering.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;

public class ChunkOptimizer implements ModInitializer {

    private static final int CHUNK_LOADING_DISTANCE = 10; // Distância de carregamento
    private static final int PRE_GENERATION_RADIUS = 5; // Raio de pré-geração
    private static final Map<Long, Chunk> chunkCache = new HashMap<>(); // Cache de chunks

    @Override
    public void onInitialize() {
        ClientWorldTickCallback.EVENT.register(this::onWorldTick);
        WorldRenderEvents.AFTER_WORLD_RENDERING.register(this::onWorldRender);
    }

    private void onWorldTick(ClientWorld world) {
        // Pré-gera chunks ao redor do jogador
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
        for (int x = -CHUNK_LOADING_DISTANCE - PRE_GENERATION_RADIUS; x <= CHUNK_LOADING_DISTANCE + PRE_GENERATION_RADIUS; x++) {
            for (int z = -CHUNK_LOADING_DISTANCE - PRE_GENERATION_RADIUS; z <= CHUNK_LOADING_DISTANCE + PRE_GENERATION_RADIUS; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);
                if (Math.abs(x - playerPos.getX()) > CHUNK_LOADING_DISTANCE ||
                        Math.abs(z - playerPos.getZ()) > CHUNK_LOADING_DISTANCE) {
                    continue; // Se o chunk está muito longe, não pré-gera
                }
                Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
                // Processa o chunk para renderização (pode incluir compressão, etc.)
                processChunk(chunk);
            }
        }
    }

    private void onWorldRender(WorldRenderContext context) {
        ClientWorld world = context.world();
        // Verifica se o chunk está no cache antes de carregar
        for (int x = -CHUNK_LOADING_DISTANCE; x <= CHUNK_LOADING_DISTANCE; x++) {
            for (int z = -CHUNK_LOADING_DISTANCE; z <= CHUNK_LOADING_DISTANCE; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);
                long chunkKey = getChunkKey(chunkPos);
                Chunk cachedChunk = chunkCache.get(chunkKey);
                if (cachedChunk != null) {
                    world.setChunk(chunkPos.x, chunkPos.z, cachedChunk);
                    continue;
                }
                Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
                // Adiciona o chunk ao cache após o carregamento
                chunkCache.put(chunkKey, chunk);
            }
        }
    }

    private void processChunk(Chunk chunk) {
        // Código para processar o chunk (ex.: compressão, otimização)
        // Exemplo de compressão com GZIP (será necessário adicionar dependência):
        try {
            byte[] compressedData = gzip(chunk.getChunkData());
            chunk.setChunkData(compressedData);
        } catch (Exception e) {
            // Tratar exceções
        }
    }

    private long getChunkKey(ChunkPos chunkPos) {
        return ((long) chunkPos.x & 0xFFFFFFFFL) << 32 | (long) chunkPos.z & 0xFFFFFFFFL;
    }

    // Método gzip (para compressão de dados) - será necessário adicionar dependência
    private static byte[] gzip(byte[] data) {
        // Código para compressão com GZIP
        return new byte[0];
    }
}
