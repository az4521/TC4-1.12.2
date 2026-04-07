package thaumcraft.common.items.armor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.model.ModelBiped;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.renderers.models.gear.ModelRobe;
import thaumcraft.common.Thaumcraft;

public class ItemCultistRobeArmor extends ItemArmor implements IRepairable, IRunicArmor, IVisDiscountGear, IWarpingGear {
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

   public ItemCultistRobeArmor(ItemArmor.ArmorMaterial enumarmormaterial, int j, int k) {
      super(enumarmormaterial, j, slotFromIndex(k));
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconHelm = ir.registerSprite("thaumcraft:cultistrobehelm");
      this.iconChest = ir.registerSprite("thaumcraft:cultistrobechest");
      this.iconLegs = ir.registerSprite("thaumcraft:cultistrobelegs");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.armorType == net.minecraft.inventory.EntityEquipmentSlot.HEAD ? this.iconHelm : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? this.iconChest : this.iconLegs);
   }

   public String getArmorTexture(ItemStack stack, Entity entity, net.minecraft.inventory.EntityEquipmentSlot slot, String type) {
      return "thaumcraft:textures/models/cultist_robe_armor.png";
   }

   public net.minecraft.item.EnumRarity getRarity(ItemStack itemstack) {
      return net.minecraft.item.EnumRarity.UNCOMMON;
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("tc.visdiscount") + ": " + this.getVisDiscount(stack, null, null) + "%");
      super.addInformation(stack, worldIn, list, flagIn);
   }

   public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
      return par2ItemStack.isItemEqual(new ItemStack(Items.IRON_INGOT)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
   }

   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   public int getVisDiscount(ItemStack stack, EntityPlayer player, Aspect aspect) {
      return 1;
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

   public int getWarp(ItemStack itemstack, EntityPlayer player) {
      return 1;
   }
}
