package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import thaumcraft.common.Thaumcraft;

public class BlockCandleItem extends ItemBlock {
   public BlockCandleItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   public String getTranslationKey(ItemStack par1ItemStack) {
      return super.getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }
}
