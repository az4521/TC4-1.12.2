package thaumcraft.common.lib.events;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXEssentiaSource;
import thaumcraft.common.tiles.TileMirrorEssentia;

public class EssentiaHandler {
   static final int DELAY = 5000;
   private static HashMap<WorldCoordinates,ArrayList<WorldCoordinates>> sources = new HashMap<>();
   private static HashMap<WorldCoordinates,Long> sourcesDelay = new HashMap<>();
   public static HashMap<String,EssentiaHandler.EssentiaSourceFX> sourceFX = new HashMap<>();

   public static boolean drainEssentia(TileEntity tile, Aspect aspect, EnumFacing direction, int range) {
      return drainEssentia(tile, aspect, direction, range, false);
   }

   public static boolean drainEssentia(TileEntity tile, Aspect aspect, EnumFacing direction, int range, boolean ignoreMirror) {
      BlockPos tilePos = tile.getPos();
      WorldCoordinates tileLoc = new WorldCoordinates(tilePos.getX(), tilePos.getY(), tilePos.getZ(), tile.getWorld().provider.getDimension());
      if (!sources.containsKey(tileLoc)) {
         getSources(tile.getWorld(), tileLoc, direction, range);
         return sources.containsKey(tileLoc) && drainEssentia(tile, aspect, direction, range);
      } else {
         for(WorldCoordinates source : sources.get(tileLoc)) {
            TileEntity sourceTile = tile.getWorld().getTileEntity(new BlockPos(source.x, source.y, source.z));
            if (!(sourceTile instanceof IAspectSource)) {
               break;
            }

            if (!ignoreMirror || !(sourceTile instanceof TileMirrorEssentia)) {
               IAspectSource as = (IAspectSource)sourceTile;
               if (as.takeFromContainer(aspect, 1)) {
                  SimpleNetworkWrapper var10000 = PacketHandler.INSTANCE;
                  PacketFXEssentiaSource var10001 = new PacketFXEssentiaSource(tilePos.getX(), tilePos.getY(), tilePos.getZ(), (byte)(tilePos.getX() - source.x), (byte)(tilePos.getY() - source.y), (byte)(tilePos.getZ() - source.z), aspect.getColor());
                  double var10005 = tilePos.getX();
                  double var10006 = tilePos.getY();
                  double var10007 = tilePos.getZ();
                  var10000.sendToAllAround(var10001, new NetworkRegistry.TargetPoint(tile.getWorld().provider.getDimension(), var10005, var10006, var10007, 32.0F));
                  return true;
               }
            }
         }

         sources.remove(tileLoc);
         sourcesDelay.put(tileLoc, System.currentTimeMillis() + 5000L);
         return false;
      }
   }

   public static boolean findEssentia(TileEntity tile, Aspect aspect, EnumFacing direction, int range) {
      BlockPos tilePos = tile.getPos();
      WorldCoordinates tileLoc = new WorldCoordinates(tilePos.getX(), tilePos.getY(), tilePos.getZ(), tile.getWorld().provider.getDimension());
      if (!sources.containsKey(tileLoc)) {
         getSources(tile.getWorld(), tileLoc, direction, range);
         return sources.containsKey(tileLoc) && findEssentia(tile, aspect, direction, range);
      } else {
         for(WorldCoordinates source : sources.get(tileLoc)) {
            TileEntity sourceTile = tile.getWorld().getTileEntity(new BlockPos(source.x, source.y, source.z));
            if (!(sourceTile instanceof IAspectSource)) {
               break;
            }

            IAspectSource as = (IAspectSource)sourceTile;
            if (as.doesContainerContainAmount(aspect, 1)) {
               return true;
            }
         }

         sources.remove(tileLoc);
         sourcesDelay.put(tileLoc, System.currentTimeMillis() + 5000L);
         return false;
      }
   }

   private static void getSources(World world, WorldCoordinates tileLoc, EnumFacing direction, int range) {
      if (sourcesDelay.containsKey(tileLoc)) {
         long d = sourcesDelay.get(tileLoc);
         if (d > System.currentTimeMillis()) {
            return;
         }

         sourcesDelay.remove(tileLoc);
      }

      TileEntity sourceTile = world.getTileEntity(new BlockPos(tileLoc.x, tileLoc.y, tileLoc.z));
      ArrayList<WorldCoordinates> sourceList = new ArrayList<>();
      int start = 0;
      if (direction == null) {
         start = -range;
         direction = EnumFacing.UP;
      }

      int xx = 0;
      int yy = 0;
      int zz = 0;

      for(int aa = -range; aa <= range; ++aa) {
         for(int bb = -range; bb <= range; ++bb) {
            for(int cc = start; cc < range; ++cc) {
               if (aa != 0 || bb != 0 || cc != 0) {
                  xx = tileLoc.x;
                  yy = tileLoc.y;
                  zz = tileLoc.z;
                  if (direction.getYOffset() != 0) {
                     xx += aa;
                     yy += cc * direction.getYOffset();
                     zz += bb;
                  } else if (direction.getXOffset() == 0) {
                     xx += aa;
                     yy += bb;
                     zz += cc * direction.getZOffset();
                  } else {
                     xx += cc * direction.getXOffset();
                     yy += aa;
                     zz += bb;
                  }

                  TileEntity te = world.getTileEntity(new BlockPos(xx, yy, zz));
                  BlockPos sourceTilePos = sourceTile != null ? sourceTile.getPos() : null;
                  if (te instanceof IAspectSource && (!(sourceTile instanceof TileMirrorEssentia) || !(te instanceof TileMirrorEssentia) || sourceTilePos.getX() != ((TileMirrorEssentia) te).linkX || sourceTilePos.getY() != ((TileMirrorEssentia) te).linkY || sourceTilePos.getZ() != ((TileMirrorEssentia) te).linkZ || sourceTile.getWorld().provider.getDimension() != ((TileMirrorEssentia) te).linkDim)) {
                     sourceList.add(new WorldCoordinates(xx, yy, zz, world.provider.getDimension()));
                  }
               }
            }
         }
      }

      if (!sourceList.isEmpty()) {
         sources.put(tileLoc, sourceList);
      } else {
         sourcesDelay.put(tileLoc, System.currentTimeMillis() + 5000L);
      }

   }

   public static void refreshSources(TileEntity tile) {
      BlockPos p = tile.getPos();
      sources.remove(new WorldCoordinates(p.getX(), p.getY(), p.getZ(), tile.getWorld().provider.getDimension()));
   }

   public static class EssentiaSourceFX {
      public BlockPos start;
      public BlockPos end;
      public int ticks;
      public int color;

      public EssentiaSourceFX(BlockPos start, BlockPos end, int ticks, int color) {
         this.start = start;
         this.end = end;
         this.ticks = ticks;
         this.color = color;
      }
   }
}
