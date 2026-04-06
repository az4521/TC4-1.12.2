package thaumcraft.common.items.wands;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.IArchitect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.expands.wandconsumption.ConsumptionModifierCalculator;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.IWandable;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.api.wands.StaffRod;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;
import thaumcraft.api.wands.WandTriggerRegistry;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileOwned;
import net.minecraft.util.math.BlockPos;

public class ItemWandCasting extends Item implements IArchitect {
    private TextureAtlasSprite icon;
    DecimalFormat myFormatter = new DecimalFormat("#######.##");
    public ItemFocusBasic.WandFocusAnimation animation = null;

    public ItemWandCasting() {
        this.maxStackSize = 1;
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setCreativeTab(Thaumcraft.tabTC);
    }

    public boolean isDamageable() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        this.icon = par1IconRegister.registerSprite("thaumcraft:blank");
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIcon(ItemStack stack, int pass) {
        return this.icon;
    }

    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    public int getMaxVis(ItemStack stack) {
        return this.getRod(stack).getCapacity() * (this.isSceptre(stack) ? 150 : 100);
    }

    public EnumRarity getRarity(ItemStack itemstack) {
        return EnumRarity.UNCOMMON;
    }

    @SideOnly(Side.CLIENT)
    @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
        ItemStack w1 = new ItemStack(this, 1, 0);
        ItemStack w2 = new ItemStack(this, 1, 9);
        ItemStack w3 = new ItemStack(this, 1, 54);
        ((ItemWandCasting) w2.getItem()).setCap(w2, ConfigItems.WAND_CAP_GOLD);
        ((ItemWandCasting) w3.getItem()).setCap(w3, ConfigItems.WAND_CAP_THAUMIUM);
        ((ItemWandCasting) w2.getItem()).setRod(w2, ConfigItems.WAND_ROD_GREATWOOD);
        ((ItemWandCasting) w3.getItem()).setRod(w3, ConfigItems.WAND_ROD_SILVERWOOD);
        ItemStack sceptre = new ItemStack(ConfigItems.itemWandCasting, 1, 128);
        ((ItemWandCasting) sceptre.getItem()).setCap(sceptre, ConfigItems.WAND_CAP_THAUMIUM);
        ((ItemWandCasting) sceptre.getItem()).setRod(sceptre, ConfigItems.WAND_ROD_SILVERWOOD);
        sceptre.setTagInfo("sceptre", new NBTTagByte((byte) 1));

        for (Aspect aspect : Aspect.getPrimalAspects()) {
            ((ItemWandCasting) w1.getItem()).addVis(w1, aspect, ((ItemWandCasting) w1.getItem()).getMaxVis(w1), true);
            ((ItemWandCasting) w2.getItem()).addVis(w2, aspect, ((ItemWandCasting) w2.getItem()).getMaxVis(w2), true);
            ((ItemWandCasting) w3.getItem()).addVis(w3, aspect, ((ItemWandCasting) w3.getItem()).getMaxVis(w3), true);
            ((ItemWandCasting) sceptre.getItem()).addVis(sceptre, aspect, ((ItemWandCasting) sceptre.getItem()).getMaxVis(sceptre), true);
        }

