package thaumcraft.common.items.armor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IWarpingGear;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;

public class ItemVoidArmor extends ItemArmor implements IRepairable, IRunicArmor, IWarpingGear {
   public TextureAtlasSprite iconHelm;
   public TextureAtlasSprite iconChest;
   public TextureAtlasSprite iconLegs;
   public TextureAtlasSprite iconBoots;

   private static net.minecraft.inventory.EntityEquipmentSlot slotFromIndex(int k) {
      switch(k) { case 0: return net.minecraft.inventory.EntityEquipmentSlot.HEAD; case 1: return net.minecraft.inventory.EntityEquipmentSlot.CHEST; case 2: return net.minecraft.inventory.EntityEquipmentSlot.LEGS; default: return net.minecraft.inventory.EntityEquipmentSlot.FEET; }
   }

   public ItemVoidArmor(ItemArmor.ArmorMaterial enumarmormaterial, int j, int k) {
      super(enumarmormaterial, j, slotFromIndex(k));
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconHelm = ir.registerSprite("thaumcraft:voidhelm");
      this.iconChest = ir.registerSprite("thaumcraft:voidchest");
      this.iconLegs = ir.registerSprite("thaumcraft:voidlegs");
      this.iconBoots = ir.registerSprite("thaumcraft:voidboots");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.armorType == net.minecraft.inventory.EntityEquipmentSlot.HEAD ? this.iconHelm : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? this.iconChest : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.LEGS ? this.iconLegs : this.iconBoots));
   }

   public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
      if (stack.getItem() != ConfigItems.itemHelmetVoid && stack.getItem() != ConfigItems.itemChestVoid && stack.getItem() != ConfigItems.itemBootsVoid) {
         return stack.getItem() == ConfigItems.itemLegsVoid ? "thaumcraft:textures/models/void_2.png" : "thaumcraft:textures/models/void_1.png";
      } else {
         return "thaumcraft:textures/models/void_1.png";
      }
   }

   public net.minecraft.item.EnumRarity getRarity(ItemStack itemstack) {
      return net.minecraft.item.EnumRarity.UNCOMMON;
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

   public int getWarp(ItemStack itemstack, EntityPlayer player) {
      return 1;
   }
}
