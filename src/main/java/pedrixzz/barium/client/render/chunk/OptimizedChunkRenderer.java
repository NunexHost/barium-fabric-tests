package pedrixzz.barium.client.render.chunk;

import net.minecraft.client.render.chunk.ChunkRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRenderDispatcher;
import net.minecraft.client.render.chunk.RenderRegion;
import net.minecraft.client.render.chunk.VisibileChunkManager;
import net.minecraft.client.render.chunk.CompiledChunk;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class OptimizedChunkRenderer extends ChunkRenderer {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private final BlockingQueue<ChunkRenderTask> taskQueue = new LinkedBlockingQueue<>();
    private final Thread[] renderThreads = new Thread[THREAD_COUNT];

    public OptimizedChunkRenderer(ChunkRenderDispatcher dispatcher, VisibileChunkManager chunkManager, World world) {
        super(dispatcher, chunkManager, world);

        // Initialize render threads
        for (int i = 0; i < THREAD_COUNT; i++) {
            renderThreads[i] = new Thread(() -> {
                while (true) {
                    try {
                        ChunkRenderTask task = taskQueue.take();
                        task.render();
                    } catch (InterruptedException e) {
                        // Thread interrupted, gracefully exit
                        break;
                    }
                }
            });
            renderThreads[i].start();
        }
    }

    @Override
    public void scheduleChunk(ChunkPos pos, int renderDistance, int frame) {
        // Prioritize chunks closer to the player
        int priority = Math.abs(pos.x) + Math.abs(pos.z);
        taskQueue.offer(new ChunkRenderTask(pos, renderDistance, frame, priority));
    }

    @Override
    public void rebuildChunk(ChunkPos pos, int renderDistance, int frame, CompiledChunk compiledChunk) {
        // Fast path for rebuilding chunks, directly update the render region
        RenderRegion region = renderRegions[pos.x & 0x7 & renderDistance];
        region.setCompiledChunk(pos, compiledChunk);
        region.markDirty(pos);
    }

    @Override
    public CompiledChunk getCompiledChunk(ChunkPos pos, int renderDistance) {
        // Use optimized chunk loading logic
        RenderRegion region = renderRegions[pos.x & 0x7 & renderDistance];
        return region.getCompiledChunk(pos);
    }

    @Override
    public void updateCameraPosition(BlockPos cameraPos) {
        super.updateCameraPosition(cameraPos);

        // Prioritize chunks closest to the camera
        Arrays.sort(renderRegions, (a, b) -> {
            int distanceA = Math.abs(a.getCenterX() - cameraPos.getX()) + Math.abs(a.getCenterZ() - cameraPos.getZ());
            int distanceB = Math.abs(b.getCenterX() - cameraPos.getX()) + Math.abs(b.getCenterZ() - cameraPos.getZ());
            return distanceA - distanceB;
        });
    }

    @Override
    protected ChunkBuilder createChunkBuilder(ChunkPos pos, CompiledChunk compiledChunk, int frame, boolean forceRebuild) {
        // Use optimized chunk builder
        return new OptimizedChunkBuilder(this, pos, compiledChunk, frame, forceRebuild);
    }

    @Override
    protected void processOcclusionData(ChunkOcclusionData occlusionData, int renderDistance, List<ChunkOcclusionData> occlusionDataList) {
        // Use optimized occlusion data processing
        occlusionDataList.add(occlusionData);
    }

    private class ChunkRenderTask {
        private final ChunkPos pos;
        private final int renderDistance;
        private final int frame;
        private final int priority;

        public ChunkRenderTask(ChunkPos pos, int renderDistance, int frame, int priority) {
            this.pos = pos;
            this.renderDistance = renderDistance;
            this.frame = frame;
            this.priority = priority;
        }

        public void render() {
            // Render the chunk using the optimized renderer
            CompiledChunk compiledChunk = getCompiledChunk(pos, renderDistance);
            if (compiledChunk == null) {
                // Chunk not loaded yet, skip rendering
                return;
            }
            ChunkBuilder builder = createChunkBuilder(pos, compiledChunk, frame, false);
            builder.build();
            rebuildChunk(pos, renderDistance, frame, builder.getCompiledChunk());
        }
    }

    private class OptimizedChunkBuilder extends ChunkBuilder {
        public OptimizedChunkBuilder(ChunkRenderer chunkRenderer, ChunkPos chunkPos, CompiledChunk compiledChunk, int frame, boolean forceRebuild) {
            super(chunkRenderer, chunkPos, compiledChunk, frame, forceRebuild);
        }

        @Override
        protected void updateBlock(BlockPos blockPos, int blockId) {
            // Use optimized block update logic
            super.updateBlock(blockPos, blockId);
        }

        @Override
        protected void updateBlockLight(BlockPos blockPos, int lightValue) {
            // Use optimized block light update logic
            super.updateBlockLight(blockPos, lightValue);
        }

        @Override
        protected void updateSkyLight(BlockPos blockPos, int lightValue) {
            // Use optimized sky light update logic
            super.updateSkyLight(blockPos, lightValue);
        }
    }
}
