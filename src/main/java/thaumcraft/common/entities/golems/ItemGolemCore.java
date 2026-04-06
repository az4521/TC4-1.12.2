package thaumcraft.common.entities.golems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import thaumcraft.common.Thaumcraft;

public class ItemGolemCore extends Item {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[12];
   public TextureAtlasSprite blankIcon;

   public ItemGolemCore() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:golem_core_fill");
      this.icon[1] = ir.registerSprite("thaumcraft:golem_core_empty");
      this.icon[2] = ir.registerSprite("thaumcraft:golem_core_gather");
      this.icon[3] = ir.registerSprite("thaumcraft:golem_core_harvest");
      this.icon[4] = ir.registerSprite("thaumcraft:golem_core_guard");
      this.icon[5] = ir.registerSprite("thaumcraft:golem_core_liquid");
      this.icon[6] = ir.registerSprite("thaumcraft:golem_core_essentia");
      this.icon[7] = ir.registerSprite("thaumcraft:golem_core_lumber");
      this.icon[8] = ir.registerSprite("thaumcraft:golem_core_use");
      this.icon[9] = ir.registerSprite("thaumcraft:golem_core_butcher");
      this.icon[10] = ir.registerSprite("thaumcraft:golem_core_sorting");
      this.icon[11] = ir.registerSprite("thaumcraft:golem_core_fish");
      this.blankIcon = ir.registerSprite("thaumcraft:golem_core_blank");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int d) {
      return d == 100 ? this.blankIcon : this.icon[d];
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 100));

      for(int a = 0; a <= 11; ++a) {
         par3List.add(new ItemStack(this, 1, a));
      }

   }

   public void addInformation(ItemStack stack, World worldIn, List<String> list, net.minecraft.client.util.ITooltipFlag flag) {
      list.add(I18n.translateToLocal("item.ItemGolemCore." + stack.getItemDamage() + ".name"));
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return itemstack.getItemDamage() == 100 ? net.minecraft.item.EnumRarity.COMMON : net.minecraft.item.EnumRarity.UNCOMMON;
   }

   public static boolean hasGUI(int core) {
      switch (core) {
         case 0:
         case 1:
         case 2:
         case 4:
         case 5:
         case 8:
         case 10:
            return true;
         case 3:
         case 6:
         case 7:
         case 9:
         default:
            return false;
      }
   }

   public static boolean canSort(int core) {
      switch (core) {
         case 0:
         case 1:
         case 2:
         case 8:
         case 10:
            return true;
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 9:
         default:
            return false;
      }
   }

   public static boolean hasInventory(int core) {
      switch (core) {
         case 0:
         case 1:
         case 2:
         case 5:
         case 8:
            return true;
         case 3:
         case 4:
         case 6:
         case 7:
         default:
            return false;
      }
   }
}
