package thaumcraft.common.tiles;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRepairableExtended;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.IWandable;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.equipment.ItemElementalPickaxe;
import thaumcraft.common.items.wands.foci.ItemFocusExcavation;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.misc.PacketBoreDig;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.lib.utils.TCVec3d;
import thaumcraft.common.lib.utils.Utils;

import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;

public class TileArcaneBore extends TileThaumcraft implements IInventory, IWandable, net.minecraft.util.ITickable {
    public int spiral = 0;
    public float currentRadius = 0.0F;
    public int maxRadius = 2;
    public float vRadX = 0.0F;
    public float vRadZ = 0.0F;
    public float tRadX = 0.0F;
    public float tRadZ = 0.0F;
    public float mRadX = 0.0F;
    public float mRadZ = 0.0F;
    private int count = 0;
    public int topRotation = 0;
    long soundDelay = 0L;
    Object beam1 = null;
    Object beam2 = null;
    int beamlength = 0;
    TileArcaneBoreBase base = null;
    public ItemStack[] contents = new ItemStack[2];
    public int rotX = 0;
    public int rotZ = 0;
    public int tarX = 0;
    public int tarZ = 0;
    public int speedX = 0;
    public int speedZ = 0;
    public boolean hasFocus = false;
    public boolean hasPickaxe = false;
    int lastX = 0;
    int lastZ = 0;
    int lastY = 0;
    boolean toDig = false;
    int digX = 0;
    int digZ = 0;
    int digY = 0;
    Block digBlock;
    int digMd;
    float radInc;
    int paused;
    int maxPause;
    long repairCounter;
    boolean first;
    public EnumFacing orientation;
    public EnumFacing baseOrientation;
    FakePlayer fakePlayer;
    private AspectList repairCost;
    private AspectList currentRepairVis;
    public int fortune;
    public int speed;
    public int area;
    int blockCount;
    private float speedyTime;
    private final int itemsPerVis;

    public TileArcaneBore() {
        this.digBlock = Blocks.AIR;
        this.digMd = 0;
        this.radInc = 0.0F;
        this.paused = 100;
        this.maxPause = 100;
        this.repairCounter = 0L;
        this.first = true;
        this.orientation = EnumFacing.byIndex(1);
        this.baseOrientation = EnumFacing.byIndex(1);
        this.fakePlayer = null;
        this.repairCost = new AspectList();
        this.currentRepairVis = new AspectList();
        this.fortune = 0;
        this.speed = 0;
        this.area = 0;
        this.blockCount = 0;
        this.itemsPerVis = 20;
    }

