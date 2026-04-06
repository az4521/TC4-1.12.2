package thaumcraft.common.lib.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import thaumcraft.api.IRepairable;

public class EnchantmentRepair extends Enchantment {
   public EnchantmentRepair(int par1, int par2) {
      super(Enchantment.Rarity.UNCOMMON, EnumEnchantmentType.ALL, new EntityEquipmentSlot[0]);
      this.setName("repair");
   }

   public int getMinEnchantability(int par1) {
      return 20 + (par1 - 1) * 10;
   }

   public int getMaxEnchantability(int par1) {
      return super.getMinEnchantability(par1) + 50;
   }

   public int getMaxLevel() {
      return 2;
   }

   public boolean canApply(ItemStack stack) {
      return stack.isItemStackDamageable() && (stack.getItem() instanceof IRepairable || stack.getItem() instanceof ItemBook);
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack) {
      return this.canApply(stack);
   }

   public boolean canApplyTogether(Enchantment par1Enchantment) {
      return super.canApplyTogether(par1Enchantment) && par1Enchantment != Enchantments.UNBREAKING;
   }
}
