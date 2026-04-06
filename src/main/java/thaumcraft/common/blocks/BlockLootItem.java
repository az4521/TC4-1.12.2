package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class BlockLootItem extends ItemBlock {
   public BlockLootItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   public EnumRarity getRarity(ItemStack stack) {
      switch (stack.getItemDamage()) {
         case 1:
            return EnumRarity.UNCOMMON;
         case 2:
            return EnumRarity.RARE;
         default:
            return EnumRarity.COMMON;
      }
   }

   @Override
   public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
      super.addInformation(stack, world, list, flag);
      list.add(this.getRarity(stack).rarityName);
   }
}