    @Override
    public void update() {
        if (!this.world.isRemote && this.speedyTime < 20.0F) {
            this.speedyTime += (float) VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), Aspect.ENTROPY, 100) / 5.0F;
            if (this.speedyTime < 20.0F && this.base != null && this.base.drawEssentia()) {
                float var10001 = this.speedyTime;
                this.getClass();
                this.speedyTime = var10001 + 20.0F;
            }
        }

        if (!this.world.isRemote && this.fakePlayer == null) {
            this.fakePlayer = FakePlayerFactory.get((WorldServer) this.world, new GameProfile(null, "FakeThaumcraftBore"));
        }

        if (this.world.isRemote && this.first) {
            this.setOrientation(this.orientation, true);
            this.first = false;
        }

        if (this.rotX < this.tarX) {
            this.rotX += this.speedX;
            if (this.rotX < this.tarX) {
                ++this.speedX;
            } else {
                this.speedX = (int) ((float) this.speedX / 3.0F);
            }
        } else if (this.rotX > this.tarX) {
            this.rotX += this.speedX;
            if (this.rotX > this.tarX) {
                --this.speedX;
            } else {
                this.speedX = (int) ((float) this.speedX / 3.0F);
            }
        } else {
            this.speedX = 0;
        }

        if (this.rotZ < this.tarZ) {
            this.rotZ += this.speedZ;
            if (this.rotZ < this.tarZ) {
                ++this.speedZ;
            } else {
                this.speedZ = (int) ((float) this.speedZ / 3.0F);
            }
        } else if (this.rotZ > this.tarZ) {
            this.rotZ += this.speedZ;
            if (this.rotZ > this.tarZ) {
                --this.speedZ;
            } else {
                this.speedZ = (int) ((float) this.speedZ / 3.0F);
            }
        } else {
            this.speedZ = 0;
        }

        if (this.gettingPower() && this.areItemsValid()) {
            this.dig();
        } else if (this.world.isRemote) {
            if (this.topRotation % 90 != 0) {
                this.topRotation += Math.min(10, 90 - this.topRotation % 90);
            }

            this.vRadX *= 0.9F;
            this.vRadZ *= 0.9F;
        }

        if (!this.world.isRemote && this.hasPickaxe && !this.getStackInSlot(1).isEmpty()) {
            if (this.repairCounter++ % 40L == 0L && this.getStackInSlot(1).isItemDamaged()) {
                this.doRepair(this.getStackInSlot(1), this.fakePlayer);
            }

            if (this.repairCost != null && this.repairCost.size() > 0 && this.repairCounter % 5L == 0L) {
                for (Aspect a : this.repairCost.getAspects()) {
                    if (this.currentRepairVis.getAmount(a) < this.repairCost.getAmount(a)) {
                        this.currentRepairVis.add(a, VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), a, this.repairCost.getAmount(a)));
                    }
                }
            }

            this.fakePlayer.ticksExisted = (int) this.repairCounter;

            try {
                this.getStackInSlot(1).updateAnimation(this.world, this.fakePlayer, 0, true);
            } catch (Exception ignored) {
            }
        }

    }

    private void doRepair(ItemStack is, EntityPlayer player) {
        int level = EnchantmentHelper.getEnchantmentLevel(Config.enchRepair, is);
        if (level > 0) {
            if (level > 2) {
                level = 2;
            }

            if (is.getItem() instanceof IRepairable) {
                AspectList cost = ThaumcraftCraftingManager.getObjectTags(is);
                if (cost == null || cost.size() == 0) {
                    return;
                }

                cost = ResearchManager.reduceToPrimals(cost);

                for (Aspect a : cost.getAspects()) {
                    if (a != null) {
                        this.repairCost.merge(a, (int) Math.sqrt(cost.getAmount(a) * 2) * level);
                    }
                }

                boolean doIt = true;
                if (is.getItem() instanceof IRepairableExtended) {
                    doIt = ((IRepairableExtended) is.getItem()).doRepair(is, player, level);
                }

                if (doIt) {
                    for (Aspect a : this.repairCost.getAspects()) {
                        if (this.currentRepairVis.getAmount(a) < this.repairCost.getAmount(a)) {
                            doIt = false;
                            break;
                        }
                    }
                }

                if (doIt) {
                    for (Aspect a : this.repairCost.getAspects()) {
                        this.currentRepairVis.reduce(a, this.repairCost.getAmount(a));
                    }

                    is.damageItem(-level, player);
                    this.markDirty();
                }
            } else {
                this.repairCost = new AspectList();
            }

        }
    }

    private boolean areItemsValid() {
        boolean notNearBroken = !this.hasPickaxe || this.getStackInSlot(1).getItemDamage() + 1 < this.getStackInSlot(1).getMaxDamage();

        return this.hasFocus && this.hasPickaxe && this.getStackInSlot(1).isItemStackDamageable() && notNearBroken;
    }

    public void markDirty() {
        super.markDirty();
        this.fortune = 0;
        this.area = 0;
        this.speed = 0;
        if (!this.getStackInSlot(0).isEmpty() && this.getStackInSlot(0).getItem() instanceof ItemFocusExcavation) {
            this.fortune = ((ItemFocusExcavation) this.getStackInSlot(0).getItem()).getUpgradeLevel(this.getStackInSlot(0), FocusUpgradeType.treasure);
            this.area = ((ItemFocusExcavation) this.getStackInSlot(0).getItem()).getUpgradeLevel(this.getStackInSlot(0), FocusUpgradeType.enlarge);
            this.speed += ((ItemFocusExcavation) this.getStackInSlot(0).getItem()).getUpgradeLevel(this.getStackInSlot(0), FocusUpgradeType.potency);
            this.hasFocus = true;
        } else {
            this.hasFocus = false;
        }

        if (!this.getStackInSlot(1).isEmpty() && this.getStackInSlot(1).getItem() instanceof ItemPickaxe) {
            this.hasPickaxe = true;
            int f = EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.FORTUNE, this.getStackInSlot(1));
            if (f > this.fortune) {
                this.fortune = f;
            }

            this.speed += EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.EFFICIENCY, this.getStackInSlot(1));
        } else {
            this.hasPickaxe = false;
        }

    }

    private void dig() {
        if (this.rotX == this.tarX && this.rotZ == this.tarZ) {
            if (!this.world.isRemote) {
                boolean dug = false;
                if (this.base == null) {
                    this.base = (TileArcaneBoreBase) this.world.getTileEntity(this.getPos().offset(this.baseOrientation.getOpposite()));
                }

                if (--this.count > 0) {
                    return;
                }

                if (this.toDig) {
                    this.toDig = false;
                    Block bi = this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)).getBlock();
                    int md = this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)).getBlock().getMetaFromState(this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)));
                    if (!bi.isAir(this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)), this.world, new BlockPos(this.digX, this.digY, this.digZ))) {
                        int tfortune = this.fortune;
                        boolean silktouch = false;
                        if (!this.getStackInSlot(1).isEmpty() && EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.SILK_TOUCH, this.getStackInSlot(1)) > 0 && bi.canSilkHarvest(this.world, new BlockPos(this.digX, this.digY, this.digZ), this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)), this.fakePlayer)) {
                            silktouch = true;
                            tfortune = 0;
                        }

                        if (!silktouch && !this.getStackInSlot(0).isEmpty() && ((ItemFocusExcavation) this.getStackInSlot(0).getItem()).isUpgradedWith(this.getStackInSlot(0), FocusUpgradeType.silktouch) && bi.canSilkHarvest(this.world, new BlockPos(this.digX, this.digY, this.digZ), this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)), this.fakePlayer)) {
                            silktouch = true;
                            tfortune = 0;
                        }

                        this.world.addBlockEvent(this.getPos(), ConfigBlocks.blockWoodenDevice, 99, Block.getIdFromBlock(bi) + (md << 12));
                        List<ItemStack> items = new ArrayList<>();
                        if (silktouch) {
                            ItemStack dropped = BlockUtils.createStackedBlock(bi, md);
                            if (dropped != null) {
                                items.add(dropped);
                            }
                        } else {
                            items = bi.getDrops(this.world, new BlockPos(this.digX, this.digY, this.digZ), this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)), tfortune);
                        }

                        List<EntityItem> targets = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(this.digX, this.digY, this.digZ, this.digX + 1, this.digY + 1, this.digZ + 1).expand(1.0F, 1.0F, 1.0F));
                        if (!targets.isEmpty()) {
                            for (EntityItem e : targets) {
                                items.add(e.getItem().copy());
                                e.setDead();
                            }
                        }

                        if (!items.isEmpty()) {
                            for (ItemStack is : items) {
                                ItemStack dropped = is.copy();
                                if (!silktouch && (!this.getStackInSlot(1).isEmpty() && this.getStackInSlot(1).getItem() instanceof ItemElementalPickaxe || !this.getStackInSlot(0).isEmpty() && this.getStackInSlot(0).getItem() instanceof ItemFocusBasic && ((ItemFocusBasic) this.getStackInSlot(0).getItem()).isUpgradedWith(this.getStackInSlot(0), ItemFocusExcavation.dowsing))) {
                                    dropped = Utils.findSpecialMiningResult(is, 0.2F + (float) tfortune * 0.075F, this.world.rand);
                                }

                                if (this.base != null && this.base instanceof TileArcaneBoreBase) {
                                    TileEntity inventory = this.world.getTileEntity(this.base.getPos().offset(this.base.orientation));
                                    if (inventory instanceof IInventory) {
                                        dropped = InventoryUtils.placeItemStackIntoInventory(dropped, (IInventory) inventory, this.base.orientation.getOpposite().ordinal(), true);
                                    }

                                    if (dropped != null) {
                                        EntityItem ei = new EntityItem(this.world, (double) this.getPos().getX() + (double) 0.5F + (double) this.base.orientation.getXOffset() * 0.66, (double) this.getPos().getY() + 0.4 + (double) this.baseOrientation.getOpposite().getYOffset(), (double) this.getPos().getZ() + (double) 0.5F + (double) this.base.orientation.getZOffset() * 0.66, dropped.copy());
                                        ei.motionX = 0.075F * (float) this.base.orientation.getXOffset();
                                        ei.motionY = 0.025F;
                                        ei.motionZ = 0.075F * (float) this.base.orientation.getZOffset();
                                        this.world.spawnEntity(ei);
                                    }
                                }
                            }
                        }
                    }

                    this.setInventorySlotContents(1, InventoryUtils.damageItem(1, this.getStackInSlot(1), this.world));
                    if (this.getStackInSlot(1).getCount() <= 0) {
                        this.setInventorySlotContents(1, ItemStack.EMPTY);
                    }

                    this.world.setBlockToAir(new BlockPos(this.digX, this.digY, this.digZ));
                    if (this.base != null) {
                        for (int lb = 2; lb < 6; ++lb) {
                            EnumFacing lbd = EnumFacing.byIndex(lb);
                            TileEntity lbte = this.world.getTileEntity(this.base.getPos().offset(lbd));
                            if (lbte instanceof TileArcaneLamp) {
                                int d = this.world.rand.nextInt(32) * 2;
                                int xx = this.getPos().getX() + this.orientation.getXOffset() + this.orientation.getXOffset() * d;
                                int yy = this.getPos().getY() + this.orientation.getYOffset() + this.orientation.getYOffset() * d;
                                int zz = this.getPos().getZ() + this.orientation.getZOffset() + this.orientation.getZOffset() * d;
                                int p = d / 2 % 4;
                                if (this.orientation.getXOffset() != 0) {
                                    zz += p == 0 ? 3 : (p != 1 && p != 3 ? -3 : 0);
                                } else {
                                    xx += p == 0 ? 3 : (p != 1 && p != 3 ? -3 : 0);
                                }

                                if (p == 3 && this.orientation.getYOffset() == 0) {
                                    yy -= 2;
                                }

                                if (this.world.isAirBlock(new BlockPos(xx, yy, zz)) && this.world.getBlockState(new BlockPos(xx, yy, zz)).getBlock() != ConfigBlocks.blockAiry && this.world.getLightFromNeighbors(new BlockPos(xx, yy, zz)) < 15) {
                                    this.world.setBlockState(new BlockPos(xx, yy, zz), ConfigBlocks.blockAiry.getDefaultState(), 3);
                                }
                                break;
                            }
                        }
                    }

                    dug = true;
                }

                this.findNextBlockToDig();
                if (dug && this.speedyTime > 0.0F) {
                    --this.speedyTime;
                }
            } else {
                ++this.paused;
                if (this.world.isAirBlock(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()))) {
                    this.invalidate();
                }

                if (this.paused < this.maxPause && this.soundDelay < System.currentTimeMillis()) {
                    this.soundDelay = System.currentTimeMillis() + 1200L + (long) this.world.rand.nextInt(100);
                    { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:rumble")); if (_snd != null) this.world.playSound(null, this.getPos(), _snd, net.minecraft.util.SoundCategory.BLOCKS, 0.25F, 0.9F + this.world.rand.nextFloat() * 0.2F); };
                }

                if (this.beamlength > 0 && this.paused > this.maxPause) {
                    --this.beamlength;
                }

                if (this.toDig) {
                    this.paused = 0;
                    this.beamlength = 64;
                    Block block = this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)).getBlock();
                    int md = this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)).getBlock().getMetaFromState(this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)));
                    if (block != null) {
                        this.maxPause = 10 + Math.max(10 - this.speed, (int) (block.getBlockHardness(this.world.getBlockState(new BlockPos(this.digX, this.digY, this.digZ)), this.world, new BlockPos(this.digX, this.digY, this.digZ)) * 2.0F) - this.speed * 2);
                    } else {
                        this.maxPause = 20;
                    }

                    if (this.speedyTime <= 0.0F) {
                        this.maxPause *= 4;
                    }

                    this.toDig = false;
                    double xd = (double) this.getPos().getX() + (double) 0.5F - ((double) this.digX + (double) 0.5F);
                    double yd = (double) this.getPos().getY() + (double) 0.5F - ((double) this.digY + (double) 0.5F);
                    double zd = (double) this.getPos().getZ() + (double) 0.5F - ((double) this.digZ + (double) 0.5F);
                    double var12 = MathHelper.sqrt(xd * xd + zd * zd);
                    float rx = (float) (Math.atan2(zd, xd) * (double) 180.0F / Math.PI);
                    float rz = (float) (-(Math.atan2(yd, var12) * (double) 180.0F / Math.PI)) + 90.0F;
                    this.tRadX = MathHelper.wrapDegrees((float) this.rotX) + rx;
                    if (this.orientation.ordinal() == 5) {
                        if (this.tRadX > 180.0F) {
                            this.tRadX -= 360.0F;
                        }

                        if (this.tRadX < -180.0F) {
                            this.tRadX += 360.0F;
                        }
                    }

                    this.tRadZ = rz - (float) this.rotZ;
                    if (this.orientation.ordinal() <= 1) {
                        this.tRadZ += 180.0F;
                        if (this.vRadX - this.tRadX >= 180.0F) {
                            this.vRadX -= 360.0F;
                        }

                        if (this.vRadX - this.tRadX <= -180.0F) {
                            this.vRadX += 360.0F;
                        }
                    }

                    this.mRadX = Math.abs((this.vRadX - this.tRadX) / 6.0F);
                    this.mRadZ = Math.abs((this.vRadZ - this.tRadZ) / 6.0F);
                    if (this.speedyTime > 0.0F) {
                        --this.speedyTime;
                    }
                }

                if (this.paused < this.maxPause) {
                    if (this.vRadX < this.tRadX) {
                        this.vRadX += this.mRadX;
                    } else if (this.vRadX > this.tRadX) {
                        this.vRadX -= this.mRadX;
                    }

                    if (this.vRadZ < this.tRadZ) {
                        this.vRadZ += this.mRadZ;
                    } else if (this.vRadZ > this.tRadZ) {
                        this.vRadZ -= this.mRadZ;
                    }
                } else {
                    this.vRadX *= 0.9F;
                    this.vRadZ *= 0.9F;
                }

                this.mRadX *= 0.9F;
                this.mRadZ *= 0.9F;
                float vx = (float) (this.rotX + 90) - this.vRadX;
                float vz = (float) (this.rotZ + 90) - this.vRadZ;
                float var3 = 1.0F;
                float dX = MathHelper.sin(vx / 180.0F * (float) Math.PI) * MathHelper.cos(vz / 180.0F * (float) Math.PI) * var3;
                float dZ = MathHelper.cos(vx / 180.0F * (float) Math.PI) * MathHelper.cos(vz / 180.0F * (float) Math.PI) * var3;
                float dY = MathHelper.sin(vz / 180.0F * (float) Math.PI) * var3;
                Vec3d var13 = new Vec3d((double) this.getPos().getX() + (double) 0.5F + (double) dX, (double) this.getPos().getY() + (double) 0.5F + (double) dY, (double) this.getPos().getZ() + (double) 0.5F + (double) dZ);
                Vec3d var14 = new Vec3d((double) this.getPos().getX() + (double) 0.5F + (double) (dX * (float) this.beamlength), (double) this.getPos().getY() + (double) 0.5F + (double) (dY * (float) this.beamlength), (double) this.getPos().getZ() + (double) 0.5F + (double) (dZ * (float) this.beamlength));
                RayTraceResult mop = this.world.rayTraceBlocks(var13, var14, false, true, false);
                int impact = 0;
                float length = 64.0F;
                double bx = var14.x;
                double by = var14.y;
                double bz = var14.z;
                if (mop != null) {
                    double xd = (double) this.getPos().getX() + (double) 0.5F + (double) dX - mop.hitVec.x;
                    double yd = (double) this.getPos().getY() + (double) 0.5F + (double) dY - mop.hitVec.y;
                    double zd = (double) this.getPos().getZ() + (double) 0.5F + (double) dZ - mop.hitVec.z;
                    bx = mop.hitVec.x;
                    by = mop.hitVec.y;
                    bz = mop.hitVec.z;
                    length = MathHelper.sqrt(xd * xd + yd * yd + zd * zd) + 0.5F;
                    impact = 5;
                    int x = MathHelper.floor(bx);
                    int y = MathHelper.floor(by);
                    int z = MathHelper.floor(bz);
                    if (!this.world.isAirBlock(new BlockPos(x, y, z))) {
                        IBlockState _bds = this.world.getBlockState(new BlockPos(x, y, z));
                        Thaumcraft.proxy.boreDigFx(this.world, x, y, z, this.getPos().getX() + this.orientation.getXOffset(), this.getPos().getY() + this.orientation.getYOffset(), this.getPos().getZ() + this.orientation.getZOffset(), _bds.getBlock(), _bds.getBlock().getMetaFromState(_bds));
                    }
                }
            }
        }
    }

    private void findNextBlockToDig() {
        if (this.radInc == 0.0F) {
            this.radInc = (float)(this.maxRadius + this.area) / 360.0F;
        }
        int x = this.lastX;
        int z = this.lastZ;
        int y;
        TCVec3d vres = TCVec3d.createVectorHelper(0.0D, 0.0D, 0.0D);
        for (y = this.lastY; x == this.lastX && z == this.lastZ && y == this.lastY; z = MathHelper.floor(vres.z)) {
            this.spiral += 2;
            if (this.spiral >= 360) this.spiral -= 360;
            this.currentRadius += this.radInc;
            if (this.currentRadius > (float)(this.maxRadius + this.area) || this.currentRadius < (float)(-(this.maxRadius + this.area))) {
                this.radInc *= -1.0F;
            }
            TCVec3d vsource = TCVec3d.createVectorHelper(
                (double)(this.getPos().getX() + this.orientation.getXOffset()) + 0.5D,
                (double)(this.getPos().getY() + this.orientation.getYOffset()) + 0.5D,
                (double)(this.getPos().getZ() + this.orientation.getZOffset()) + 0.5D);
            TCVec3d vtar = TCVec3d.createVectorHelper(0.0D, (double)this.currentRadius, 0.0D);
            vtar.rotateAroundZ((float)this.spiral / 180.0F * (float)Math.PI);
            vtar.rotateAroundY(((float)Math.PI / 2F) * (float)this.orientation.getXOffset());
            vtar.rotateAroundX(((float)Math.PI / 2F) * (float)this.orientation.getYOffset());
            vres = vsource.addVector(vtar.x, vtar.y, vtar.z);
            x = MathHelper.floor(vres.x);
            y = MathHelper.floor(vres.y);
        }
        this.lastX = x;
        this.lastZ = z;
        this.lastY = y;
        x += this.orientation.getXOffset();
        y += this.orientation.getYOffset();
        z += this.orientation.getZOffset();
        for (int depth = 0; depth < 64; ++depth) {
            x += this.orientation.getXOffset();
            y += this.orientation.getYOffset();
            z += this.orientation.getZOffset();
            BlockPos bpos = new BlockPos(x, y, z);
            IBlockState state = this.world.getBlockState(bpos);
            Block block = state.getBlock();
            if (block.getBlockHardness(state, this.world, bpos) < 0.0F) break;
            if (!this.world.isAirBlock(bpos) && block.canCollideCheck(state, false) && block.getCollisionBoundingBox(state, this.world, bpos) != null) {
                this.digX = x;
                this.digY = y;
                this.digZ = z;
                if (++this.blockCount > 2) this.blockCount = 0;
                this.count = Math.max(10 - this.speed, (int)(block.getBlockHardness(state, this.world, bpos) * 2.0F) - this.speed * 2);
                if (this.speedyTime < 1.0F) this.count *= 4;
                this.toDig = true;
                Vec3d var13 = new Vec3d(
                    this.getPos().getX() + 0.5D + this.orientation.getXOffset(),
                    this.getPos().getY() + 0.5D + this.orientation.getYOffset(),
                    this.getPos().getZ() + 0.5D + this.orientation.getZOffset());
                Vec3d var14 = new Vec3d(this.digX + 0.5D, this.digY + 0.5D, this.digZ + 0.5D);
                RayTraceResult mop = this.world.rayTraceBlocks(var13, var14, false, true, false);
                if (mop != null) {
                    BlockPos mp = mop.getBlockPos();
                    IBlockState ms = this.world.getBlockState(mp);
                    Block mb = ms.getBlock();
                    if (mb.getBlockHardness(ms, this.world, mp) > -1.0F && mb.getCollisionBoundingBox(ms, this.world, mp) != null) {
                        this.count = Math.max(10 - this.speed, (int)(mb.getBlockHardness(ms, this.world, mp) * 2.0F) - this.speed * 2);
                        if (this.speedyTime < 1.0F) this.count *= 4;
                        this.digX = mp.getX();
                        this.digY = mp.getY();
                        this.digZ = mp.getZ();
                    }
                }
                this.sendDigEvent();
                break;
            }
        }
    }

    public boolean gettingPower() {
        return this.world.isBlockPowered(this.getPos()) ||
               this.world.isBlockPowered(this.getPos().offset(this.baseOrientation.getOpposite()));
    }

    public void setOrientation(EnumFacing or, boolean initial) {
        this.orientation = or;
        this.lastX = 0;
        this.lastZ = 0;
        switch (or.ordinal()) {
            case 0: this.tarZ = 180; this.tarX = 0; break;
            case 1: this.tarZ = 0;   this.tarX = 0; break;
            case 2: this.tarZ = 90;  this.tarX = 270; break;
            case 3: this.tarZ = 90;  this.tarX = 90; break;
            case 4: this.tarZ = 90;  this.tarX = 0; break;
            default: this.tarZ = 90; this.tarX = 180; break;
        }
        if (initial) {
            this.rotX = this.tarX;
            this.rotZ = this.tarZ;
        }
        this.toDig = false;
        this.radInc = 0.0F;
        this.paused = 100;
        this.tRadX = 0.0F;
        this.tRadZ = 0.0F;
        this.mRadX = 0.0F;
        this.mRadZ = 0.0F;
        this.digX = 0;
        this.digY = 0;
        this.digZ = 0;
        if (this.world != null) {
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
        }
    }

    public void sendDigEvent() {
        int x = this.digX - this.getPos().getX() + 64;
        int y = this.digY - this.getPos().getY() + 64;
        int z = this.digZ - this.getPos().getZ() + 64;
        int c = (x & 255) << 16 | (y & 255) << 8 | z & 255;
        PacketHandler.INSTANCE.sendToAllAround(new PacketBoreDig(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), c), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 64.0F));
    }

    public void getDigEvent(int digloc) {
        int dx = ((digloc >> 16) & 255) - 64;
        int dy = ((digloc >> 8) & 255) - 64;
        int dz = (digloc & 255) - 64;
        int x = this.getPos().getX() + dx;
        int y = this.getPos().getY() + dy;
        int z = this.getPos().getZ() + dz;
        net.minecraft.block.Block bi = this.world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock();
        int md = bi.getMetaFromState(this.world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
        thaumcraft.common.Thaumcraft.proxy.boreDigFx(this.world, x, y, z, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), bi, md);
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public ItemStack getStackInSlot(int var1) {
        ItemStack s = this.contents[var1]; return s != null ? s : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int var1, int var2) {
        if (this.contents[var1] != null) {
            ItemStack var3;
            if (this.contents[var1].getCount() <= var2) {
                var3 = this.contents[var1];
                this.contents[var1] = null;
            } else {
                var3 = this.contents[var1].splitStack(var2);
                if (this.contents[var1].getCount() == 0) {
                    this.contents[var1] = null;
                }

            }
            this.markDirty();
            return var3;
        } else {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int var1) {
        if (this.contents[var1] != null) {
            ItemStack var2 = this.contents[var1];
            this.contents[var1] = null;
            return var2;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int var1, ItemStack var2) {
        this.contents[var1] = var2;
        if (var2 != null && var2.getCount() > this.getInventoryStackLimit()) {
            var2.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    public String getInventoryName() {
        return net.minecraft.client.resources.I18n.format("tile.blockWoodenDevice.5.name");
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public boolean isUsableByPlayer(EntityPlayer var1) {
        return this.world.getTileEntity(this.getPos()) == this && var1.getDistanceSq((double) this.getPos().getX() + (double) 0.5F, (double) this.getPos().getY() + (double) 0.5F, (double) this.getPos().getZ() + (double) 0.5F) <= (double) 64.0F;
    }

    public void openInventory(EntityPlayer player) {
    }

    public void closeInventory(EntityPlayer player) {
    }

    public boolean isEmpty() {
        for (ItemStack stack : this.contents) {
            if (stack != null) return false;
        }
        return true;
    }

    public void clear() {
        for (int i = 0; i < this.contents.length; ++i) {
            this.contents[i] = null;
        }
    }

    public int getField(int id) { return 0; }
    public void setField(int id, int value) {}
    public int getFieldCount() { return 0; }

    public boolean hasCustomName() { return false; }
    public boolean hasCustomInventoryName() { return false; }
    public net.minecraft.util.text.ITextComponent getDisplayName() {
        return new net.minecraft.util.text.TextComponentString(getInventoryName());
    }
    public String getName() { return getInventoryName(); }

    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return true;
    }

    public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
        this.setOrientation(EnumFacing.byIndex(side), false);
        { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:tool")); if (_snd != null) player.world.playSound(null, new BlockPos(x, y, z), _snd, net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 0.9F + player.world.rand.nextFloat() * 0.2F); }
        player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
        this.markDirty();
        return 0;
    }

    public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
        return null;
    }

    public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
    }

    public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
    }
}
