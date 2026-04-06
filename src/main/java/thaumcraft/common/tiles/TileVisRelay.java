package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import tc4tweak.CommonUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.TileVisNode;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import net.minecraft.util.math.BlockPos;

public class TileVisRelay extends TileVisNode implements IWandable {
   public short orientation = 1;
   public byte color = -1;
   public static HashMap nearbyPlayers = new HashMap<>();
   protected Object beam1 = null;
   protected int pulse;
   public float pRed = 0.5F;
   public float pGreen = 0.5F;
   public float pBlue = 0.5F;
   public static final int[] colors = new int[]{16777086, 16727041, 37119, 40960, 15650047, 5592439};
   protected int px;
   protected int py;
   protected int pz;
   protected boolean needToLoadParent = false;

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1);
   }

   public byte getAttunement() {
      return this.color;
   }

   public int getRange() {
      return 8;
   }

   public boolean isSource() {
      return false;
   }

   public void parentChanged() {
      if (this.world != null && this.world.isRemote) {
         this.world.checkLightFor(EnumSkyBlock.BLOCK, this.getPos());
      }

   }

   public void invalidate() {
      this.beam1 = null;
      super.invalidate();
   }

   public void updateEntity() {
      this.drawEffect();
            if (!this.world.isRemote && this.nodeCounter % 20 == 0) {
         List<EntityPlayer> var5 = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(5.0F, 5.0F, 5.0F));
         if (var5 != null && !var5.isEmpty()) {
            for(EntityPlayer player : var5) {
               if (!nearbyPlayers.containsKey(player.getEntityId()) || ((WeakReference)nearbyPlayers.get(player.getEntityId())).get() == null || !(distSq((TileVisRelay)((WeakReference)nearbyPlayers.get(player.getEntityId())).get(), player.posX, player.posY, player.posZ) < distSq(this, player.posX, player.posY, player.posZ))) {
                  nearbyPlayers.put(player.getEntityId(), new WeakReference(this));
               }
            }
         }
      }

   }

   private static double distSq(TileVisRelay te, double x, double y, double z) {
      double dx = (double)te.getPos().getX() + 0.5 - x;
      double dy = (double)te.getPos().getY() + 0.5 - y;
      double dz = (double)te.getPos().getZ() + 0.5 - z;
      return dx * dx + dy * dy + dz * dz;
   }

   protected void drawEffect() {
      if (this.world.isRemote) {
         if (this.needToLoadParent) {
            if (this.px == 0 && this.py == 0 && this.pz == 0) {
               this.setParent(null);
            } else {
               if (
                    !CommonUtils.isChunkLoaded(
                    this.getWorld(),
                    this.getPos().getX() - this.px,
                    this.getPos().getY() - this.py,
                    this.getPos().getZ() - this.pz)
               ){
                  return;
               }
               TileEntity tile = this.getWorld().getTileEntity(new BlockPos(this.getPos().getX() - this.px, this.getPos().getY() - this.py, this.getPos().getZ() - this.pz));
               if (tile instanceof TileVisNode) {
                  this.setParent(new WeakReference(tile));
               }
            }

            this.needToLoadParent = false;
            this.parentChanged();
         }

         if (VisNetHandler.isNodeValid(this.getParent())) {
            double xx = (double) this.getParent().get().getPos().getX() + 0.5;
            double yy = (double) this.getParent().get().getPos().getY() + 0.5;
            double zz = (double) this.getParent().get().getPos().getZ() + 0.5;
            EnumFacing d1 = EnumFacing.UP;
            if (this.getParent().get() instanceof TileVisRelay) {
               d1 = EnumFacing.byIndex(((TileVisRelay)this.getParent().get()).orientation);
            }

            EnumFacing d2 = EnumFacing.byIndex(this.orientation);
            this.beam1 = Thaumcraft.proxy.beamPower(this.world, xx - (double)d1.getXOffset() * 0.05, yy - (double)d1.getYOffset() * 0.05, zz - (double)d1.getZOffset() * 0.05, (double)this.getPos().getX() + (double)0.5F - (double)d2.getXOffset() * 0.05, (double)this.getPos().getY() + (double)0.5F - (double)d2.getYOffset() * 0.05, (double)this.getPos().getZ() + (double)0.5F - (double)d2.getZOffset() * 0.05, this.pRed, this.pGreen, this.pBlue, this.pulse > 0, this.beam1);
         }

         if (this.pRed < 1.0F) {
            this.pRed += 0.025F;
         }

         if (this.pRed > 1.0F) {
            this.pRed = 1.0F;
         }

         if (this.pGreen < 1.0F) {
            this.pGreen += 0.025F;
         }

         if (this.pGreen > 1.0F) {
            this.pGreen = 1.0F;
         }

         if (this.pBlue < 1.0F) {
            this.pBlue += 0.025F;
         }

         if (this.pBlue > 1.0F) {
            this.pBlue = 1.0F;
         }
      }

      if (this.pulse > 0) {
         --this.pulse;
      }

   }

   public void triggerConsumeEffect(Aspect aspect) {
      this.addPulse(aspect);
   }

   protected void addPulse(Aspect aspect) {
      int c = -1;
      if (aspect == Aspect.AIR) {
         c = 0;
      } else if (aspect == Aspect.FIRE) {
         c = 1;
      } else if (aspect == Aspect.WATER) {
         c = 2;
      } else if (aspect == Aspect.EARTH) {
         c = 3;
      } else if (aspect == Aspect.ORDER) {
         c = 4;
      } else if (aspect == Aspect.ENTROPY) {
         c = 5;
      }

      if (c >= 0 && this.pulse == 0) {
         this.pulse = 5;
         this.world.addBlockEvent(this.getPos(), this.getBlockType(), 0, c);
      }

   }

   public boolean receiveClientEvent(int i, int j) {
      if (i != 0) {
         return super.receiveClientEvent(i, j);
      } else {
         if (this.world.isRemote) {
            Color c = new Color(colors[j]);
            this.pulse = 5;
            this.pRed = (float)c.getRed() / 255.0F;
            this.pGreen = (float)c.getGreen() / 255.0F;
            this.pBlue = (float)c.getBlue() / 255.0F;

            for(WeakReference<TileVisNode> vr = this.getParent(); VisNetHandler.isNodeValid(vr) && vr.get() instanceof TileVisRelay && ((TileVisRelay)vr.get()).pulse == 0; vr = vr.get().getParent()) {
               ((TileVisRelay)vr.get()).pRed = this.pRed;
               ((TileVisRelay)vr.get()).pGreen = this.pGreen;
               ((TileVisRelay)vr.get()).pBlue = this.pBlue;
               ((TileVisRelay)vr.get()).pulse = 5;
            }
         }

         return true;
      }
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      super.readCustomNBT(nbttagcompound);
      this.orientation = nbttagcompound.getShort("orientation");
      this.color = nbttagcompound.getByte("color");
      this.px = nbttagcompound.getByte("px");
      this.py = nbttagcompound.getByte("py");
      this.pz = nbttagcompound.getByte("pz");
      this.needToLoadParent = true;
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      super.writeCustomNBT(nbttagcompound);
      nbttagcompound.setShort("orientation", this.orientation);
      nbttagcompound.setByte("color", this.color);
      if (VisNetHandler.isNodeValid(this.getParent())) {
         nbttagcompound.setByte("px", (byte)(this.getPos().getX() - this.getParent().get().getPos().getX()));
         nbttagcompound.setByte("py", (byte)(this.getPos().getY() - this.getParent().get().getPos().getY()));
         nbttagcompound.setByte("pz", (byte)(this.getPos().getZ() - this.getParent().get().getPos().getZ()));
      } else {
         nbttagcompound.setByte("px", (byte)0);
         nbttagcompound.setByte("py", (byte)0);
         nbttagcompound.setByte("pz", (byte)0);
      }

   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      if (!this.world.isRemote) {
         ++this.color;
         if (this.color > 5) {
            this.color = -1;
         }

         this.removeThisNode();
         this.nodeRefresh = true;
         this.markDirty();
         { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:crystal")); if (_snd != null) world.playSound(null, new BlockPos(x, y, z), _snd, net.minecraft.util.SoundCategory.BLOCKS, 0.2F, 1.0F); }
      }

      return 0;
   }

   public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
      return null;
   }

   public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
   }

   public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
   }

   @Override
   public void setParent(WeakReference<TileVisNode> parent) {
      super.setParent(parent);
      CommonUtils.sendSupplementaryS35(this);
   }
}
