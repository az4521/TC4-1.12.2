package thaumcraft.common.items.armor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.model.ModelBiped;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.ISpecialArmor;
import thaumcraft.api.IGoggles;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.client.renderers.models.gear.ModelFortressArmor;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;

public class ItemFortressArmor extends ItemArmor implements IRepairable, IRunicArmor, ISpecialArmor, IGoggles, IRevealer {
   public TextureAtlasSprite iconHelm;
   public TextureAtlasSprite iconChest;
   public TextureAtlasSprite iconLegs;
   ModelBiped model1 = null;
   ModelBiped model2 = null;
   ModelBiped model = null;

   private static net.minecraft.inventory.EntityEquipmentSlot slotFromIndex(int k) {
      switch(k) { case 0: return net.minecraft.inventory.EntityEquipmentSlot.HEAD; case 1: return net.minecraft.inventory.EntityEquipmentSlot.CHEST; case 2: return net.minecraft.inventory.EntityEquipmentSlot.LEGS; default: return net.minecraft.inventory.EntityEquipmentSlot.FEET; }
   }

   public ItemFortressArmor(ItemArmor.ArmorMaterial enumarmormaterial, int j, int k) {
      super(enumarmormaterial, j, slotFromIndex(k));
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconHelm = ir.registerSprite("thaumcraft:thaumiumfortresshelm");
      this.iconChest = ir.registerSprite("thaumcraft:thaumiumfortresschest");
      this.iconLegs = ir.registerSprite("thaumcraft:thaumiumfortresslegs");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.armorType == net.minecraft.inventory.EntityEquipmentSlot.HEAD ? this.iconHelm : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? this.iconChest : this.iconLegs);
   }

   @SideOnly(Side.CLIENT)
   public net.minecraft.client.model.ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, net.minecraft.client.model.ModelBiped _default) {
      net.minecraft.inventory.EntityEquipmentSlot type = ((ItemArmor)itemStack.getItem()).armorType;
      if (this.model1 == null) {
         this.model1 = new ModelFortressArmor(1.0F);
      }

      if (this.model2 == null) {
         this.model2 = new ModelFortressArmor(0.5F);
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

   public String getArmorTexture(ItemStack stack, Entity entity, net.minecraft.inventory.EntityEquipmentSlot slot, String type) {
      return "thaumcraft:textures/models/fortress_armor.png";
   }

   public net.minecraft.item.EnumRarity getRarity(ItemStack itemstack) {
      return net.minecraft.item.EnumRarity.RARE;
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      if (stack.hasTagCompound() && stack.getTagCompound().hasKey("goggles")) {
         list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("item.ItemGoggles.name"));
      }

      if (stack.hasTagCompound() && stack.getTagCompound().hasKey("mask")) {
         list.add(TextFormatting.GOLD + I18n.translateToLocal("item.HelmetFortress.mask." + stack.getTagCompound().getInteger("mask")));
      }

      super.addInformation(stack, worldIn, list, flagIn);
   }

   public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
      return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 2)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
   }

   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
      int priority = 0;
      double ratio = (double)this.damageReduceAmount / (double)25.0F;
      if (source.isMagicDamage()) {
         priority = 1;
         ratio = (double)this.damageReduceAmount / (double)35.0F;
      } else if (!source.isFireDamage() && !source.isExplosion()) {
         if (source.isUnblockable()) {
            priority = 0;
            ratio = 0.0F;
         }
      } else {
         priority = 1;
         ratio = (double)this.damageReduceAmount / (double)20.0F;
      }

      if (player instanceof EntityPlayer) {
         double set = 0.875F;

         for(int a = 1; a < 4; ++a) {
            ItemStack piece = ((EntityPlayer)player).inventory.armorInventory.get(a);
            if (!piece.isEmpty() && piece.getItem() instanceof ItemFortressArmor) {
               set += 0.125F;
               if (piece.hasTagCompound() && piece.getTagCompound().hasKey("mask")) {
                  set += 0.05;
               }
            }
         }

         ratio *= set;
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

   public boolean showNodes(ItemStack itemstack, EntityLivingBase player) {
      return itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("goggles");
   }

   public boolean showIngamePopups(ItemStack itemstack, EntityLivingBase player) {
      return itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("goggles");
   }
}