        par3List.add(w1);
        par3List.add(w2);
        par3List.add(w3);
        par3List.add(sceptre);
    }

    public String getItemStackDisplayName(ItemStack is) {
        String name = I18n.translateToLocal("item.Wand.name");
        name = name.replace("%CAP", I18n.translateToLocal("item.Wand." + this.getCap(is).getTag() + ".cap"));
        String rod = this.getRod(is).getTag();
        if (rod.contains("_staff")) {
            rod = rod.substring(0, this.getRod(is).getTag().indexOf("_staff"));
        }

        name = name.replace("%ROD", I18n.translateToLocal("item.Wand." + rod + ".rod"));
        name = name.replace("%OBJ", this.isStaff(is) ? I18n.translateToLocal("item.Wand.staff.obj") : (this.isSceptre(is) ? I18n.translateToLocal("item.Wand.sceptre.obj") : I18n.translateToLocal("item.Wand.wand.obj")));
        return name;
    }

    @Override
    public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World world, List list, net.minecraft.client.util.ITooltipFlag flag) {
        EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;
        int pos = list.size();
        String tt2 = "";
        if (stack.hasTagCompound()) {
            StringBuilder tt = new StringBuilder();
            int tot = 0;
            int num = 0;

            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (stack.getTagCompound().hasKey(aspect.getTag())) {
                    String amount = this.myFormatter.format((float) stack.getTagCompound().getInteger(aspect.getTag()) / 100.0F);
                    float mod = this.getConsumptionModifier(stack, player, aspect, false);
                    String consumption = this.myFormatter.format(mod * 100.0F);
                    ++num;
                    tot = (int) ((float) tot + mod * 100.0F);
                    String text = "";
                    ItemStack focus = this.getFocusItem(stack);
                    if (focus != null) {
                        int amt = ((ItemFocusBasic) focus.getItem()).getVisCost(focus).getAmount(aspect);
                        if (amt > 0) {
                            text = "§r, " + this.myFormatter.format((float) amt * mod / 100.0F) + " " + I18n.translateToLocal(((ItemFocusBasic) focus.getItem()).isVisCostPerTick(focus) ? "item.Focus.cost2" : "item.Focus.cost1");
                        }
                    }

                    if (Thaumcraft.proxy.isShiftKeyDown()) {
                        list.add(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + amount + ", §o(" + consumption + "% " + I18n.translateToLocal("tc.vis.cost") + ")" + text);
                    } else {
                        if (tt.length() > 0) {
                            tt.append(" | ");
                        }

                        tt.append("§").append(aspect.getChatcolor()).append(amount).append("§r");
                    }
                }
            }

            if (!Thaumcraft.proxy.isShiftKeyDown() && num > 0) {
                list.add(tt.toString());
                tot /= num;
                tt2 = " (" + tot + "% " + I18n.translateToLocal("tc.vis.costavg") + ")";
            }
        }

        list.add(pos, TextFormatting.GOLD + I18n.translateToLocal("item.capacity.text") + " " + this.getMaxVis(stack) / 100 + "§r" + tt2);
        if (this.getFocus(stack) != null) {
            list.add(TextFormatting.BOLD + "" + TextFormatting.ITALIC + TextFormatting.GREEN + this.getFocus(stack).getItemStackDisplayName(this.getFocusItem(stack)));
            if (Thaumcraft.proxy.isShiftKeyDown()) {
                this.getFocus(stack).addFocusInformation(this.getFocusItem(stack), list, flag);
            }
        }

    }

    public AspectList getAllVis(ItemStack is) {
        AspectList out = new AspectList();

        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (is.hasTagCompound() && is.getTagCompound().hasKey(aspect.getTag())) {
                out.merge(aspect, is.getTagCompound().getInteger(aspect.getTag()));
            } else {
                out.merge(aspect, 0);
            }
        }

        return out;
    }

    public AspectList getAspectsWithRoom(ItemStack wandstack) {
        AspectList out = new AspectList();
        AspectList cur = this.getAllVis(wandstack);

        for (Aspect aspect : cur.getAspects()) {
            if (cur.getAmount(aspect) < this.getMaxVis(wandstack)) {
                out.add(aspect, 1);
            }
        }

        return out;
    }

    public void storeAllVis(ItemStack is, AspectList in) {
        for (Aspect aspect : in.getAspects()) {
            is.setTagInfo(aspect.getTag(), new NBTTagInt(in.getAmount(aspect)));
        }

    }

    public int getVis(ItemStack is, Aspect aspect) {
        int out = 0;
        if (is != null && aspect != null && is.hasTagCompound() && is.getTagCompound().hasKey(aspect.getTag())) {
            out = is.getTagCompound().getInteger(aspect.getTag());
        }

        return out;
    }

    public void storeVis(ItemStack is, Aspect aspect, int amount) {
        is.setTagInfo(aspect.getTag(), new NBTTagInt(amount));
    }

    public float getConsumptionModifier(ItemStack is, EntityPlayer player, Aspect aspect, boolean crafting) {
        return ConsumptionModifierCalculator.getConsumptionModifier(this, is, player, aspect, crafting);
//        float consumptionModifier = 1.0F;
//        if (this.getCap(is).getSpecialCostModifierAspects() != null
//                && this.getCap(is).getSpecialCostModifierAspects().contains(aspect)
//        ) {
//            consumptionModifier = this.getCap(is).getSpecialCostModifier();
//        } else {
//            consumptionModifier = this.getCap(is).getBaseCostModifier();
//        }
//
//        if (player != null) {
//            consumptionModifier -= WandManager.getTotalVisDiscount(player, aspect);
//        }
//
//        if (this.getFocus(is) != null && !crafting) {
//            consumptionModifier -= (float) this.getFocusFrugal(is) / 10.0F;
//        }
//
//        if (this.isSceptre(is)) {
//            consumptionModifier -= 0.1F;
//        }
//
//        return Math.max(consumptionModifier, 0.1F);
    }

    public int getFocusPotency(ItemStack itemstack) {
        return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.potency) + (this.hasRunes(itemstack) ? 1 : 0);
    }

    public int getFocusTreasure(ItemStack itemstack) {
        return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.treasure);
    }

    public int getFocusFrugal(ItemStack itemstack) {
        return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.frugal);
    }

    public int getFocusEnlarge(ItemStack itemstack) {
        return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.enlarge);
    }

    public int getFocusExtend(ItemStack itemstack) {
        return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.extend);
    }

    public boolean consumeVis(ItemStack is, EntityPlayer player, Aspect aspect, int amount, boolean crafting) {
        amount = (int) ((float) amount * this.getConsumptionModifier(is, player, aspect, crafting));
        if (this.getVis(is, aspect) >= amount) {
            this.storeVis(is, aspect, this.getVis(is, aspect) - amount);
            return true;
        } else {
            return false;
        }
    }

    public boolean consumeAllVisCrafting(ItemStack is, EntityPlayer player, AspectList aspects, boolean doit) {
        if (aspects != null && aspects.size() != 0) {
            AspectList nl = new AspectList();

            for (Aspect aspect : aspects.getAspects()) {
                int cost = aspects.getAmount(aspect) * 100;
                nl.add(aspect, cost);
            }

            return this.consumeAllVis(is, player, nl, doit, true);
        } else {
            return false;
        }
    }

    public boolean consumeAllVis(ItemStack is, EntityPlayer player, AspectList aspects, boolean doit, boolean crafting) {
        if (aspects != null && aspects.size() != 0) {
            AspectList nl = new AspectList();

            for (Aspect aspect : aspects.getAspects()) {
                int cost = aspects.getAmount(aspect);
                cost = (int) ((float) cost * this.getConsumptionModifier(is, player, aspect, crafting));
                nl.add(aspect, cost);
            }

            for (Aspect aspect : nl.getAspects()) {
                if (this.getVis(is, aspect) < nl.getAmount(aspect)) {
                    return false;
                }
            }

            if (doit && !player.world.isRemote) {
                for (Aspect aspect : nl.getAspects()) {
                    this.storeVis(is, aspect, this.getVis(is, aspect) - nl.getAmount(aspect));
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public int addVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
        if (!aspect.isPrimal()) {
            return 0;
        } else {
            int storeAmount = this.getVis(is, aspect) + amount * 100;
            int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
            if (doit) {
                this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
            }

            return leftover / 100;
        }
    }

    public int addRealVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
        if (!aspect.isPrimal()) {
            return 0;
        } else {
            int storeAmount = this.getVis(is, aspect) + amount;
            int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
            if (doit) {
                this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
            }

            return leftover;
        }
    }

    public void onUpdate(ItemStack is, World w, Entity e, int slot, boolean currentItem) {
        if (!w.isRemote) {
            EntityPlayer player = (EntityPlayer) e;
            if (this.getRod(is).getOnUpdate() != null) {
                this.getRod(is).getOnUpdate().onUpdate(is, player);
            }
        }

    }

    @Override
    public net.minecraft.util.EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, net.minecraft.util.EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        int x = blockPos.getX(), y = blockPos.getY(), z = blockPos.getZ();
        int side = facing.ordinal();
        Block bi = world.getBlockState(blockPos).getBlock();
        int md = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
        boolean result = false;
        EnumFacing direction = facing;
        if (bi instanceof IWandable) {
            int ret = ((IWandable) bi).onWandRightClick(world, itemstack, player, x, y, z, side, md);
            if (ret >= 0) {
                return ret == 1 ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.FAIL;
            }
        }

        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (tile instanceof IWandable) {
            int ret = ((IWandable) tile).onWandRightClick(world, itemstack, player, x, y, z, side, md);
            if (ret >= 0) {
                return ret == 1 ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.FAIL;
            }
        }

        if (WandTriggerRegistry.hasTrigger(bi, md)) {
            return WandTriggerRegistry.performTrigger(world, itemstack, player, x, y, z, side, bi, md)
                    ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
        } else {
            if ((bi == ConfigBlocks.blockWoodenDevice && md == 2 || bi == ConfigBlocks.blockCosmeticOpaque && md == 2) && (!Config.wardedStone || tile instanceof TileOwned && player.getName().equals(((TileOwned) tile).owner))) {
                if (!world.isRemote) {
                    ((TileOwned) tile).safeToRemove = true;
                    world.spawnEntity(new EntityItem(world, (double) x + (double) 0.5F, (double) y + (double) 0.5F, (double) z + (double) 0.5F, new ItemStack(bi, 1, md)));
                    world.playEvent(2001, new BlockPos(x, y, z), Block.getStateId(bi.getDefaultState()));
                    world.setBlockToAir(new BlockPos(x, y, z));
                } else {
                    player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
                }
            }

            if (bi == ConfigBlocks.blockArcaneDoor && (!Config.wardedStone || tile instanceof TileOwned && player.getName().equals(((TileOwned) tile).owner))) {
                if (!world.isRemote) {
                    ((TileOwned) tile).safeToRemove = true;
                    if ((md & 8) == 0) {
                        tile = world.getTileEntity(new BlockPos(x, y + 1, z));
                    } else {
                        tile = world.getTileEntity(new BlockPos(x, y - 1, z));
                    }

                    if (tile instanceof TileOwned) {
                        ((TileOwned) tile).safeToRemove = true;
                    }

                    if (Config.wardedStone || !Config.wardedStone && (md & 8) == 0) {
                        world.spawnEntity(new EntityItem(world, (double) x + (double) 0.5F, (double) y + (double) 0.5F, (double) z + (double) 0.5F, new ItemStack(ConfigItems.itemArcaneDoor)));
                    }

                    world.playEvent(2001, new BlockPos(x, y, z), Block.getStateId(bi.getDefaultState()));
                    world.setBlockToAir(new BlockPos(x, y, z));
                } else {
                    player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
                }
            }

            return result ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
        }
    }

    @Override
    public net.minecraft.util.EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, net.minecraft.util.EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.getHeldItem(hand);
        TileEntity tile = world.getTileEntity(blockPos);
        if (tile instanceof IWandable) {
            ItemStack result = ((IWandable) tile).onWandRightClick(world, itemstack, player);
            if (result != null) {
                player.setActiveHand(hand);
                // Don't swing arm - return PASS to suppress animation but setActiveHand already started the action
                return net.minecraft.util.EnumActionResult.PASS;
            }
        }
        Block bi = world.getBlockState(blockPos).getBlock();
        if (bi instanceof IWandable) {
            ItemStack result = ((IWandable) bi).onWandRightClick(world, itemstack, player);
            if (result != null) {
                player.setActiveHand(hand);
                return net.minecraft.util.EnumActionResult.PASS;
            }
        }
        return net.minecraft.util.EnumActionResult.PASS;
    }

    public ItemFocusBasic getFocus(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("focus")) {
            NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("focus");
            return (ItemFocusBasic) new ItemStack(nbt).getItem();
        } else {
            return null;
        }
    }

    public ItemStack getFocusItem(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("focus")) {
            NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("focus");
            return new ItemStack(nbt);
        } else {
            return null;
        }
    }

    public void setFocus(ItemStack stack, ItemStack focus) {
        if (focus == null) {
            stack.getTagCompound().removeTag("focus");
        } else {
            stack.setTagInfo("focus", focus.writeToNBT(new NBTTagCompound()));
        }

    }

    public WandRod getRod(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("rod") ? WandRod.rods.get(stack.getTagCompound().getString("rod")) : ConfigItems.WAND_ROD_WOOD;
    }

    public boolean isStaff(ItemStack stack) {
        WandRod rod = this.getRod(stack);
        return rod instanceof StaffRod;
    }

    public boolean isSceptre(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("sceptre");
    }

    public boolean hasRunes(ItemStack stack) {
        WandRod rod = this.getRod(stack);
        return rod instanceof StaffRod && ((StaffRod) rod).hasRunes();
    }

    public void setRod(ItemStack stack, WandRod rod) {
        stack.setTagInfo("rod", new NBTTagString(rod.getTag()));
        if (rod instanceof StaffRod) {
            NBTTagList tags = new NBTTagList();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("AttributeName", SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            AttributeModifier am = new AttributeModifier(java.util.UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"), "Weapon modifier", 6.0F, 0);
            tag.setString("Name", am.getName());
            tag.setDouble("Amount", am.getAmount());
            tag.setInteger("Operation", am.getOperation());
            tag.setLong("UUIDMost", am.getID().getMostSignificantBits());
            tag.setLong("UUIDLeast", am.getID().getLeastSignificantBits());
            tags.appendTag(tag);
            stack.getTagCompound().setTag("AttributeModifiers", tags);
        }

    }

    public WandCap getCap(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("cap") ? WandCap.caps.get(stack.getTagCompound().getString("cap")) : ConfigItems.WAND_CAP_IRON;
    }

    public void setCap(ItemStack stack, WandCap cap) {
        stack.setTagInfo("cap", new NBTTagString(cap.getTag()));
    }

    @Override
    public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        // IWandable block/tile interactions are handled in onItemUse - don't duplicate here
        RayTraceResult movingobjectposition = this.rayTrace(world, player, true);

        ItemFocusBasic focus = this.getFocus(itemstack);
        if (focus != null && !WandManager.isOnCooldown(player)) {
            WandManager.setCooldown(player, focus.getActivationCooldown(this.getFocusItem(itemstack)));
            ItemStack ret = focus.onFocusRightClick(itemstack, world, player, movingobjectposition);
            if (ret != null) {
                return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, ret);
            }
        }

        return super.onItemRightClick(world, player, hand);
    }

    public void setObjectInUse(ItemStack stack, int x, int y, int z) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setInteger("IIUX", x);
        stack.getTagCompound().setInteger("IIUY", y);
        stack.getTagCompound().setInteger("IIUZ", z);
    }

    public void clearObjectInUse(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().removeTag("IIUX");
        stack.getTagCompound().removeTag("IIUY");
        stack.getTagCompound().removeTag("IIUZ");
    }

    public IWandable getObjectInUse(ItemStack stack, World world) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("IIUX")) {
            TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(stack.getTagCompound().getInteger("IIUX"), stack.getTagCompound().getInteger("IIUY"), stack.getTagCompound().getInteger("IIUZ")));
            if (te instanceof IWandable) {
                return (IWandable) te;
            }
        }

        return null;
    }

    @Override
    public void onUsingTick(ItemStack stack, net.minecraft.entity.EntityLivingBase livingBase, int count) {
        if (!(livingBase instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) livingBase;
        IWandable tv = this.getObjectInUse(stack, player.world);
        if (tv != null) {
            this.animation = ItemFocusBasic.WandFocusAnimation.WAVE;
            tv.onUsingWandTick(stack, player, count);
        } else {
            ItemFocusBasic focus = this.getFocus(stack);
            if (focus != null && !WandManager.isOnCooldown(player)) {
                WandManager.setCooldown(player, focus.getActivationCooldown(this.getFocusItem(stack)));
                focus.onUsingFocusTick(stack, player, count);
            }
        }

    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, net.minecraft.entity.EntityLivingBase livingBase, int count) {
        if (!(livingBase instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) livingBase;
        IWandable tv = this.getObjectInUse(stack, player.world);
        if (tv != null) {
            tv.onWandStoppedUsing(stack, world, player, count);
            this.animation = null;
        } else {
            ItemFocusBasic focus = this.getFocus(stack);
            if (focus != null) {
                focus.onPlayerStoppedUsingFocus(stack, world, player, count);
            }
        }

        this.clearObjectInUse(stack);
    }

    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.BOW;
    }

    public int getMaxItemUseDuration(ItemStack itemstack) {
        return Integer.MAX_VALUE;
    }

    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        ItemStack focus = this.getFocusItem(stack);
        if (focus != null && !WandManager.isOnCooldown(entityLiving)) {
            WandManager.setCooldown(entityLiving, this.getFocus(stack).getActivationCooldown(focus));
            return focus.getItem().onEntitySwing(entityLiving, stack);
        } else {
            return super.onEntitySwing(entityLiving, stack);
        }
    }

    public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer player) {
        ItemFocusBasic focus = this.getFocus(itemstack);
        if (focus != null && !WandManager.isOnCooldown(player)) {
            WandManager.setCooldown(player, focus.getActivationCooldown(this.getFocusItem(itemstack)));
            return focus.onFocusBlockStartBreak(itemstack, x, y, z, player);
        } else {
            return false;
        }
    }

    public boolean canHarvestBlock(net.minecraft.block.state.IBlockState state, ItemStack itemstack) {
        ItemFocusBasic focus = this.getFocus(itemstack);
        return focus != null && this.getFocusItem(itemstack).getItem().canHarvestBlock(state, itemstack);
    }

    public float getDestroySpeed(ItemStack itemstack, net.minecraft.block.state.IBlockState state) {
        ItemFocusBasic focus = this.getFocus(itemstack);
        return focus != null ? this.getFocusItem(itemstack).getItem().getDestroySpeed(itemstack, state) : super.getDestroySpeed(itemstack, state);
    }

    public ArrayList getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, net.minecraft.util.EnumFacing side, EntityPlayer player) {
        ItemFocusBasic focus = this.getFocus(stack);
        return focus instanceof IArchitect && focus.isUpgradedWith(this.getFocusItem(stack), FocusUpgradeType.architect) ? ((IArchitect) focus).getArchitectBlocks(stack, world, x, y, z, side, player) : null;
    }

    public boolean showAxis(ItemStack stack, World world, EntityPlayer player, net.minecraft.util.EnumFacing side, IArchitect.EnumAxis axis) {
        ItemFocusBasic focus = this.getFocus(stack);
        return focus instanceof IArchitect && focus.isUpgradedWith(this.getFocusItem(stack), FocusUpgradeType.architect) && ((IArchitect) focus).showAxis(stack, world, player, side, axis);
    }
}
