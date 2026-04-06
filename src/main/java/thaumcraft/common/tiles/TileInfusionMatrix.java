package thaumcraft.common.tiles;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.api.crafting.InfusionEnchantmentRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.container.InventoryFake;
import thaumcraft.common.lib.crafting.InfusionRunicAugmentRecipe;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.events.EssentiaHandler;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockZap;
import thaumcraft.common.lib.network.fx.PacketFXInfusionSource;
import thaumcraft.common.lib.utils.InventoryUtils;

public class TileInfusionMatrix extends TileThaumcraft implements IWandable, IAspectContainer {
    private ArrayList<BlockPos> pedestals = new ArrayList<>();
    private int dangerCount = 0;
    public boolean active = false;
    public boolean crafting = false;
    public boolean checkSurroundings = true;
    public int symmetry = 0;
    public int instability = 0;
    private AspectList recipeEssentia = new AspectList();
    private ArrayList<ItemStack> recipeIngredients = null;
    private Object recipeOutput = null;
    private String recipePlayer = null;
    private String recipeOutputLabel = null;
    private ItemStack recipeInput = null;
    private int recipeInstability = 0;
    private int recipeXP = 0;
    private int recipeType = 0;
    public HashMap sourceFX = new HashMap<>();
    public int count = 0;
    public int craftCount = 0;
    public float startUp;
    private int countDelay = 10;
    ArrayList ingredients = new ArrayList<>();
    int itemCount = 0;

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos().getX() - 1, this.getPos().getY() - 1, this.getPos().getZ() - 1, this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1);
    }

    public void readCustomNBT(NBTTagCompound nbtCompound) {
        this.active = nbtCompound.getBoolean("active");
        this.crafting = nbtCompound.getBoolean("crafting");
        this.instability = nbtCompound.getShort("instability");
        this.recipeEssentia.readFromNBT(nbtCompound);
    }

    public void writeCustomNBT(NBTTagCompound nbtCompound) {
        nbtCompound.setBoolean("active", this.active);
        nbtCompound.setBoolean("crafting", this.crafting);
        nbtCompound.setShort("instability", (short) this.instability);
        this.recipeEssentia.writeToNBT(nbtCompound);
    }

    public void readFromNBT(NBTTagCompound nbtCompound) {
        super.readFromNBT(nbtCompound);
        NBTTagList nbttaglist = nbtCompound.getTagList("recipein", 10);
        this.recipeIngredients = new ArrayList<>();

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbttagcompound1.getByte("item");
            this.recipeIngredients.add(new ItemStack(nbttagcompound1));
        }

        String rot = nbtCompound.getString("rotype");
        if (rot != null && rot.equals("@")) {
            this.recipeOutput = new ItemStack(nbtCompound.getCompoundTag("recipeout"));
        } else if (rot != null) {
            this.recipeOutputLabel = rot;
            this.recipeOutput = nbtCompound.getTag("recipeout");
        }

        this.recipeInput = new ItemStack(nbtCompound.getCompoundTag("recipeinput"));
        this.recipeInstability = nbtCompound.getInteger("recipeinst");
        this.recipeType = nbtCompound.getInteger("recipetype");
        this.recipeXP = nbtCompound.getInteger("recipexp");
        this.recipePlayer = nbtCompound.getString("recipeplayer");
        if (this.recipePlayer.isEmpty()) {
            this.recipePlayer = null;
        }

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound) {
        super.writeToNBT(nbtCompound);
        if (this.recipeIngredients != null && !this.recipeIngredients.isEmpty()) {
            NBTTagList nbttaglist = new NBTTagList();
            int count = 0;

            for (ItemStack stack : this.recipeIngredients) {
                if (stack != null) {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setByte("item", (byte) count);
                    stack.writeToNBT(nbttagcompound1);
                    nbttaglist.appendTag(nbttagcompound1);
                    ++count;
                }
            }

            nbtCompound.setTag("recipein", nbttaglist);
        }

        if (this.recipeOutput != null && this.recipeOutput instanceof ItemStack) {
            nbtCompound.setString("rotype", "@");
        }

        if (this.recipeOutput != null && this.recipeOutput instanceof NBTBase) {
            nbtCompound.setString("rotype", this.recipeOutputLabel);
        }

        if (this.recipeOutput != null && this.recipeOutput instanceof ItemStack) {
            nbtCompound.setTag("recipeout", ((ItemStack) this.recipeOutput).writeToNBT(new NBTTagCompound()));
        }

        if (this.recipeOutput != null && this.recipeOutput instanceof NBTBase) {
            nbtCompound.setTag("recipeout", (NBTBase) this.recipeOutput);
        }

        if (this.recipeInput != null) {
            nbtCompound.setTag("recipeinput", this.recipeInput.writeToNBT(new NBTTagCompound()));
        }

        nbtCompound.setInteger("recipeinst", this.recipeInstability);
        nbtCompound.setInteger("recipetype", this.recipeType);
        nbtCompound.setInteger("recipexp", this.recipeXP);
        if (this.recipePlayer == null) {
            nbtCompound.setString("recipeplayer", "");
        } else {
            nbtCompound.setString("recipeplayer", this.recipePlayer);
        }

        return nbtCompound;
    }

    public void updateEntity() {
                ++this.count;
        if (this.checkSurroundings) {
            this.checkSurroundings = false;
            this.getSurroundings();
        }

        if (this.world.isRemote) {
            this.doEffects();
        } else {
            if (this.count % (this.crafting ? 20 : 100) == 0 && !this.validLocation()) {
                this.active = false;
                this.markDirty();
                { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                return;
            }

            if (this.active && this.crafting && this.count % this.countDelay == 0) {
                this.craftCycle();
                this.markDirty();
            }
        }

    }

    public boolean validLocation() {
        TileEntity te = null;
        te = this.world.getTileEntity(this.getPos().down(2));
        if (te instanceof TilePedestal) {
            te = this.world.getTileEntity(new BlockPos(this.getPos().getX() + 1, this.getPos().getY() - 2, this.getPos().getZ() + 1));
            if (te instanceof TileInfusionPillar) {
                te = this.world.getTileEntity(new BlockPos(this.getPos().getX() + 1, this.getPos().getY() - 2, this.getPos().getZ() - 1));
                if (te instanceof TileInfusionPillar) {
                    te = this.world.getTileEntity(new BlockPos(this.getPos().getX() - 1, this.getPos().getY() - 2, this.getPos().getZ() - 1));
                    if (te instanceof TileInfusionPillar) {
                        te = this.world.getTileEntity(new BlockPos(this.getPos().getX() - 1, this.getPos().getY() - 2, this.getPos().getZ() + 1));
                        return te instanceof TileInfusionPillar;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void craftingStart(EntityPlayer player) {
        if (!this.validLocation()) {
            this.active = false;
            this.markDirty();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
        } else {
            this.getSurroundings();
            TileEntity te = null;
            this.recipeInput = null;
            te = this.world.getTileEntity(this.getPos().down(2));
            if (te instanceof TilePedestal) {
                TilePedestal ped = (TilePedestal) te;
                if (!ped.getStackInSlot(0).isEmpty()) {
                    this.recipeInput = ped.getStackInSlot(0).copy();
                }
            }

            if (this.recipeInput != null) {
                ArrayList<ItemStack> components = new ArrayList<>();

                for (BlockPos cc : this.pedestals) {
                    te = this.world.getTileEntity(new BlockPos(cc.getX(), cc.getY(), cc.getZ()));
                    if (te instanceof TilePedestal) {
                        TilePedestal ped = (TilePedestal) te;
                        if (!ped.getStackInSlot(0).isEmpty()) {
                            components.add(ped.getStackInSlot(0).copy());
                        }
                    }
                }

                if (!components.isEmpty()) {
                    InfusionRecipe recipe = ThaumcraftCraftingManager.findMatchingInfusionRecipe(components, this.recipeInput, player);
                    if (recipe != null) {
                        this.recipeType = 0;
                        this.recipeIngredients = new ArrayList<>();
                        if (recipe instanceof InfusionRunicAugmentRecipe) {
                            for (ItemStack ing : ((InfusionRunicAugmentRecipe) recipe).getComponents(this.recipeInput)) {
                                this.recipeIngredients.add(ing.copy());
                            }
                        } else {
                            for (ItemStack ing : recipe.getComponents()) {
                                this.recipeIngredients.add(ing.copy());
                            }
                        }

                        if (recipe.getRecipeOutput(this.recipeInput) instanceof Object[]) {
                            Object[] obj = (Object[]) recipe.getRecipeOutput(this.recipeInput);
                            this.recipeOutputLabel = (String) obj[0];
                            this.recipeOutput = obj[1];
                        } else {
                            this.recipeOutput = recipe.getRecipeOutput(this.recipeInput);
                        }

                        this.recipeInstability = recipe.getInstability(this.recipeInput);
                        this.recipeEssentia = recipe.getAspects(this.recipeInput).copy();
                        this.recipePlayer = player.getName();
                        this.instability = this.symmetry + this.recipeInstability;
                        this.crafting = true;
                        this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "craftstart")), net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 1.0F);
                        { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                        this.markDirty();
                    } else {
                        InfusionEnchantmentRecipe recipe2 = ThaumcraftCraftingManager.findMatchingInfusionEnchantmentRecipe(components, this.recipeInput, player);
                        if (recipe2 != null) {
                            this.recipeType = 1;
                            this.recipeIngredients = new ArrayList<>();

                            for (ItemStack ing : recipe2.components) {
                                this.recipeIngredients.add(ing.copy());
                            }

                            this.recipeOutput = recipe2.getEnchantment();
                            this.recipeInstability = recipe2.calcInstability(this.recipeInput);
                            AspectList esscost = recipe2.aspects.copy();
                            float essmod = recipe2.getEssentiaMod(this.recipeInput);

                            for (Aspect as : esscost.getAspects()) {
                                esscost.add(as, (int) ((float) esscost.getAmount(as) * essmod));
                            }

                            this.recipeEssentia = esscost;
                            this.recipeXP = recipe2.calcXP(this.recipeInput);
                            this.instability = this.symmetry + this.recipeInstability;
                            this.crafting = true;
                            this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "craftstart")), net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 1.0F);
                            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                            this.markDirty();
                        }
                    }
                }
            }
        }
    }

    public void craftCycle() {
        boolean validCraftingFlag = false;
        TileEntity te = this.world.getTileEntity(this.getPos().down(2));
        if (te instanceof TilePedestal) {
            TilePedestal ped = (TilePedestal) te;
            if (!ped.getStackInSlot(0).isEmpty()) {
                ItemStack i2 = ped.getStackInSlot(0).copy();
                if (this.recipeInput.getItemDamage() == 32767) {
                    i2.setItemDamage(32767);
                }

                if (InventoryUtils.areItemStacksEqualForCrafting(i2, this.recipeInput, true, true, false)) {
                    validCraftingFlag = true;
                }
            }
        }

        if (!validCraftingFlag || this.instability > 0 && Math.max(1, this.world.rand.nextInt(500)) <= this.instability) {
            //events for instability
            switch (this.world.rand.nextInt(21)) {
                case 0:
                case 2:
                case 10:
                case 13:
                    this.inEvEjectItem(0);
                    break;
                case 1:
                case 11:
                    this.inEvEjectItem(2);
                    break;
                case 3:
                case 8:
                case 14:
                    this.inEvZap(false);
                    break;
                case 4:
                case 15:
                    this.inEvEjectItem(5);
                    break;
                case 5:
                case 16:
                    this.inEvHarm(false);
                    break;
                case 6:
                case 17:
                    this.inEvEjectItem(1);
                    break;
                case 7:
                    this.inEvEjectItem(4);
                    break;
                case 9:
                    this.world.createExplosion(null, (float) this.getPos().getX() + 0.5F, (float) this.getPos().getY() + 0.5F, (float) this.getPos().getZ() + 0.5F, 1.5F + this.world.rand.nextFloat(), false);
                    break;
                case 12:
                    this.inEvZap(true);
                    break;
                case 18:
                    this.inEvHarm(true);
                    break;
                case 19:
                    this.inEvEjectItem(3);
                    break;
                case 20:
                    this.inEvWarp();
            }

            if (validCraftingFlag) {
                return;
            }
        }

        if (!validCraftingFlag) {
            //cancel infusion
            this.instability = 0;
            this.crafting = false;
            this.recipeEssentia = new AspectList();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "craftfail")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 0.6F);
            this.markDirty();
        } else if (this.recipeType == 1 && this.recipeXP > 0) {
            List<EntityPlayer> targets =
                    this.world.getEntitiesWithinAABB(EntityPlayer.class,
                            new AxisAlignedBB(
                                            this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(),
                                            this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1
                                    )
                                    .expand(10.0F, 10.0F, 10.0F)
                    );
            if (targets != null && !targets.isEmpty()) {
                for (EntityPlayer playerBeingTokenXP : targets) {
                    //taking XP from players(yes multiple players!)
                    if (playerBeingTokenXP.experienceLevel > 0) {
                        playerBeingTokenXP.addExperienceLevel(-1);
                        --this.recipeXP;
                        playerBeingTokenXP.attackEntityFrom(DamageSource.MAGIC, (float) this.world.rand.nextInt(2));
                        PacketFXInfusionSource var22 = new PacketFXInfusionSource(
                                this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(),
                                (byte) 0, (byte) 0, (byte) 0,
                                playerBeingTokenXP.getEntityId());
                        PacketHandler.INSTANCE.sendToAllAround(var22, new NetworkRegistry.TargetPoint(
                                this.getWorld().provider.getDimension(),
                                this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(),
                                32.0F));
                        { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("random.fizz")); if (_snd != null) this.world.playSound(null, playerBeingTokenXP.getPosition(), _snd, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 2.0F + this.world.rand.nextFloat() * 0.4F); }
                        this.countDelay = 20;
                        return;
                    }
                }

                Aspect[] recipeRequiredAspects = this.recipeEssentia.getAspects();
                if (recipeRequiredAspects != null
                        && recipeRequiredAspects.length > 0
                        && this.world.rand.nextInt(3) == 0) {
                    addInstabilityAndRequiredAspect(recipeRequiredAspects);
                }
            }

        } else {
            if (this.recipeType == 1 && this.recipeXP == 0) {
                this.countDelay = 10;
            }

            if (this.recipeEssentia.visSize() > 0) {
                //draining essentia,may add instability if failed
                for (Aspect aspect : this.recipeEssentia.getAspects()) {
                    if (this.recipeEssentia.getAmount(aspect) > 0) {
                        if (EssentiaHandler.drainEssentia(this, aspect, null, 12)) {
                            this.recipeEssentia.reduce(aspect, 1);
                            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                            this.markDirty();
                            return;
                        }

                        if (this.world.rand.nextInt(100 - this.recipeInstability * 3) == 0) {
                            ++this.instability;
                        }
                        checkInstability();
                    }
                }

                this.checkSurroundings = true;
            } else if (this.recipeIngredients.isEmpty()) {
                //finish infusion
                this.instability = 0;
                this.crafting = false;
                this.craftingFinish(this.recipeOutput, this.recipeOutputLabel);
                this.recipeOutput = null;
                { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                this.markDirty();
            } else {
                for (int a = 0; a < this.recipeIngredients.size(); ++a) {
                    for (BlockPos cc : this.pedestals) {
                        te = this.world.getTileEntity(new BlockPos(cc.getX(), cc.getY(), cc.getZ()));
                        if (te instanceof TilePedestal && !((TilePedestal) te).getStackInSlot(0).isEmpty() && InfusionRecipe.areItemStacksEqual(((TilePedestal) te).getStackInSlot(0), this.recipeIngredients.get(a), true)) {
                            if (this.itemCount == 0) {
                                this.itemCount = 5;
                                SimpleNetworkWrapper var10000 = PacketHandler.INSTANCE;
                                PacketFXInfusionSource var10001 = new PacketFXInfusionSource(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), (byte) (this.getPos().getX() - cc.getX()), (byte) (this.getPos().getY() - cc.getY()), (byte) (this.getPos().getZ() - cc.getZ()), 0);
                                double var10005 = this.getPos().getX();
                                double var10006 = this.getPos().getY();
                                double var10007 = this.getPos().getZ();
                                var10000.sendToAllAround(var10001, new NetworkRegistry.TargetPoint(this.getWorld().provider.getDimension(), var10005, var10006, var10007, 32.0F));
                            } else if (this.itemCount-- <= 1) {
                                ItemStack is = ((TilePedestal) te).getStackInSlot(0).getItem().getContainerItem(((TilePedestal) te).getStackInSlot(0));
                                ((TilePedestal) te).setInventorySlotContents(0, is == null ? null : is.copy());
                                this.recipeIngredients.remove(a);
                            }
                            return;
                        }
                    }

                    Aspect[] recipeRequiredAspects = this.recipeEssentia.getAspects();
                    if (recipeRequiredAspects != null
                            && recipeRequiredAspects.length > 0
                            && this.world.rand.nextInt(1 + a) == 0) {
                        addInstabilityAndRequiredAspect(recipeRequiredAspects);
                    }
                }

            }
        }
    }

    private void addInstabilityAndRequiredAspect(Aspect[] recipeRequiredAspects) {
        Aspect asToAdd = recipeRequiredAspects[this.world.rand.nextInt(recipeRequiredAspects.length)];
        this.recipeEssentia.add(asToAdd, 1);
        if (this.world.rand.nextInt(50 - this.recipeInstability * 2) == 0) {
            ++this.instability;
        }

        checkInstability();
    }

    private void checkInstability() {
        if (this.instability > 25) {
            this.instability = 25;
        }

        { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
        this.markDirty();
    }

    private void inEvZap(boolean all) {
        List<Entity> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(10.0F, 10.0F, 10.0F));
        if (targets != null && !targets.isEmpty()) {
            for (Entity target : targets) {
                PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockZap((float) this.getPos().getX() + 0.5F, (float) this.getPos().getY() + 0.5F, (float) this.getPos().getZ() + 0.5F, (float) target.posX, (float) target.posY + target.height / 2.0F, (float) target.posZ), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 32.0F));
                target.attackEntityFrom(DamageSource.MAGIC, (float) (4 + this.world.rand.nextInt(4)));
                if (!all) {
                    break;
                }
            }
        }

    }

    private void inEvHarm(boolean all) {
        List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(10.0F, 10.0F, 10.0F));
        if (targets != null && !targets.isEmpty()) {
            for (EntityLivingBase target : targets) {
                if (this.world.rand.nextBoolean()) {
                    target.addPotionEffect(new PotionEffect(net.minecraft.potion.Potion.getPotionById(Config.potionTaintPoisonID), 120, 0, false, true));
                } else {
                    PotionEffect pe = new PotionEffect(net.minecraft.potion.Potion.getPotionById(Config.potionVisExhaustID), 2400, 0, true, false);
                    pe.getCurativeItems().clear();
                    target.addPotionEffect(pe);
                }

                if (!all) {
                    break;
                }
            }
        }

    }

    private void inEvWarp() {
        List<EntityPlayer> targets = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(10.0F, 10.0F, 10.0F));
        if (targets != null && !targets.isEmpty()) {
            EntityPlayer target = targets.get(this.world.rand.nextInt(targets.size()));
            if (this.world.rand.nextFloat() < 0.25F) {
                Thaumcraft.addStickyWarpToPlayer(target, 1);
            } else {
                Thaumcraft.addWarpToPlayer(target, 1 + this.world.rand.nextInt(5), true);
            }
        }

    }

    private void inEvEjectItem(int type) {
        for (int q = 0; q < 50 && !this.pedestals.isEmpty(); ++q) {
            BlockPos cc = this.pedestals.get(this.world.rand.nextInt(this.pedestals.size()));
            TileEntity te = this.world.getTileEntity(new BlockPos(cc.getX(), cc.getY(), cc.getZ()));
            if (te instanceof TilePedestal && !((TilePedestal) te).getStackInSlot(0).isEmpty()) {
                if (type >= 3 && type != 5) {
                    ((TilePedestal) te).setInventorySlotContents(0, ItemStack.EMPTY);
                } else {
                    InventoryUtils.dropItems(this.world, cc.getX(), cc.getY(), cc.getZ());
                }

                if (type != 1 && type != 3) {
                    if (type != 2 && type != 4) {
                        if (type == 5) {
                            this.world.createExplosion(null, (float) cc.getX() + 0.5F, (float) cc.getY() + 0.5F, (float) cc.getZ() + 0.5F, 1.0F, false);
                        }
                    } else {
                        this.world.setBlockState(new BlockPos(cc.getX(), cc.getY() + 1, cc.getZ()), ConfigBlocks.blockFluxGas.getStateFromMeta(7), 3);
                        this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("random.fizz")), net.minecraft.util.SoundCategory.BLOCKS, 0.3F, 1.0F);
                    }
                } else {
                    this.world.setBlockState(new BlockPos(cc.getX(), cc.getY() + 1, cc.getZ()), ConfigBlocks.blockFluxGoo.getStateFromMeta(7), 3);
                    this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("game.neutral.swim")), net.minecraft.util.SoundCategory.BLOCKS, 0.3F, 1.0F);
                }

                this.world.addBlockEvent(new BlockPos(cc.getX(), cc.getY(), cc.getZ()), ConfigBlocks.blockStoneDevice, 11, 0);
                PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockZap((float) this.getPos().getX() + 0.5F, (float) this.getPos().getY() + 0.5F, (float) this.getPos().getZ() + 0.5F, (float) cc.getX() + 0.5F, (float) cc.getY() + 1.5F, (float) cc.getZ() + 0.5F), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 32.0F));
                return;
            }
        }

    }

    public void craftingFinish(Object out, String label) {
        BlockPos pedestalPos = this.getPos().down(2);
        TileEntity te = this.world.getTileEntity(pedestalPos);
        if (te instanceof TilePedestal) {
            if (out instanceof ItemStack) {
                ((TilePedestal) te).setInventorySlotContentsFromInfusion(0, ((ItemStack) out).copy());
            } else if (out instanceof NBTBase) {
                ItemStack temp = ((TilePedestal) te).getStackInSlot(0);
                NBTBase tag = (NBTBase) out;
                temp.setTagInfo(label, tag);
                { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(pedestalPos); this.world.notifyBlockUpdate(pedestalPos, _bs, _bs, 3); }
                te.markDirty();
            } else if (out instanceof Enchantment) {
                ItemStack temp = ((TilePedestal) te).getStackInSlot(0);
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(temp);
                enchantments.put((Enchantment) out, EnchantmentHelper.getEnchantmentLevel((Enchantment) out, temp) + 1);
                EnchantmentHelper.setEnchantments(enchantments, temp);
                { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(pedestalPos); this.world.notifyBlockUpdate(pedestalPos, _bs, _bs, 3); }
                te.markDirty();
            }

            if (this.recipePlayer != null) {
                EntityPlayer p = this.world.getPlayerEntityByName(this.recipePlayer);
                if (p != null) {
                    FMLCommonHandler.instance().firePlayerCraftingEvent(p, ((TilePedestal) te).getStackInSlot(0), new InventoryFake(this.recipeIngredients));
                }
            }

            this.recipeEssentia = new AspectList();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.markDirty();
            this.world.addBlockEvent(this.getPos().down(2), ConfigBlocks.blockStoneDevice, 12, 0);
        }

    }

    private void getSurroundings() {
        ArrayList<BlockPos> stuff = new ArrayList<>();
        this.pedestals.clear();

        try {
            for (int xx = -12; xx <= 12; ++xx) {
                for (int zz = -12; zz <= 12; ++zz) {
                    boolean skip = false;

                    for (int yy = -5; yy <= 10; ++yy) {
                        if (xx != 0 || zz != 0) {
                            int x = this.getPos().getX() + xx;
                            int y = this.getPos().getY() - yy;
                            int z = this.getPos().getZ() + zz;
                            TileEntity te = this.world.getTileEntity(new BlockPos(x, y, z));
                            if (!skip && yy > 0 && Math.abs(xx) <= 8 && Math.abs(zz) <= 8 && te instanceof TilePedestal) {
                                this.pedestals.add(new BlockPos(x, y, z));
                                skip = true;
                            } else {
                                Block bi = this.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                                if (bi == Blocks.SKULL || bi instanceof IInfusionStabiliser && ((IInfusionStabiliser) bi).canStabaliseInfusion(this.getWorld(), x, y, z)) {
                                    stuff.add(new BlockPos(x, y, z));
                                }
                            }
                        }
                    }
                }
            }

            this.symmetry = 0;

            for (BlockPos cc : this.pedestals) {
                boolean items = false;
                int x = this.getPos().getX() - cc.getX();
                int z = this.getPos().getZ() - cc.getZ();
                TileEntity te = this.world.getTileEntity(new BlockPos(cc.getX(), cc.getY(), cc.getZ()));
                if (te instanceof TilePedestal) {
                    this.symmetry += 2;
                    if (!((TilePedestal) te).getStackInSlot(0).isEmpty()) {
                        ++this.symmetry;
                        items = true;
                    }
                }

                int xx = this.getPos().getX() + x;
                int zz = this.getPos().getZ() + z;
                te = this.world.getTileEntity(new BlockPos(xx, cc.getY(), zz));
                if (te instanceof TilePedestal) {
                    this.symmetry -= 2;
                    if (!((TilePedestal) te).getStackInSlot(0).isEmpty() && items) {
                        --this.symmetry;
                    }
                }
            }

            float sym = 0.0F;

            for (BlockPos cc : stuff) {
                boolean items = false;
                int x = this.getPos().getX() - cc.getX();
                int z = this.getPos().getZ() - cc.getZ();
                Block bi = this.world.getBlockState(new BlockPos(cc.getX(), cc.getY(), cc.getZ())).getBlock();
                if (bi == Blocks.SKULL || bi instanceof IInfusionStabiliser && ((IInfusionStabiliser) bi).canStabaliseInfusion(this.getWorld(), cc.getX(), cc.getY(), cc.getZ())) {
                    sym += 0.1F;
                }

                int xx = this.getPos().getX() + x;
                int zz = this.getPos().getZ() + z;
                bi = this.world.getBlockState(new BlockPos(xx, cc.getY(), zz)).getBlock();
                if (bi == Blocks.SKULL || bi instanceof IInfusionStabiliser && ((IInfusionStabiliser) bi).canStabaliseInfusion(this.getWorld(), cc.getX(), cc.getY(), cc.getZ())) {
                    sym -= 0.2F;
                }
            }

            this.symmetry = (int) ((float) this.symmetry + sym);
        } catch (Exception ignored) {
        }

    }

    public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
        if (!world.isRemote && this.active && !this.crafting) {
            this.craftingStart(player);
            return 0;
        } else if (!world.isRemote && !this.active && this.validLocation()) {
            this.active = true;
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.markDirty();
            return 0;
        } else {
            return -1;
        }
    }

    public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
        return wandstack;
    }

    public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
    }

    public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
    }

    private void doEffects() {
        if (this.crafting) {
            if (this.craftCount == 0) {
                { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:infuserstart")); if (_snd != null) this.world.playSound(null, this.getPos(), _snd, net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 1.0F); }
            } else if (this.craftCount % 65 == 0) {
                { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:infuser")); if (_snd != null) this.world.playSound(null, this.getPos(), _snd, net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 1.0F); }
            }

            ++this.craftCount;
            Thaumcraft.proxy.blockRunes(this.world, this.getPos().getX(), this.getPos().getY() - 2, this.getPos().getZ(), 0.5F + this.world.rand.nextFloat() * 0.2F, 0.1F, 0.7F + this.world.rand.nextFloat() * 0.3F, 25, -0.03F);
        } else if (this.craftCount > 0) {
            this.craftCount -= 2;
            if (this.craftCount < 0) {
                this.craftCount = 0;
            }

            if (this.craftCount > 50) {
                this.craftCount = 50;
            }
        }

        if (this.active && this.startUp != 1.0F) {
            if (this.startUp < 1.0F) {
                this.startUp += Math.max(this.startUp / 10.0F, 0.001F);
            }

            if ((double) this.startUp > 0.999) {
                this.startUp = 1.0F;
            }
        }

        if (!this.active && this.startUp > 0.0F) {

            this.startUp -= this.startUp / 10.0F;


            if ((double) this.startUp < 0.001) {
                this.startUp = 0.0F;
            }
        }

        for (String fxk : (String[]) this.sourceFX.keySet().toArray(new String[0])) {
            SourceFX fx = (SourceFX) this.sourceFX.get(fxk);
            if (fx.ticks <= 0) {
                this.sourceFX.remove(fxk);
            } else {
                if (fx.loc.getX() == this.getPos().getX() && fx.loc.getY() == this.getPos().getY() && fx.loc.getZ() == this.getPos().getZ()) {
                    Entity player = this.world.getEntityByID(fx.color);
                    if (player != null) {
                        for (int a = 0; a < Thaumcraft.proxy.particleCount(2); ++a) {
                            Thaumcraft.proxy.drawInfusionParticles4(this.world, player.posX + (double) ((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * player.width), player.getEntityBoundingBox().minY + (double) (this.world.rand.nextFloat() * player.height), player.posZ + (double) ((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * player.width), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
                        }
                    }
                } else {
                    TileEntity tile = this.world.getTileEntity(new BlockPos(fx.loc.getX(), fx.loc.getY(), fx.loc.getZ()));
                    if (tile instanceof TilePedestal) {
                        ItemStack is = ((TilePedestal) tile).getStackInSlot(0);
                        if (is != null) {
                            if (this.world.rand.nextInt(3) == 0) {
                                Thaumcraft.proxy.drawInfusionParticles3(this.world, (float) fx.loc.getX() + this.world.rand.nextFloat(), (float) fx.loc.getY() + this.world.rand.nextFloat() + 1.0F, (float) fx.loc.getZ() + this.world.rand.nextFloat(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
                            } else {
                                Item bi = is.getItem();
                                int md = is.getItemDamage();
                                if (bi instanceof ItemBlock) {
                                    for (int a = 0; a < Thaumcraft.proxy.particleCount(2); ++a) {
                                        Thaumcraft.proxy.drawInfusionParticles2(this.world, (float) fx.loc.getX() + this.world.rand.nextFloat(), (float) fx.loc.getY() + this.world.rand.nextFloat() + 1.0F, (float) fx.loc.getZ() + this.world.rand.nextFloat(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), Block.getBlockFromItem(bi), md);
                                    }
                                } else {
                                    for (int a = 0; a < Thaumcraft.proxy.particleCount(2); ++a) {
                                        Thaumcraft.proxy.drawInfusionParticles1(this.world, (float) fx.loc.getX() + 0.4F + this.world.rand.nextFloat() * 0.2F, (float) fx.loc.getY() + 1.23F + this.world.rand.nextFloat() * 0.2F, (float) fx.loc.getZ() + 0.4F + this.world.rand.nextFloat() * 0.2F, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), bi, md);
                                    }
                                }
                            }
                        }
                    } else {
                        fx.ticks = 0;
                    }
                }

                --fx.ticks;
                this.sourceFX.put(fxk, fx);
            }
        }

        if (this.crafting && this.instability > 0 && this.world.rand.nextInt(200) <= this.instability) {
            Thaumcraft.proxy.nodeBolt(this.world, (float) this.getPos().getX() + 0.5F, (float) this.getPos().getY() + 0.5F, (float) this.getPos().getZ() + 0.5F, (float) this.getPos().getX() + 0.5F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 2.0F, (float) this.getPos().getY() + 0.5F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 2.0F, (float) this.getPos().getZ() + 0.5F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 2.0F);
        }

    }

    public AspectList getAspects() {
        return this.recipeEssentia;
    }

    public void setAspects(AspectList aspects) {
    }

    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    public int containerContains(Aspect tag) {
        return 0;
    }

    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    public static class SourceFX {
        public BlockPos loc;
        public int ticks;
        public int color;
        public int entity;

        public SourceFX(BlockPos loc, int ticks, int color) {
            this.loc = loc;
            this.ticks = ticks;
            this.color = color;
        }
    }
}
