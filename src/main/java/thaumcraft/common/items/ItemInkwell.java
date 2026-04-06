package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.IScribeTools;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileResearchTable;
import thaumcraft.common.tiles.TileTable;

public class ItemInkwell extends Item implements IScribeTools {
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite icon;

   public ItemInkwell() {
      this.maxStackSize = 1;
      this.canRepair = true;
      this.setMaxDamage(100);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setHasSubtypes(false);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:inkwell");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public net.minecraft.util.EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      int x = blockPos.getX(), y = blockPos.getY(), z = blockPos.getZ();
      TileEntity tile = world.getTileEntity(blockPos);
      int md = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
      Block bi = world.getBlockState(blockPos).getBlock();
      if (tile instanceof TileTable && md != 6) {
         if (world.isRemote) {
            return net.minecraft.util.EnumActionResult.PASS;
         }

         for(int a = 2; a < 6; ++a) {
            EnumFacing af = EnumFacing.byIndex(a);
            BlockPos neighbor = new BlockPos(x + af.getXOffset(), y + af.getYOffset(), z + af.getZOffset());
            TileEntity tile2 = world.getTileEntity(neighbor);
            int md2 = world.getBlockState(neighbor).getBlock().getMetaFromState(world.getBlockState(neighbor));
            if (tile2 instanceof TileTable && md2 < 6) {
               world.setBlockState(blockPos, bi.getStateFromMeta(a), 0);
               world.setTileEntity(blockPos, new TileResearchTable());
               world.setBlockState(neighbor, bi.getStateFromMeta(af.getOpposite().ordinal() + 4), 0);
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(blockPos); world.notifyBlockUpdate(blockPos, _bs, _bs, 3); }
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(neighbor); world.notifyBlockUpdate(neighbor, _bs, _bs, 3); }
               TileEntity tile3 = world.getTileEntity(blockPos);
               if (tile3 instanceof TileResearchTable) {
                  ((TileResearchTable)tile3).setInventorySlotContents(0, stack.copy());
                  if (!player.capabilities.isCreativeMode) {
                     player.inventory.decrStackSize(player.inventory.currentItem, 1);
                     player.inventory.markDirty();
                  }

                  { net.minecraft.block.state.IBlockState _bs = world.getBlockState(blockPos); world.notifyBlockUpdate(blockPos, _bs, _bs, 3); }
               }

               return net.minecraft.util.EnumActionResult.SUCCESS;
            }
         }
      }

      return net.minecraft.util.EnumActionResult.PASS;
   }
}
