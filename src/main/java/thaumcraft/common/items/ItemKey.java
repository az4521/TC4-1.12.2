package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockArcaneDoor;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileOwned;

public class ItemKey extends Item {
   public TextureAtlasSprite iconIron;
   public TextureAtlasSprite iconGold;

   public ItemKey() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconIron = ir.registerSprite("thaumcraft:keyiron");
      this.iconGold = ir.registerSprite("thaumcraft:keygold");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return par1 == 0 ? this.iconIron : this.iconGold;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   public boolean hasEffect(ItemStack par1ItemStack) {
      return par1ItemStack.hasTagCompound();
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.UNCOMMON;
   }

   @Override
   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
      ItemStack itemstack = player.getHeldItem(hand);
      int x = blockPos.getX();
      int y = blockPos.getY();
      int z = blockPos.getZ();
      Block bi = world.getBlockState(blockPos).getBlock();
      int md = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
      if (bi == ConfigBlocks.blockArcaneDoor || bi == ConfigBlocks.blockWoodenDevice && (md == 2 || md == 3)) {
         int mod = 0;
         int mod2 = 1;
         byte type = 0;
         if (bi == ConfigBlocks.blockArcaneDoor) {
            int var10 = ((BlockArcaneDoor)bi).getFullMetadata(world, new BlockPos(x, y, z));
            if ((var10 & 8) != 0) {
               mod = -1;
               mod2 = 0;
            }
         } else {
            type = 1;
         }

         String loc = x + "," + (y + mod) + "," + z;
         TileEntity tile = world.getTileEntity(new BlockPos(x, y + mod, z));
         if (tile instanceof TileOwned) {
            if (!itemstack.hasTagCompound()) {
               if (player.getName().equals(((TileOwned)tile).owner) || ((TileOwned)tile).accessList.contains("1" + player.getName()) && itemstack.getItemDamage() == 0) {
                  ItemStack st = new ItemStack(ConfigItems.itemKey, 1, itemstack.getItemDamage());
                  st.setTagInfo("location", new NBTTagString(loc));
                  st.setTagInfo("type", new NBTTagByte(type));
                  if (!player.inventory.addItemStackToInventory(st) && !world.isRemote) {
                     world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, st));
                  }

                  if (!player.capabilities.isCreativeMode) {
                     itemstack.shrink(1);
                  }

                  if (!world.isRemote) {
                     switch (type) {
                        case 0:
                           player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.key1")));
                           break;
                        case 1:
                           player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.key2")));
                     }

                     { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:key")); if (_snd != null) world.playSound(null, x, y, z, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 0.9F); }
                  }

                  player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
               }
            } else if (!player.getName().equals(((TileOwned)tile).owner) && !((TileOwned)tile).accessList.contains(itemstack.getItemDamage() + player.getName()) && !((TileOwned)tile).accessList.contains("1" + player.getName()) && loc.equals(itemstack.getTagCompound().getString("location"))) {
               ((TileOwned)tile).accessList.add(itemstack.getItemDamage() + player.getName());
               if (type == 0) {
                  TileEntity tile2 = world.getTileEntity(new BlockPos(x, y + mod2, z));
                  if (tile2 instanceof TileOwned) {
                     ((TileOwned)tile2).accessList.add(itemstack.getItemDamage() + player.getName());
                  }

                  { BlockPos _kp = new BlockPos(x, y + mod2, z); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_kp); world.notifyBlockUpdate(_kp, _bs, _bs, 3); }
               }

               { BlockPos _kp = new BlockPos(x, y + mod, z); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_kp); world.notifyBlockUpdate(_kp, _bs, _bs, 3); }
               if (!world.isRemote) {
                  switch (type) {
                     case 0:
                        player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.key3") + (itemstack.getItemDamage() == 0 ? "" : I18n.translateToLocal("tc.key4"))));
                        break;
                     case 1:
                        player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.key5") + (itemstack.getItemDamage() == 0 ? "" : I18n.translateToLocal("tc.key6"))));
                  }

                  { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:key")); if (_snd != null) world.playSound(null, x, y, z, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.1F); }
               }

               if (!player.capabilities.isCreativeMode) {
                  itemstack.shrink(1);
               }

               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            } else if (!world.isRemote) {
               if (!loc.equals(itemstack.getTagCompound().getString("location"))) {
                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.key7")));
               } else {
                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.key8")));
               }
            }
         }

         return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.PASS;
      }
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      if (stack.hasTagCompound() && stack.getTagCompound().hasKey("location")) {
         String location = stack.getTagCompound().getString("location");

         try {
            String[] ss = location.split(",");
            location = "x " + ss[0] + ", z " + ss[2] + ", y " + ss[1];
         } catch (Exception ignored) {
         }

         byte type = stack.getTagCompound().getByte("type");
         list.add("§5§o" + I18n.translateToLocal("tc.key9"));
         list.add("§5§o" + (type == 0 ? I18n.translateToLocal("tc.key10") : I18n.translateToLocal("tc.key11")));
         list.add("§5§o" + location);
      }

   }
}
