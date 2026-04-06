package thaumcraft.common.items.wands;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;

public class ItemWandCap extends Item {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[9];

   public ItemWandCap() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:wand_cap_iron");
      this.icon[1] = ir.registerSprite("thaumcraft:wand_cap_gold");
      this.icon[2] = ir.registerSprite("thaumcraft:wand_cap_thaumium");
      this.icon[3] = ir.registerSprite("thaumcraft:wand_cap_copper");
      this.icon[4] = ir.registerSprite("thaumcraft:wand_cap_silver");
      this.icon[5] = ir.registerSprite("thaumcraft:wand_cap_silver_inert");
      this.icon[6] = ir.registerSprite("thaumcraft:wand_cap_thaumium_inert");
      this.icon[7] = ir.registerSprite("thaumcraft:wand_cap_void");
      this.icon[8] = ir.registerSprite("thaumcraft:wand_cap_void_inert");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int meta) {
      return this.icon[meta];
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
      if (Config.foundCopperIngot) {
         par3List.add(new ItemStack(this, 1, 3));
      }

      if (Config.foundSilverIngot) {
         par3List.add(new ItemStack(this, 1, 4));
         par3List.add(new ItemStack(this, 1, 5));
      }

      par3List.add(new ItemStack(this, 1, 2));
      par3List.add(new ItemStack(this, 1, 6));
      par3List.add(new ItemStack(this, 1, 7));
      par3List.add(new ItemStack(this, 1, 8));
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }
}
