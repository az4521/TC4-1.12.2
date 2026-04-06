package thaumcraft.common.entities.golems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import thaumcraft.common.Thaumcraft;

public class ItemGolemDecoration extends Item {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[8];

   public ItemGolemDecoration() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:golemdecotophat");
      this.icon[1] = ir.registerSprite("thaumcraft:golemdecoglasses");
      this.icon[2] = ir.registerSprite("thaumcraft:golemdecobowtie");
      this.icon[3] = ir.registerSprite("thaumcraft:golemdecofez");
      this.icon[4] = ir.registerSprite("thaumcraft:golemdecodart");
      this.icon[5] = ir.registerSprite("thaumcraft:golemdecovisor");
      this.icon[6] = ir.registerSprite("thaumcraft:golemdecoarmor");
      this.icon[7] = ir.registerSprite("thaumcraft:golemdecomace");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int d) {
      return this.icon[d];
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      for(int a = 0; a <= 7; ++a) {
         par3List.add(new ItemStack(this, 1, a));
      }

   }

   public static String getDecoChar(int md) {
      switch (md) {
         case 0:
            return "H";
         case 1:
            return "G";
         case 2:
            return "B";
         case 3:
            return "F";
         case 4:
            return "R";
         case 5:
            return "V";
         case 6:
            return "P";
         case 7:
            return "M";
         default:
            return "";
      }
   }

   public String getItemStackDisplayName(ItemStack stack) {
      return I18n.translateToLocal("item.ItemGolemDecoration.name") + ": " + super.getItemStackDisplayName(stack);
   }

   public String getTranslationKey(ItemStack par1ItemStack) {
      return super.getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }
}
