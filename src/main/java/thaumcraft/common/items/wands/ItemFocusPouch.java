package thaumcraft.common.items.wands;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;

public class ItemFocusPouch extends Item {
   protected TextureAtlasSprite icon;

   public ItemFocusPouch() {
      this.setMaxStackSize(1);
      this.setHasSubtypes(false);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister par1IconRegister) {
      this.icon = par1IconRegister.registerSprite("thaumcraft:focuspouch");
   }

   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public boolean getShareTag() {
       return super.getShareTag();
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.RARE;
   }

   public boolean hasEffect(ItemStack par1ItemStack) {
      return false;
   }

   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World par2World, EntityPlayer par3EntityPlayer, net.minecraft.util.EnumHand hand) {
      if (!par2World.isRemote) {
         par3EntityPlayer.openGui(Thaumcraft.instance, 5, par2World, net.minecraft.util.math.MathHelper.floor(par3EntityPlayer.posX), net.minecraft.util.math.MathHelper.floor(par3EntityPlayer.posY), net.minecraft.util.math.MathHelper.floor(par3EntityPlayer.posZ));
      }

      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, par3EntityPlayer.getHeldItem(hand));
   }

   public ItemStack[] getInventory(ItemStack item) {
      ItemStack[] stackList = new ItemStack[18];
      if (item.hasTagCompound()) {
         NBTTagList var2 = item.getTagCompound().getTagList("Inventory", 10);

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            NBTTagCompound var4 = var2.getCompoundTagAt(var3);
            int var5 = var4.getByte("Slot") & 255;
            if (var5 >= 0 && var5 < stackList.length) {
               stackList[var5] = new ItemStack(var4);
            }
         }
      }

      return stackList;
   }

   public void setInventory(ItemStack item, ItemStack[] stackList) {
      NBTTagList var2 = new NBTTagList();

      for(int var3 = 0; var3 < stackList.length; ++var3) {
         if (stackList[var3] != null) {
            NBTTagCompound var4 = new NBTTagCompound();
            var4.setByte("Slot", (byte)var3);
            stackList[var3].writeToNBT(var4);
            var2.appendTag(var4);
         }
      }

      item.setTagInfo("Inventory", var2);
   }
}
