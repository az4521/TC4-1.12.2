package thaumcraft.common.lib;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.EntityMindSpider;
import thaumcraft.common.lib.events.EventHandlerRunic;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.misc.PacketMiscEvent;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.utils.EntityUtils;

import static simpleutils.bauble.BaubleUtils.forEachBauble;
import static thaumcraft.api.expands.warp.WarpEventManager.tryTriggerRandomWarpEvent;

public class WarpEvents {

   public static void checkWarpEvent(EntityPlayer player) {
      tryTriggerRandomWarpEvent(player);
//      int warp = Thaumcraft.proxy.getPlayerKnowledge().getWarpTotal(player.getName());
//      int actualwarp = Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(player.getName())
//              + Thaumcraft.proxy.getPlayerKnowledge().getWarpSticky(player.getName());
//      warp += getWarpFromGear(player);
//      int warpCounter = Thaumcraft.proxy.getPlayerKnowledge().getWarpCounter(player.getName());
//      int r = player.world.rand.nextInt(100);
//      if (warpCounter > 0 && warp > 0 && (double)r <= Math.sqrt(warpCounter)) {
//         warp = Math.min(100, (warp + warp + warpCounter) / 3);
//         warpCounter = (int)((double)warpCounter - Math.max(5.0F, Math.sqrt(warpCounter) * (double)2.0F));
//         Thaumcraft.proxy.getPlayerKnowledge().setWarpCounter(player.getName(), warpCounter);
//         int eff = player.world.rand.nextInt(warp);
//         ItemStack helm = player.inventory.armorInventory.get(3);
//         if (helm != null
//                 && helm.getItem() instanceof ItemFortressArmor
//                 && helm.hasTagCompound() && helm.getTagCompound().hasKey("mask")
//                 && helm.getTagCompound().getInteger("mask") == 0) {
//            eff -= 2 + player.world.rand.nextInt(4);
//         }
//
//         PacketHandler.INSTANCE.sendTo(new PacketMiscEvent((short)0), (EntityPlayerMP)player);
//         if (eff > 0) {
//            if (eff <= 4) {
//               grantResearch(player, 1);
//               player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.3")));
//            }
//            else if (eff > 8) {
//               if (eff <= 12) {
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.11")));
//               }
//               else if (eff <= 16) {
//                  PotionEffect pe = new PotionEffect(Config.potionVisExhaustID, 5000, Math.min(3, warp / 15), true);
//                  pe.getCurativeItems().clear();
//
//                  try {
//                     player.addPotionEffect(pe);
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.1")));
//               }
//               else if (eff <= 20) {
//                  PotionEffect pe = new PotionEffect(Config.potionThaumarhiaID, Math.min(32000, 10 * warp), 0, true);
//                  pe.getCurativeItems().clear();
//
//                  try {
//                     player.addPotionEffect(pe);
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.15")));
//               }
//               else if (eff <= 24) {
//                  PotionEffect pe = new PotionEffect(Config.potionUnHungerID, 5000, Math.min(3, warp / 15), true);
//                  pe.getCurativeItems().clear();
//                  pe.addCurativeItem(new ItemStack(Items.ROTTEN_FLESH));
//                  pe.addCurativeItem(new ItemStack(ConfigItems.itemZombieBrain));
//
//                  try {
//                     player.addPotionEffect(pe);
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.2")));
//               }
//               else if (eff <= 28) {
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.12")));
//               }
//               else if (eff <= 32) {
//                  spawnMist(player, warp, 1);
//               }
//               else if (eff <= 36) {
//                  try {
//                     player.addPotionEffect(new PotionEffect(Config.potionBlurredID, Math.min(32000, 10 * warp), 0, true));
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//               }
//               else if (eff <= 40) {
//                  PotionEffect pe = new PotionEffect(Config.potionSunScornedID, 5000, Math.min(3, warp / 15), true);
//                  pe.getCurativeItems().clear();
//
//                  try {
//                     player.addPotionEffect(pe);
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.5")));
//               }
//               else if (eff <= 44) {
//                  try {
//                     player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.MINING_FATIGUE, 1200, Math.min(3, warp / 15), true));
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.9")));
//               }
//               else if (eff <= 48) {
//                  PotionEffect pe = new PotionEffect(Config.potionInfVisExhaustID, 6000, Math.min(3, warp / 15), false);
//                  pe.getCurativeItems().clear();
//
//                  try {
//                     player.addPotionEffect(pe);
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.1")));
//               }
//               else if (eff <= 52) {
//                  player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0, true));
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.10")));
//               }
//               else if (eff <= 56) {
//                  PotionEffect pe = new PotionEffect(Config.potionDeathGazeID, 6000, Math.min(3, warp / 15), true);
//                  pe.getCurativeItems().clear();
//
//                  try {
//                     player.addPotionEffect(pe);
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.4")));
//               }
//               else if (eff <= 60) {
//                  suddenlySpiders(player, warp, false);
//               }
//               else if (eff <= 64) {
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.13")));
//               }
//               else if (eff <= 68) {
//                  spawnMist(player, warp, warp / 30);
//               }
//               else if (eff <= 72) {
//                  try {
//                     player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0, true));
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//               }
//               else if (eff == 76) {//??? "=="?
//                  if (Thaumcraft.proxy.getPlayerKnowledge().getWarpSticky(player.getName()) > 0) {
//                     Thaumcraft.proxy.getPlayerKnowledge().addWarpSticky(player.getName(), -1);
//                     PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)1), (EntityPlayerMP)player);
//                     PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)1, -1), (EntityPlayerMP)player);
//                  }
//
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.14")));
//               }
//               else if (eff <= 80) {
//                  PotionEffect pe = new PotionEffect(Config.potionUnHungerID, 6000, Math.min(3, warp / 15), true);
//                  pe.getCurativeItems().clear();
//                  pe.addCurativeItem(new ItemStack(Items.ROTTEN_FLESH));
//                  pe.addCurativeItem(new ItemStack(ConfigItems.itemZombieBrain));
//
//                  try {
//                     player.addPotionEffect(pe);
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.2")));
//               }
//               else if (eff <= 84) {
//                  grantResearch(player, warp / 10);
//                  player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.3")));
//               }
//               else if (eff > 88) {
//                  if (eff <= 92) {
//                     suddenlySpiders(player, warp, true);
//                  } else {
//                     spawnMist(player, warp, warp / 15);
//                  }
//               }
//            }
//         }
//
//         if (actualwarp > 10 && !ThaumcraftApiHelper.isResearchComplete(player.getName(), "BATHSALTS") && !ThaumcraftApiHelper.isResearchComplete(player.getName(), "@BATHSALTS")) {
//            player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.8")));
//            PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("@BATHSALTS"), (EntityPlayerMP)player);
//            Thaumcraft.proxy.getResearchManager().completeResearch(player, "@BATHSALTS");
//         }
//
//         if (actualwarp > 25 && !ThaumcraftApiHelper.isResearchComplete(player.getName(), "ELDRITCHMINOR")) {
//            grantResearch(player, 10);
//            PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("ELDRITCHMINOR"), (EntityPlayerMP)player);
//            Thaumcraft.proxy.getResearchManager().completeResearch(player, "ELDRITCHMINOR");
//         }
//
//         if (actualwarp > 50 && !ThaumcraftApiHelper.isResearchComplete(player.getName(), "ELDRITCHMAJOR")) {
//            grantResearch(player, 20);
//            PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("ELDRITCHMAJOR"), (EntityPlayerMP)player);
//            Thaumcraft.proxy.getResearchManager().completeResearch(player, "ELDRITCHMAJOR");
//         }
//      }
//
//      Thaumcraft.proxy.getPlayerKnowledge().addWarpTemp(player.getName(), -1);
//      PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)2), (EntityPlayerMP)player);
   }

