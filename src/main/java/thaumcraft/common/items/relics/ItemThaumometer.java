package thaumcraft.common.items.relics;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.research.IScanEventHandler;
import thaumcraft.api.research.ScanResult;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketScannedToServer;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.EntityUtils;

public class ItemThaumometer extends Item {
   public TextureAtlasSprite icon;
   ScanResult startScan = null;

   public ItemThaumometer() {
      this.setMaxStackSize(1);
      this.setNoRepair();
      this.setCreativeTab(Thaumcraft.tabTC);
      // TEISR is registered in ClientProxy.setupItemRenderers()
   }

   @Override
   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.UNCOMMON;
   }   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:blank");
   }   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   @Override
   public int getMaxItemUseDuration(ItemStack itemstack) {
      return 25;
   }

   @Override
   public EnumAction getItemUseAction(ItemStack itemstack) {
      return EnumAction.NONE;
   }

   private ScanResult doScan(ItemStack stack, World world, EntityPlayer p, int count) {
      Entity pointedEntity = EntityUtils.getPointedEntity(p.world, p, 0.5F, 10.0F, 0.0F, true);
      if (pointedEntity != null) {
         ScanResult sr = new ScanResult((byte)2, 0, 0, pointedEntity, "");
         if (ScanManager.isValidScanTarget(p, sr, "@")) {
            Thaumcraft.proxy.blockRunes(world, pointedEntity.posX - (double)0.5F, pointedEntity.posY + (double)(pointedEntity.getEyeHeight() / 2.0F), pointedEntity.posZ - (double)0.5F, 0.3F + world.rand.nextFloat() * 0.7F, 0.0F, 0.3F + world.rand.nextFloat() * 0.7F, (int)(pointedEntity.height * 15.0F), 0.03F);
            return sr;
         } else {
            return null;
         }
      } else {
         RayTraceResult mop = this.rayTrace(p.world, p, true);
         if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos mopPos = mop.getBlockPos();
            TileEntity tile = world.getTileEntity(mopPos);
            if (tile instanceof INode) {
               ScanResult sr = new ScanResult((byte)3, 0, 0, null, "NODE" + ((INode)tile).getId());
               if (ScanManager.isValidScanTarget(p, sr, "@")) {
                  Thaumcraft.proxy.blockRunes(world, mopPos.getX(), (double)mopPos.getY() + (double)0.25F, mopPos.getZ(), 0.3F + world.rand.nextFloat() * 0.7F, 0.0F, 0.3F + world.rand.nextFloat() * 0.7F, 15, 0.03F);
                  return sr;
               }

               return null;
            }

            Block bi = world.getBlockState(mopPos).getBlock();
            if (bi != Blocks.AIR) {
               int md = world.getBlockState(mopPos).getBlock().getMetaFromState(world.getBlockState(mopPos));
               ItemStack is = bi.getPickBlock(world.getBlockState(mopPos), mop, world, mopPos, null);
               ScanResult sr = null;

               try {
                  if (is == null) {
                     is = BlockUtils.createStackedBlock(bi, md);
                  }
               } catch (Exception ignored) {
               }

               try {
                  if (is == null) {
                     sr = new ScanResult((byte)1, Block.getIdFromBlock(bi), md, null, "");
                  } else {
                     sr = new ScanResult((byte)1, Item.getIdFromItem(is.getItem()), is.getItemDamage(), null, "");
                  }
               } catch (Exception ignored) {
               }

               if (ScanManager.isValidScanTarget(p, sr, "@")) {
                  Thaumcraft.proxy.blockRunes(world, mopPos.getX(), (double)mopPos.getY() + (double)0.25F, mopPos.getZ(), 0.3F + world.rand.nextFloat() * 0.7F, 0.0F, 0.3F + world.rand.nextFloat() * 0.7F, 15, 0.03F);
                  return sr;
               }

               return null;
            }
         }

         for(IScanEventHandler seh : ThaumcraftApi.scanEventhandlers) {
            ScanResult scan = seh.scanPhenomena(stack, world, p);
            if (scan != null) {
               return scan;
            }
         }

         return null;
      }
   }

   @Override
   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer p, EnumHand hand) {
      ItemStack stack = p.getHeldItem(hand);
      if (world.isRemote) {
         ScanResult scan = this.doScan(stack, world, p, 0);
         if (scan != null) {
            this.startScan = scan;
         }
      }

      p.setActiveHand(hand);
      return new ActionResult<>(EnumActionResult.SUCCESS, stack);
   }

   @Override
   public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
      EntityPlayer p = (EntityPlayer) living;
      if (p.world.isRemote && p.getName() == Minecraft.getMinecraft().player.getName()) {
         ScanResult scan = this.doScan(stack, p.world, p, count);
         if (scan != null && scan.equals(this.startScan)) {
            if (count <= 5) {
               this.startScan = null;
               p.stopActiveHand();
               if (ScanManager.completeScan(p, scan, "@")) {
                  PacketHandler.INSTANCE.sendToServer(new PacketScannedToServer(scan, p, "@"));
               }
            }

            if (count % 2 == 0) {
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:cameraticks")); if (_snd != null) p.world.playSound(null, p.posX, p.posY, p.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.2F, 0.45F + p.world.rand.nextFloat() * 0.1F); };
            }
         } else {
            this.startScan = null;
         }
      }

   }

   @Override
   public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityLivingBase par3EntityLiving, int par4) {
      super.onPlayerStoppedUsing(par1ItemStack, par2World, par3EntityLiving, par4);
      this.startScan = null;
   }
}
