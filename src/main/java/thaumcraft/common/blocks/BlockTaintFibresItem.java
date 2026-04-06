package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class BlockTaintFibresItem extends ItemBlock {
   public BlockTaintFibresItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   @Override
   public int getMetadata(int par1) {
      return par1;
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return super.getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   // Item tint color is registered via Minecraft.getMinecraft().getItemColors()
   // .registerItemColorHandler() on the client side in 1.12.2 — not an Item override.
   // getIconFromDamage removed — textures handled by JSON models in 1.12.2.
}
