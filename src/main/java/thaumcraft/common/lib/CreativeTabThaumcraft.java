package thaumcraft.common.lib;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import thaumcraft.common.config.ConfigItems;

public final class CreativeTabThaumcraft extends CreativeTabs {
   public CreativeTabThaumcraft(String par2Str) {
      super(par2Str);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public ItemStack createIcon() {
      return new ItemStack(ConfigItems.itemWandCasting);
   }
}
