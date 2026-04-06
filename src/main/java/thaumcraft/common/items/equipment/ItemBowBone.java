package thaumcraft.common.items.equipment;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import thaumcraft.api.IRepairable;
import thaumcraft.common.Thaumcraft;

public class ItemBowBone extends ItemBow implements IRepairable {

   public ItemBowBone() {
      this.maxStackSize = 1;
      this.setMaxDamage(512);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
      int ticks = this.getMaxItemUseDuration(stack) - count;
      if (ticks > 18) {
         player.stopActiveHand();
      }
   }

   public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityLivingBase par3EntityLiving, int par4) {
      if (!(par3EntityLiving instanceof EntityPlayer)) return;
      EntityPlayer par3EntityPlayer = (EntityPlayer) par3EntityLiving;
      int j = this.getMaxItemUseDuration(par1ItemStack) - par4;
      boolean hasAmmo = par3EntityPlayer.capabilities.isCreativeMode || par3EntityPlayer.inventory.hasItemStack(new ItemStack(Items.ARROW));
      ArrowLooseEvent event = new ArrowLooseEvent(par3EntityPlayer, par1ItemStack, par2World, j, hasAmmo);
      MinecraftForge.EVENT_BUS.post(event);
      if (!event.isCanceled()) {
         j = event.getCharge();
         boolean flag = par3EntityPlayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, par1ItemStack) > 0;
         if (flag || par3EntityPlayer.inventory.hasItemStack(new ItemStack(Items.ARROW))) {
            float f = (float)j / 10.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            if ((double)f < 0.1) {
               return;
            }
            if (f > 1.0F) {
               f = 1.0F;
            }

            EntityTippedArrow entityarrow = new EntityTippedArrow(par2World, par3EntityPlayer);
            entityarrow.shoot(par3EntityPlayer, par3EntityPlayer.rotationPitch, par3EntityPlayer.rotationYaw, 0.0F, f * 2.5F, 1.0F);
            entityarrow.setDamage(entityarrow.getDamage() + 0.5);
            int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, par1ItemStack);
            if (k > 0) {
               entityarrow.setDamage(entityarrow.getDamage() + (double)k * 0.5 + 0.5);
            }
            int l = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, par1ItemStack);
            if (l > 0) {
               entityarrow.setKnockbackStrength(l);
            }
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, par1ItemStack) > 0) {
               entityarrow.setFire(100);
            }

            par1ItemStack.damageItem(1, par3EntityPlayer);
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.arrow.shoot")); if (_snd != null) par2World.playSound(null, par3EntityPlayer.posX, par3EntityPlayer.posY, par3EntityPlayer.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F); }
            if (flag) {
               entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
            } else {
               par3EntityPlayer.inventory.clearMatchingItems(Items.ARROW, -1, 1, null);
            }
            if (!par2World.isRemote) {
               par2World.spawnEntity(entityarrow);
            }
         }
      }
   }

   public ActionResult<ItemStack> onItemRightClick(World par2World, EntityPlayer par3EntityPlayer, EnumHand hand) {
      ItemStack par1ItemStack = par3EntityPlayer.getHeldItem(hand);
      boolean hasAmmo = par3EntityPlayer.capabilities.isCreativeMode || par3EntityPlayer.inventory.hasItemStack(new ItemStack(Items.ARROW));
      ArrowNockEvent event = new ArrowNockEvent(par3EntityPlayer, par1ItemStack, hand, par2World, hasAmmo);
      MinecraftForge.EVENT_BUS.post(event);
      if (event.isCanceled()) {
         return new ActionResult<>(EnumActionResult.FAIL, par1ItemStack);
      } else {
         if (par3EntityPlayer.capabilities.isCreativeMode || par3EntityPlayer.inventory.hasItemStack(new ItemStack(Items.ARROW)) || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, par1ItemStack) > 0) {
            par3EntityPlayer.setActiveHand(hand);
         }
         return new ActionResult<>(EnumActionResult.SUCCESS, par1ItemStack);
      }
   }

   public int getItemEnchantability() {
      return 3;
   }
}
