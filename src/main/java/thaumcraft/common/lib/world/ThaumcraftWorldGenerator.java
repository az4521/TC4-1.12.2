package thaumcraft.common.lib.world;

import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager.BiomeType;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.expands.worldgen.node.NodeGenerationManager;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.biomes.BiomeGenTaint;
import thaumcraft.common.lib.world.biomes.BiomeHandler;
import thaumcraft.common.lib.world.dim.MazeHandler;
import thaumcraft.common.lib.world.dim.MazeThread;
import thaumcraft.common.tiles.TileNode;
import net.minecraft.world.gen.IChunkGenerator;

public class ThaumcraftWorldGenerator implements IWorldGenerator {
    public static Biome biomeTaint;
    public static Biome biomeEerie;
    public static Biome biomeMagicalForest;
    public static Biome biomeEldritchLands;
    static Collection<Aspect> c;
    static ArrayList<Aspect> basicAspects;
    static ArrayList<Aspect> complexAspects;
    public static HashMap<Integer, Integer> dimensionBlacklist;
    public static HashMap<Integer, Integer> biomeBlacklist;
    HashMap<Integer, Boolean> structureNode = new HashMap<>();

    public static int getFirstFreeBiomeSlot(int old) {
        // In 1.12.2, biomes use the Forge registry and don't need manual slot management
        return old;
    }

    public void initialize() {
        BiomeGenTaint.blobs = new WorldGenBlockBlob(ConfigBlocks.blockTaint, 0);
        BiomeManager.addBiome(BiomeType.WARM, new BiomeManager.BiomeEntry(biomeMagicalForest, Config.biomeMagicalForestWeight));
        BiomeManager.addBiome(BiomeType.COOL, new BiomeManager.BiomeEntry(biomeMagicalForest, Config.biomeMagicalForestWeight));
        BiomeManager.addBiome(BiomeType.WARM, new BiomeManager.BiomeEntry(biomeTaint, Config.biomeTaintWeight));
        BiomeManager.addBiome(BiomeType.COOL, new BiomeManager.BiomeEntry(biomeTaint, Config.biomeTaintWeight));
    }

    public static void addDimBlacklist(int dim, int level) {
        dimensionBlacklist.put(dim, level);
    }

    public static int getDimBlacklist(int dim) {
        return dimensionBlacklist.getOrDefault(dim, -1);
    }

    public static void addBiomeBlacklist(int biome, int level) {
        biomeBlacklist.put(biome, level);
    }

