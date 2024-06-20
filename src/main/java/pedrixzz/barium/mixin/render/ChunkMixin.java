package pedrixzz.barium.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.light.ChunkSkyLight;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.structure.StructureStart;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.util.crash.CrashReport;

@Mixin(Chunk.class)
public class ChunkMixin implements ChunkMixinInterface {

    @Shadow
    protected volatile boolean needsSaving;

    @Shadow
    protected final ChunkPos pos;

    @Shadow
    protected final ChunkSection[] sectionArray;

    @Shadow
    protected final Map<Heightmap.Type, Heightmap> heightmaps;

    @Shadow
    protected final ChunkSkyLight chunkSkyLight;

    @Shadow
    protected final Map<Structure, StructureStart> structureStarts;

    @Shadow
    protected final Map<Structure, LongSet> structureReferences;

    @Shadow
    protected final Map<BlockPos, NbtCompound> blockEntityNbts;

    @Shadow
    protected final Map<BlockPos, BlockEntity> blockEntities;

    @Shadow
    protected final UpgradeData upgradeData;

    @Shadow
    protected BlendingData blendingData;

    @Shadow
    protected long inhabitedTime;

    @Override
    public void addStructureReference(Structure structure, long reference) {
        this.structureReferences.computeIfAbsent(structure, s -> new LongOpenHashSet()).add(reference);
        this.needsSaving = true;
    }

    @Override
    public void setStructureReferences(Map<Structure, LongSet> structureReferences) {
        this.structureReferences.clear();
        this.structureReferences.putAll(structureReferences);
        this.needsSaving = true;
    }

    @Override
    public boolean needsSaving() {
        return this.needsSaving;
    }

    @Override
    public void setNeedsSaving(boolean needsSaving) {
        this.needsSaving = needsSaving;
    }

    @Override
    public ChunkPos getPos() {
        return this.pos;
    }

    @Override
    public ChunkSection getSection(int yIndex) {
        return this.sectionArray[yIndex];
    }

    @Override
    public int sectionCoordToIndex(int coord) {
        return coord >> 4;
    }

    @Override
    public int getTopY() {
        return this.heightLimitView.getHeight();
    }

    @Override
    public int getBottomY() {
        return this.heightLimitView.getBottomY();
    }

    @Override
    public HeightLimitView getHeightLimitView() {
        return this.heightLimitView;
    }

    @Override
    public LongSet getStructureReferences(Structure structure) {
        return this.structureReferences.getOrDefault(structure, EMPTY_STRUCTURE_REFERENCES);
    }

    @Override
    public void setLightOn(boolean lightOn) {
        this.lightOn = lightOn;
        this.needsSaving = true;
    }

    @Override
    public boolean isLightOn() {
        return this.lightOn;
    }

    // Outros métodos conforme necessário...
}
