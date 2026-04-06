package thaumcraft.common.items;

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

public class ItemNugget extends Item {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[64];

   public ItemNugget() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:nuggetiron");
      this.icon[16] = ir.registerSprite("thaumcraft:clusteriron");
      this.icon[5] = ir.registerSprite("thaumcraft:nuggetquicksilver");
      this.icon[21] = ir.registerSprite("thaumcraft:clustercinnabar");
      this.icon[6] = ir.registerSprite("thaumcraft:nuggetthaumium");
      this.icon[7] = ir.registerSprite("thaumcraft:nuggetvoid");
      this.icon[31] = ir.registerSprite("thaumcraft:clustergold");
      this.icon[1] = ir.registerSprite("thaumcraft:nuggetcopper");
      this.icon[17] = ir.registerSprite("thaumcraft:clustercopper");
      this.icon[2] = ir.registerSprite("thaumcraft:nuggettin");
      this.icon[18] = ir.registerSprite("thaumcraft:clustertin");
      this.icon[3] = ir.registerSprite("thaumcraft:nuggetsilver");
      this.icon[19] = ir.registerSprite("thaumcraft:clustersilver");
      this.icon[4] = ir.registerSprite("thaumcraft:nuggetlead");
      this.icon[20] = ir.registerSprite("thaumcraft:clusterlead");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int meta) {
      return this.icon[meta];
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 5));
      par3List.add(new ItemStack(this, 1, 21));
      par3List.add(new ItemStack(this, 1, 6));
      par3List.add(new ItemStack(this, 1, 7));
      par3List.add(new ItemStack(this, 1, 16));
      par3List.add(new ItemStack(this, 1, 31));
      if (Config.foundCopperIngot) {
         par3List.add(new ItemStack(this, 1, 1));
         par3List.add(new ItemStack(this, 1, 17));
      }

      if (Config.foundTinIngot) {
         par3List.add(new ItemStack(this, 1, 2));
         par3List.add(new ItemStack(this, 1, 18));
      }

      if (Config.foundSilverIngot) {
         par3List.add(new ItemStack(this, 1, 3));
         par3List.add(new ItemStack(this, 1, 19));
      }

      if (Config.foundLeadIngot) {
         par3List.add(new ItemStack(this, 1, 4));
         par3List.add(new ItemStack(this, 1, 20));
      }

   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }
}
