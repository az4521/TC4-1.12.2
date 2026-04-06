package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.utils.EntityUtils;

public class ItemCompassStone extends Item {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[2];
   private TextureAtlasSprite t = null;
   public static HashMap<WorldCoordinates,Long> sinisterNodes = new HashMap<>();

   public ItemCompassStone() {
      this.setMaxStackSize(1);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:sinister_stone");
      this.icon[1] = ir.registerSprite("thaumcraft:sinister_stone_active");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return par1 == 1 ? this.icon[1] : (this.t == null ? this.icon[0] : this.t);
   }

   public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
      if (world.isRemote) {
         ArrayList<WorldCoordinates> del = new ArrayList<>();
         this.t = null;

         for(WorldCoordinates wc : sinisterNodes.keySet()) {
            if (sinisterNodes.get(wc) < System.currentTimeMillis() - 10000L) {
               del.add(wc);
            }

            if (wc.dim == world.provider.getDimension() && EntityUtils.isVisibleTo(0.66F, entity, (double)wc.x + (double)0.5F, (double)wc.y + (double)0.5F, (double)wc.z + (double)0.5F, 256.0F)) {
               this.t = this.icon[1];
               break;
            }
         }

         for(WorldCoordinates wc : del) {
            sinisterNodes.remove(wc);
         }
      }

   }

   private double directionToPoint(double x1, double z1, double x2, double z2) {
      double dx = x1 - x2;
      double dz = z1 - z2;
      return Math.atan2(dz, dx) * (double)180.0F / Math.PI;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
   }

   public EnumRarity getRarity(ItemStack stack) {
      return EnumRarity.RARE;
   }
}
