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
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileJarNode;
import net.minecraft.client.util.ITooltipFlag;

import java.util.List;

public class ItemJarNode extends Item implements IEssentiaContainerItem {

   public ItemJarNode() {
      this.setMaxDamage(0);
      this.setMaxStackSize(1);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
      String desc = "\u00a79" + I18n.translateToLocal("nodetype." + this.getNodeType(stack) + ".name");
      if (this.getNodeModifier(stack) != null) {
         desc = desc + ", " + I18n.translateToLocal("nodemod." + this.getNodeModifier(stack) + ".name");
      }
      list.add(desc);
      AspectList.addAspectDescriptionToList(this.getAspects(stack), net.minecraft.client.Minecraft.getMinecraft().player, list);
      super.addInformation(stack, world, list, flag);
   }

   @Override
   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
         EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack stack = player.getHeldItem(hand);

      IBlockState hitState = world.getBlockState(pos);
      Block hitBlock = hitState.getBlock();

      BlockPos placePos;
      if (hitBlock == Blocks.SNOW_LAYER) {
         placePos = pos;
      } else if (hitBlock == Blocks.VINE || hitBlock == Blocks.TALLGRASS || hitBlock == Blocks.DEADBUSH
            || hitBlock.isAir(hitState, world, pos) || hitBlock.isReplaceable(world, pos)) {
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

      int metadata = 2; // node jar metadata
      IBlockState newState = ConfigBlocks.blockJar.getStateFromMeta(metadata);
      if (world.setBlockState(placePos, newState, 3)) {
         if (world.getBlockState(placePos).getBlock() == ConfigBlocks.blockJar) {
            ConfigBlocks.blockJar.onBlockPlacedBy(world, placePos, newState, player, stack);
         }

         TileEntity te = world.getTileEntity(placePos);
         if (te instanceof TileJarNode && stack.hasTagCompound()) {
            AspectList aspects = this.getAspects(stack);
            if (aspects != null) {
               ((TileJarNode) te).setAspects(aspects);
               ((TileJarNode) te).setNodeType(this.getNodeType(stack));
               ((TileJarNode) te).setNodeModifier(this.getNodeModifier(stack));
               ((TileJarNode) te).setId(this.getNodeId(stack));
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

   public void setAspects(ItemStack itemstack, AspectList aspects) {
      if (!itemstack.hasTagCompound()) {
         itemstack.setTagCompound(new NBTTagCompound());
      }
      aspects.writeToNBT(itemstack.getTagCompound());
   }

   public void setNodeAttributes(ItemStack itemstack, NodeType type, NodeModifier mod, String id) {
      if (!itemstack.hasTagCompound()) {
         itemstack.setTagCompound(new NBTTagCompound());
      }
      itemstack.setTagInfo("nodetype", new NBTTagInt(type.ordinal()));
      if (mod != null) {
         itemstack.setTagInfo("nodemod", new NBTTagInt(mod.ordinal()));
      }
      itemstack.setTagInfo("nodeid", new NBTTagString(id));
   }

   public NodeType getNodeType(ItemStack itemstack) {
      return !itemstack.hasTagCompound() ? null : NodeType.values()[itemstack.getTagCompound().getInteger("nodetype")];
   }

   public NodeModifier getNodeModifier(ItemStack itemstack) {
      return itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("nodemod")
            ? NodeModifier.values()[itemstack.getTagCompound().getInteger("nodemod")]
            : null;
   }

   public String getNodeId(ItemStack itemstack) {
      return !itemstack.hasTagCompound() ? "0" : itemstack.getTagCompound().getString("nodeid");
   }
}
