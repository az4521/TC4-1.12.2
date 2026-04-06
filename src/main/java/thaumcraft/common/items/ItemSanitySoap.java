package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class ItemSanitySoap extends Item {
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite icon;

   public ItemSanitySoap() {
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setHasSubtypes(false);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:soap");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public int getMaxItemUseDuration(ItemStack stack) {
      return 200;
   }

   public EnumAction getItemUseAction(ItemStack stack) {
      return EnumAction.BLOCK;
   }

   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
      player.setActiveHand(hand);
      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, player.getHeldItem(hand));
   }

   public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
      if (!(living instanceof EntityPlayer)) return;
      EntityPlayer player = (EntityPlayer) living;
      int ticks = this.getMaxItemUseDuration(stack) - count;
      if (ticks > 195) {
         player.stopActiveHand();
      }

      if (player.world.isRemote) {
         if (player.world.rand.nextFloat() < 0.2F) {
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:roots")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.1F, 1.5F + player.world.rand.nextFloat() * 0.2F); };
         }

         for(int a = 0; a < Thaumcraft.proxy.particleCount(5); ++a) {
            Thaumcraft.proxy.crucibleBubble(Thaumcraft.proxy.getClientWorld(), (float)player.posX - 0.5F + player.world.rand.nextFloat(), (float)player.getEntityBoundingBox().minY + player.world.rand.nextFloat() * player.height, (float)player.posZ - 0.5F + player.world.rand.nextFloat(), 1.0F, 0.8F, 0.9F);
         }
      }

   }

   public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int par4) {
      if (!(living instanceof EntityPlayer)) return;
      EntityPlayer player = (EntityPlayer) living;
      int qq = this.getMaxItemUseDuration(stack) - par4;
      if (qq > 195) {
         stack.shrink(1);
         if (!world.isRemote) {
            float chance = 0.33F;
            if (player.isPotionActive(net.minecraft.potion.Potion.getPotionById(Config.potionWarpWardID))) {
               chance += 0.25F;
            }

            int i = MathHelper.floor(player.posX);
            int j = MathHelper.floor(player.posY);
            int k = MathHelper.floor(player.posZ);
            if (world.getBlockState(new BlockPos(i, j, k)).getBlock() == ConfigBlocks.blockFluidPure) {
               chance += 0.25F;
            }

            if (world.rand.nextFloat() < chance && Thaumcraft.proxy.getPlayerKnowledge().getWarpSticky(player.getName()) > 0) {
               Thaumcraft.addStickyWarpToPlayer(player, -1);
            }

            if (Thaumcraft.proxy.getPlayerKnowledge().getWarpTemp(player.getName()) > 0) {
               Thaumcraft.addWarpToPlayer(player, -Thaumcraft.proxy.getPlayerKnowledge().getWarpTemp(player.getName()), true);
            }
         } else {
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:craftstart")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.25F, 1.0F); };

            for(int a = 0; a < Thaumcraft.proxy.particleCount(20); ++a) {
               Thaumcraft.proxy.crucibleBubble(Thaumcraft.proxy.getClientWorld(), (float)player.posX - 0.5F + player.world.rand.nextFloat() * 1.5F, (float)player.getEntityBoundingBox().minY + player.world.rand.nextFloat() * player.height, (float)player.posZ - 0.5F + player.world.rand.nextFloat() * 1.5F, 1.0F, 0.7F, 0.9F);
            }
         }
      }

   }
}
