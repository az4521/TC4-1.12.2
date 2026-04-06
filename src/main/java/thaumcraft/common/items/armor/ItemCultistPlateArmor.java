package thaumcraft.common.items.armor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.client.renderers.models.gear.ModelKnightArmor;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.monster.EntityInhabitedZombie;

public class ItemCultistPlateArmor extends ItemArmor implements IRepairable, IRunicArmor {
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

   public ItemCultistPlateArmor(ItemArmor.ArmorMaterial enumarmormaterial, int j, int k) {
      super(enumarmormaterial, j, slotFromIndex(k));
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconHelm = ir.registerSprite("thaumcraft:cultistplatehelm");
      this.iconChest = ir.registerSprite("thaumcraft:cultistplatechest");
      this.iconLegs = ir.registerSprite("thaumcraft:cultistplatelegs");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.armorType == net.minecraft.inventory.EntityEquipmentSlot.HEAD ? this.iconHelm : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? this.iconChest : this.iconLegs);
   }

   public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
      return entity instanceof EntityInhabitedZombie ? "thaumcraft:textures/models/zombie_plate_armor.png" : "thaumcraft:textures/models/cultist_plate_armor.png";
   }

   public net.minecraft.item.EnumRarity getRarity(ItemStack itemstack) {
      return net.minecraft.item.EnumRarity.UNCOMMON;
   }

   public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
      return par2ItemStack.isItemEqual(new ItemStack(Items.IRON_INGOT)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
   }

   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public net.minecraft.client.model.ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, net.minecraft.client.model.ModelBiped _default) {
      net.minecraft.inventory.EntityEquipmentSlot type = ((ItemArmor)itemStack.getItem()).armorType;
      if (this.model1 == null) {
         this.model1 = new ModelKnightArmor(1.0F);
      }

      if (this.model2 == null) {
         this.model2 = new ModelKnightArmor(0.5F);
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
}
