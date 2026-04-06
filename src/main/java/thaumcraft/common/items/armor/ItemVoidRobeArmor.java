package thaumcraft.common.items.armor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.model.ModelBiped;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import thaumcraft.api.IGoggles;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.client.renderers.models.gear.ModelRobe;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import net.minecraft.util.math.BlockPos;

public class ItemVoidRobeArmor extends ItemArmor implements IRepairable, IRunicArmor, IVisDiscountGear, IGoggles, IRevealer, ISpecialArmor, IWarpingGear {
   public TextureAtlasSprite iconHelm;
   public TextureAtlasSprite iconChest;
   public TextureAtlasSprite iconLegs;
   public TextureAtlasSprite iconChestOver;
   public TextureAtlasSprite iconLegsOver;
   public TextureAtlasSprite iconBlank;
   ModelBiped model1 = null;
   ModelBiped model2 = null;
   ModelBiped model = null;

   private static net.minecraft.inventory.EntityEquipmentSlot slotFromIndex(int k) {
      switch(k) { case 0: return net.minecraft.inventory.EntityEquipmentSlot.HEAD; case 1: return net.minecraft.inventory.EntityEquipmentSlot.CHEST; case 2: return net.minecraft.inventory.EntityEquipmentSlot.LEGS; default: return net.minecraft.inventory.EntityEquipmentSlot.FEET; }
   }

   public ItemVoidRobeArmor(ItemArmor.ArmorMaterial enumarmormaterial, int j, int k) {
      super(enumarmormaterial, j, slotFromIndex(k));
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconHelm = ir.registerSprite("thaumcraft:voidrobehelm");
      this.iconBlank = ir.registerSprite("thaumcraft:blank");
      this.iconChest = ir.registerSprite("thaumcraft:voidrobechestover");
      this.iconLegs = ir.registerSprite("thaumcraft:voidrobelegsover");
      this.iconChestOver = ir.registerSprite("thaumcraft:voidrobechest");
      this.iconLegsOver = ir.registerSprite("thaumcraft:voidrobelegs");
   }

