package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileMirror;
import thaumcraft.common.tiles.TileMirrorEssentia;

import java.util.List;

public class BlockMirrorItem extends ItemBlock {

   public BlockMirrorItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   @Override
   public boolean getShareTag() {
      return super.getShareTag();
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      int d = par1ItemStack.getItemDamage() < 6 ? 0 : 6;
      return super.getTranslationKey() + "." + d;
   }

   @Override
   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      if (world.getBlockState(pos).getBlock() == ConfigBlocks.blockMirror) {
         if (world.isRemote) {
            player.swingArm(hand);
            return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
         }

         if (stack.getItemDamage() <= 5) {
            TileEntity tm = world.getTileEntity(pos);
            if (tm instanceof TileMirror && !((TileMirror) tm).isLinkValid()) {
               ItemStack st = stack.copy();
               st.setCount(1);
               st.setItemDamage(1);
               st.setTagInfo("linkX", new NBTTagInt(tm.getPos().getX()));
               st.setTagInfo("linkY", new NBTTagInt(tm.getPos().getY()));
               st.setTagInfo("linkZ", new NBTTagInt(tm.getPos().getZ()));
               st.setTagInfo("linkDim", new NBTTagInt(world.provider.getDimension()));
               st.setTagInfo("dimname", new NBTTagString(world.provider.getDimensionType().getName()));
               world.playSound(null, pos,
                     new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "jar")),
                     net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 2.0F);
               if (!player.inventory.addItemStackToInventory(st) && !world.isRemote) {
                  world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, st));
               }

               if (!player.capabilities.isCreativeMode) {
                  stack.shrink(1);
               }

               player.inventoryContainer.detectAndSendChanges();
            } else if (tm instanceof TileMirror) {
               player.sendMessage(new TextComponentTranslation("§5§oThat mirror is already linked to a valid destination."));
            }
         } else {
            TileEntity tm = world.getTileEntity(pos);
            if (tm instanceof TileMirrorEssentia && !((TileMirrorEssentia) tm).isLinkValid()) {
               ItemStack st = stack.copy();
               st.setCount(1);
               st.setItemDamage(7);
               st.setTagInfo("linkX", new NBTTagInt(tm.getPos().getX()));
               st.setTagInfo("linkY", new NBTTagInt(tm.getPos().getY()));
               st.setTagInfo("linkZ", new NBTTagInt(tm.getPos().getZ()));
               st.setTagInfo("linkDim", new NBTTagInt(world.provider.getDimension()));
               st.setTagInfo("dimname", new NBTTagString(world.provider.getDimensionType().getName()));
               world.playSound(null, pos,
                     new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "jar")),
                     net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 2.0F);
               if (!player.inventory.addItemStackToInventory(st) && !world.isRemote) {
                  world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, st));
               }

               if (!player.capabilities.isCreativeMode) {
                  stack.shrink(1);
               }

               player.inventoryContainer.detectAndSendChanges();
            } else if (tm instanceof TileMirrorEssentia) {
               player.sendMessage(new TextComponentTranslation("§5§oThat mirror is already linked to a valid destination."));
            }
         }
      }

      return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
   }

   @Override
   public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
      int metadata = stack.getItemDamage();
      boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
      if (ret && !world.isRemote) {
         if (metadata <= 5) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileMirror && stack.hasTagCompound()) {
               ((TileMirror) te).linkX = stack.getTagCompound().getInteger("linkX");
               ((TileMirror) te).linkY = stack.getTagCompound().getInteger("linkY");
               ((TileMirror) te).linkZ = stack.getTagCompound().getInteger("linkZ");
               ((TileMirror) te).linkDim = stack.getTagCompound().getInteger("linkDim");
               ((TileMirror) te).restoreLink();
            }
         } else {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileMirrorEssentia && stack.hasTagCompound()) {
               ((TileMirrorEssentia) te).linkX = stack.getTagCompound().getInteger("linkX");
               ((TileMirrorEssentia) te).linkY = stack.getTagCompound().getInteger("linkY");
               ((TileMirrorEssentia) te).linkZ = stack.getTagCompound().getInteger("linkZ");
               ((TileMirrorEssentia) te).linkDim = stack.getTagCompound().getInteger("linkDim");
               ((TileMirrorEssentia) te).restoreLink();
            }
         }
      }
      return ret;
   }

   @Override
   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.UNCOMMON;
   }

   @Override
   public int getMetadata(int par1) {
      return par1;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack item, World world, List<String> list, ITooltipFlag flag) {
      if (item.hasTagCompound()) {
         int lx = item.getTagCompound().getInteger("linkX");
         int ly = item.getTagCompound().getInteger("linkY");
         int lz = item.getTagCompound().getInteger("linkZ");
         String dimname = item.getTagCompound().getString("dimname");
         list.add("Linked to " + lx + "," + ly + "," + lz + " in " + dimname);
      }
   }
}
