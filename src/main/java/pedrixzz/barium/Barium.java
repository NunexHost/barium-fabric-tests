package pedrixzz.barium;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderTickCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncChunkLoading implements ModInitializer {

    private static final Identifier CHUNK_REQUEST_PACKET = new Identifier("barium", "chunk_request");
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Set<ChunkPos> loadingChunks = new HashSet<>();

    @Override
    public void onInitialize() {
        // Registra o listener do evento de renderização do mundo
        WorldRenderEvents.AFTER_WORLD_RENDERING.register(this::onWorldRender);

        // Registra o handler para o pacote de solicitação de chunk
        ServerPlayNetworking.registerGlobalReceiver(CHUNK_REQUEST_PACKET, (client, handler, buf, responseSender) -> {
            ChunkPos chunkPos = buf.readBlockPos().toChunkPos();
            // Adiciona o chunk à lista de chunks para carregamento
            loadingChunks.add(chunkPos);
        });
    }

    private void onWorldRender(WorldRenderContext context) {
        // Verifica se há chunks na fila para carregamento
        if (!loadingChunks.isEmpty()) {
            // Seleciona um chunk para carregamento
            ChunkPos chunkPos = loadingChunks.iterator().next();
            // Remove o chunk da fila
            loadingChunks.remove(chunkPos);

            // Carrega o chunk em uma thread separada
            executor.execute(() -> {
                // Obtém o chunk builder
                ChunkBuilder builder = context.getChunkBuilder();
                // Carrega o chunk usando o chunk builder
                builder.buildChunk(chunkPos.x, chunkPos.z);
                // Atualiza o renderizador do mundo para mostrar o chunk carregado
                WorldRenderer.INSTANCE.updateChunk(chunkPos.x, chunkPos.z);
            });
        }
    }

    // Método para solicitar o carregamento de um chunk
    public static void requestChunk(ChunkPos chunkPos) {
        // Cria um pacote de solicitação de chunk
        PacketByteBuf buf = new PacketByteBuf(new net.minecraft.network.PacketByteBuf(null));
        buf.writeBlockPos(chunkPos.toBlockPos());

        // Envia o pacote para o servidor
        ServerPlayNetworking.send(CHUNK_REQUEST_PACKET, MinecraftClient.getInstance().player, buf);
    }
}