   public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
      return type == null ? "thaumcraft:textures/models/void_robe_armor_overlay.png" : "thaumcraft:textures/models/void_robe_armor.png";
   }

   public net.minecraft.item.EnumRarity getRarity(ItemStack itemstack) {
      return net.minecraft.item.EnumRarity.EPIC;
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("tc.visdiscount") + ": " + this.getVisDiscount(stack, null, null) + "%");
      super.addInformation(stack, worldIn, list, flagIn);
   }

   public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
      return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 16)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
   }

   public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
      super.onUpdate(stack, world, entity, itemSlot, isSelected);
      if (!world.isRemote && stack.isItemDamaged() && entity.ticksExisted % 20 == 0 && entity instanceof EntityLivingBase) {
         stack.damageItem(-1, (EntityLivingBase)entity);
      }

   }

   public void onArmorTick(World world, EntityPlayer player, ItemStack armor) {
      super.onArmorTick(world, player, armor);
      if (!world.isRemote && armor.getItemDamage() > 0 && player.ticksExisted % 20 == 0) {
         armor.damageItem(-1, player);
      }

   }

   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   public boolean showNodes(ItemStack itemstack, EntityLivingBase player) {
      net.minecraft.inventory.EntityEquipmentSlot type = ((ItemArmor)itemstack.getItem()).armorType;
      return type == net.minecraft.inventory.EntityEquipmentSlot.HEAD;
   }

   public boolean showIngamePopups(ItemStack itemstack, EntityLivingBase player) {
      net.minecraft.inventory.EntityEquipmentSlot type = ((ItemArmor)itemstack.getItem()).armorType;
      return type == net.minecraft.inventory.EntityEquipmentSlot.HEAD;
   }

   public int getVisDiscount(ItemStack stack, EntityPlayer player, Aspect aspect) {
      return 5;
   }

   @SideOnly(Side.CLIENT)
   public net.minecraft.client.model.ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, net.minecraft.client.model.ModelBiped _default) {
      net.minecraft.inventory.EntityEquipmentSlot type = ((ItemArmor)itemStack.getItem()).armorType;
      if (this.model1 == null) {
         this.model1 = new ModelRobe(1.0F);
      }

      if (this.model2 == null) {
         this.model2 = new ModelRobe(0.5F);
      }

      if (type != net.minecraft.inventory.EntityEquipmentSlot.CHEST && type != net.minecraft.inventory.EntityEquipmentSlot.LEGS) {
         this.model = this.model2;
      } else {
         this.model = this.model1;
      }

      if (this.model != null) {
         this.model.bipedHead.showModel = armorSlot == net.minecraft.inventory.EntityEquipmentSlot.HEAD;
         this.model.bipedHeadwear.showModel = armorSlot == net.minecraft.inventory.EntityEquipmentSlot.HEAD;
         this.model.bipedBody.showModel = armorSlot == net.minecraft.inventory.EntityEquipmentSlot.CHEST || armorSlot == net.minecraft.inventory.EntityEquipmentSlot.LEGS;
         this.model.bipedRightArm.showModel = armorSlot == net.minecraft.inventory.EntityEquipmentSlot.CHEST;
         this.model.bipedLeftArm.showModel = armorSlot == net.minecraft.inventory.EntityEquipmentSlot.CHEST;
         this.model.bipedRightLeg.showModel = armorSlot == net.minecraft.inventory.EntityEquipmentSlot.LEGS;
         this.model.bipedLeftLeg.showModel = armorSlot == net.minecraft.inventory.EntityEquipmentSlot.LEGS;
         this.model.isSneak = entityLiving.isSneaking();
         this.model.isRiding = entityLiving.isRiding();
         this.model.isChild = entityLiving.isChild();
         this.model.rightArmPose = !entityLiving.getHeldItemMainhand().isEmpty() ? net.minecraft.client.model.ModelBiped.ArmPose.ITEM : net.minecraft.client.model.ModelBiped.ArmPose.EMPTY;
         if (entityLiving instanceof EntityPlayer && ((EntityPlayer)entityLiving).isHandActive()) {
            net.minecraft.item.EnumAction enumaction = ((EntityPlayer)entityLiving).getActiveItemStack().getItemUseAction();
            if (enumaction == net.minecraft.item.EnumAction.BLOCK) {
               this.model.rightArmPose = net.minecraft.client.model.ModelBiped.ArmPose.BLOCK;
            } else if (enumaction == net.minecraft.item.EnumAction.BOW) {
               this.model.rightArmPose = net.minecraft.client.model.ModelBiped.ArmPose.BOW_AND_ARROW;
            }
         }
      }

      return this.model;
   }

   @SideOnly(Side.CLIENT)
   public boolean requiresMultipleRenderPasses() {
      return true;
   }

   public boolean hasColor(ItemStack par1ItemStack) {
      return true;
   }

   public TextureAtlasSprite getIconFromDamageForRenderPass(int par1, int par2) {
      return par2 == 0 ? (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? this.iconChest : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.LEGS ? this.iconLegs : this.iconHelm)) : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? this.iconChestOver : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.LEGS ? this.iconLegsOver : this.iconBlank));
   }

   public int getColor(ItemStack par1ItemStack) {
      NBTTagCompound nbttagcompound = par1ItemStack.getTagCompound();
      if (nbttagcompound == null) {
         return 6961280;
      } else {
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
         return nbttagcompound1 == null ? 6961280 : (nbttagcompound1.hasKey("color") ? nbttagcompound1.getInteger("color") : 6961280);
      }
   }

   public void removeColor(ItemStack par1ItemStack) {
      NBTTagCompound nbttagcompound = par1ItemStack.getTagCompound();
      if (nbttagcompound != null) {
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
         if (nbttagcompound1.hasKey("color")) {
            nbttagcompound1.removeTag("color");
         }
      }

   }

   public void setColor(ItemStack par1ItemStack, int par2) {
      NBTTagCompound nbttagcompound = par1ItemStack.getTagCompound();
      if (nbttagcompound == null) {
         nbttagcompound = new NBTTagCompound();
         par1ItemStack.setTagCompound(nbttagcompound);
      }

      NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
      if (!nbttagcompound.hasKey("display")) {
         nbttagcompound.setTag("display", nbttagcompound1);
      }

      nbttagcompound1.setInteger("color", par2);
   }

   public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
      int priority = 0;
      double ratio = (double)this.damageReduceAmount / (double)25.0F;
      if (source.isMagicDamage()) {
         priority = 1;
         ratio = (double)this.damageReduceAmount / (double)35.0F;
      } else if (source.isUnblockable()) {
         priority = 0;
         ratio = 0.0F;
      }

      return new ISpecialArmor.ArmorProperties(priority, ratio, armor.getMaxDamage() + 1 - armor.getItemDamage());
   }

   public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
      return this.damageReduceAmount;
   }

   public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
      if (source != DamageSource.FALL) {
         stack.damageItem(damage, entity);
      }

   }

   public net.minecraft.util.EnumActionResult onItemUseFirst(EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side, float hitX, float hitY, float hitZ, net.minecraft.util.EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      if (!world.isRemote && world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.CAULDRON &&
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z))) > 0) {
         this.removeColor(stack);
         { net.minecraft.util.math.BlockPos _p = new net.minecraft.util.math.BlockPos(x, y, z); int _m = world.getBlockState(_p).getBlock().getMetaFromState(world.getBlockState(_p)); world.setBlockState(_p, world.getBlockState(_p).getBlock().getStateFromMeta(_m - 1), 2); }
         world.notifyNeighborsOfStateChange(new net.minecraft.util.math.BlockPos(x, y, z), Blocks.CAULDRON, false);
         return net.minecraft.util.EnumActionResult.SUCCESS;
      } else {
         return net.minecraft.util.EnumActionResult.PASS;
      }
   }

   public int getWarp(ItemStack itemstack, EntityPlayer player) {
      return 2;
   }
}
