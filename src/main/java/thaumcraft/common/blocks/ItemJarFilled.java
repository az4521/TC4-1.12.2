package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileJarFillable;
import net.minecraft.client.util.ITooltipFlag;

import java.util.List;

import static thaumcraft.api.aspects.AspectList.addAspectDescriptionToList;

public class ItemJarFilled extends Item implements IEssentiaContainerItem {

   public ItemJarFilled() {
      this.setMaxDamage(0);
      this.setMaxStackSize(1);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
      AspectList aspects = this.getAspects(stack);
      // addAspectDescriptionToList needs a player; pass null-safe via proxy
      addAspectDescriptionToList(aspects, net.minecraft.client.Minecraft.getMinecraft().player, list);

      if (stack.hasTagCompound() && stack.getTagCompound().hasKey("AspectFilter")) {
         String tf = stack.getTagCompound().getString("AspectFilter");
         Aspect tag = Aspect.getAspect(tf);
         if (tag != null) {
            list.add("\u00a75" + tag.getName());
         } else {
            list.add("\u00a75" + I18n.translateToLocal("tc.aspect.unknown"));
         }
      }

      super.addInformation(stack, world, list, flag);
   }

   @Override
   public String getTranslationKey(ItemStack stack) {
      return stack.getItemDamage() == 3
            ? super.getTranslationKey() + ".void"
            : super.getTranslationKey();
   }

   @Override
   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
         EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack stack = player.getHeldItem(hand);

      // Determine target placement position, same logic as vanilla ItemBlock
      IBlockState hitState = world.getBlockState(pos);
      Block hitBlock = hitState.getBlock();

      BlockPos placePos;
      if (hitBlock == Blocks.SNOW_LAYER && (hitState.getBlock().getMetaFromState(hitState) & 7) < 1) {
         placePos = pos;
      } else if (hitBlock == Blocks.VINE || hitBlock == Blocks.TALLGRASS || hitBlock == Blocks.DEADBUSH
            || hitBlock.isReplaceable(world, pos)) {
         placePos = pos;
      } else {
         placePos = pos.offset(facing);
      }

      if (stack.isEmpty()) {
         return EnumActionResult.FAIL;
      }
      if (!player.canPlayerEdit(placePos, facing, stack)) {
         return EnumActionResult.FAIL;
      }
      if (placePos.getY() == 255 && ConfigBlocks.blockJar.getMaterial(ConfigBlocks.blockJar.getDefaultState()).isSolid()) {
         return EnumActionResult.FAIL;
      }
      if (!ConfigBlocks.blockJar.canPlaceBlockAt(world, placePos)) {
         return EnumActionResult.FAIL;
      }

      int metadata = this.getMetadata(stack.getItemDamage());
      IBlockState newState = ConfigBlocks.blockJar.getStateFromMeta(metadata);
      if (world.setBlockState(placePos, newState, 3)) {
         if (world.getBlockState(placePos).getBlock() == ConfigBlocks.blockJar) {
            ConfigBlocks.blockJar.onBlockPlacedBy(world, placePos, newState, player, stack);
         }

         TileEntity te = world.getTileEntity(placePos);
         if (te instanceof TileJarFillable && stack.hasTagCompound()) {
            AspectList aspects = this.getAspects(stack);
            if (aspects != null && aspects.size() == 1) {
               ((TileJarFillable) te).amount = aspects.getAmount(aspects.getAspects()[0]);
               ((TileJarFillable) te).aspect = aspects.getAspects()[0];
            }
            String tf = stack.getTagCompound().getString("AspectFilter");
            if (tf != null && !tf.isEmpty()) {
               ((TileJarFillable) te).aspectFilter = Aspect.getAspect(tf);
            }
         }

         SoundType sound = ConfigBlocks.blockJar.getSoundType(newState, world, placePos, player);
         world.playSound(null, placePos, sound.getPlaceSound(), SoundCategory.BLOCKS,
               (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
         stack.shrink(1);
      }

      return EnumActionResult.SUCCESS;
   }

   public AspectList getAspects(ItemStack itemstack) {
      if (itemstack.hasTagCompound()) {
         AspectList aspects = new AspectList();
         aspects.readFromNBT(itemstack.getTagCompound());
         return aspects.size() > 0 ? aspects : null;
      } else {
         return null;
      }
   }

   public Aspect getFilter(ItemStack itemstack) {
      return itemstack.hasTagCompound() ? Aspect.getAspect(itemstack.getTagCompound().getString("AspectFilter")) : null;
   }

   public void setAspects(ItemStack itemstack, AspectList aspects) {
      if (!itemstack.hasTagCompound()) {
         itemstack.setTagCompound(new NBTTagCompound());
      }
      aspects.writeToNBT(itemstack.getTagCompound());
   }
}
