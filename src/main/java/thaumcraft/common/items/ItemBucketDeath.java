package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;

public class ItemBucketDeath extends Item {
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite icon;

   public ItemBucketDeath() {
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setHasSubtypes(false);
      this.setMaxStackSize(1);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:bucket_death");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
      ItemStack itemStackIn = player.getHeldItem(hand);
      RayTraceResult movingobjectposition = this.rayTrace(worldIn, player, true);
      if (movingobjectposition != null) {
         if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos hitPos = movingobjectposition.getBlockPos();
            EnumFacing facing = movingobjectposition.sideHit;
            BlockPos placePos = hitPos.offset(facing);
            if (!player.canPlayerEdit(placePos, facing, itemStackIn)) {
               return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.PASS, itemStackIn);
            }

            if (this.tryPlaceContainedLiquid(worldIn, placePos.getX(), placePos.getY(), placePos.getZ()) && !player.capabilities.isCreativeMode) {
               return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET));
            }
         }
      }
      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.PASS, itemStackIn);
   }

   public boolean tryPlaceContainedLiquid(World world, int x, int y, int z) {
      Material material = world.getBlockState(new BlockPos(x, y, z)).getMaterial();
      boolean flag = !material.isSolid();
      if (!world.isAirBlock(new BlockPos(x, y, z)) && !flag) {
         return false;
      } else {
         if (!world.isRemote && flag && !material.isLiquid()) {
            world.destroyBlock(new BlockPos(x, y, z), true);
         }
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y, z), (ConfigBlocks.blockFluidDeath).getStateFromMeta(3), 3);
         return true;
      }
   }
}
