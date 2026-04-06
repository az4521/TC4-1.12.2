package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;

public class ItemZombieBrain extends ItemFood {
   public TextureAtlasSprite icon;

   public ItemZombieBrain() {
      super(4, 0.2F, true);
      this.setPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 0), 0.8F);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:brain");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int meta) {
      return this.icon;
   }

   protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
      if (!world.isRemote && player instanceof EntityPlayerMP) {
         if (world.rand.nextFloat() < 0.1F) {
            Thaumcraft.addStickyWarpToPlayer(player, 1);
         } else {
            Thaumcraft.addWarpToPlayer(player, 1 + world.rand.nextInt(3), true);
         }
      }

      super.onFoodEaten(stack, world, player);
   }
}