    public static int getBiomeBlacklist(int biome) {
        return biomeBlacklist.getOrDefault(biome, -1);
    }

    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        this.worldGeneration(random, chunkX, chunkZ, world, true);
    }

    public void worldGeneration(Random random, int chunkX, int chunkZ, World world, boolean newGen) {
        if (world.provider.getDimension() == Config.dimensionOuterId) {
            MazeHandler.generateEldritch(world, random, chunkX, chunkZ);
            world.getChunk(chunkX, chunkZ).markDirty();
        } else {
            switch (world.provider.getDimension()) {
                case -1:
                    this.generateNether(world, random, chunkX, chunkZ, newGen);
                case 1:
                    break;
                default:
                    this.generateSurface(world, random, chunkX, chunkZ, newGen);
            }

            if (!newGen) {
                world.getChunk(chunkX, chunkZ).markDirty();
            }
        }

    }

    private boolean generateTotem(World world, Random random, int chunkX, int chunkZ, boolean auraGen, boolean newGen) {
        if (Config.genStructure
                && (world.provider.getDimension() == 0 || world.provider.getDimension() == 1)
                && newGen
                && !auraGen && random.nextInt(Config.nodeRarity * 10) == 0
        ) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int topy = world.provider.getDimension() == -1
                    ? Utils.getFirstUncoveredY(world, x, z) - 1
                    : world.getHeight(x, z) - 1;
            if (topy > world.getActualHeight()) {
                return false;
            }

            if (world.getBlockState(new BlockPos(x, topy, z)).getBlock().isLeaves(world.getBlockState(new BlockPos(x, topy, z)), world, new BlockPos(x, topy, z))) {
                do {
                    --topy;
                } while (world.getBlockState(new BlockPos(x, topy, z)).getBlock() != Blocks.GRASS && topy > 40);
            }

            if (world.getBlockState(new BlockPos(x, topy, z)).getBlock() == Blocks.SNOW_LAYER || world.getBlockState(new BlockPos(x, topy, z)).getBlock() == Blocks.TALLGRASS) {
                --topy;
            }

            if (world.getBlockState(new BlockPos(x, topy, z)).getBlock() == Blocks.GRASS || world.getBlockState(new BlockPos(x, topy, z)).getBlock() == Blocks.SAND || world.getBlockState(new BlockPos(x, topy, z)).getBlock() == Blocks.DIRT || world.getBlockState(new BlockPos(x, topy, z)).getBlock() == Blocks.STONE || world.getBlockState(new BlockPos(x, topy, z)).getBlock() == Blocks.NETHERRACK) {
                int count;
                for (count = 1; (world.isAirBlock(new BlockPos(x, topy + count, z)) || world.getBlockState(new BlockPos(x, topy + count, z)).getBlock() == Blocks.SNOW_LAYER || world.getBlockState(new BlockPos(x, topy + count, z)).getBlock() == Blocks.TALLGRASS) && count < 3; ++count) {
                }

                if (count >= 2) {
                    world.setBlockState(new BlockPos(x, topy, z), ConfigBlocks.blockCosmeticSolid.getStateFromMeta(1), 3);
                    count = 1;

                    while ((world.isAirBlock(new BlockPos(x, topy + count, z)) || world.getBlockState(new BlockPos(x, topy + count, z)).getBlock() == Blocks.SNOW_LAYER || world.getBlockState(new BlockPos(x, topy + count, z)).getBlock() == Blocks.TALLGRASS) && count < 5) {
                        world.setBlockState(new BlockPos(x, topy + count, z), ConfigBlocks.blockCosmeticSolid.getStateFromMeta(0), 3);
                        if (count > 1 && random.nextInt(4) == 0) {
                            world.setBlockState(new BlockPos(x, topy + count, z), ConfigBlocks.blockCosmeticSolid.getStateFromMeta(8), 3);
                            createRandomNodeAt(world, x, topy + count, z, random, false, true, false);
                            count = 5;
                            auraGen = true;
                        }

                        ++count;
                        if (count >= 5 && !auraGen) {
                            world.setBlockState(new BlockPos(x, topy + 5, z), ConfigBlocks.blockCosmeticSolid.getStateFromMeta(8), 3);
                            createRandomNodeAt(world, x, topy + 5, z, random, false, true, false);
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean generateWildNodes(World world, Random random, int chunkX, int chunkZ, boolean auraGen, boolean newGen) {
        return NodeGenerationManager.generateWildNodes(world, random, chunkX, chunkZ, auraGen, newGen);
//        if (Config.genAura && random.nextInt(Config.nodeRarity) == 0 && !auraGen) {
//            int x = chunkX * 16 + random.nextInt(16);
//            int z = chunkZ * 16 + random.nextInt(16);
//            int q = Utils.getFirstUncoveredY(world, x, z);
//            if (q < 2) {
//                q = world.provider.getAverageGroundLevel() + random.nextInt(64) - 32 + Utils.getFirstUncoveredY(world, x, z);
//            }
//
//            if (q < 2) {
//                q = 32 + random.nextInt(64);
//            }
//
//            if (world.isAirBlock(x, q + 1, z)) {
//                ++q;
//            }
//
//            int p = random.nextInt(4);
//            Block b = world.getBlockState(new net.minecraft.util.math.BlockPos(x, q + p, z)).getBlock();
//            if (world.isAirBlock(x, q + p, z) || b.isReplaceable(world, x, q + p, z)) {
//                q += p;
//            }
//
//            if (q > world.getActualHeight()) {
//                return false;
//            } else {
//                createRandomNodeAt(world, x, q, z, random, false, false, false);
//                return true;
//            }
//        }
//        else {
//            return false;
//        }
    }

    public static void createRandomNodeAt(World world, int x, int y, int z, Random random, boolean silverwood, boolean eerie, boolean small) {
        if (basicAspects.isEmpty()) {
            for (Aspect as : c) {
                if (as.getComponents() != null) {
                    complexAspects.add(as);
                } else {
                    basicAspects.add(as);
                }
            }
        }

        NodeType type = NodeType.NORMAL;
        if (silverwood) {
            type = NodeType.PURE;
        } else if (eerie) {
            type = NodeType.DARK;
        } else if (random.nextInt(Config.specialNodeRarity) == 0) {
            switch (random.nextInt(10)) {
                case 0:
                case 1:
                case 2:
                    type = NodeType.DARK;
                    break;
                case 3:
                case 4:
                case 5:
                    type = NodeType.UNSTABLE;
                    break;
                case 6:
                case 7:
                case 8:
                    type = NodeType.PURE;
                    break;
                case 9:
                    type = NodeType.HUNGRY;
            }
        }

        NodeModifier modifier = null;
        if (random.nextInt(Config.specialNodeRarity / 2) == 0) {
            switch (random.nextInt(3)) {
                case 0:
                    modifier = NodeModifier.BRIGHT;
                    break;
                case 1:
                    modifier = NodeModifier.PALE;
                    break;
                case 2:
                    modifier = NodeModifier.FADING;
            }
        }

        Biome bg = world.getBiome(new BlockPos(x, 0, z));
        int baura = BiomeHandler.getBiomeAura(bg);
        if (type != NodeType.PURE && Biome.getIdForBiome(bg) == Biome.getIdForBiome(biomeTaint)) {
            baura = (int) ((float) baura * 1.5F);
            if (random.nextBoolean()) {
                type = NodeType.TAINTED;
                baura = (int) ((float) baura * 1.5F);
            }
        }

        if (silverwood || small) {
            baura /= 4;
        }

        int value = random.nextInt(baura / 2) + baura / 2;
        Aspect ra = BiomeHandler.getRandomBiomeTag(Biome.getIdForBiome(bg), random);
        AspectList al = new AspectList();
        if (ra != null) {
            al.add(ra, 2);
        } else {
            Aspect aa = complexAspects.get(random.nextInt(complexAspects.size()));
            al.add(aa, 1);
            aa = basicAspects.get(random.nextInt(basicAspects.size()));
            al.add(aa, 1);
        }

        for (int a = 0; a < 3; ++a) {
            if (random.nextBoolean()) {
                if (random.nextInt(Config.specialNodeRarity) == 0) {
                    Aspect aa = complexAspects.get(random.nextInt(complexAspects.size()));
                    al.merge(aa, 1);
                } else {
                    Aspect aa = basicAspects.get(random.nextInt(basicAspects.size()));
                    al.merge(aa, 1);
                }
            }
        }

        if (type == NodeType.HUNGRY) {
            al.merge(Aspect.HUNGER, 2);
            if (random.nextBoolean()) {
                al.merge(Aspect.GREED, 1);
            }
        } else if (type == NodeType.PURE) {
            if (random.nextBoolean()) {
                al.merge(Aspect.LIFE, 2);
            } else {
                al.merge(Aspect.ORDER, 2);
            }
        } else if (type == NodeType.DARK) {
            if (random.nextBoolean()) {
                al.merge(Aspect.DEATH, 1);
            }

            if (random.nextBoolean()) {
                al.merge(Aspect.UNDEAD, 1);
            }

            if (random.nextBoolean()) {
                al.merge(Aspect.ENTROPY, 1);
            }

            if (random.nextBoolean()) {
                al.merge(Aspect.DARKNESS, 1);
            }
        }

        int water = 0;
        int lava = 0;
        int stone = 0;
        int foliage = 0;

        try {
            for (int xx = -5; xx <= 5; ++xx) {
                for (int yy = -5; yy <= 5; ++yy) {
                    for (int zz = -5; zz <= 5; ++zz) {
                        try {
                            BlockPos bpos = new BlockPos(x + xx, y + yy, z + zz);
                            Block bi = world.getBlockState(bpos).getBlock();
                            if (bi.getMaterial(world.getBlockState(bpos)) == Material.WATER) {
                                ++water;
                            } else if (bi.getMaterial(world.getBlockState(bpos)) == Material.LAVA) {
                                ++lava;
                            } else if (bi == Blocks.STONE) {
                                ++stone;
                            }

                            if (bi.isFoliage(world, bpos)) {
                                ++foliage;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (water > 100) {
            al.merge(Aspect.WATER, 1);
        }

        if (lava > 100) {
            al.merge(Aspect.FIRE, 1);
            al.merge(Aspect.EARTH, 1);
        }

        if (stone > 500) {
            al.merge(Aspect.EARTH, 1);
        }

        if (foliage > 100) {
            al.merge(Aspect.PLANT, 1);
        }

        int[] spread = new int[al.size()];
        float total = 0.0F;

        for (int a = 0; a < spread.length; ++a) {
            if (al.getAmount(al.getAspectsSorted()[a]) == 2) {
                spread[a] = 50 + random.nextInt(25);
            } else {
                spread[a] = 25 + random.nextInt(50);
            }

            total += (float) spread[a];
        }

        for (int a = 0; a < spread.length; ++a) {
            al.merge(al.getAspectsSorted()[a], (int) ((float) spread[a] / total * (float) value));
        }

        createNodeAt(world, x, y, z, type, modifier, al);
    }

    public static void createNodeAt(World world, int x, int y, int z, NodeType nt, NodeModifier nm, AspectList al) {
        if (world.isAirBlock(new BlockPos(x, y, z))) {
            world.setBlockState(new BlockPos(x, y, z), ConfigBlocks.blockAiry.getDefaultState(), 0);
        }

        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te instanceof TileNode) {
            ((TileNode) te).setNodeType(nt);
            ((TileNode) te).setNodeModifier(nm);
            ((TileNode) te).setAspects(al);
        }

        { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
    }

    private void generateSurface(World world, Random random, int chunkX, int chunkZ, boolean newGen) {
        boolean auraGen = false;
        int blacklist = getDimBlacklist(world.provider.getDimension());
        if (blacklist == -1 && Config.genTrees && !world.getWorldInfo().getTerrainType().getName().startsWith("flat") && (newGen || Config.regenTrees)) {
            this.generateVegetation(world, random, chunkX, chunkZ, newGen);
        }

        if (blacklist != 0 && blacklist != 2) {
            this.generateOres(world, random, chunkX, chunkZ, newGen);
        }

        if (blacklist != 0 && blacklist != 2 && Config.genAura && (newGen || Config.regenAura)) {
            // In 1.12.2, finding nearest structure position requires WorldServer and different API
            // Skip temple-based node placement; aura nodes will still be placed by generateWildNodes
            BlockPos var7 = null;
            if (var7 != null && !this.structureNode.containsKey(var7.hashCode())) {
                auraGen = true;
                this.structureNode.put(var7.hashCode(), true);
                createRandomNodeAt(world, var7.getX(), world.getHeight(var7.getX(), var7.getZ()) + 3, var7.getZ(), random, false, false, false);
            }

            auraGen = this.generateWildNodes(world, random, chunkX, chunkZ, auraGen, newGen);
        }

        if (blacklist == -1
                && Config.genStructure
                && world.provider.getDimension() == 0
                && !world.getWorldInfo().getTerrainType().getName().startsWith("flat")
                && (newGen || Config.regenStructure)
        ) {
            int randPosX = chunkX * 16 + random.nextInt(16);
            int randPosZ = chunkZ * 16 + random.nextInt(16);
            int randPosY = world.getHeight(randPosX, randPosZ) - 9;
            if (randPosY < world.getActualHeight()) {
                world.getChunk(new BlockPos(MathHelper.floor(randPosX), 0, MathHelper.floor(randPosZ)));
                if (random.nextInt(150) == 0) {
                    if (WorldGenMound.generateStatic(world, random, randPosX, randPosY, randPosZ)) {
                        auraGen = true;
                        int value = random.nextInt(200) + 400;
                        createRandomNodeAt(
                                world,
                                randPosX + 9, randPosY + 8, randPosZ + 9,
                                random,
                                false,
                                true,
                                false
                        );
                    }
                } else if (random.nextInt(66) == 0) {
                    WorldGenEldritchRing stonering = new WorldGenEldritchRing();
                    randPosY += 8;
                    int w = 11 + random.nextInt(6) * 2;
                    int h = 11 + random.nextInt(6) * 2;
                    stonering.chunkX = chunkX;
                    stonering.chunkZ = chunkZ;
                    stonering.width = w;
                    stonering.height = h;
                    if (stonering.generate(world, random, new BlockPos(randPosX, randPosY, randPosZ))) {
                        auraGen = true;
                        createRandomNodeAt(world, randPosX, randPosY + 2, randPosZ, random, false, true, false);
                        Thread t = new Thread(new MazeThread(chunkX, chunkZ, w, h, random.nextLong()));
                        t.start();
                    }
                } else if (random.nextInt(40) == 0) {
                    randPosY += 9;
                    WorldGenHilltopStones hilltopStones = new WorldGenHilltopStones();
                    if (hilltopStones.generate(world, random, new BlockPos(randPosX, randPosY, randPosZ))) {
                        auraGen = true;
                        createRandomNodeAt(world, randPosX, randPosY + 5, randPosZ, random, false, true, false);
                    }
                }
            }

            this.generateTotem(world, random, chunkX, chunkZ, auraGen, newGen);
        }

    }

    private void generateVegetation(World world, Random random, int chunkX, int chunkZ, boolean newGen) {
        Biome bgb = world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
        if (getBiomeBlacklist(Biome.getIdForBiome(bgb)) == -1) {
            if (random.nextInt(60) == 3) {
                generateSilverwood(world, random, chunkX, chunkZ);
            }

            if (random.nextInt(25) == 7) {
                generateGreatwood(world, random, chunkX, chunkZ);
            }

            int randPosX = chunkX * 16 + random.nextInt(16);
            int randPosZ = chunkZ * 16 + random.nextInt(16);
            int randPosY = world.getHeight(randPosX, randPosZ);
            if (randPosY <= world.getActualHeight()) {
                if (world.getBiome(new BlockPos(randPosX, 0, randPosZ)).topBlock.getBlock() == Blocks.SAND && world.getBiome(new BlockPos(randPosX, 0, randPosZ)).getTemperature(new BlockPos(randPosX, randPosY, randPosZ)) > 1.0F && random.nextInt(30) == 0) {
                    generateFlowers(world, random, randPosX, randPosY, randPosZ, 3);
                }

            }
        }
    }

    private void generateOres(World world, Random random, int chunkX, int chunkZ, boolean newGen) {
        Biome bgb = world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
        if (getBiomeBlacklist(Biome.getIdForBiome(bgb)) != 0 && getBiomeBlacklist(Biome.getIdForBiome(bgb)) != 2) {
            if (Config.genCinnibar && (newGen || Config.regenCinnibar)) {
                for (int i = 0; i < 18; ++i) {
                    int randPosX = chunkX * 16 + random.nextInt(16);
                    int randPosY = random.nextInt(world.getHeight() / 5);
                    int randPosZ = chunkZ * 16 + random.nextInt(16);
                    BlockPos orePos = new BlockPos(randPosX, randPosY, randPosZ);
                    Block block = world.getBlockState(orePos).getBlock();
                    if (block != null && block.isReplaceableOreGen(world.getBlockState(orePos), world, orePos, state -> state.getBlock() == Blocks.STONE)) {
                        world.setBlockState(orePos, ConfigBlocks.blockCustomOre.getStateFromMeta(0), 0);
                    }
                }
            }

            if (Config.genAmber && (newGen || Config.regenAmber)) {
                for (int i = 0; i < 20; ++i) {
                    int randPosX = chunkX * 16 + random.nextInt(16);
                    int randPosZ = chunkZ * 16 + random.nextInt(16);
                    int randPosY = world.getHeight(randPosX, randPosZ) - random.nextInt(25);
                    BlockPos orePos = new BlockPos(randPosX, randPosY, randPosZ);
                    Block block = world.getBlockState(orePos).getBlock();
                    if (block != null && block.isReplaceableOreGen(world.getBlockState(orePos), world, orePos, state -> state.getBlock() == Blocks.STONE)) {
                        world.setBlockState(orePos, ConfigBlocks.blockCustomOre.getStateFromMeta(7), 2);
                    }
                }
            }

            if (Config.genInfusedStone && (newGen || Config.regenInfusedStone)) {
                for (int i = 0; i < 8; ++i) {
                    int randPosX = chunkX * 16 + random.nextInt(16);
                    int randPosZ = chunkZ * 16 + random.nextInt(16);
                    int randPosY = random.nextInt(Math.max(5, world.getHeight(randPosX, randPosZ) - 5));
                    int md = random.nextInt(6) + 1;
                    if (random.nextInt(3) == 0) {
                        Aspect tag = BiomeHandler.getRandomBiomeTag(Biome.getIdForBiome(world.getBiome(new BlockPos(randPosX, 0, randPosZ))), random);
                        if (tag == null) {
                            md = 1 + random.nextInt(6);
                        } else if (tag == Aspect.AIR) {
                            md = 1;
                        } else if (tag == Aspect.FIRE) {
                            md = 2;
                        } else if (tag == Aspect.WATER) {
                            md = 3;
                        } else if (tag == Aspect.EARTH) {
                            md = 4;
                        } else if (tag == Aspect.ORDER) {
                            md = 5;
                        } else if (tag == Aspect.ENTROPY) {
                            md = 6;
                        }
                    }

                    try {
                        (new WorldGenMinable(ConfigBlocks.blockCustomOre.getStateFromMeta(md), 6, state -> state.getBlock() == Blocks.STONE)).generate(world, random, new BlockPos(randPosX, randPosY, randPosZ));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void generateNether(World world, Random random, int chunkX, int chunkZ, boolean newGen) {
        boolean auraGen = false;
        if (!world.getWorldInfo().getTerrainType().getName().startsWith("flat") && (newGen || Config.regenStructure)) {
            this.generateTotem(world, random, chunkX, chunkZ, auraGen, newGen);
        }

        if (newGen || Config.regenAura) {
            this.generateWildNodes(world, random, chunkX, chunkZ, auraGen, newGen);
        }

    }

    public static boolean generateFlowers(World world, Random random, int x, int y, int z, int flower) {
        WorldGenCustomFlowers flowers = new WorldGenCustomFlowers(ConfigBlocks.blockCustomPlant, flower);
        return flowers.generate(world, random, new BlockPos(x, y, z));
    }

    public static boolean generateGreatwood(World world, Random random, int chunkX, int chunkZ) {
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = world.getHeight(x, z);
        int bio = Biome.getIdForBiome(world.getBiome(new BlockPos(x, 0, z)));
        if (BiomeHandler.getBiomeSupportsGreatwood(bio) > random.nextFloat()) {
            boolean t = (new WorldGenGreatwoodTrees(false)).generate(world, random, x, y, z, random.nextInt(8) == 0);
            return t;
        } else {
            return false;
        }
    }

    public static boolean generateSilverwood(World world, Random random, int chunkX, int chunkZ) {
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = world.getHeight(x, z);
        Biome bio = world.getBiome(new BlockPos(x, 0, z));
        if (bio.equals(biomeMagicalForest) || bio.equals(biomeTaint) || !BiomeDictionary.hasType(bio, Type.MAGICAL) && Biome.getIdForBiome(bio) != Biome.getIdForBiome(net.minecraft.init.Biomes.FOREST_HILLS) && Biome.getIdForBiome(bio) != Biome.getIdForBiome(net.minecraft.init.Biomes.BIRCH_FOREST_HILLS)) {
            return false;
        } else {
            boolean t = (new WorldGenSilverwoodTrees(false, 7, 4)).generate(world, random, new BlockPos(x, y, z));
            return t;
        }
    }

    static {
        c = Aspect.aspects.values();
        basicAspects = new ArrayList<>();
        complexAspects = new ArrayList<>();
        dimensionBlacklist = new HashMap<>();
        biomeBlacklist = new HashMap<>();
    }
}
