package thaumcraft.common.items.equipment;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.common.Thaumcraft;

public class ItemPrimalArrow extends Item {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[6];

   public ItemPrimalArrow() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:el_arrow_air");
      this.icon[1] = ir.registerSprite("thaumcraft:el_arrow_fire");
      this.icon[2] = ir.registerSprite("thaumcraft:el_arrow_water");
      this.icon[3] = ir.registerSprite("thaumcraft:el_arrow_earth");
      this.icon[4] = ir.registerSprite("thaumcraft:el_arrow_order");
      this.icon[5] = ir.registerSprite("thaumcraft:el_arrow_entropy");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon[par1];
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      for(int a = 0; a <= 5; ++a) {
         par3List.add(new ItemStack(this, 1, a));
      }

   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }
}
