package thaumcraft.common.items.baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.api.ItemRunic;
import thaumcraft.common.Thaumcraft;

public class ItemRingRunic extends ItemRunic implements IBauble {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[5];

   public ItemRingRunic() {
      super(5);
      this.maxStackSize = 1;
      this.canRepair = false;
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setHasSubtypes(true);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:runic_ring_lesser");
      this.icon[1] = ir.registerSprite("thaumcraft:runic_ring");
      this.icon[2] = ir.registerSprite("thaumcraft:runic_ring_charged");
      this.icon[3] = ir.registerSprite("thaumcraft:runic_ring_regen");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon[par1];
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return itemstack.getItemDamage() == 0 ? EnumRarity.UNCOMMON : EnumRarity.RARE;
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
      par3List.add(new ItemStack(this, 1, 2));
      par3List.add(new ItemStack(this, 1, 3));
   }

   public BaubleType getBaubleType(ItemStack itemstack) {
      return BaubleType.RING;
   }

   public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
   }

   public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
      Thaumcraft.instance.runicEventHandler.isDirty = true;
   }

   public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
      Thaumcraft.instance.runicEventHandler.isDirty = true;
   }

   public int getRunicCharge(ItemStack itemstack) {
      return itemstack.getItemDamage() == 0 ? 1 : (itemstack.getItemDamage() == 1 ? 5 : 4);
   }

   public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
      return true;
   }

   public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
      return true;
   }
}
