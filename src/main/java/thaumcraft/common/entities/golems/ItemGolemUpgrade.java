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

public class ItemGolemUpgrade extends Item {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[6];
   public TextureAtlasSprite iconEmpty;

   public ItemGolemUpgrade() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconEmpty = ir.registerSprite("thaumcraft:golem_upgrade_empty");
      this.icon[0] = ir.registerSprite("thaumcraft:golem_upgrade_air");
      this.icon[1] = ir.registerSprite("thaumcraft:golem_upgrade_earth");
      this.icon[2] = ir.registerSprite("thaumcraft:golem_upgrade_fire");
      this.icon[3] = ir.registerSprite("thaumcraft:golem_upgrade_water");
      this.icon[4] = ir.registerSprite("thaumcraft:golem_upgrade_order");
      this.icon[5] = ir.registerSprite("thaumcraft:golem_upgrade_entropy");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int d) {
      return d < 0 ? this.iconEmpty : this.icon[d];
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      for(int a = 0; a <= 5; ++a) {
         par3List.add(new ItemStack(this, 1, a));
      }

   }

   public String getTranslationKey(ItemStack par1ItemStack) {
      return super.getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   public void addInformation(ItemStack stack, net.minecraft.world.World worldIn, List<String> list, net.minecraft.client.util.ITooltipFlag flag) {
      list.add(I18n.translateToLocal("item.ItemGolemUpgrade." + stack.getItemDamage() + ".desc"));
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return net.minecraft.item.EnumRarity.UNCOMMON;
   }
}
