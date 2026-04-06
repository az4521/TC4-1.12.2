package thaumcraft.common.items.wands;

import baubles.api.BaublesApi;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.oredict.OreDictionary;
import simpleutils.bauble.BaubleConsumer;
import thaumcraft.api.IArchitect;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.IWandTriggerManager;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntitySpecialItem;
import thaumcraft.common.items.baubles.ItemAmuletVis;
import thaumcraft.common.items.wands.foci.ItemFocusTrade;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.tiles.TileEldritchAltar;
import thaumcraft.common.tiles.TileInfusionMatrix;
import thaumcraft.common.tiles.TileInfusionPillar;
import thaumcraft.common.tiles.TileJarNode;
import thaumcraft.common.tiles.TileNode;
import thaumcraft.common.tiles.TilePedestal;
import thaumcraft.common.tiles.TileThaumatorium;

import static simpleutils.bauble.BaubleUtils.forEachBauble;
import static thaumcraft.common.Thaumcraft.log;
import net.minecraft.util.math.BlockPos;

public class WandManager implements IWandTriggerManager {
    static HashMap<Integer, Long> cooldownServer = new HashMap<>();
    static HashMap<Integer, Long> cooldownClient = new HashMap<>();

    //Do not modify it's bytecode.go to see thaumcraft.api.expands.wandconsumption.ConsumptionModifierCalculator and add listener.
    public static float getTotalVisDiscount(EntityPlayer player, Aspect aspect) {
        int cheatGadomancy = 0;//Gadomancy used tricks to modify java bytecode.deleting this will cause error with no stacktrace point to it.
        final AtomicInteger total = new AtomicInteger(cheatGadomancy);
        if (player == null) {
            return 0.0F;
        } else {
            BaubleConsumer<IVisDiscountGear> visDiscountGearBaubleConsumer = (slot, stack, visDiscountGear) -> {
                total.addAndGet(visDiscountGear.getVisDiscount(stack, player, aspect));
                return false;
            };
            forEachBauble(player, IVisDiscountGear.class, visDiscountGearBaubleConsumer);

            for (int a = 0; a < 4; ++a) {
                ItemStack stack = player.inventory.getStackInSlot(a);
                if (player.inventory.armorItemInSlot(a) != null) {
                    Item item = player.inventory.armorItemInSlot(a).getItem();
                    if (item instanceof IVisDiscountGear) {
                        visDiscountGearBaubleConsumer.accept(a, stack, ((IVisDiscountGear) item));
                    }
                }
            }

            if (player.isPotionActive(net.minecraft.potion.Potion.getPotionById(Config.potionVisExhaustID)) || player.isPotionActive(net.minecraft.potion.Potion.getPotionById(Config.potionInfVisExhaustID))) {
                int level1 = 0;
                int level2 = 0;
                if (player.isPotionActive(net.minecraft.potion.Potion.getPotionById(Config.potionVisExhaustID))) {
                    level1 = player.getActivePotionEffect(net.minecraft.potion.Potion.getPotionById(Config.potionVisExhaustID)).getAmplifier();
                }

                if (player.isPotionActive(net.minecraft.potion.Potion.getPotionById(Config.potionInfVisExhaustID))) {
                    level2 = player.getActivePotionEffect(net.minecraft.potion.Potion.getPotionById(Config.potionInfVisExhaustID)).getAmplifier();
                }

                total.addAndGet((Math.max(level1, level2) + 1) * 10);
            }

            return (float) total.get() / 100.0F;
        }
    }

    public static boolean consumeVisFromInventory(EntityPlayer player, AspectList cost) {
        BaubleConsumer<ItemAmuletVis> amuletVisBaubleConsumer = (slot, stack, itemAmuletVis)
                -> itemAmuletVis.consumeAllVis(stack, player, cost, true, true);
        if (forEachBauble(player, ItemAmuletVis.class, amuletVisBaubleConsumer)) {
            return true;
        }


        BaubleConsumer<ItemWandCasting> wandCastingBaubleConsumer = (slot, stack, itemWandCasting) -> itemWandCasting.consumeAllVis(stack, player, cost, true, true);
        return forEachBauble(player, ItemWandCasting.class, wandCastingBaubleConsumer);

//      for(int a = player.inventory.mainInventory.length - 1; a >= 0; --a) {
//         ItemStack item = player.inventory.mainInventory.get(a);
//         if (item != null && item.getItem() instanceof ItemWandCasting) {
//            boolean done = ((ItemWandCasting)item.getItem()).consumeAllVis(item, player, cost, true, true);
//            if (done) {
//               return true;
//            }
//         }
//      }
    }

