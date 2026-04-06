package thaumcraft.common.container;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

public class SlotCraftingArcaneWorkbench extends Slot {
   private final IInventory craftMatrix;
   private EntityPlayer thePlayer;
   private int amountCrafted;

   public SlotCraftingArcaneWorkbench(EntityPlayer par1EntityPlayer, IInventory par2IInventory, IInventory par3IInventory, int par4, int par5, int par6) {
      super(par3IInventory, par4, par5, par6);
      this.thePlayer = par1EntityPlayer;
      this.craftMatrix = par2IInventory;
   }

   public boolean isItemValid(ItemStack stack) {
      return false;
   }

   public ItemStack decrStackSize(int amount) {
      if (this.getHasStack()) {
         this.amountCrafted += Math.min(amount, this.getStack().getCount());
      }
      return super.decrStackSize(amount);
   }

   protected void onCrafting(ItemStack stack, int amount) {
      this.amountCrafted += amount;
      this.onCrafting(stack);
   }

   protected void onCrafting(ItemStack stack) {
      if (this.amountCrafted > 0) {
         stack.onCrafting(this.thePlayer.world, this.thePlayer, this.amountCrafted);
      }
      this.amountCrafted = 0;
   }

   @Override
   public ItemStack onTake(EntityPlayer par1EntityPlayer, ItemStack par1ItemStack) {
      FMLCommonHandler.instance().firePlayerCraftingEvent(this.thePlayer, par1ItemStack, this.craftMatrix);
      this.onCrafting(par1ItemStack);
      AspectList aspects = ThaumcraftCraftingManager.findMatchingArcaneRecipeAspects(this.craftMatrix, this.thePlayer);
      if (aspects.size() > 0 && !this.craftMatrix.getStackInSlot(10).isEmpty()) {
         ItemWandCasting wand = (ItemWandCasting)this.craftMatrix.getStackInSlot(10).getItem();
         wand.consumeAllVisCrafting(this.craftMatrix.getStackInSlot(10), par1EntityPlayer, aspects, true);
      }

      for (int var2 = 0; var2 < 9; ++var2) {
         ItemStack var3 = this.craftMatrix.getStackInSlot(var2);
         if (!var3.isEmpty()) {
            this.craftMatrix.decrStackSize(var2, 1);
            if (var3.getItem().hasContainerItem(var3)) {
               ItemStack var4 = var3.getItem().getContainerItem(var3);
               if (!var4.isEmpty() && var4.isItemStackDamageable() && var4.getItemDamage() > var4.getMaxDamage()) {
                  MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(this.thePlayer, var4, null));
               } else if (!this.thePlayer.inventory.addItemStackToInventory(var4)) {
                  if (this.craftMatrix.getStackInSlot(var2).isEmpty()) {
                     this.craftMatrix.setInventorySlotContents(var2, var4);
                  } else {
                     this.thePlayer.dropItem(var4, false);
                  }
               }
            }
         }
      }

      return par1ItemStack;
   }
}
