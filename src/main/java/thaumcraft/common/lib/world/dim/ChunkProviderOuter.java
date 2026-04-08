package thaumcraft.common.lib.world.dim;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;

public class ChunkProviderOuter implements IChunkProvider, IChunkGenerator {
    private Random rand;
    private World world;
    private WorldType worldType;
    private Biome[] biomesForGeneration;

    public ChunkProviderOuter(World world, long seed, boolean structures) {
        this.world = world;
        this.worldType = world.getWorldInfo().getTerrainType();
        this.rand = new Random(seed);
    }

    @Override
    public Chunk getLoadedChunk(int chunkX, int chunkZ) {
        return this.world.isChunkGeneratedAt(chunkX, chunkZ) ? this.world.getChunk(chunkX, chunkZ) : null;
    }

    @Override
    public Chunk provideChunk(int chunkX, int chunkZ) {
        this.rand.setSeed((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);

        ChunkPrimer primer = new ChunkPrimer();
        Chunk chunk = new Chunk(this.world, primer, chunkX, chunkZ);

        BiomeProvider biomeProvider = this.world.getBiomeProvider();
        this.biomesForGeneration = biomeProvider.getBiomes(this.biomesForGeneration,
                chunkX * 16, chunkZ * 16, 16, 16, true);
        byte[] abyte = chunk.getBiomeArray();

        for (int k = 0; k < abyte.length; ++k) {
            abyte[k] = (byte) Biome.getIdForBiome(this.biomesForGeneration[k]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return this.world.isChunkGeneratedAt(x, z);
    }

    public void populate(int x, int z) {
        BlockFalling.fallInstantly = true;
        int k = x * 16;
        int l = z * 16;
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        MazeHandler.generateEldritch(this.world, this.rand, x, z);
        this.world.getChunk(x, z).markDirty();
        Biome biome = this.world.getBiome(new BlockPos(k + 16, 0, l + 16));
        biome.decorate(this.world, this.world.rand, new BlockPos(k, 0, l));
        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean tick() {
        return false;
    }

    public boolean canSave() {
        return true;
    }

    public String makeString() {
        return "RandomLevelSource";
    }

    public int getLoadedChunkCount() {
        return 0;
    }

    // IChunkGenerator implementation
    @Override
    public Chunk generateChunk(int x, int z) {
        return this.provideChunk(x, z);
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return null;
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
    }
}
