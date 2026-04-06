package thaumcraft.common.entities.ai.fluid;

import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tc4tweak.ConfigurationHandler;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;
import thaumcraft.common.entities.golems.Marker;

public class AILiquidGather extends EntityAIBase {
    private EntityGolemBase theGolem;
    private int waterX;
    private int waterY;
    private int waterZ;
    private EnumFacing markerOrientation;
    private World theWorld;
    private float pumpDist = 0.0F;
    int count = 0;
    HashMap<BlockPos, ArrayList<SourceBlock>> queue = new HashMap<>();
    ArrayList<BlockPos> cache = new ArrayList<>();
    BlockPos origin = null;

    public AILiquidGather(EntityGolemBase par1EntityCreature) {
        this.theGolem = par1EntityCreature;
        this.theWorld = par1EntityCreature.world;
        this.setMutexBits(3);
    }

    public boolean shouldExecute() {
        ArrayList<FluidStack> fluids = GolemHelper.getMissingLiquids(this.theGolem);
        if (fluids == null) {
            return false;
        } else if (this.theGolem.itemWatched != null && !fluids.isEmpty() && this.theGolem.getNavigator().noPath()) {
            int camt = 0;
            if (this.theGolem.fluidCarried != null) {
                camt = this.theGolem.fluidCarried.amount;
            }

            int max = this.theGolem.getFluidCarryLimit();

            for (FluidStack fluid : fluids) {
                for (Marker marker : GolemHelper.getMarkedFluidHandlersAdjacentToGolem(fluid, this.theWorld, this.theGolem)) {
                    TileEntity te = this.theWorld.getTileEntity(new BlockPos(marker.x, marker.y, marker.z));
                    if (te instanceof IFluidHandler) {
                        FluidStack fs = ((IFluidHandler) te).drain(new FluidStack(fluid.getFluid(), max - camt), false);
                        if (fs != null && fs.amount > 0) {
                            return true;
                        }
                    }
                }

                for (BlockPos loc : GolemHelper.getMarkedBlocksAdjacentToGolem(this.theWorld, this.theGolem, (byte) -1)) {
                    Block bi = this.theWorld.getBlockState(loc).getBlock();
                    if (FluidRegistry.getFluid(fluid.getFluid().getName()).getBlock() == bi) {
                        if (bi instanceof IFluidBlock && ((IFluidBlock) bi).canDrain(this.theWorld, loc)) {
                            FluidStack fs = ((IFluidBlock) bi).drain(this.theWorld, loc, false);
                            return fs != null && fs.amount <= max - camt;
                        }

                        if (fluid.getFluid() == FluidRegistry.WATER || fluid.getFluid() == FluidRegistry.LAVA) {
                            return this.theWorld.getBlockState(loc).getValue(net.minecraft.block.BlockLiquid.LEVEL) == 0;
                        }
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public boolean continueExecuting() {
        return this.count < 20 && this.theGolem.itemWatched != null;
    }

    public boolean isInterruptible() {
        return false;
    }

    public void startExecuting() {
        this.count = 0;
    }

    public void resetTask() {
        this.count = 0;
        this.theGolem.itemWatched = null;
        super.resetTask();
    }

    public void updateTask() {
        ++this.count;
        if (this.count >= 10) {
            int camt = 0;
            if (this.theGolem.fluidCarried != null) {
                camt = this.theGolem.fluidCarried.amount;
            }

            int max = this.theGolem.getFluidCarryLimit();
            ArrayList<FluidStack> fluids = GolemHelper.getMissingLiquids(this.theGolem);
            if (fluids != null) {
                for (FluidStack fluidstack : fluids) {
                    for (Marker marker : GolemHelper.getMarkedFluidHandlersAdjacentToGolem(fluidstack, this.theWorld, this.theGolem)) {
                        TileEntity te = this.theWorld.getTileEntity(new BlockPos(marker.x, marker.y, marker.z));
                        if (te instanceof IFluidHandler) {
                            FluidStack fs = ((IFluidHandler) te).drain(new FluidStack(fluidstack.getFluid(), max - camt), true);
                            if (fs != null && fs.amount > 0) {
                                if (this.theGolem.fluidCarried != null) {
                                    FluidStack var31 = this.theGolem.fluidCarried;
                                    var31.amount += fs.amount;
                                } else {
                                    this.theGolem.fluidCarried = fs.copy();
                                }

                                if (fs.amount > 200) {
                                    this.theWorld.playSound(null, this.theGolem.getPosition(), net.minecraft.init.SoundEvents.ENTITY_GENERIC_SWIM, net.minecraft.util.SoundCategory.NEUTRAL, 0.2F * ((float) fs.amount / (float) max), 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
                                }

                                this.theGolem.updateCarried();
                                if (this.theGolem.fluidCarried.amount >= this.theGolem.getFluidCarryLimit()) {
                                    this.theGolem.itemWatched = null;
                                }

                                this.count = 0;
                            }
                        }
                    }

                    for (BlockPos loc : GolemHelper.getMarkedBlocksAdjacentToGolem(this.theWorld, this.theGolem, (byte) -1)) {
                        Block bi = this.theWorld.getBlockState(loc).getBlock();
                        int i = loc.getX();
                        int j = loc.getY();
                        int k = loc.getZ();
                        if (this.theGolem.getUpgradeAmount(5) > 0) {
                            if (!this.queue.containsKey(loc) || this.queue.get(loc).isEmpty()) {
                                this.rebuildQueue(loc, fluidstack.getFluid());
                            }

                            if (this.queue.containsKey(loc) && !this.queue.get(loc).isEmpty()) {
                                ArrayList<SourceBlock> t = this.queue.get(loc);

                                do {
                                    BlockPos current = t.get(0).loc;
                                    i = current.getX();
                                    j = current.getY();
                                    k = current.getZ();
                                    t.remove(0);
                                } while (!t.isEmpty() && !this.validFluidBlock(fluidstack.getFluid(), i, j, k));

                                this.queue.put(loc, t);
                            }
                        }

                        if (FluidRegistry.getFluid(fluidstack.getFluid().getName()).getBlock() == bi) {
                            if (bi instanceof BlockFluidBase && ((IFluidBlock) bi).canDrain(this.theWorld, new BlockPos(i, j, k))) {
                                FluidStack fs = ((IFluidBlock) bi).drain(this.theWorld, new BlockPos(i, j, k), false);
                                if (fs != null && fs.amount <= max - camt) {
                                    ((IFluidBlock) bi).drain(this.theWorld, new BlockPos(i, j, k), true);
                                    if (this.theGolem.fluidCarried != null) {
                                        FluidStack var33 = this.theGolem.fluidCarried;
                                        var33.amount += fs.amount;
                                    } else {
                                        this.theGolem.fluidCarried = fs.copy();
                                    }

                                    this.theWorld.setBlockToAir(new BlockPos(i, j, k));
                                    this.theWorld.playSound(null, this.theGolem.getPosition(), net.minecraft.init.SoundEvents.ENTITY_GENERIC_SWIM, net.minecraft.util.SoundCategory.NEUTRAL, 0.2F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
                                    this.theGolem.updateCarried();
                                    if (this.theGolem.fluidCarried.amount > this.theGolem.getFluidCarryLimit() - 1000) {
                                        this.theGolem.itemWatched = null;
                                    }

                                    this.count = 0;
                                }
                            } else if (fluidstack.getFluid() == FluidRegistry.WATER || fluidstack.getFluid() == FluidRegistry.LAVA) {
                                int wmd = this.theWorld.getBlockState(new BlockPos(i, j, k)).getValue(net.minecraft.block.BlockLiquid.LEVEL);
                                if ((FluidRegistry.lookupFluidForBlock(bi) == FluidRegistry.WATER && fluidstack.getFluid() == FluidRegistry.WATER || FluidRegistry.lookupFluidForBlock(bi) == FluidRegistry.LAVA && fluidstack.getFluid() == FluidRegistry.LAVA) && wmd == 0) {
                                    FluidStack fs = new FluidStack(fluidstack.getFluid(), 1000);
                                    if (this.theGolem.fluidCarried != null) {
                                        FluidStack var32 = this.theGolem.fluidCarried;
                                        var32.amount += fs.amount;
                                    } else {
                                        this.theGolem.fluidCarried = fs.copy();
                                    }

                                    this.theWorld.setBlockToAir(new BlockPos(i, j, k));
                                    this.theWorld.playSound(null, this.theGolem.getPosition(), net.minecraft.init.SoundEvents.ENTITY_GENERIC_SWIM, net.minecraft.util.SoundCategory.NEUTRAL, 0.2F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
                                    this.theGolem.updateCarried();
                                    if (this.theGolem.fluidCarried.amount > this.theGolem.getFluidCarryLimit() - 1000) {
                                        this.theGolem.itemWatched = null;
                                    }

                                    this.count = 0;
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private boolean validFluidBlock(Fluid fluid, int i, int j, int k) {
        BlockPos pos = new BlockPos(i, j, k);
        Block bi = this.theWorld.getBlockState(pos).getBlock();
        if (FluidRegistry.lookupFluidForBlock(bi) != fluid) {
            return false;
        } else {
            if (bi instanceof BlockFluidBase && ((IFluidBlock) bi).canDrain(this.theWorld, pos)) {
                FluidStack fs = ((IFluidBlock) bi).drain(this.theWorld, pos, false);
                if (fs != null) {
                    return true;
                }
            }

            return (FluidRegistry.lookupFluidForBlock(bi) == FluidRegistry.WATER && fluid == FluidRegistry.WATER || FluidRegistry.lookupFluidForBlock(bi) == FluidRegistry.LAVA && fluid == FluidRegistry.LAVA) && this.theWorld.getBlockState(pos).getValue(net.minecraft.block.BlockLiquid.LEVEL) == 0;
        }
    }

    private void rebuildQueue(BlockPos loc, Fluid fluid) {
        this.pumpDist = this.theGolem.getRange() * this.theGolem.getRange();
        this.cache.clear();
        this.origin = loc;
        ArrayList<SourceBlock> sources = new ArrayList<>();
        getConnectedFluidBlocks_tweak(this.theWorld, loc.getX(), loc.getY(), loc.getZ(), fluid, sources, pumpDist);
        sources.sort(Collections.reverseOrder());
        this.queue.put(loc, sources);
    }

    private static boolean validFluidBlock_tweak(World world, Fluid fluid, int i, int j, int k) {
        BlockPos pos = new BlockPos(i, j, k);
        Block bi = world.getBlockState(pos).getBlock();
        if (FluidRegistry.lookupFluidForBlock(bi) != fluid) {
            return false;
        } else {
            if (bi instanceof BlockFluidBase && ((IFluidBlock) bi).canDrain(world, pos)) {
                FluidStack fs = ((IFluidBlock) bi).drain(world, pos, false);
                if (fs != null) {
                    return true;
                }
            }

            return (FluidRegistry.lookupFluidForBlock(bi) == FluidRegistry.WATER && fluid == FluidRegistry.WATER || FluidRegistry.lookupFluidForBlock(bi) == FluidRegistry.LAVA && fluid == FluidRegistry.LAVA) && world.getBlockState(pos).getValue(net.minecraft.block.BlockLiquid.LEVEL) == 0;
        }
    }

    public static void getConnectedFluidBlocks_tweak(World world, int x, int y, int z, Fluid fluid, ArrayList<SourceBlock> sources, float pumpDist) {
        if (fluid == null) return;
        Set<BlockPos> seen = new HashSet<>();  // BlockPos has quite terrible hash function, but there are multiple different optimization mod that optimize this. given the popularity of those I'd say it's fine to use it
        Queue<BlockPos> toVisit = new ArrayDeque<>();
        BlockPos origin = new BlockPos(x, y, z);
        toVisit.add(origin);
        while (!toVisit.isEmpty()) {
            BlockPos v = toVisit.poll();
            if (seen.contains(v)) continue;
            seen.add(v);
            float dist = (float)v.distanceSq(x, y, z);
            if (dist > pumpDist) continue;
            Block block = world.getBlockState(v).getBlock();
            if (block == Blocks.FLOWING_LAVA)
                block = Blocks.LAVA;
            else if (block == Blocks.FLOWING_WATER)
                block = Blocks.WATER;
            Fluid f = FluidRegistry.lookupFluidForBlock(block);
            if (f != fluid) continue;
            if (validFluidBlock_tweak(world, fluid, v.getX(), v.getY(), v.getZ())) {
                sources.add(new SourceBlock(v, dist));
                if (sources.size() >= ConfigurationHandler.INSTANCE.getDecantMaxBlocks())
                    return;
            }
            for (EnumFacing direction : EnumFacing.values()) {
                toVisit.add(new BlockPos(v.getX() + direction.getXOffset(), v.getY() + direction.getYOffset(), v.getZ() + direction.getZOffset()));
            }
        }
    }

    private void getConnectedFluidBlocks(World world, int x, int y, int z, Fluid fluid, ArrayList<SourceBlock> sources) {
        try {
            if (this.cache.contains(new BlockPos(x, y, z))) {
                return;
            }

            this.cache.add(new BlockPos(x, y, z));

            for (int a = -1; a <= 1; ++a) {
                for (int b = -1; b <= 1; ++b) {
                    for (int c = -1; c <= 1; ++c) {
                        if (a != 0 || b != 0 || c != 0) {
                            int xx = x + a;
                            int yy = y + b;
                            int zz = z + c;
                            BlockPos cc = new BlockPos(xx, yy, zz);
                            float dist = (float)cc.distanceSq(this.origin.getX(), this.origin.getY(), this.origin.getZ());
                            if (!(dist > this.pumpDist)) {
                                Block bi = world.getBlockState(new BlockPos(xx, yy, zz)).getBlock();
                                if (bi == Blocks.FLOWING_LAVA) {
                                    bi = Blocks.LAVA;
                                }

                                if (bi == Blocks.FLOWING_WATER) {
                                    bi = Blocks.WATER;
                                }

                                Fluid fi = FluidRegistry.lookupFluidForBlock(bi);
                                if (fi != null && fi == fluid) {
                                    if (this.validFluidBlock(fluid, xx, yy, zz)) {
                                        sources.add(new SourceBlock(cc, dist));
                                    }

                                    this.getConnectedFluidBlocks(world, xx, yy, zz, fluid, sources);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

    }

    public static class SourceBlock implements Comparable<SourceBlock> {
        BlockPos loc;
        float dist;

        public SourceBlock(BlockPos loc, float dist) {
            this.loc = loc;
            this.dist = dist;
        }

        @Override
        public int compareTo(SourceBlock target) {
            return Float.compare(this.dist, target.dist);
        }

    }
}
