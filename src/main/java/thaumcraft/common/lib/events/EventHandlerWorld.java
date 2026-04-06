package thaumcraft.common.lib.events;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import java.util.ArrayList;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.Level;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.monster.boss.EntityThaumcraftBoss;
import thaumcraft.common.items.ItemEssence;
import thaumcraft.common.items.equipment.ItemElementalPickaxe;
import thaumcraft.common.items.equipment.ItemPrimalCrusher;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.foci.ItemFocusExcavation;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ChunkLoc;
import thaumcraft.common.lib.world.dim.Cell;
import thaumcraft.common.lib.world.dim.CellLoc;
import thaumcraft.common.lib.world.dim.MazeHandler;
import thaumcraft.common.tiles.TileSensor;
import net.minecraft.util.math.BlockPos;

public class EventHandlerWorld implements IFuelHandler {
   @SubscribeEvent
   public void worldLoad(WorldEvent.Load event) {
      if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
         MazeHandler.loadMaze(event.getWorld());
      }

   }

   @SubscribeEvent
   public void worldSave(WorldEvent.Save event) {
      if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
         MazeHandler.saveMaze(event.getWorld());
      }

   }

   @SubscribeEvent
   public void worldUnload(WorldEvent.Unload event) {
      if (!event.getWorld().isRemote) {
         VisNetHandler.sources.remove(event.getWorld().provider.getDimension());

         try {
            TileSensor.noteBlockEvents.remove(event.getWorld());
         } catch (Exception e) {
            FMLCommonHandler.instance().getFMLLogger().log(Level.WARN, "[Thaumcraft] Error unloading noteblock even handlers.", e);
         }

      }
   }

   @SubscribeEvent
   public void chunkSave(ChunkDataEvent.Save event) {
      NBTTagCompound var4 = new NBTTagCompound();
      event.getData().setTag("Thaumcraft", var4);
      var4.setBoolean(Config.regenKey, true);
   }

   @SubscribeEvent
   public void chunkLoad(ChunkDataEvent.Load event) {
      int dim = event.getWorld().provider.getDimension();
      ChunkPos loc = event.getChunk().getPos();
      if (!event.getData().getCompoundTag("Thaumcraft").hasKey(Config.regenKey) && (Config.regenAmber || Config.regenAura || Config.regenCinnibar || Config.regenInfusedStone || Config.regenStructure || Config.regenTrees)) {
         FMLCommonHandler.instance().getFMLLogger().log(Level.WARN, "[Thaumcraft] World gen was never run for chunk at {}. Adding to queue for regeneration.",event.getChunk().getPos());
         ArrayList<ChunkLoc> chunks = (ArrayList)ServerTickEventsFML.chunksToGenerate.get(dim);
         if (chunks == null) {
            ServerTickEventsFML.chunksToGenerate.put(dim, new ArrayList());
            chunks = (ArrayList)ServerTickEventsFML.chunksToGenerate.get(dim);
         }

         if (chunks != null) {
            chunks.add(new ChunkLoc(loc.x, loc.z));
            ServerTickEventsFML.chunksToGenerate.put(dim, chunks);
         }
      }

   }

   public int getBurnTime(ItemStack fuel) {
      if (fuel.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 0))) {
         return 6400;
      } else {
         return fuel.isItemEqual(new ItemStack(ConfigBlocks.blockMagicalLog)) ? 400 : 0;
      }
   }

   @SubscribeEvent
   public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
      int warp = ThaumcraftApi.getWarp(event.crafting);
      if (!Config.wuss && warp > 0 && !event.player.world.isRemote) {
         Thaumcraft.addStickyWarpToPlayer(event.player, warp);
      }

      if (event.crafting.getItem() == ConfigItems.itemResource && event.crafting.getItemDamage() == 13 && event.crafting.hasTagCompound()) {
         for(int var2 = 0; var2 < 9; ++var2) {
            ItemStack var3 = event.craftMatrix.getStackInSlot(var2);
            if (var3 != null && var3.getItem() instanceof ItemEssence) {
               var3.grow(1);
               event.craftMatrix.setInventorySlotContents(var2, var3);
            }
         }
      }

      if (event.crafting.getItem() == Item.getItemFromBlock(ConfigBlocks.blockMetalDevice) && event.crafting.getItemDamage() == 3) {
         ItemStack var3 = event.craftMatrix.getStackInSlot(4);
         var3.grow(1);
         event.craftMatrix.setInventorySlotContents(4, var3);
      }

   }

   @SubscribeEvent
   public void harvestEvent(BlockEvent.HarvestDropsEvent event) {
      EntityPlayer player = event.getHarvester();
      if (event.getDrops() != null && !event.getDrops().isEmpty() && player != null) {
         ItemStack held = player.inventory.getCurrentItem();
         if (held != null && (held.getItem() instanceof ItemElementalPickaxe || held.getItem() instanceof ItemPrimalCrusher || held.getItem() instanceof ItemWandCasting && ((ItemWandCasting)held.getItem()).getFocus(held) != null && ((ItemWandCasting)held.getItem()).getFocus(held).isUpgradedWith(((ItemWandCasting)held.getItem()).getFocusItem(held), ItemFocusExcavation.dowsing))) {
            int fortune = event.getFortuneLevel();
            if (held.getItem() instanceof ItemWandCasting) {
               fortune = ((ItemWandCasting)held.getItem()).getFocus(held).getUpgradeLevel(((ItemWandCasting)held.getItem()).getFocusItem(held), FocusUpgradeType.treasure);
            }

            float chance = 0.2F + (float)fortune * 0.075F;

            for(int a = 0; a < event.getDrops().size(); ++a) {
               ItemStack is = event.getDrops().get(a);
               ItemStack smr = Utils.findSpecialMiningResult(is, chance, event.getWorld().rand);
               if (!is.isItemEqual(smr)) {
                  event.getDrops().set(a, smr);
                  if (!event.getWorld().isRemote) {
                     event.getWorld().playSound(null, event.getPos(), net.minecraft.init.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, net.minecraft.util.SoundCategory.PLAYERS, 0.2F, 0.7F + event.getWorld().rand.nextFloat() * 0.2F);
                  }
               }
            }
         }

      }
   }

   @SubscribeEvent
   public void noteEvent(NoteBlockEvent.Play event) {
      if (!event.getWorld().isRemote) {
         if (!TileSensor.noteBlockEvents.containsKey(event.getWorld())) {
            TileSensor.noteBlockEvents.put(event.getWorld(), new ArrayList());
         }

         ArrayList<Integer[]> list = (ArrayList)TileSensor.noteBlockEvents.get(event.getWorld());
         list.add(new Integer[]{event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getInstrument().ordinal(), event.getVanillaNoteId()});
         TileSensor.noteBlockEvents.put(event.getWorld(), list);
      }
   }

   @SubscribeEvent
   public void fillBucket(FillBucketEvent event) {
      if (event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
         BlockPos targetPos = event.getTarget().getBlockPos();
         net.minecraft.block.state.IBlockState targetState = event.getWorld().getBlockState(targetPos);
         if (targetState.getBlock() == ConfigBlocks.blockFluidPure
                 && targetState.getBlock().getMetaFromState(targetState) == 0) {
            event.getWorld().setBlockToAir(targetPos);
            event.setFilledBucket(new ItemStack(ConfigItems.itemBucketPure));
            event.setResult(Result.ALLOW);
            return;
         }

         if (targetState.getBlock() == ConfigBlocks.blockFluidDeath
                 && targetState.getBlock().getMetaFromState(targetState) == 3) {
            event.getWorld().setBlockToAir(targetPos);
            event.setFilledBucket(new ItemStack(ConfigItems.itemBucketDeath));
            event.setResult(Result.ALLOW);
            return;
         }
      }

   }

   @SubscribeEvent
   public void placeBlockEvent(BlockEvent.PlaceEvent event) {
      if (this.isNearActiveBoss(event.getWorld(), event.getPlayer(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public void placeBlockEvent(BlockEvent.MultiPlaceEvent event) {
      if (this.isNearActiveBoss(event.getWorld(), event.getPlayer(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ())) {
         event.setCanceled(true);
      }

   }

   private boolean isNearActiveBoss(World world, EntityPlayer player, int x, int y, int z) {
      if (world.provider.getDimension() == Config.dimensionOuterId && player != null && !player.capabilities.isCreativeMode) {
         int xx = x >> 4;
         int zz = z >> 4;
         Cell c = MazeHandler.getFromHashMap(new CellLoc(xx, zz));
         if (c != null && c.feature >= 2 && c.feature <= 5) {
            ArrayList<Entity> list = EntityUtils.getEntitiesInRange(world, x, y, z, null, EntityThaumcraftBoss.class, 32.0F);
             return list != null && !list.isEmpty();
         }
      }

      return false;
   }
}
