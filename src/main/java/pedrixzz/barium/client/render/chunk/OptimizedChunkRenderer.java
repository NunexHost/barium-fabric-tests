package pedrixzz.barium.client.render.chunk;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public class OptimizedChunkRenderer {

    private static final int MAX_VERTICES_PER_CHUNK = 100000; // Adjust based on your hardware

    // Pre-allocated arrays to reduce GC pressure
    private final int[] lightmapColors = new int[65536];
    private final int[] blockColors = new int[65536];
    private final BitSet[] blockSides = new BitSet[65536];

    // Fast access to chunk data
    private final Long2ObjectMap<ChunkData> chunkDataMap = new Long2ObjectOpenHashMap<>();

    // Buffer for vertex data
    private final VertexBuffer vertexBuffer = new VertexBuffer(MAX_VERTICES_PER_CHUNK);

    // Reuse for efficiency
    private final MatrixStack matrixStack = new MatrixStack();

    @Override
    public void renderChunk(Chunk chunk, BlockPos pos, WorldRenderer worldRenderer) {
        // Check if chunk is already processed
        long chunkKey = ChunkPos.toLong(chunk.getPos());
        ChunkData chunkData = chunkDataMap.get(chunkKey);

        // If chunk data is not cached, process it
        if (chunkData == null) {
            chunkData = new ChunkData(chunk);
            chunkDataMap.put(chunkKey, chunkData);
        }

        // Render using optimized methods
        renderChunkData(chunkData, pos, worldRenderer);
    }

    private void renderChunkData(ChunkData chunkData, BlockPos pos, WorldRenderer worldRenderer) {
        // Pre-process chunk data for fast rendering
        preProcessChunkData(chunkData);

        // Render all visible blocks
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < chunkData.maxY; y++) {
                    int blockIndex = chunkData.getIndex(x, y, z);
                    if (chunkData.isBlockVisible(blockIndex)) {
                        renderBlock(chunkData.blockState[blockIndex], chunkData, x, y, z, pos, worldRenderer);
                    }
                }
            }
        }
    }

    private void preProcessChunkData(ChunkData chunkData) {
        // Optimize chunk data for faster rendering
        // 1. Calculate visible block heights
        calculateVisibleHeights(chunkData);
        // 2. Pre-calculate block colors and lightmaps
        calculateBlockColors(chunkData);
        // 3. Determine block side visibility
        calculateBlockSides(chunkData);
    }

    private void calculateVisibleHeights(ChunkData chunkData) {
        // Optimize visible block heights for fast iteration
        chunkData.maxY = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int maxVisibleY = 0;
                for (int y = 255; y >= 0; y--) {
                    int blockIndex = chunkData.getIndex(x, y, z);
                    if (chunkData.blockState[blockIndex] != null) {
                        maxVisibleY = y;
                        break;
                    }
                }
                chunkData.maxY = Math.max(chunkData.maxY, maxVisibleY);
            }
        }
    }

    private void calculateBlockColors(ChunkData chunkData) {
        // Optimize block colors and lightmaps for faster rendering
        BlockView world = MinecraftClient.getInstance().world;
        for (int i = 0; i < chunkData.blockState.length; i++) {
            BlockState state = chunkData.blockState[i];
            if (state != null) {
                chunkData.blockColors[i] = world.getBlockColor(state, chunkData.chunkPos.getStartPos().add(i % 16, i / 256, i / 16 & 15));
                chunkData.lightmapColors[i] = world.getLightmapColor(chunkData.chunkPos.getStartPos().add(i % 16, i / 256, i / 16 & 15));
            }
        }
    }

    private void calculateBlockSides(ChunkData chunkData) {
        // Optimize block side visibility for faster rendering
        for (int i = 0; i < chunkData.blockState.length; i++) {
            BlockState state = chunkData.blockState[i];
            if (state != null) {
                chunkData.blockSides[i] = state.getSides(chunkData.chunk, chunkData.chunkPos.getStartPos().add(i % 16, i / 256, i / 16 & 15));
            }
        }
    }

    private void renderBlock(BlockState state, ChunkData chunkData, int x, int y, int z, BlockPos pos, WorldRenderer worldRenderer) {
        // Optimized block rendering using pre-processed data
        int blockIndex = chunkData.getIndex(x, y, z);
        int lightmapColor = chunkData.lightmapColors[blockIndex];
        int blockColor = chunkData.blockColors[blockIndex];
        BitSet blockSides = chunkData.blockSides[blockIndex];

        // Render visible block sides
        for (int i = 0; i < 6; i++) {
            if (blockSides.get(i)) {
                renderBlockSide(state, chunkData, x, y, z, i, lightmapColor, blockColor, pos, worldRenderer);
            }
        }
    }

    private void renderBlockSide(BlockState state, ChunkData chunkData, int x, int y, int z, int side, int lightmapColor, int blockColor, BlockPos pos, WorldRenderer worldRenderer) {
        // Optimized block side rendering with pre-allocated buffers
        vertexBuffer.begin(RenderSystem.State.RENDER_PASS_START);

        // Add vertex data based on pre-processed data
        state.getRenderShape().tessellateBlock(worldRenderer.getBlockModels(), chunkData.chunk, pos.add(x, y, z), state, vertexBuffer, lightmapColor, blockColor, side, matrixStack.peek().getModel(), 1.0f, 1.0f, 1.0f);

        // Render vertices
        vertexBuffer.end();
    }

    private static class ChunkData {

        private final Chunk chunk;
        private final ChunkPos chunkPos;
        private final BlockState[] blockState;
        private final int[] blockColors;
        private final int[] lightmapColors;
        private final BitSet[] blockSides;

        private int maxY;

        public ChunkData(Chunk chunk) {
            this.chunk = chunk;
            this.chunkPos = chunk.getPos();
            this.blockState = chunk.getBlockStates();
            this.blockColors = new int[blockState.length];
            this.lightmapColors = new int[blockState.length];
            this.blockSides = new BitSet[blockState.length];
        }

        public int getIndex(int x, int y, int z) {
            return (y << 8) | (z << 4) | x;
        }

        public boolean isBlockVisible(int blockIndex) {
            return blockState[blockIndex] != null;
        }
    }
}
