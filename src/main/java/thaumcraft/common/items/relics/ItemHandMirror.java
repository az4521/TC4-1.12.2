package thaumcraft.common.items.relics;

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
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileMirror;
import net.minecraft.util.math.BlockPos;

public class ItemHandMirror extends Item {
   private TextureAtlasSprite icon;

   public ItemHandMirror() {
      this.setMaxStackSize(1);
      this.setHasSubtypes(false);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister par1IconRegister) {
      this.icon = par1IconRegister.registerSprite("thaumcraft:mirrorhand");
   }

   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this));
   }

   public boolean getShareTag() {
       return super.getShareTag();
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.UNCOMMON;
   }

   public boolean hasEffect(ItemStack par1ItemStack) {
      return par1ItemStack.hasTagCompound();
   }

   @Override
   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
      int x = blockPos.getX(), y = blockPos.getY(), z = blockPos.getZ();
      Block bi = world.getBlockState(blockPos).getBlock();
      if (bi == ConfigBlocks.blockMirror) {
         if (world.isRemote) {
            player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            return EnumActionResult.PASS;
         } else {
            ItemStack itemstack = player.getHeldItem(hand);
            TileEntity tm = world.getTileEntity(blockPos);
            if (tm instanceof TileMirror) {
               itemstack.setTagInfo("linkX", new NBTTagInt(tm.getPos().getX()));
               itemstack.setTagInfo("linkY", new NBTTagInt(tm.getPos().getY()));
               itemstack.setTagInfo("linkZ", new NBTTagInt(tm.getPos().getZ()));
               itemstack.setTagInfo("linkDim", new NBTTagInt(world.provider.getDimension()));
               itemstack.setTagInfo("dimname", new NBTTagString(world.provider.getDimensionType().getName()));
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:jar")); if (_snd != null) world.playSound(null, x, y, z, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 2.0F); }
               player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.handmirrorlinked")));
               player.inventoryContainer.detectAndSendChanges();
            }

            return EnumActionResult.SUCCESS;
         }
      } else {
         return EnumActionResult.PASS;
      }
   }

   @Override
   public ActionResult<ItemStack> onItemRightClick(World par2World, EntityPlayer par3EntityPlayer, EnumHand hand) {
      ItemStack par1ItemStack = par3EntityPlayer.getHeldItem(hand);
      if (!par2World.isRemote && par1ItemStack.hasTagCompound()) {
         int lx = par1ItemStack.getTagCompound().getInteger("linkX");
         int ly = par1ItemStack.getTagCompound().getInteger("linkY");
         int lz = par1ItemStack.getTagCompound().getInteger("linkZ");
         int ldim = par1ItemStack.getTagCompound().getInteger("linkDim");
         WorldServer targetWorld = DimensionManager.getWorld(ldim);
         if (targetWorld == null) {
            return new ActionResult<>(EnumActionResult.PASS, par1ItemStack);
         }

         TileEntity te = targetWorld.getTileEntity(new BlockPos(lx, ly, lz));
         if (!(te instanceof TileMirror)) {
            par1ItemStack.setTagCompound(null);
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:zap")); if (_snd != null) par2World.playSound(null, par3EntityPlayer.posX, par3EntityPlayer.posY, par3EntityPlayer.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 0.8F); };
            par3EntityPlayer.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.handmirrorerror")));
            return new ActionResult<>(EnumActionResult.PASS, par1ItemStack);
         }

         par3EntityPlayer.openGui(Thaumcraft.instance, 16, par2World, MathHelper.floor(par3EntityPlayer.posX), MathHelper.floor(par3EntityPlayer.posY), MathHelper.floor(par3EntityPlayer.posZ));
      }

      return new ActionResult<>(EnumActionResult.PASS, par1ItemStack);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack item, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      if (item.hasTagCompound()) {
         int lx = item.getTagCompound().getInteger("linkX");
         int ly = item.getTagCompound().getInteger("linkY");
         int lz = item.getTagCompound().getInteger("linkZ");
         int ldim = item.getTagCompound().getInteger("linkDim");
         String dimname = item.getTagCompound().getString("dimname");
         list.add(I18n.translateToLocal("tc.handmirrorlinkedto") + " " + lx + "," + ly + "," + lz + " in " + dimname);
      }

   }

   public static boolean transport(ItemStack mirror, ItemStack items, EntityPlayer player, World world) {
      if (mirror.hasTagCompound()) {
         int lx = mirror.getTagCompound().getInteger("linkX");
         int ly = mirror.getTagCompound().getInteger("linkY");
         int lz = mirror.getTagCompound().getInteger("linkZ");
         int ldim = mirror.getTagCompound().getInteger("linkDim");
         WorldServer targetWorld = DimensionManager.getWorld(ldim);
         if (targetWorld == null) {
            return false;
         } else {
            TileEntity te = targetWorld.getTileEntity(new BlockPos(lx, ly, lz));
            if (te instanceof TileMirror) {
               TileMirror tm = (TileMirror)te;
               EnumFacing linkedFacing = EnumFacing.byIndex(ConfigBlocks.blockMirror.getMetaFromState(targetWorld.getBlockState(new BlockPos(lx, ly, lz))));
               EntityItem ie2 = new EntityItem(targetWorld, (double)lx + (double)0.5F - (double)linkedFacing.getXOffset() * 0.3, (double)ly + (double)0.5F - (double)linkedFacing.getYOffset() * 0.3, (double)lz + (double)0.5F - (double)linkedFacing.getZOffset() * 0.3, items.copy());
               ie2.motionX = (float)linkedFacing.getXOffset() * 0.15F;
               ie2.motionY = (float)linkedFacing.getYOffset() * 0.15F;
               ie2.motionZ = (float)linkedFacing.getZOffset() * 0.15F;
               ie2.timeUntilPortal = 20;
               targetWorld.spawnEntity(ie2);
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:mob.endermen.portal")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.1F, 1.0F); };
               targetWorld.addBlockEvent(new BlockPos(lx, ly, lz), ConfigBlocks.blockMirror, 1, 0);
               return true;
            } else {
               mirror.setTagCompound(null);
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:zap")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 0.8F); };
               player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.handmirrorerror")));
               return false;
            }
         }
      } else {
         return false;
      }
   }
}