   public static void spawnMist(EntityPlayer player, int warp, int guardian) {
      PacketHandler.INSTANCE.sendTo(new PacketMiscEvent((short)1), (EntityPlayerMP)player);
      if (guardian > 0) {
         guardian = Math.min(8, guardian);

         for(int a = 0; a < guardian; ++a) {
            spawnGuardian(player);
         }
      }

      player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.6")));
   }

   public static void grantResearch(EntityPlayer player, int times) {
      int amt = 1 + player.world.rand.nextInt(times);

      for(int a = 0; a < amt; ++a) {
         Aspect aspect = Aspect.getPrimalAspects().get(player.world.rand.nextInt(6));
         Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getName(), aspect, (short)1);
         PacketHandler.INSTANCE.sendTo(new PacketAspectPool(aspect.getTag(), (short) 1, Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(player.getName(), aspect)), (EntityPlayerMP)player);
      }

      ResearchManager.scheduleSave(player);
   }

   public static void spawnGuardian(EntityPlayer player) {
      EntityEldritchGuardian eg = new EntityEldritchGuardian(player.world);
      int i = MathHelper.floor(player.posX);
      int j = MathHelper.floor(player.posY);
      int k = MathHelper.floor(player.posZ);

      for(int l = 0; l < 50; ++l) {
         int i1 = i + (7 + player.world.rand.nextInt(24 - 7 + 1)) * (-1 + player.world.rand.nextInt(1 - -1 + 1));
         int j1 = j + (7 + player.world.rand.nextInt(24 - 7 + 1)) * (-1 + player.world.rand.nextInt(1 - -1 + 1));
         int k1 = k + (7 + player.world.rand.nextInt(24 - 7 + 1)) * (-1 + player.world.rand.nextInt(1 - -1 + 1));
         if (player.world.isSideSolid(new net.minecraft.util.math.BlockPos(i1, j1 - 1, k1), net.minecraft.util.EnumFacing.UP)) {
            eg.setPosition(i1, j1, k1);
            if (player.world.checkNoEntityCollision(eg.getEntityBoundingBox()) && player.world.getCollisionBoxes(eg, eg.getEntityBoundingBox()).isEmpty() && !player.world.containsAnyLiquid(eg.getEntityBoundingBox())) {
               eg.setAttackTarget(player);
               player.world.spawnEntity(eg);
               break;
            }
         }
      }

   }

   public static void suddenlySpiders(EntityPlayer player, int warp, boolean real) {
      int spawns = Math.min(50, warp);

      for(int a = 0; a < spawns; ++a) {
         EntityMindSpider spider = new EntityMindSpider(player.world);
         int i = MathHelper.floor(player.posX);
         int j = MathHelper.floor(player.posY);
         int k = MathHelper.floor(player.posZ);
         boolean success = false;

         for(int l = 0; l < 50; ++l) {
            int i1 = i + (7 + player.world.rand.nextInt(24 - 7 + 1))
                    * (-1 + player.world.rand.nextInt(1 - -1 + 1));
            int j1 = j + (7 + player.world.rand.nextInt(24 - 7 + 1))
                    * (-1 + player.world.rand.nextInt(1 - -1 + 1));
            int k1 = k + (7 + player.world.rand.nextInt(24 - 7 + 1))
                    * (-1 + player.world.rand.nextInt(1 - -1 + 1));
            if (player.world.isSideSolid(new net.minecraft.util.math.BlockPos(i1, j1 - 1, k1), net.minecraft.util.EnumFacing.UP)) {
               spider.setPosition(i1, j1, k1);
               if (player.world.checkNoEntityCollision(spider.getEntityBoundingBox()) && player.world.getCollisionBoxes(spider, spider.getEntityBoundingBox()).isEmpty() && !player.world.containsAnyLiquid(spider.getEntityBoundingBox())) {
                  success = true;
                  break;
               }
            }
         }

         if (success) {
            spider.setAttackTarget(player);
            if (!real) {
               spider.setViewer(player.getName());
               spider.setHarmless(true);
            }

            player.world.spawnEntity(spider);
         }
      }

      player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.7")));
   }

   public static void checkDeathGaze(EntityPlayer player) {
      PotionEffect pe = player.getActivePotionEffect(Potion.getPotionById(Config.potionDeathGazeID));
      if (pe != null) {
         int level = pe.getAmplifier();
         int range = Math.min(8 + level * 3, 24);
         List<Entity> list = (List<Entity>)player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(range, range, range));

          for (Entity entity : list) {
              if (entity.canBeCollidedWith()
                      && entity instanceof EntityLivingBase
                      && entity.isEntityAlive()
                      && EntityUtils.isVisibleTo(0.75F, player, entity, (float) range)
                      && player.canEntityBeSeen(entity)
                      && (!(entity instanceof EntityPlayer)
                      || net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled())
                      && !((EntityLivingBase) entity).isPotionActive(net.minecraft.init.MobEffects.WITHER))
              {
                 EntityLivingBase living = (EntityLivingBase) entity;
                  living.setRevengeTarget(player);
                  if (entity instanceof EntityCreature) {
                      ((EntityCreature) entity).setAttackTarget(player);
                  }

                  living.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.WITHER, 80));
              }
          }

      }
   }

   public static int getWarpFromGear(EntityPlayer player) {
      AtomicInteger w = new AtomicInteger(EventHandlerRunic.getFinalWarp(player.getHeldItemMainhand(), player));

      for(int a = 0; a < 4; ++a) {
         w.addAndGet(EventHandlerRunic.getFinalWarp(player.inventory.armorInventory.get(a), player));
      }
      forEachBauble(player,(slot, stack, item) -> {
         w.addAndGet(EventHandlerRunic.getFinalWarp(stack, player));
         return false;
      });
      return w.get();
   }
}
