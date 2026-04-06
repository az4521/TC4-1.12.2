package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class BlockMagicalLeavesItem extends ItemBlock {
   public BlockMagicalLeavesItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int par1) {
      return par1 | 4;
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      int var2 = par1ItemStack.getItemDamage();
      return super.getTranslationKey(par1ItemStack) + "." + BlockMagicalLeaves.leafType[var2 & 1];
   }
}
