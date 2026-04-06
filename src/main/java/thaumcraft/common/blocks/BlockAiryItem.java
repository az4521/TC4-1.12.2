package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockAiryItem extends ItemBlock {

   public BlockAiryItem(Block b) {
      super(b);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   public String getTranslationKey(ItemStack par1ItemStack) {
      return super.getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack par1ItemStack, @Nullable World world, List<String> list, ITooltipFlag flag) {
      if (par1ItemStack.getItemDamage() == 0) {
         list.add("§5Place a randomly generated node");
         list.add("§oCreative Mode Only");
      }

      super.addInformation(par1ItemStack, world, list, flag);
   }
}
