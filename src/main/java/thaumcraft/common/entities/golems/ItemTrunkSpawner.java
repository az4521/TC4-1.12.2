package thaumcraft.common.entities.golems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumActionResult;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import net.minecraft.util.math.BlockPos;

public class ItemTrunkSpawner extends Item {
   private TextureAtlasSprite icon;

   public ItemTrunkSpawner() {
      this.setMaxStackSize(1);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister par1IconRegister) {
      this.icon = par1IconRegister.registerSprite("thaumcraft:blank");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
   }

   public void addInformation(ItemStack stack, World worldIn, List<String> list, net.minecraft.client.util.ITooltipFlag flag) {
      if (stack.hasTagCompound()) {
         if (stack.getTagCompound().hasKey("upgrade")) {
            byte ba = stack.getTagCompound().getByte("upgrade");
            String text = "§9";
            if (ba > -1) {
               text = text + I18n.translateToLocal("item.ItemGolemUpgrade." + ba + ".name") + " ";
            }

            list.add(text);
         }

         if (stack.getTagCompound().hasKey("inventory")) {
            list.add(I18n.translateToLocal("item.TrunkSpawner.text.1"));
         }
      }

      super.addInformation(stack, worldIn, list, flag);
   }

   public EnumActionResult onItemUse(EntityPlayer par2EntityPlayer, World par3World, BlockPos pos, EnumHand hand, EnumFacing facing, float par8, float par9, float par10) {
       ItemStack stack = par2EntityPlayer.getHeldItem(hand);
       if (!par3World.isRemote) {
           int par7 = facing.getIndex();
           int par4 = pos.getX();
           int par5 = pos.getY();
           int par6 = pos.getZ();
           Block i1 = par3World.getBlockState(pos).getBlock();
           par4 += facing.getXOffset();
           par5 += facing.getYOffset();
           par6 += facing.getZOffset();
           double d0 = 0.0F;
           if (par7 == 1 && !i1.isAir(par3World.getBlockState(new BlockPos(par4, par5, par6)), par3World, new BlockPos(par4, par5, par6))) {
               d0 = 0.5F;
           }

           EntityTravelingTrunk entity = new EntityTravelingTrunk(par3World);
           if (entity instanceof EntityLivingBase) {
               EntityLiving entityliving = entity;
               entity.setLocationAndAngles(par4, (double) par5 + d0, par6, MathHelper.wrapDegrees(par3World.rand.nextFloat() * 360.0F), 0.0F);
               entityliving.rotationYawHead = entityliving.rotationYaw;
               entityliving.renderYawOffset = entityliving.rotationYaw;
               entity.setOwner(par2EntityPlayer.getName());
               entity.setOwnerUUID(par2EntityPlayer.getUniqueID());
               if (stack.hasDisplayName()) {
                   entity.setCustomNameTag(stack.getDisplayName());
               }

               if (stack.hasTagCompound() && stack.getTagCompound().hasKey("upgrade")) {
                   entity.setUpgrade(stack.getTagCompound().getByte("upgrade"));
                   entity.setInvSize();
               }

               if (stack.hasTagCompound() && stack.getTagCompound().hasKey("inventory")) {
                   NBTTagList nbttaglist = stack.getTagCompound().getTagList("inventory", 10);
                   entity.inventory.readFromNBT(nbttaglist);
               }

               entityliving.onInitialSpawn(par3World.getDifficultyForLocation(new BlockPos(entity)), null);
               par3World.spawnEntity(entity);
               entityliving.playLivingSound();
               if (!par2EntityPlayer.capabilities.isCreativeMode) {
                   stack.shrink(1);
               }
           }

       }
       return EnumActionResult.SUCCESS;
   }
}
