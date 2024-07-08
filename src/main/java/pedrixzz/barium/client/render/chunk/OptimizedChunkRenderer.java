package pedrixzz.barium.client.render.chunk;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class OptimizedChunkRenderer extends ChunkRenderer {

    private final BlockRenderManager blockRenderManager;

    // BufferBuilder for each render layer
    private final BufferBuilder[] bufferBuilders;

    // Reuse matrices for each render layer
    private final MatrixStack[] matrixStacks;

    // Pre-allocated arrays for efficient vertex data storage
    private final float[][][] vertexData;
    private final int[][][] vertexCountData;

    public OptimizedChunkRenderer(BlockRenderManager blockRenderManager) {
        super(blockRenderManager);
        this.blockRenderManager = blockRenderManager;

        // Initialize buffer builders and matrix stacks for each render layer
        this.bufferBuilders = new BufferBuilder[RenderLayer.getLayers().size()];
        this.matrixStacks = new MatrixStack[RenderLayer.getLayers().size()];
        for (int i = 0; i < RenderLayer.getLayers().size(); i++) {
            this.bufferBuilders[i] = new BufferBuilder(2097152);
            this.matrixStacks[i] = new MatrixStack();
        }

        // Initialize vertex data and count arrays for each render layer
        this.vertexData = new float[RenderLayer.getLayers().size()][16][16];
        this.vertexCountData = new int[RenderLayer.getLayers().size()][16][16];
    }

    @Override
    public void renderChunk(Camera camera, VertexConsumerProvider vertexConsumerProvider, Chunk chunk, boolean hasTransparency) {
        // Early out if the chunk is not loaded or has no blocks
        if (!chunk.isLoaded() || !chunk.hasBlocks()) {
            return;
        }

        // Get the chunk position
        ChunkPos chunkPos = chunk.getPos();

        // Calculate the render distance and cull if too far
        int renderDistance = MinecraftClient.getInstance().options.viewDistance;
        if (Math.abs(chunkPos.x - camera.getBlockPos().getX() >> 4) > renderDistance ||
            Math.abs(chunkPos.z - camera.getBlockPos().getZ() >> 4) > renderDistance) {
            return;
        }

        // Calculate the minimum and maximum block positions of the chunk
        int minX = chunkPos.getStartX();
        int maxX = minX + 16;
        int minZ = chunkPos.getStartZ();
        int maxZ = minZ + 16;

        // Iterate through each render layer
        for (int layerIndex = 0; layerIndex < RenderLayer.getLayers().size(); layerIndex++) {
            RenderLayer renderLayer = RenderLayer.getLayers().get(layerIndex);

            // Reset vertex data and count arrays for the layer
            Arrays.fill(vertexData[layerIndex], new float[16]);
            Arrays.fill(vertexCountData[layerIndex], new int[16]);

            // Reset the buffer builder and matrix stack for the layer
            bufferBuilders[layerIndex].reset();
            matrixStacks[layerIndex].loadIdentity();

            // Iterate through each block in the chunk
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    for (int y = 0; y < 256; y++) {
                        BlockPos blockPos = new BlockPos(x, y, z);

                        // Get the block state
                        @Nullable BlockState blockState = chunk.getBlockState(blockPos);

                        // Render the block if it is visible and matches the render layer
                        if (blockState != null && blockRenderManager.isRenderable(blockState, blockPos, chunk) &&
                            blockState.getRenderLayer() == renderLayer) {
                            renderBlock(blockState, blockPos, chunk, vertexConsumerProvider, bufferBuilders[layerIndex],
                                matrixStacks[layerIndex], layerIndex);
                        }
                    }
                }
            }

            // Draw the buffered vertices for the layer
            if (!bufferBuilders[layerIndex].getVertexCount() == 0) {
                RenderSystem.pushMatrix();
                RenderSystem.multMatrix(matrixStacks[layerIndex].peek().getModel());

                vertexConsumerProvider.getBuffer(renderLayer).draw(bufferBuilders[layerIndex].getVertexData(),
                    bufferBuilders[layerIndex].getVertexCount());

                RenderSystem.popMatrix();
            }
        }
    }

    private void renderBlock(BlockState blockState, BlockPos blockPos, Chunk chunk,
                             VertexConsumerProvider vertexConsumerProvider, BufferBuilder bufferBuilder,
                             MatrixStack matrixStack, int layerIndex) {
        // Apply the block state's model transformation
        blockState.getModel().getTransformation().apply(matrixStack);

        // Render the block with the buffer builder
        blockRenderManager.getModelRenderer().render(blockState, blockPos, chunk, matrixStack, bufferBuilder,
            vertexConsumerProvider, false, MinecraftClient.getInstance().getRandom(), 0, blockState.getEffectiveTint(
                chunk.getWorld(), blockPos, blockState.getBlock()));
    }

    @Override
    protected int getPackedLight(BlockView world, BlockPos blockPos) {
        if (blockPos.getY() >= 0 && blockPos.getY() < 256) {
            return world.getChunk(blockPos).getPackedLight(blockPos);
        }
        return 0x00ffffff;
    }

    @Override
    protected void setBlockVisibility(Chunk chunk, Direction direction, boolean visible) {
        if (chunk instanceof WorldChunk) {
            ((WorldChunk) chunk).setBlockVisibility(direction, visible);
        }
    }

    @Override
    protected boolean isBlockVisible(Chunk chunk, Direction direction) {
        if (chunk instanceof WorldChunk) {
            return ((WorldChunk) chunk).isBlockVisible(direction);
        }
        return false;
    }
}
