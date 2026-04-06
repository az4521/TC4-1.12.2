package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;

import static thaumcraft.api.aspects.AspectList.addAspectDescriptionToList;

public class ItemWispEssence extends Item implements IEssentiaContainerItem {
   public TextureAtlasSprite icon;
   static Aspect[] displayAspects;

   public ItemWispEssence() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:wispessence");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      for(Aspect tag : Aspect.aspects.values()) {
         ItemStack i = new ItemStack(this, 1, 0);
         this.setAspects(i, (new AspectList()).add(tag, 2));
         par3List.add(i);
      }

   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      AspectList aspects = this.getAspects(stack);
      addAspectDescriptionToList(aspects, net.minecraft.client.Minecraft.getMinecraft().player, list);

      super.addInformation(stack, worldIn, list, flagIn);
   }

   @SideOnly(Side.CLIENT)
   public int getColorFromItemStack(ItemStack stack, int par2) {
      if (stack == null){return 0;}
      if (this.getAspects(stack) != null) {
         return this.getAspects(stack).getAspects()[0].getColor();
      } else {
         int idx = (int)(System.currentTimeMillis() / 500L % (long)displayAspects.length);
         return displayAspects[idx].getColor();
      }
   }

   public AspectList getAspects(ItemStack itemstack) {
      if (itemstack == null) {
         return null;
      }
      if (itemstack.hasTagCompound()) {
         AspectList aspects = new AspectList();
         aspects.readFromNBT(itemstack.getTagCompound());
         return aspects.size() > 0 ? aspects : null;
      } else {
         return null;
      }
   }

   public void setAspects(ItemStack itemstack, AspectList aspects) {
      if (!itemstack.hasTagCompound()) {
         itemstack.setTagCompound(new NBTTagCompound());
      }

      aspects.writeToNBT(itemstack.getTagCompound());
   }

   static {
      displayAspects = Aspect.aspects.values().toArray(new Aspect[0]);
   }
}