    public static boolean createCrucible(ItemStack is, EntityPlayer player, World world, int x, int y, int z) {
        ItemWandCasting wand = (ItemWandCasting) is.getItem();
        if (!world.isRemote) {
            world.playSound(null, (double) x + 0.5, (double) y + 0.5, (double) z + 0.5, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockToAir(new BlockPos(x, y, z));
        world.setBlockState(new BlockPos(x, y, z), ConfigBlocks.blockMetalDevice.getStateFromMeta(0), 3);
            world.notifyNeighborsOfStateChange(new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)).getBlock(), true);
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
            world.addBlockEvent(new BlockPos(x, y, z), ConfigBlocks.blockMetalDevice, 1, 1);
            return true;
        } else {
            return false;
        }
    }

    public static boolean createInfusionAltar(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z) {
        ItemWandCasting wand = (ItemWandCasting) itemstack.getItem();

        for (int xx = x - 2; xx <= x; ++xx) {
            for (int yy = y - 2; yy <= y; ++yy) {
                for (int zz = z - 2; zz <= z; ++zz) {
                    if (fitInfusionAltar(world, xx, yy, zz) && wand.consumeAllVisCrafting(itemstack, player, (new AspectList()).add(Aspect.FIRE, 25).add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.AIR, 25).add(Aspect.ENTROPY, 25).add(Aspect.WATER, 25), true)) {
                        if (!world.isRemote) {
                            replaceInfusionAltar(world, xx, yy, zz);
                            return true;
                        }

                        return false;
                    }
                }
            }
        }

        return false;
    }

    public static boolean fitInfusionAltar(World world, int x, int y, int z) {
        ItemStack br1 = new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 6);
        ItemStack br2 = new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 7);
        ItemStack bs = new ItemStack(ConfigBlocks.blockStoneDevice, 1, 2);
        new ItemStack(ConfigBlocks.blockStoneDevice, 1, 1);
        ItemStack[][][] blueprint = new ItemStack[][][]{{{null, null, null}, {null, bs, null}, {null, null, null}}, {{br1, null, br1}, {null, null, null}, {br1, null, br1}}, {{br2, null, br2}, {null, null, null}, {br2, null, br2}}};

        for (int yy = 0; yy < 3; ++yy) {
            for (int xx = 0; xx < 3; ++xx) {
                for (int zz = 0; zz < 3; ++zz) {
                    if (blueprint[yy][xx][zz] == null) {
                        if (xx == 1 && zz == 1 && yy == 2) {
                            TileEntity t = world.getTileEntity(new BlockPos(x + xx, y - yy + 2, z + zz));
                            if (!(t instanceof TilePedestal)) {
                                return false;
                            }
                        } else if (!world.isAirBlock(new net.minecraft.util.math.BlockPos(x + xx, y - yy + 2, z + zz))) {
                            return false;
                        }
                    } else {
                        Block block = world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y - yy + 2, z + zz)).getBlock();
                        int md =
        world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y - yy + 2, z + zz)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y - yy + 2, z + zz)));
                        if (!(new ItemStack(block, 1, md)).isItemEqual(blueprint[yy][xx][zz])) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public static void replaceInfusionAltar(World world, int x, int y, int z) {
        int[][][] blueprint = new int[][][]{{{0, 0, 0}, {0, 9, 0}, {0, 0, 0}}, {{1, 0, 1}, {0, 0, 0}, {1, 0, 1}}, {{2, 0, 3}, {0, 0, 0}, {4, 0, 5}}};

        for (int yy = 0; yy < 3; ++yy) {
            for (int xx = 0; xx < 3; ++xx) {
                for (int zz = 0; zz < 3; ++zz) {
                    if (blueprint[yy][xx][zz] != 0) {
                        if (blueprint[yy][xx][zz] == 1) {
        world.setBlockState(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockStoneDevice.getStateFromMeta(4), 3);
                            world.addBlockEvent(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockStoneDevice, 1, 0);
                        }

                        if (blueprint[yy][xx][zz] > 1 && blueprint[yy][xx][zz] < 9) {
        world.setBlockState(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockStoneDevice.getStateFromMeta(3), 3);
                            TileInfusionPillar tip = (TileInfusionPillar) world.getTileEntity(new BlockPos(x + xx, y - yy + 2, z + zz));
                            tip.orientation = (byte) blueprint[yy][xx][zz];
                            world.notifyBlockUpdate(new BlockPos(x + xx, y - yy + 2, z + zz), world.getBlockState(new BlockPos(x + xx, y - yy + 2, z + zz)), world.getBlockState(new BlockPos(x + xx, y - yy + 2, z + zz)), 3);
                            world.addBlockEvent(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockStoneDevice, 1, 0);
                        }

                        if (blueprint[yy][xx][zz] == 9) {
                            TileInfusionMatrix tis = (TileInfusionMatrix) world.getTileEntity(new BlockPos(x + xx, y - yy + 2, z + zz));
                            tis.active = true;
                            world.notifyBlockUpdate(new BlockPos(x + xx, y - yy + 2, z + zz), world.getBlockState(new BlockPos(x + xx, y - yy + 2, z + zz)), world.getBlockState(new BlockPos(x + xx, y - yy + 2, z + zz)), 3);
                        }
                    }
                }
            }
        }

        world.playSound(null, (double) x + 0.5, (double) y + 0.5, (double) z + 0.5, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public static boolean createNodeJar(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z) {
        ItemWandCasting wand = (ItemWandCasting) itemstack.getItem();

        for (int xx = x - 2; xx <= x; ++xx) {
            for (int yy = y - 3; yy <= y; ++yy) {
                for (int zz = z - 2; zz <= z; ++zz) {
                    if (fitNodeJar(world, xx, yy, zz) && wand.consumeAllVisCrafting(itemstack, player, (new AspectList()).add(Aspect.FIRE, 70).add(Aspect.EARTH, 70).add(Aspect.ORDER, 70).add(Aspect.AIR, 70).add(Aspect.ENTROPY, 70).add(Aspect.WATER, 70), true)) {
                        if (!world.isRemote) {
                            replaceNodeJar(world, xx, yy, zz);
                            return true;
                        }

                        return false;
                    }
                }
            }
        }

        return false;
    }

    public static boolean createThaumatorium(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side) {
        ItemWandCasting wand = (ItemWandCasting) itemstack.getItem();
        if (world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock() != ConfigBlocks.blockMetalDevice ||
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z))) != 9 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock() != ConfigBlocks.blockMetalDevice || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z))) != 0) {
            if (world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock() != ConfigBlocks.blockMetalDevice || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z))) != 9 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 2, z)).getBlock() != ConfigBlocks.blockMetalDevice || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 2, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 2, z))) != 0) {
                return false;
            }

            --y;
        }

        if (wand.consumeAllVisCrafting(itemstack, player, (new AspectList()).add(Aspect.FIRE, 15).add(Aspect.ORDER, 30).add(Aspect.WATER, 30), true) && !world.isRemote) {
        world.setBlockState(new BlockPos(x, y, z), ConfigBlocks.blockMetalDevice.getStateFromMeta(10), 0);
        world.setBlockState(new BlockPos(x, y + 1, z), ConfigBlocks.blockMetalDevice.getStateFromMeta(11), 0);
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof TileThaumatorium) {
                ((TileThaumatorium) tile).facing = net.minecraft.util.EnumFacing.byIndex(side);
            }

            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
            world.notifyBlockUpdate(new BlockPos(x, y + 1, z), world.getBlockState(new BlockPos(x, y + 1, z)), world.getBlockState(new BlockPos(x, y + 1, z)), 3);
            world.notifyNeighborsOfStateChange(new BlockPos(x, y, z), ConfigBlocks.blockMetalDevice, true);
            world.notifyNeighborsOfStateChange(new BlockPos(x, y + 1, z), ConfigBlocks.blockMetalDevice, true);
            PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(x, y, z, -9999), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 32.0F));
            PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(x, y + 1, z, -9999), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 32.0F));
            world.playSound(null, (double) x + 0.5, (double) y + 0.5, (double) z + 0.5, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }

    static boolean containsMatch(boolean strict, List<ItemStack> inputs, ItemStack... targets) {
        for (ItemStack input : inputs) {
            for (ItemStack target : targets) {
                if (OreDictionary.itemMatches(input, target, strict)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean fitNodeJar(World world, int x, int y, int z) {
        int[][][] blueprint = new int[][][]{{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}, {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}, {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}}, {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}};

        for (int yy = 0; yy < 4; ++yy) {
            for (int xx = 0; xx < 3; ++xx) {
                for (int zz = 0; zz < 3; ++zz) {
                    Block block = world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y - yy + 2, z + zz)).getBlock();
                    int md =
        world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y - yy + 2, z + zz)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y - yy + 2, z + zz)));
                    if (blueprint[yy][xx][zz] == 1 && !containsMatch(false, OreDictionary.getOres("slabWood"), new ItemStack(block, 1, md))) {
                        return false;
                    }

                    if (blueprint[yy][xx][zz] == 2 && block != Blocks.GLASS) {
                        return false;
                    }

                    if (blueprint[yy][xx][zz] == 3) {
                        TileEntity tile = world.getTileEntity(new BlockPos(x + xx, y - yy + 2, z + zz));
                        if (!(tile instanceof INode) || tile instanceof TileJarNode) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public static void replaceNodeJar(World world, int x, int y, int z) {
        if (!world.isRemote) {
            int[][][] blueprint = new int[][][]{{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}, {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}, {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}}, {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}};

            for (int yy = 0; yy < 4; ++yy) {
                for (int xx = 0; xx < 3; ++xx) {
                    for (int zz = 0; zz < 3; ++zz) {
                        if (blueprint[yy][xx][zz] == 3) {
                            TileEntity tile = world.getTileEntity(new BlockPos(x + xx, y - yy + 2, z + zz));
                            INode node = (INode) tile;
                            AspectList na = node.getAspects().copy();
                            int nt = node.getNodeType().ordinal();
                            int nm = -1;
                            if (node.getNodeModifier() != null) {
                                nm = node.getNodeModifier().ordinal();
                            }

                            if (world.rand.nextFloat() < 0.75F) {
                                if (node.getNodeModifier() == null) {
                                    nm = NodeModifier.PALE.ordinal();
                                } else if (node.getNodeModifier() == NodeModifier.BRIGHT) {
                                    nm = -1;
                                } else if (node.getNodeModifier() == NodeModifier.PALE) {
                                    nm = NodeModifier.FADING.ordinal();
                                }
                            }

                            String nid = node.getId();
                            node.setAspects(new AspectList());
                            world.removeTileEntity(new BlockPos(x + xx, y - yy + 2, z + zz));
        world.setBlockState(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockJar.getStateFromMeta(2), 3);
                            tile = world.getTileEntity(new BlockPos(x + xx, y - yy + 2, z + zz));
                            TileJarNode jar = (TileJarNode) tile;
                            jar.setAspects(na);
                            if (nm >= 0) {
                                jar.setNodeModifier(NodeModifier.values()[nm]);
                            }

                            jar.setNodeType(NodeType.values()[nt]);
                            jar.setId(nid);
                            world.addBlockEvent(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockJar, 9, 0);
                        } else {
                            world.setBlockToAir(new BlockPos(x + xx, y - yy + 2, z + zz));
                        }
                    }
                }
            }

            world.playSound(null, (double) x + 0.5, (double) y + 0.5, (double) z + 0.5, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    public static boolean createArcaneFurnace(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z) {
        ItemWandCasting wand = (ItemWandCasting) itemstack.getItem();

        for (int xx = x - 2; xx <= x; ++xx) {
            for (int yy = y - 2; yy <= y; ++yy) {
                for (int zz = z - 2; zz <= z; ++zz) {
                    if (fitArcaneFurnace(world, xx, yy, zz) && wand.consumeAllVisCrafting(itemstack, player, (new AspectList()).add(Aspect.FIRE, 50).add(Aspect.EARTH, 50), true)) {
                        if (!world.isRemote) {
                            replaceArcaneFurnace(world, xx, yy, zz);
                            return true;
                        }

                        return false;
                    }
                }
            }
        }

        return false;
    }

    public static boolean fitArcaneFurnace(World world, int x, int y, int z) {
        Block bo = Blocks.OBSIDIAN;
        Block bn = Blocks.NETHER_BRICK;
        Block bf = Blocks.IRON_BARS;
        Block bl = Blocks.LAVA;
        Block[][][] blueprint = new Block[][][]{{{bn, bo, bn}, {bo, Blocks.AIR, bo}, {bn, bo, bn}}, {{bn, bo, bn}, {bo, bl, bo}, {bn, bo, bn}}, {{bn, bo, bn}, {bo, bo, bo}, {bn, bo, bn}}};
        boolean fencefound = false;

        for (int yy = 0; yy < 3; ++yy) {
            for (int xx = 0; xx < 3; ++xx) {
                for (int zz = 0; zz < 3; ++zz) {
                    Block block = world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y - yy + 2, z + zz)).getBlock();
                    if (world.isAirBlock(new net.minecraft.util.math.BlockPos(x + xx, y - yy + 2, z + zz))) {
                        block = Blocks.AIR;
                    }

                    if (block != blueprint[yy][xx][zz]) {
                        if (yy != 1 || fencefound || block != bf || xx == zz || xx != 1 && zz != 1) {
                            return false;
                        }

                        fencefound = true;
                    }
                }
            }
        }

        return fencefound;
    }

    public static boolean replaceArcaneFurnace(World world, int x, int y, int z) {
        boolean fencefound = false;

        for (int yy = 0; yy < 3; ++yy) {
            int step = 1;

            for (int zz = 0; zz < 3; ++zz) {
                for (int xx = 0; xx < 3; ++xx) {
                    int md = step;
                    if (world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz)).getBlock() == Blocks.LAVA || world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz)).getBlock() == Blocks.FLOWING_LAVA) {
                        md = 0;
                    }

                    if (world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz)).getBlock() == Blocks.IRON_BARS) {
                        md = 10;
                    }

                    if (!world.isAirBlock(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz))) {
        world.setBlockState(new BlockPos(x + xx, y + yy, z + zz), ConfigBlocks.blockArcaneFurnace.getStateFromMeta(md), 0);
                        world.addBlockEvent(new BlockPos(x + xx, y + yy, z + zz), ConfigBlocks.blockArcaneFurnace, 1, 4);
                    }

                    ++step;
                }
            }
        }

        for (int yy = 0; yy < 3; ++yy) {
            for (int zz = 0; zz < 3; ++zz) {
                for (int xx = 0; xx < 3; ++xx) {
                    world.notifyBlockUpdate(new BlockPos(x + xx, y + yy, z + zz), world.getBlockState(new BlockPos(x + xx, y + yy, z + zz)), world.getBlockState(new BlockPos(x + xx, y + yy, z + zz)), 3);
                }
            }
        }

        world.playSound(null, (double) x + 0.5, (double) y + 0.5, (double) z + 0.5, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
        return fencefound;
    }

    public static boolean createThaumonomicon(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z) {
        if (!world.isRemote) {
            ItemWandCasting wand = (ItemWandCasting) itemstack.getItem();
            if (wand.getFocus(itemstack) != null) {
                return false;
            } else {
                world.setBlockToAir(new BlockPos(x, y, z));
                EntitySpecialItem entityItem = new EntitySpecialItem(world, (float) x + 0.5F, (float) y + 0.3F, (float) z + 0.5F, new ItemStack(ConfigItems.itemThaumonomicon));
                entityItem.motionY = 0.0F;
                entityItem.motionX = 0.0F;
                entityItem.motionZ = 0.0F;
                world.spawnEntity(entityItem);
                PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(x, y, z, -9999), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 32.0F));
                world.playSound(null, (double) x + 0.5, (double) y + 0.5, (double) z + 0.5, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        } else {
            return false;
        }
    }

    private static boolean createOculus(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            TileEntity node = world.getTileEntity(new BlockPos(x, y + 1, z));
            if (node != null && tile instanceof TileEldritchAltar && ((TileEldritchAltar) tile).getEyes() == 4 && !((TileEldritchAltar) tile).isOpen() && node instanceof TileNode && ((TileNode) node).getNodeType() == NodeType.DARK && ((TileEldritchAltar) tile).checkForMaze()) {
                ItemWandCasting wand = (ItemWandCasting) itemstack.getItem();
                if (wand.consumeAllVisCrafting(itemstack, player, (new AspectList()).add(Aspect.AIR, 100).add(Aspect.FIRE, 100).add(Aspect.EARTH, 100).add(Aspect.WATER, 100).add(Aspect.ORDER, 100).add(Aspect.ENTROPY, 100), true)) {
                    world.playSound(null, (double) x + 0.5, (double) y + 0.5, (double) z + 0.5, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
                    ((TileEldritchAltar) tile).setOpen(true);
                    world.setBlockToAir(new BlockPos(x, y + 1, z));
        world.setBlockState(new BlockPos(x, y + 1, z), ConfigBlocks.blockEldritchPortal.getDefaultState());
                    tile.markDirty();
                    { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
                }
            }
        }

        return false;
    }

    private static BaubleConsumer<ItemFocusPouch> getItemFocusPouchBaubleConsumer(
            int offset,
            int[] pouchcountPointer,
            HashMap<Integer, Integer> pouches,
            TreeMap<String, Integer> foci
    ) {
        return (slot, stack, itemFocusPouch) -> {
            ++pouchcountPointer[0];
            pouches.put(pouchcountPointer[0], slot + offset);
            ItemStack[] inv = itemFocusPouch.getInventory(stack);

            for (int q = 0; q < inv.length; ++q) {
                ItemStack probablyFocusBasicStack = inv[q];
                if (probablyFocusBasicStack != null && probablyFocusBasicStack.getItem() instanceof ItemFocusBasic) {
                    foci.put(((ItemFocusBasic) probablyFocusBasicStack.getItem()).getSortingHelper(probablyFocusBasicStack), q + pouchcountPointer[0] * 1000);
                }
            }
            return false;
        };
    }

    public static void changeFocus(ItemStack is, World w, EntityPlayer player, String focus) {
        ItemWandCasting wand = (ItemWandCasting) is.getItem();
        TreeMap<String, Integer> foci = new TreeMap<>();
        HashMap<Integer, Integer> pouches = new HashMap<>();
        final int[] pouchcount = {0};
        final ItemStack[] item = {null};
        IInventory baubles = BaublesApi.getBaubles(player);

        BaubleConsumer<ItemFocusPouch> itemFocusPouchInventoryConsumer = getItemFocusPouchBaubleConsumer(0, pouchcount, pouches, foci);
        BaubleConsumer<ItemFocusPouch> itemFocusPouchBaubleConsumer = getItemFocusPouchBaubleConsumer(-9, pouchcount, pouches, foci);
        forEachBauble(player, ItemFocusPouch.class, itemFocusPouchBaubleConsumer);

        for (int a = 0; a < 36; ++a) {
            item[0] = player.inventory.mainInventory.get(a);
            if (item[0] != null && item[0].getItem() instanceof ItemFocusBasic) {
                foci.put(((ItemFocusBasic) item[0].getItem()).getSortingHelper(item[0]), a);
            }

            if (item[0] != null && item[0].getItem() instanceof ItemFocusPouch) {
                itemFocusPouchInventoryConsumer.accept(a, item[0], (ItemFocusPouch) item[0].getItem());
            }
        }

        if (focus != null && !focus.equals("REMOVE") && !foci.isEmpty()
        ) {
            {
                String newkey = focus;
                if (foci.get(focus) == null) {
                    newkey = foci.higherKey(focus);
                }

                if (newkey == null || foci.get(newkey) == null) {
                    newkey = foci.firstKey();
                }

                if (foci.get(newkey) < 1000 && foci.get(newkey) >= 0) {
                    item[0] = player.inventory.mainInventory.get(foci.get(newkey)).copy();
                } else {
                    int pid = foci.get(newkey) / 1000;
                    if (pouches.containsKey(pid)) {
                        int pouchslot = pouches.get(pid);
                        int focusslot = foci.get(newkey) - pid * 1000;
                        ItemStack tmp = null;
                        if (pouchslot >= 0) {
                            tmp = player.inventory.mainInventory.get(pouchslot).copy();
                        } else {
                            tmp = baubles.getStackInSlot(pouchslot + 9).copy();
                        }

                        item[0] = fetchFocusFromPouch(player, focusslot, tmp, pouchslot);
                    }
                }

                if (item[0] == null) {
                    return;
                }

                if (foci.get(newkey) < 1000 && foci.get(newkey) >= 0) {
                    player.inventory.setInventorySlotContents(foci.get(newkey), ItemStack.EMPTY);
                }

                player.world.playSound(null, player.posX, player.posY, player.posZ, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "cameraticks")), net.minecraft.util.SoundCategory.PLAYERS, 0.3F, 1.0F);
                if (wand.getFocus(is) != null && (addFocusToPouch(player, wand.getFocusItem(is).copy(), pouches) || player.inventory.addItemStackToInventory(wand.getFocusItem(is).copy()))) {
                    wand.setFocus(is, null);
                }

                if (wand.getFocus(is) == null) {
                    wand.setFocus(is, item[0]);
                } else if (!addFocusToPouch(player, item[0], pouches)) {
                    player.inventory.addItemStackToInventory(item[0]);
                }
            }

        } else {
            if (wand.getFocus(is) != null
                    && (addFocusToPouch(player, wand.getFocusItem(is).copy(), pouches)
                    || player.inventory.addItemStackToInventory(wand.getFocusItem(is).copy()))
            ) {
                wand.setFocus(is, null);
                player.world.playSound(null, player.posX, player.posY, player.posZ, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "cameraticks")), net.minecraft.util.SoundCategory.PLAYERS, 0.3F, 0.9F);
            }

        }
    }

    private static ItemStack fetchFocusFromPouch(EntityPlayer player, int focusid, ItemStack pouch, int pouchslot) {
        ItemStack focus = null;
        ItemStack[] inv = ((ItemFocusPouch) pouch.getItem()).getInventory(pouch);
        ItemStack contents = inv[focusid];
        if (contents != null && contents.getItem() instanceof ItemFocusBasic) {
            focus = contents.copy();
            inv[focusid] = null;
            ((ItemFocusPouch) pouch.getItem()).setInventory(pouch, inv);
            if (pouchslot >= 0) {
                player.inventory.setInventorySlotContents(pouchslot, pouch);
                player.inventory.markDirty();
            } else {
                IInventory baubles = BaublesApi.getBaubles(player);
                baubles.setInventorySlotContents(pouchslot + 4, pouch);
                baubles.markDirty();
            }
        }

        return focus;
    }

    private static boolean addFocusToPouch(EntityPlayer player, ItemStack focus, HashMap<Integer, Integer> pouches) {
        IInventory baubles = BaublesApi.getBaubles(player);

        for (Integer pouchslot : pouches.values()) {
            ItemStack pouch;
            if (pouchslot >= 0) {
                pouch = player.inventory.mainInventory.get(pouchslot);
            } else {
                pouch = baubles.getStackInSlot(pouchslot + 4);
            }

            ItemStack[] inv = ((ItemFocusPouch) pouch.getItem()).getInventory(pouch);

            for (int q = 0; q < inv.length; ++q) {
                ItemStack contents = inv[q];
                if (contents == null) {
                    inv[q] = focus.copy();
                    ((ItemFocusPouch) pouch.getItem()).setInventory(pouch, inv);
                    if (pouchslot >= 0) {
                        player.inventory.setInventorySlotContents(pouchslot, pouch);
                        player.inventory.markDirty();
                    } else {
                        baubles.setInventorySlotContents(pouchslot + 4, pouch);
                        baubles.markDirty();
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public static void toggleMisc(ItemStack itemstack, World world, EntityPlayer player) {
        if (itemstack.getItem() instanceof ItemWandCasting) {
            ItemWandCasting wand = (ItemWandCasting) itemstack.getItem();
            if (wand.getFocus(itemstack) != null && wand.getFocus(itemstack) instanceof IArchitect && wand.getFocus(itemstack).isUpgradedWith(wand.getFocusItem(itemstack), FocusUpgradeType.architect)) {
                int dim = getAreaDim(itemstack);
                IArchitect fa = (IArchitect) wand.getFocus(itemstack);
                if (player.isSneaking()) {
                    ++dim;
                    if (dim > (wand.getFocusItem(itemstack).getItem() instanceof ItemFocusTrade ? 2 : 3)) {
                        dim = 0;
                    }

                    setAreaDim(itemstack, dim);
                } else {
                    int areax = getAreaX(itemstack);
                    int areay = getAreaY(itemstack);
                    int areaz = getAreaZ(itemstack);
                    if (dim == 0) {
                        ++areax;
                        ++areaz;
                        ++areay;
                    } else if (dim == 1) {
                        ++areax;
                    } else if (dim == 2) {
                        ++areaz;
                    } else if (dim == 3) {
                        ++areay;
                    }

                    if (areax > wand.getFocus(itemstack).getMaxAreaSize(wand.getFocusItem(itemstack))) {
                        areax = 0;
                    }

                    if (areaz > wand.getFocus(itemstack).getMaxAreaSize(wand.getFocusItem(itemstack))) {
                        areaz = 0;
                    }

                    if (areay > wand.getFocus(itemstack).getMaxAreaSize(wand.getFocusItem(itemstack))) {
                        areay = 0;
                    }

                    setAreaX(itemstack, areax);
                    setAreaY(itemstack, areay);
                    setAreaZ(itemstack, areaz);
                }
            }

        }
    }

    public static int getAreaDim(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("aread") ? stack.getTagCompound().getInteger("aread") : 0;
    }

    public static int getAreaX(ItemStack stack) {
        ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("areax")) {
            int a = stack.getTagCompound().getInteger("areax");
            if (a > wand.getFocus(stack).getMaxAreaSize(wand.getFocusItem(stack))) {
                a = wand.getFocus(stack).getMaxAreaSize(wand.getFocusItem(stack));
            }

            return a;
        } else {
            return wand.getFocus(stack).getMaxAreaSize(wand.getFocusItem(stack));
        }
    }

    public static int getAreaY(ItemStack stack) {
        ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("areay")) {
            int a = stack.getTagCompound().getInteger("areay");
            if (a > wand.getFocus(stack).getMaxAreaSize(wand.getFocusItem(stack))) {
                a = wand.getFocus(stack).getMaxAreaSize(wand.getFocusItem(stack));
            }

            return a;
        } else {
            return wand.getFocus(stack).getMaxAreaSize(wand.getFocusItem(stack));
        }
    }

    public static int getAreaZ(ItemStack stack) {
        ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("areaz")) {
            int a = stack.getTagCompound().getInteger("areaz");
            if (a > wand.getFocus(stack).getMaxAreaSize(wand.getFocusItem(stack))) {
                a = wand.getFocus(stack).getMaxAreaSize(wand.getFocusItem(stack));
            }

            return a;
        } else {
            return wand.getFocus(stack).getMaxAreaSize(wand.getFocusItem(stack));
        }
    }

    public static void setAreaX(ItemStack stack, int area) {
        if (stack.hasTagCompound()) {
            stack.getTagCompound().setInteger("areax", area);
        }

    }

    public static void setAreaY(ItemStack stack, int area) {
        if (stack.hasTagCompound()) {
            stack.getTagCompound().setInteger("areay", area);
        }

    }

    public static void setAreaZ(ItemStack stack, int area) {
        if (stack.hasTagCompound()) {
            stack.getTagCompound().setInteger("areaz", area);
        }

    }

    public static void setAreaDim(ItemStack stack, int dim) {
        if (stack.hasTagCompound()) {
            stack.getTagCompound().setInteger("aread", dim);
        }

    }

    static boolean isOnCooldown(EntityLivingBase entityLiving) {
        if (entityLiving.world.isRemote && cooldownClient.containsKey(entityLiving.getEntityId())) {
            return cooldownClient.get(entityLiving.getEntityId()) > System.currentTimeMillis();
        } else if (!entityLiving.world.isRemote && cooldownServer.containsKey(entityLiving.getEntityId())) {
            return cooldownServer.get(entityLiving.getEntityId()) > System.currentTimeMillis();
        } else {
            return false;
        }
    }

    public static float getCooldown(EntityLivingBase entityLiving) {
        return entityLiving.world.isRemote && cooldownClient.containsKey(entityLiving.getEntityId()) ? (float) (cooldownClient.get(entityLiving.getEntityId()) - System.currentTimeMillis()) / 1000.0F : 0.0F;
    }

    public static void setCooldown(EntityLivingBase entityLiving, int cd) {
        if (cd == 0) {
            cooldownClient.remove(entityLiving.getEntityId());
            cooldownServer.remove(entityLiving.getEntityId());
        } else if (entityLiving.world.isRemote) {
            cooldownClient.put(entityLiving.getEntityId(), System.currentTimeMillis() + (long) cd);
        } else {
            cooldownServer.put(entityLiving.getEntityId(), System.currentTimeMillis() + (long) cd);
        }

    }

    public boolean performTrigger(World world, ItemStack wand, EntityPlayer player, int x, int y, int z, int side, int event) {
        switch (event) {
            case 0:
                return createThaumonomicon(wand, player, world, x, y, z);
            case 1:
                return createCrucible(wand, player, world, x, y, z);
            case 2:
                if (ResearchManager.isResearchComplete(player.getName(), "INFERNALFURNACE")) {
                    return createArcaneFurnace(wand, player, world, x, y, z);
                }
                break;
            case 3:
                if (ResearchManager.isResearchComplete(player.getName(), "INFUSION")) {
                    return createInfusionAltar(wand, player, world, x, y, z);
                }
                break;
            case 4:
                if (ResearchManager.isResearchComplete(player.getName(), "NODEJAR")) {
                    return createNodeJar(wand, player, world, x, y, z);
                }
                break;
            case 5:
                if (ResearchManager.isResearchComplete(player.getName(), "THAUMATORIUM")) {
                    return createThaumatorium(wand, player, world, x, y, z, side);
                }
                break;
            case 6:
                if (ResearchManager.isResearchComplete(player.getName(), "OCULUS")) {
                    return createOculus(wand, player, world, x, y, z, side);
                }
                break;
            case 7:
                if (ResearchManager.isResearchComplete(player.getName(), "ADVALCHEMYFURNACE")) {
                    return createAdvancedAlchemicalFurnace(wand, player, world, x, y, z, side);
                }
        }

        return false;
    }

    private static boolean createAdvancedAlchemicalFurnace(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side) {
        if (!world.isRemote) {
            int[][][] blueprint = new int[][][]{{{4, 4, 4}, {4, 3, 4}, {4, 4, 4}}, {{1, 2, 1}, {2, 0, 2}, {1, 2, 1}}};

            for (int a = -1; a <= 1; ++a) {
                for (int b = -1; b <= 1; ++b) {
                    for (int c = -1; c <= 1; ++c) {
                        if (world.getBlockState(new net.minecraft.util.math.BlockPos(x + a, y + b, z + c)).getBlock() == ConfigBlocks.blockStoneDevice &&
        world.getBlockState(new net.minecraft.util.math.BlockPos(x + a, y + b, z + c)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + a, y + b, z + c))) == 0) {
                            for (int aa = -1; aa <= 1; ++aa) {
                                for (int bb = 0; bb <= 1; ++bb) {
                                    int cc = -1;

                                    while (cc <= 1) {
                                        if (blueprint[bb][aa + 1][cc + 1] != 1 || world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc)).getBlock() == ConfigBlocks.blockMetalDevice &&
        world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc))) == 1) {
                                            if (blueprint[bb][aa + 1][cc + 1] != 2 || world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc)).getBlock() == ConfigBlocks.blockMetalDevice && world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc))) == 9) {
                                                if (blueprint[bb][aa + 1][cc + 1] != 4 || world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc)).getBlock() == ConfigBlocks.blockMetalDevice && world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc))) == 3) {
                                                    if (blueprint[bb][aa + 1][cc + 1] != 3 || world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc)).getBlock() == ConfigBlocks.blockStoneDevice && world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + a + aa, y + b + bb, z + c + cc))) == 0) {
                                                        ++cc;
                                                        continue;
                                                    }

                                                    return false;
                                                }

                                                return false;
                                            }

                                            return false;
                                        }

                                        return false;
                                    }
                                }
                            }

                            ItemWandCasting wand = (ItemWandCasting) itemstack.getItem();
                            if (!wand.consumeAllVisCrafting(itemstack, player, (new AspectList()).add(Aspect.FIRE, 50).add(Aspect.WATER, 50).add(Aspect.ORDER, 50), true)) {
                                return false;
                            }
        world.setBlockState(new BlockPos(x + a, y + b, z + c), ConfigBlocks.blockAlchemyFurnace.getDefaultState());
        world.setBlockState(new BlockPos(x + a - 1, y + b, z + c), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(1), 3);
        world.setBlockState(new BlockPos(x + a + 1, y + b, z + c), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(1), 3);
        world.setBlockState(new BlockPos(x + a, y + b, z + c - 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(1), 3);
        world.setBlockState(new BlockPos(x + a, y + b, z + c + 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(1), 3);
        world.setBlockState(new BlockPos(x + a - 1, y + b, z + c - 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(4), 3);
        world.setBlockState(new BlockPos(x + a + 1, y + b, z + c + 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(4), 3);
        world.setBlockState(new BlockPos(x + a + 1, y + b, z + c - 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(4), 3);
        world.setBlockState(new BlockPos(x + a - 1, y + b, z + c + 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(4), 3);
        world.setBlockState(new BlockPos(x + a - 1, y + b + 1, z + c), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(3), 3);
        world.setBlockState(new BlockPos(x + a + 1, y + b + 1, z + c), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(3), 3);
        world.setBlockState(new BlockPos(x + a, y + b + 1, z + c - 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(3), 3);
        world.setBlockState(new BlockPos(x + a, y + b + 1, z + c + 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(3), 3);
        world.setBlockState(new BlockPos(x + a - 1, y + b + 1, z + c - 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(2), 3);
        world.setBlockState(new BlockPos(x + a + 1, y + b + 1, z + c + 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(2), 3);
        world.setBlockState(new BlockPos(x + a + 1, y + b + 1, z + c - 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(2), 3);
        world.setBlockState(new BlockPos(x + a - 1, y + b + 1, z + c + 1), ConfigBlocks.blockAlchemyFurnace.getStateFromMeta(2), 3);
                            world.playSound(null, (double) (x + a) + (double) 0.5F, (double) (y + b) + (double) 0.5F, (double) (z + c) + (double) 0.5F, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);

                            for (int aa = -1; aa <= 1; ++aa) {
                                for (int bb = 0; bb <= 1; ++bb) {
                                    for (int cc = -1; cc <= 1; ++cc) {
                                        PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(x + a + aa, y + b + bb, z + c + cc, -9999), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x + a, y + b, z + c, 32.0F));
                                    }
                                }
                            }

                            return true;
                        }
                    }
                }
            }

        }
        return false;
    }
}
