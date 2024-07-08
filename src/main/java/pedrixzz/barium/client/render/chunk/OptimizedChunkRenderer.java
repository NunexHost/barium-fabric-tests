package pedrixzz.barium.client.render.chunk;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizedChunkRenderer {

    private final Map<ChunkPos, ChunkRendererRegion> chunkRenderers = new HashMap<>();
    private final List<ChunkPos> visibleChunks = new ArrayList<>();
    private final ChunkRendererRegionPool rendererPool = new ChunkRendererRegionPool();
    private final Thread renderThread = new Thread(this::renderChunksInBackground);

    public OptimizedChunkRenderer() {
        renderThread.start();
    }

    public void renderChunk(WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        ChunkRendererRegion renderer = chunkRenderers.get(chunkPos);
        if (renderer == null) {
            renderer = rendererPool.acquireRenderer(chunk);
            chunkRenderers.put(chunkPos, renderer);
        }
        renderer.renderChunk(chunk);
    }

    public void updateVisibleChunks(Collection<ChunkPos> newVisibleChunks) {
        visibleChunks.clear();
        visibleChunks.addAll(newVisibleChunks);
        renderThread.interrupt();
    }

    private void renderChunksInBackground() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignorar interrupções, pois isso indica uma atualização
            }
            for (ChunkPos chunkPos : visibleChunks) {
                ChunkRendererRegion renderer = chunkRenderers.get(chunkPos);
                if (renderer != null) {
                    renderer.renderChunk(null);
                }
            }
        }
    }

    private static class ChunkRendererRegionPool {
        private final List<ChunkRendererRegion> pool = new ArrayList<>();

        public ChunkRendererRegion acquireRenderer(WorldChunk chunk) {
            if (!pool.isEmpty()) {
                return pool.remove(0);
            }
            return new ChunkRendererRegion(new ChunkBuilder(chunk));
        }

        public void releaseRenderer(ChunkRendererRegion renderer) {
            pool.add(renderer);
        }
    }
}
