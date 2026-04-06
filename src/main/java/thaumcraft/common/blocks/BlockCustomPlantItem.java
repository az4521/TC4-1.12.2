package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import thaumcraft.common.config.ConfigBlocks;

public class BlockCustomPlantItem extends ItemBlock {

   public BlockCustomPlantItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   @Override
   public String getTranslationKey(ItemStack stack) {
      return super.getTranslationKey(stack) + "." + stack.getItemDamage();
   }

   @Override
   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
         EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack stack = player.getHeldItem(hand);
      if (facing != EnumFacing.UP) {
         return EnumActionResult.FAIL;
      }
      if (!player.canPlayerEdit(pos, facing, stack) || !player.canPlayerEdit(pos.up(), facing, stack)) {
         return EnumActionResult.FAIL;
      }
      IPlantable plantable = new CustomPlantTypes(stack.getMetadata());
      if (world.getBlockState(pos).getBlock().canSustainPlant(world.getBlockState(pos), world, pos, EnumFacing.UP, plantable)
            && world.isAirBlock(pos.up())) {
         world.setBlockState(pos.up(), ConfigBlocks.blockCustomPlant.getStateFromMeta(stack.getMetadata()), 3);
         SoundType sound = ConfigBlocks.blockCustomPlant.getSoundType();
         world.playSound(null, pos.up(),
               sound.getPlaceSound(), SoundCategory.BLOCKS,
               (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
         stack.shrink(1);
         return EnumActionResult.SUCCESS;
      }
      return EnumActionResult.FAIL;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side,
         EntityPlayer player, ItemStack stack) {
      return world.getBlockState(pos).getBlock()
            .canSustainPlant(world.getBlockState(pos), world, pos, EnumFacing.UP,
                  new CustomPlantTypes(stack.getMetadata()))
            && world.isAirBlock(pos.up());
   }

   private static class CustomPlantTypes implements IPlantable {
      int md = 0;

      public CustomPlantTypes(int md) {
         this.md = md;
      }

      @Override
      public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
         if (this.md == 3) {
            return EnumPlantType.Desert;
         } else {
            return this.md != 4 && this.md != 5 ? EnumPlantType.Plains : EnumPlantType.Cave;
         }
      }

      @Override
      public net.minecraft.block.state.IBlockState getPlant(IBlockAccess world, BlockPos pos) {
         return ConfigBlocks.blockCustomPlant.getStateFromMeta(this.md);
      }
   }
}
