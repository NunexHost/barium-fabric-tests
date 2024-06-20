package pedrixzz.barium.mixin.render;

import com.google.common.collect.Maps;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Shadow protected abstract Map<BlockPos, BlockEntity> getBlockEntities();

    private Map<BlockPos, BlockEntity> optimizedBlockEntities = null;

    /**
     * Otimiza o carregamento da Chunk.
     * Substitui o construtor original para inicializar de forma mais eficiente.
     */
    public ChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView,
                      Registry<Biome> biomeRegistry, long inhabitedTime,
                      @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        this.pos = pos;
        this.upgradeData = upgradeData;
        this.heightLimitView = heightLimitView;
        this.sectionArray = new ChunkSection[heightLimitView.countVerticalSections()];
        this.inhabitedTime = inhabitedTime;
        this.postProcessingLists = new ShortList[heightLimitView.countVerticalSections()];
        this.blendingData = blendingData;
        this.chunkSkyLight = new ChunkSkyLight(heightLimitView);
        if (sectionArray != null) {
            if (this.sectionArray.length == sectionArray.length) {
                System.arraycopy(sectionArray, 0, this.sectionArray, 0, this.sectionArray.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", sectionArray.length, this.sectionArray.length);
            }
        }

        fillSectionArray(biomeRegistry, this.sectionArray);
        optimizeBlockEntities(); // Nova função para otimizar o acesso às entidades de bloco
    }

    /**
     * Otimiza o acesso às entidades de bloco ao criar uma cópia local.
     */
    private void optimizeBlockEntities() {
        this.optimizedBlockEntities = Maps.newHashMap(this.getBlockEntities());
    }

    /**
     * Substitui o método original getBlockEntity para utilizar a cópia otimizada.
     */
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.optimizedBlockEntities.get(pos);
    }
}
