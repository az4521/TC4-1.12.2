package thaumcraft.common.lib.events;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.apache.logging.log4j.Level;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.lib.world.ChunkLoc;
import thaumcraft.common.tiles.TileSensor;
import net.minecraft.util.math.BlockPos;

public class ServerTickEventsFML {
    public static Map<Integer, LinkedBlockingQueue<VirtualSwapper>> swapList = new HashMap<>();
    public static HashMap<Integer, ArrayList<ChunkLoc>> chunksToGenerate = new HashMap<>();

    @SubscribeEvent
    public void serverWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side != Side.CLIENT) {
            if (event.phase != Phase.START) {
                this.tickChunkRegeneration(event);
                this.tickBlockSwap(event.world);
                if (TileSensor.noteBlockEvents.get(event.world) != null) {
                    ((ArrayList) TileSensor.noteBlockEvents.get(event.world)).clear();
                }
            }

        }
    }

    public void tickChunkRegeneration(TickEvent.WorldTickEvent event) {
        int dim = event.world.provider.getDimension();
        int count = 0;
        ArrayList<ChunkLoc> chunks = chunksToGenerate.get(dim);
        if (chunks != null && !chunks.isEmpty()) {
            for (int a = 0; a < 10; ++a) {
                chunks = chunksToGenerate.get(dim);
                if (chunks == null || chunks.isEmpty()) {
                    break;
                }

                ++count;
                ChunkLoc loc = chunks.get(0);
                long worldSeed = event.world.getSeed();
                Random fmlRandom = new Random(worldSeed);
                long xSeed = fmlRandom.nextLong() >> 3;
                long zSeed = fmlRandom.nextLong() >> 3;
                fmlRandom.setSeed(xSeed * (long) loc.x + zSeed * (long) loc.z ^ worldSeed);
                Thaumcraft.instance.worldGen.worldGeneration(fmlRandom, loc.x, loc.z, event.world, false);
                chunks.remove(0);
                chunksToGenerate.put(dim, chunks);
            }
        }

        if (count > 0) {
            FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, "[Thaumcraft] Regenerated {} chunks. {} chunks left", count, chunks.size());
        }

    }

    private void tickBlockSwap(World world) {
        int dim = world.provider.getDimension();
        LinkedBlockingQueue<VirtualSwapper> queue = swapList.get(dim);
        if (queue != null) {
            boolean didSomething = false;

            while (!didSomething) {
                VirtualSwapper vs = queue.poll();
                if (vs != null) {
                    Block bi = world.getBlockState(new BlockPos(vs.x, vs.y, vs.z)).getBlock();
                    net.minecraft.block.state.IBlockState biState = world.getBlockState(new BlockPos(vs.x, vs.y, vs.z));
                    int md = biState.getBlock().getMetaFromState(biState);
                    ItemWandCasting wand = null;
                    ItemFocusBasic focus = null;
                    ItemStack focusStack = null;
                    if (!vs.player.inventory.getStackInSlot(vs.wand).isEmpty()
                            && vs.player.inventory.getStackInSlot(vs.wand).getItem() instanceof ItemWandCasting) {
                        wand = (ItemWandCasting) vs.player.inventory.getStackInSlot(vs.wand).getItem();
                        focusStack = wand.getFocusItem(vs.player.inventory.getStackInSlot(vs.wand));
                        focus = wand.getFocus(vs.player.inventory.getStackInSlot(vs.wand));
                    }

                    if (vs.player.canHarvestBlock(world.getBlockState(new BlockPos(vs.x, vs.y, vs.z)))
                            && !vs.target.isItemEqual(new ItemStack(bi, 1, md))
                            && wand != null
                            && focus != null
                            && wand.consumeAllVis(
                                    vs.player.inventory.getStackInSlot(vs.wand),
                            vs.player,
                            focus.getVisCost(focusStack), false, false)
                    ) {
                        int slot = InventoryUtils.isPlayerCarrying(vs.player, vs.target);
                        if (vs.player.capabilities.isCreativeMode) {
                            slot = 1;
                        }

                        if (vs.bSource == bi && vs.mSource == md && slot >= 0) {
                            didSomething = true;
                            if (!vs.player.capabilities.isCreativeMode) {
                                int fortune = wand.getFocusTreasure(vs.player.inventory.getStackInSlot(vs.wand));
                                boolean silk = wand.getFocus(vs.player.inventory.getStackInSlot(vs.wand)).isUpgradedWith(wand.getFocusItem(vs.player.inventory.getStackInSlot(vs.wand)), FocusUpgradeType.silktouch);
                                vs.player.inventory.decrStackSize(slot, 1);
                                BlockPos vsPos = new BlockPos(vs.x, vs.y, vs.z);
                                net.minecraft.block.state.IBlockState vsState = world.getBlockState(vsPos);
                                net.minecraft.util.NonNullList<ItemStack> retNNL = net.minecraft.util.NonNullList.create();
                                if (silk && bi.canSilkHarvest(world, vsPos, vsState, vs.player)) {
                                    ItemStack itemstack = BlockUtils.createStackedBlock(bi, md);
                                    if (itemstack != null) {
                                        retNNL.add(itemstack);
                                    }
                                } else {
                                    bi.getDrops(retNNL, world, vsPos, vsState, fortune);
                                }
                                ArrayList<ItemStack> ret = new ArrayList<>(retNNL);

                                if (!ret.isEmpty()) {
                                    for (ItemStack is : ret) {
                                        if (!vs.player.inventory.addItemStackToInventory(is)) {
                                            world.spawnEntity(new EntityItem(world, (double) vs.x + (double) 0.5F, (double) vs.y + (double) 0.5F, (double) vs.z + (double) 0.5F, is));
                                        }
                                    }
                                }

                                wand.consumeAllVis(vs.player.inventory.getStackInSlot(vs.wand), vs.player, focus.getVisCost(focusStack), true, false);
                            }

                            world.setBlockState(new BlockPos(vs.x, vs.y, vs.z), Block.getBlockFromItem(vs.target.getItem()).getStateFromMeta(vs.target.getItemDamage()), 3);
                            Block placedBlock = Block.getBlockFromItem(vs.target.getItem());
                            placedBlock.onBlockPlacedBy(world, new BlockPos(vs.x, vs.y, vs.z), placedBlock.getStateFromMeta(vs.target.getItemDamage()), vs.player, vs.target);
                            PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(vs.x, vs.y, vs.z, 12632319), new NetworkRegistry.TargetPoint(world.provider.getDimension(), vs.x, vs.y, vs.z, 32.0F));
                            world.playEvent(2001, new BlockPos(vs.x, vs.y, vs.z), Block.getIdFromBlock(vs.bSource) + (vs.mSource << 12));
                            if (vs.lifespan > 0) {
                                for (int xx = -1; xx <= 1; ++xx) {
                                    for (int yy = -1; yy <= 1; ++yy) {
                                        for (int zz = -1; zz <= 1; ++zz) {
                                            if ((xx != 0 || yy != 0 || zz != 0) && world.getBlockState(new BlockPos(vs.x + xx, vs.y + yy, vs.z + zz)).getBlock() == vs.bSource && world.getBlockState(new BlockPos(vs.x + xx, vs.y + yy, vs.z + zz)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(vs.x + xx, vs.y + yy, vs.z + zz))) == vs.mSource && BlockUtils.isBlockExposed(world, vs.x + xx, vs.y + yy, vs.z + zz)) {
                                                queue.offer(new VirtualSwapper(vs.x + xx, vs.y + yy, vs.z + zz, vs.bSource, vs.mSource, vs.target, vs.lifespan - 1, vs.player, vs.wand));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    didSomething = true;
                }
            }

            swapList.put(dim, queue);
        }

    }

    public static void addSwapper(World world, int x, int y, int z, Block bs, int ms, ItemStack target, int life, EntityPlayer player, int wand) {
        int dim = world.provider.getDimension();
        BlockPos bsPos = new BlockPos(x, y, z);
        if (bs != Blocks.AIR && !(bs.getBlockHardness(world.getBlockState(bsPos), world, bsPos) < 0.0F) && !target.isItemEqual(new ItemStack(bs, 1, ms))) {
            LinkedBlockingQueue<VirtualSwapper> queue = swapList.get(dim);
            if (queue == null) {
                swapList.put(dim, new LinkedBlockingQueue<>());
                queue = swapList.get(dim);
            }

            queue.offer(new VirtualSwapper(x, y, z, bs, ms, target, life, player, wand));
            world.playSound(null, player.posX, player.posY, player.posZ, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.PLAYERS, 0.25F, 1.0F);
            swapList.put(dim, queue);
        }
    }

    public static class VirtualSwapper {
        int lifespan = 0;
        int x = 0;
        int y = 0;
        int z = 0;
        Block bSource;
        int mSource = 0;
        ItemStack target;
        int wand = 0;
        EntityPlayer player = null;

        VirtualSwapper(int x, int y, int z, Block bs, int ms, ItemStack t, int life, EntityPlayer p, int wand) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.bSource = bs;
            this.mSource = ms;
            this.target = t;
            this.lifespan = life;
            this.player = p;
            this.wand = wand;
        }
    }

    public static class RestorableWardedBlock {
        int x = 0;
        int y = 0;
        int z = 0;
        Block bi;
        int md = 0;
        NBTTagCompound nbt = null;

        RestorableWardedBlock(World world, int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.bi = world.getBlockState(new BlockPos(x, y, z)).getBlock();
            net.minecraft.block.state.IBlockState biState2 = world.getBlockState(new BlockPos(x, y, z));
            this.md = biState2.getBlock().getMetaFromState(biState2);
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te != null) {
                this.nbt = new NBTTagCompound();
                te.writeToNBT(this.nbt);
            }

        }
    }
}
