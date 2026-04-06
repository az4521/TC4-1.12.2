package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.common.Thaumcraft;

public class ItemNuggetEdible extends ItemFood {
   public final int itemUseDuration = 10;
   public final String iconName;
   public TextureAtlasSprite icon;

   public ItemNuggetEdible(String iconName) {
      super(1, 0.3F, false);
      this.iconName = iconName;
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public int getMaxItemUseDuration(ItemStack par1ItemStack) {
      return this.itemUseDuration;
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:" + this.iconName);
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int meta) {
      return this.icon;
   }
}
