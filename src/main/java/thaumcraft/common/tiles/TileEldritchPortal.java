package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketResearchComplete;
import thaumcraft.common.lib.world.dim.TeleporterThaumcraft;

public class TileEldritchPortal extends TileEntity implements net.minecraft.util.ITickable {
   public int opencount = -1;
   private int count = 0;

   @Override
   public double getMaxRenderDistanceSquared() {
      return 9216.0F;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX() - 1, this.getPos().getY() - 1, this.getPos().getZ() - 1, this.getPos().getX() + 2, this.getPos().getY() + 2, this.getPos().getZ() + 2);
   }

   @Override
   public void update() {
      ++this.count;
      if (this.world.isRemote && (this.count % 250 == 0 || this.count == 0)) {
         this.world.playSound(null, this.getPos(), thaumcraft.common.lib.SoundsTC.get("thaumcraft:evilportal"), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

      if (this.world.isRemote && this.opencount < 30) {
         ++this.opencount;
      }

      if (!this.world.isRemote && this.count % 5 == 0) {
         List ents = this.world.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(0.5F, 1.0F, 0.5F));
         if (!ents.isEmpty()) {
            for(Object e : ents) {
               EntityPlayerMP player = (EntityPlayerMP)e;
               if (player.getRidingEntity() == null && !player.isBeingRidden()) {
                  if (player.timeUntilPortal > 0) {
                     player.timeUntilPortal = 100;
                  } else if (player.dimension != Config.dimensionOuterId) {
                     player.timeUntilPortal = 100;
                     net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().transferPlayerToDimension(player, Config.dimensionOuterId, new TeleporterThaumcraft(net.minecraftforge.common.DimensionManager.getWorld(Config.dimensionOuterId)));
                     if (!ThaumcraftApiHelper.isResearchComplete(player.getName(), "ENTEROUTER")) {
                        PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("ENTEROUTER"), player);
                        Thaumcraft.proxy.getResearchManager().completeResearch(player, "ENTEROUTER");
                     }
                  } else {
                     player.timeUntilPortal = 100;
                     net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().transferPlayerToDimension(player, 0, new TeleporterThaumcraft(net.minecraftforge.common.DimensionManager.getWorld(0)));
                  }
               }
            }
         }
      }

   }
}
