package pedrixzz.barium.mixin.render;

import net.minecraft.world.Chunk;
import net.minecraft.world.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.lighting.LevelLightProcessor;
import org.spongepoweredmc.mixin.Mixin;
import org.spongepoweredmc.mixin.Overwrite;
import org.spongepoweredmc.mixin.injection.At;
import org.spongepoweredmc.mixin.injection.ModifyConstant;
import org.spongepoweredmc.mixin.injection.Redirect;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Overwrite
    public void tick() {
        super.tick();

        // Cache frequently accessed data
        BlockPos pos = this.getPos();
        int x = pos.getX();
        int z = pos.getZ();

        // Avoid unnecessary calculations
        for (int y = 0; y < 16; y++) {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos otherPos = new BlockPos(x + dx, y, z + dz);
                    Chunk otherChunk = this.world.getChunk(otherPos);

                    // Update light levels
                    this.world.getLightingProvider().getLightLevel(otherPos);

                    // Update block states
                    otherChunk.getBlockState(otherPos);
                }
            }
        }
    }
}
