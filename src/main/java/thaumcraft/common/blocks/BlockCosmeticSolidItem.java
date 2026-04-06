package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class BlockCosmeticSolidItem extends ItemBlock {
   public BlockCosmeticSolidItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   @Override
   public String getTranslationKey(ItemStack stack) {
      return super.getTranslationKey(stack) + "." + stack.getItemDamage();
   }
}
