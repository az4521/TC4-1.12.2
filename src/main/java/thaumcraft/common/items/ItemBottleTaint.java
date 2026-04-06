package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import thaumcraft.api.IScribeTools;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.projectile.EntityBottleTaint;

public class ItemBottleTaint extends Item implements IScribeTools {
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite icon;

   public ItemBottleTaint() {
      this.maxStackSize = 8;
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setHasSubtypes(false);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:bottle_taint");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      if (!player.capabilities.isCreativeMode) {
         stack.shrink(1);
      }

      { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:random.bow")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F)); };
      if (!world.isRemote) {
         world.spawnEntity(new EntityBottleTaint(world, player));
      }

      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, stack);
   }
}
