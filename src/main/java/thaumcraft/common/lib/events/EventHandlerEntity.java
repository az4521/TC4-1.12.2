package thaumcraft.common.lib.events;

import com.google.common.io.Files;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRepairableExtended;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.damagesource.DamageSourceThaumcraft;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigEntities;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntityAspectOrb;
import thaumcraft.common.entities.golems.EntityTravelingTrunk;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.monster.EntityBrainyZombie;
import thaumcraft.common.entities.monster.EntityTaintChicken;
import thaumcraft.common.entities.monster.EntityTaintCow;
import thaumcraft.common.entities.monster.EntityTaintCreeper;
import thaumcraft.common.entities.monster.EntityTaintPig;
import thaumcraft.common.entities.monster.EntityTaintSheep;
import thaumcraft.common.entities.monster.EntityTaintVillager;
import thaumcraft.common.entities.monster.EntityThaumicSlime;
import thaumcraft.common.entities.monster.boss.EntityThaumcraftBoss;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.entities.projectile.EntityPrimalArrow;
import thaumcraft.common.items.ItemBathSalts;
import thaumcraft.common.items.ItemCrystalEssence;
import thaumcraft.common.items.armor.Hover;
import thaumcraft.common.items.armor.ItemHoverHarness;
import thaumcraft.common.items.equipment.ItemBowBone;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.lib.WarpEvents;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.lib.world.dim.Cell;
import thaumcraft.common.lib.world.dim.CellLoc;
import thaumcraft.common.lib.world.dim.MazeHandler;
import thaumcraft.common.tiles.TileOwned;

import static thaumcraft.api.expands.warp.WarpEventManager.getWarpEventDelayForPlayer;
import net.minecraft.util.math.BlockPos;

public class EventHandlerEntity {
   public HashMap<Integer,Float> prevStep = new HashMap<>();
   public static HashMap<String,ArrayList<WeakReference<Entity>>> linkedEntities = new HashMap<>();

   @SubscribeEvent
   public void droppedItem(ItemTossEvent event) {
      NBTTagCompound itemData = event.getEntityItem().getEntityData();
      itemData.setString("thrower", event.getPlayer().getName());
   }

   @SubscribeEvent
   public void playerLoad(PlayerEvent.LoadFromFile event) {
      EntityPlayer p = event.getEntityPlayer();
      Thaumcraft.proxy.getPlayerKnowledge().wipePlayerKnowledge(p.getName());
      File file1 = this.getPlayerFile("thaum", event.getPlayerDirectory(), p.getName());
      boolean legacy = false;
      if (!file1.exists()) {
         File filep = event.getPlayerFile("thaum");
         if (filep.exists()) {
            try {
               Files.copy(filep, file1);
                Thaumcraft.log.info("Using and converting UUID Thaumcraft savefile for {}", p.getName());
               legacy = true;
               filep.delete();
               File fb = event.getPlayerFile("thaumback");
               if (fb.exists()) {
                  fb.delete();
               }
            } catch (IOException ignored) {
            }
         } else {
            File filet = this.getLegacyPlayerFile(p);
            if (filet.exists()) {
               try {
                  Files.copy(filet, file1);
                   Thaumcraft.log.info("Using pre MC 1.7.10 Thaumcraft savefile for {}", p.getName());
                  legacy = true;
               } catch (IOException ignored) {
               }
            }
         }
      }

      ResearchManager.loadPlayerData(p, file1, this.getPlayerFile("thaumback", event.getPlayerDirectory(), p.getName()), legacy);

      for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
         for(ResearchItem ri : cat.research.values()) {
            if (ri.isAutoUnlock()) {
               Thaumcraft.proxy.getResearchManager().completeResearch(p, ri.key);
            }
         }
      }

   }

   public File getLegacyPlayerFile(EntityPlayer player) {
      try {
         File playersDirectory = new File(player.world.getSaveHandler().getWorldDirectory(), "players");
         return new File(playersDirectory, player.getName() + ".thaum");
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public File getPlayerFile(String suffix, File playerDirectory, String playername) {
      if ("dat".equals(suffix)) {
         throw new IllegalArgumentException("The suffix 'dat' is reserved");
      } else {
         return new File(playerDirectory, playername + "." + suffix);
      }
   }

   @SubscribeEvent
   public void playerSave(PlayerEvent.SaveToFile event) {
      EntityPlayer p = event.getEntityPlayer();
      ResearchManager.savePlayerData(p, this.getPlayerFile("thaum", event.getPlayerDirectory(), p.getName()), this.getPlayerFile("thaumback", event.getPlayerDirectory(), p.getName()));
   }

   public static void doRepair(ItemStack is, EntityPlayer player) {
      int level = EnchantmentHelper.getEnchantmentLevel(Config.enchRepair, is);
      if (level > 0) {
         if (level > 2) {
            level = 2;
         }

         AspectList cost = ThaumcraftCraftingManager.getObjectTags(is);
         if (cost != null && cost.size() != 0) {
            cost = ResearchManager.reduceToPrimals(cost);
            AspectList finalCost = new AspectList();

            for(Aspect a : cost.getAspects()) {
               if (a != null) {
                  finalCost.merge(a, (int)Math.sqrt(cost.getAmount(a) * 2) * level);
               }
            }

            if (is.getItem() instanceof IRepairableExtended) {
               if (((IRepairableExtended)is.getItem()).doRepair(is, player, level) && WandManager.consumeVisFromInventory(player, finalCost)) {
                  is.damageItem(-level, player);
               }
            } else if (WandManager.consumeVisFromInventory(player, finalCost)) {
               is.damageItem(-level, player);
            }

         }
      }
   }

   @SubscribeEvent
   public void livingTick(LivingEvent.LivingUpdateEvent event) {
      if (event.getEntity() instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)event.getEntity();
         if (event.getEntity().world.provider.getDimension() == Config.dimensionOuterId && !player.capabilities.isCreativeMode && player.ticksExisted % 20 == 0 && (player.capabilities.isFlying || Hover.getHover(player.getEntityId()))) {
            player.capabilities.isFlying = false;
            Hover.setHover(player.getEntityId(), false);
            player.sendMessage(new TextComponentString(TextFormatting.ITALIC + "" + TextFormatting.GRAY + I18n.translateToLocal("tc.break.fly")));
         }

         if (Hover.getHover(player.getEntityId()) && (player.inventory.armorInventory.get(2) == null || !(player.inventory.armorInventory.get(2).getItem() instanceof ItemHoverHarness))) {
            Hover.setHover(player.getEntityId(), false);
            player.capabilities.isFlying = false;
         }

         if (!event.getEntity().world.isRemote) {
            if (!Config.wuss && player.ticksExisted > 0 && player.ticksExisted % getWarpEventDelayForPlayer(player) == 0) {
               WarpEvents.checkWarpEvent(player);
            }

            if (player.ticksExisted % 10 == 0 && player.isPotionActive(Potion.getPotionById(Config.potionDeathGazeID))) {
               WarpEvents.checkDeathGaze(player);
            }

            if (player.ticksExisted % 40 == 0) {
               int a = 0;

               while(true) {
                  InventoryPlayer var10001 = player.inventory;
                  if (a >= InventoryPlayer.getHotbarSize()) {
                     for(/*int */a = 0; a < 4; ++a) {
                        if (player.inventory.armorInventory.get(a) != null) {
                           ItemStack is = player.inventory.armorInventory.get(a);
                           if (is.getItemDamage() > 0 && is.getItem() instanceof IRepairable && !player.capabilities.isCreativeMode) {
                              doRepair(is, player);
                           }
                        }
                     }
                     break;
                  }

                  if (!player.inventory.mainInventory.get(a).isEmpty()) {
                     ItemStack is = player.inventory.mainInventory.get(a);
                     if (is.getItemDamage() > 0 && is.getItem() instanceof IRepairable && !player.capabilities.isCreativeMode && !(is.getItem() instanceof ItemHoverHarness)) {
                        doRepair(is, player);
                     }
                  }

                  ++a;
               }
            }
         }

         this.updateSpeed(player);
         if (player.world.isRemote && (player.isSneaking() || player.inventory.armorInventory.get(0) == null || player.inventory.armorInventory.get(0).getItem() != ConfigItems.itemBootsTraveller) && this.prevStep.containsKey(player.getEntityId())) {
            player.stepHeight = this.prevStep.get(player.getEntityId());
            this.prevStep.remove(player.getEntityId());
         }
      }

      if (event.getEntity() instanceof EntityMob && !event.getEntity().isDead) {
         EntityMob mob = (EntityMob)event.getEntity();
         int t = (int)mob.getEntityAttribute(EntityUtils.CHAMPION_MOD).getAttributeValue();
         if (t >= 0 && ChampionModifier.mods[t].type == 0) {
            ChampionModifier.mods[t].effect.performEffect(mob, null, null, 0.0F);
         }
      }

   }

   private void updateSpeed(EntityPlayer player) {
      try {
         if (!player.capabilities.isFlying && player.inventory.armorInventory.get(0) != null && player.moveForward > 0.0F) {
            int haste = EnchantmentHelper.getEnchantmentLevel(Config.enchHaste, player.inventory.armorInventory.get(0));
            if (haste > 0) {
               float bonus = (float)haste * 0.015F;
               if (player.isAirBorne) {
                  bonus /= 2.0F;
               }

               if (player.isInWater()) {
                  bonus /= 2.0F;
               }

               player.moveRelative(0.0F, 0.0F, 1.0F, bonus);
            }
         }
      } catch (Exception ignored) {
      }

   }

   @SubscribeEvent
   public void playerJumps(LivingEvent.LivingJumpEvent event) {
      if (event.getEntity() instanceof EntityPlayer && ((EntityPlayer)event.getEntity()).inventory.armorInventory.get(0) != null && ((EntityPlayer)event.getEntity()).inventory.armorInventory.get(0).getItem() == ConfigItems.itemBootsTraveller) {
         EntityLivingBase var10000 = event.getEntityLiving();
         var10000.motionY += 0.275F;
      }

   }

   @SubscribeEvent
   public void playerInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
      if (event.getTarget() instanceof EntityGolemBase && !((EntityGolemBase) event.getTarget()).getOwnerName().isEmpty() && !((EntityGolemBase)event.getTarget()).getOwnerName().equals(event.getEntityPlayer().getName())) {
         if (!event.getEntityPlayer().world.isRemote) {
            event.getEntityPlayer().sendMessage(new TextComponentTranslation("You are not my Master!"));
         }

         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public void entitySpawns(EntityJoinWorldEvent event) {
      if (!event.getWorld().isRemote) {
         if (event.getEntity() instanceof EntityEnderPearl) {
            int x = MathHelper.floor(event.getEntity().posX);
            int y = MathHelper.floor(event.getEntity().posY);
            int z = MathHelper.floor(event.getEntity().posZ);

            label138:
            for(int xx = -5; xx <= 5; ++xx) {
               for(int yy = -5; yy <= 5; ++yy) {
                  for(int zz = -5; zz <= 5; ++zz) {
                     TileEntity tile = event.getWorld().getTileEntity(new BlockPos(x + xx, y + yy, z + zz));
                     if (tile instanceof TileOwned) {
                        if (((EntityEnderPearl)event.getEntity()).getThrower() instanceof EntityPlayer) {
                           ((EntityPlayer)((EntityEnderPearl)event.getEntity()).getThrower()).sendMessage(new TextComponentString("§5§oThe magic of a nearby warded object destroys the ender pearl."));
                        }

                        event.getEntity().setDead();
                        break label138;
                     }
                  }
               }
            }
         }

         if (event.getEntity() instanceof EntityMob) {
            EntityMob mob = (EntityMob)event.getEntity();
            if (mob.getEntityAttribute(EntityUtils.CHAMPION_MOD).getAttributeValue() < (double)-1.0F) {
               int championChance = event.getWorld().rand.nextInt(100);
               if (event.getWorld().getDifficulty() == EnumDifficulty.EASY || !Config.championMobs) {
                  championChance += 2;
               }

               if (event.getWorld().getDifficulty() == EnumDifficulty.HARD) {
                  championChance -= Config.championMobs ? 2 : 0;
               }

               if (event.getWorld().provider.getDimension() == Config.dimensionOuterId) {
                  championChance -= 3;
               }

               Biome bg = mob.world.getBiome(new BlockPos(MathHelper.ceil(mob.posX), 0, MathHelper.ceil(mob.posZ)));
               if (BiomeDictionary.hasType(bg, Type.SPOOKY)
                       || BiomeDictionary.hasType(bg, Type.NETHER)
                       || BiomeDictionary.hasType(bg, Type.END)) {
                  championChance -= Config.championMobs ? 2 : 1;
               }

               if (this.isDangerousLocation(
                       mob.world, MathHelper.ceil(mob.posX),
                       MathHelper.ceil(mob.posY),
                       MathHelper.ceil(mob.posZ))) {
                  championChance -= Config.championMobs ? 10 : 3;
               }

               int cc = 0;
               boolean whitelisted = false;

               for(Class<?> clazz : ConfigEntities.championModWhitelist.keySet()) {
                  if (clazz.isAssignableFrom(event.getEntity().getClass())) {
                     whitelisted = true;
                     if (Config.championMobs || event.getEntity() instanceof EntityThaumcraftBoss) {
                        cc = Math.max(cc, ConfigEntities.championModWhitelist.get(clazz) - 1);
                     }
                  }
               }

               championChance -= cc;
               if (whitelisted && championChance <= 0
                       && mob.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() >= (double)10.0F) {
                  EntityUtils.makeChampion(mob, false);
               } else {
                  IAttributeInstance modai = mob.getEntityAttribute(EntityUtils.CHAMPION_MOD);
                  modai.removeModifier(ChampionModifier.ATTRIBUTE_MOD_NONE);
                  modai.applyModifier(ChampionModifier.ATTRIBUTE_MOD_NONE);
               }
            }
         }
      }

   }

   @SubscribeEvent
   public void playerChangedDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
      if (!event.player.world.isRemote) {
         this.moveLinkedEntities(event.player);
      }
   }

   @SubscribeEvent
   public void playerRespawn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
      if (!event.player.world.isRemote) {
         this.moveLinkedEntities(event.player);
      }
   }

   private void moveLinkedEntities(EntityPlayer player) {
      ArrayList<WeakReference<Entity>> dudes = linkedEntities.get(player.getName());
      if (dudes == null) {
         return;
      }

      ArrayList<WeakReference<Entity>> retained = new ArrayList<>();
      for (WeakReference<Entity> dude : dudes) {
         Entity entity = dude.get();
         if (entity != null && !entity.isDead) {
            retained.add(dude);
         }
      }

      if (retained.isEmpty()) {
         linkedEntities.remove(player.getName());
      } else {
         linkedEntities.put(player.getName(), retained);
      }
   }

   private boolean isDangerousLocation(World world, int x, int y, int z) {
      if (world.provider.getDimension() == Config.dimensionOuterId) {
         int xx = x >> 4;
         int zz = z >> 4;
         Cell c = MazeHandler.getFromHashMap(new CellLoc(xx, zz));
          return c != null && (c.feature == 6 || c.feature == 8);
      }

      return false;
   }

   @SubscribeEvent
   public void entityConstuct(EntityEvent.EntityConstructing event) {
      if (event.getEntity() instanceof EntityMob) {
         EntityMob mob = (EntityMob)event.getEntity();
         mob.getAttributeMap().registerAttribute(EntityUtils.CHAMPION_MOD).setBaseValue(-2.0F);
      }

   }

   @SubscribeEvent
   public void itemPickup(EntityItemPickupEvent event) {
      if (event.getEntityPlayer().getName().startsWith("FakeThaumcraft")) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public void livingDrops(LivingDropsEvent event) {
      boolean fakePlayerFlag = event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof FakePlayer;
      if (!event.getEntity().world.isRemote
              && event.isRecentlyHit()
              && !fakePlayerFlag
              && event.getEntity() instanceof EntityMob
              && !(event.getEntity() instanceof EntityThaumcraftBoss)
              && ((EntityMob)event.getEntity()).getEntityAttribute(EntityUtils.CHAMPION_MOD).getAttributeValue() >= (double)0.0F) {
         int i = 5 + event.getEntity().world.rand.nextInt(3);

         while(i > 0) {
            int j = EntityXPOrb.getXPSplit(i);
            i -= j;
            event.getEntity().world.spawnEntity(
                    new EntityXPOrb(event.getEntity().world, event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, j
                    ));
         }

         int lb = Math.min(2, MathHelper.floor((float)(event.getEntity().world.rand.nextInt(9) + event.getLootingLevel()) / 5.0F));
         event.getDrops().add(
                 new EntityItem(event.getEntity().world,
                         event.getEntityLiving().posX,
                         event.getEntityLiving().posY + (double)event.getEntityLiving().getEyeHeight(),
                         event.getEntityLiving().posZ,
                         new ItemStack(ConfigItems.itemLootbag, 1, lb)));
      }

      if (event.getEntityLiving() instanceof EntityZombie && !(event.getEntityLiving() instanceof EntityBrainyZombie) && event.isRecentlyHit() && event.getEntity().world.rand.nextInt(10) - event.getLootingLevel() < 1) {
         event.getDrops().add(new EntityItem(event.getEntity().world, event.getEntityLiving().posX, event.getEntityLiving().posY + (double)event.getEntityLiving().getEyeHeight(), event.getEntityLiving().posZ, new ItemStack(ConfigItems.itemZombieBrain)));
      }

      if (event.getEntityLiving() instanceof EntityVillager && event.getEntity().world.rand.nextInt(10) - event.getLootingLevel() < 1) {
         event.getDrops().add(new EntityItem(event.getEntity().world, event.getEntityLiving().posX, event.getEntityLiving().posY + (double)event.getEntityLiving().getEyeHeight(), event.getEntityLiving().posZ, new ItemStack(ConfigItems.itemResource, 1, 18)));
      }

      if (event.getSource() == DamageSourceThaumcraft.dissolve) {
         AspectList aspects = ScanManager.generateEntityAspects(event.getEntityLiving());
         if (aspects != null && aspects.size() > 0) {
            for(Aspect aspect : aspects.getAspects()) {
               if (!event.getEntity().world.rand.nextBoolean()) {
                  int size = 1 + event.getEntity().world.rand.nextInt(aspects.getAmount(aspect));
                  size = Math.max(1, size / 2);
                  ItemStack stack = new ItemStack(ConfigItems.itemCrystalEssence, size, 0);
                  ((ItemCrystalEssence)stack.getItem()).setAspects(stack, (new AspectList()).add(aspect, 1));
                  event.getDrops().add(new EntityItem(event.getEntity().world, event.getEntityLiving().posX, event.getEntityLiving().posY + (double)event.getEntityLiving().getEyeHeight(), event.getEntityLiving().posZ, stack));
               }
            }
         }
      }

   }

   @SubscribeEvent
   public void livingTick(LivingDeathEvent event) {
      if (!event.getEntityLiving().world.isRemote && !(event.getEntityLiving() instanceof ITaintedMob) && event.getEntityLiving().isPotionActive(Potion.getPotionById(Config.potionTaintPoisonID))) {
         Entity entity = null;
         if (event.getEntityLiving() instanceof EntityCreeper) {
            entity = new EntityTaintCreeper(event.getEntityLiving().world);
         } else if (event.getEntityLiving() instanceof EntitySheep) {
            entity = new EntityTaintSheep(event.getEntityLiving().world);
         } else if (event.getEntityLiving() instanceof EntityCow) {
            entity = new EntityTaintCow(event.getEntityLiving().world);
         } else if (event.getEntityLiving() instanceof EntityPig) {
            entity = new EntityTaintPig(event.getEntityLiving().world);
         } else if (event.getEntityLiving() instanceof EntityChicken) {
            entity = new EntityTaintChicken(event.getEntityLiving().world);
         } else if (event.getEntityLiving() instanceof EntityVillager) {
            entity = new EntityTaintVillager(event.getEntityLiving().world);
         } else {
            entity = new EntityThaumicSlime(event.getEntityLiving().world);
            if (entity != null) {
               ((EntityThaumicSlime)entity).setSlimeSize((int)(1.0F + Math.min(event.getEntityLiving().getMaxHealth() / 10.0F, 6.0F)));
            }
         }

         if (entity != null) {
            entity.setLocationAndAngles(event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, event.getEntityLiving().rotationYaw, 0.0F);
            event.getEntityLiving().world.spawnEntity(entity);
            event.getEntityLiving().setDead();
         }
      } else if (!event.getEntityLiving().world.isRemote && EntityUtils.getRecentlyHit(event.getEntityLiving()) > 0) {
         AspectList aspectsCompound = ScanManager.generateEntityAspects(event.getEntityLiving());
         if (aspectsCompound != null && aspectsCompound.size() > 0) {
            AspectList aspects = ResearchManager.reduceToPrimals(aspectsCompound);

            for(Aspect aspect : aspects.getAspects()) {
               if (event.getEntityLiving().world.rand.nextBoolean()) {
                  EntityAspectOrb orb = new EntityAspectOrb(event.getEntityLiving().world, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, aspect, 1 + event.getEntityLiving().world.rand.nextInt(aspects.getAmount(aspect)));
                  event.getEntityLiving().world.spawnEntity(orb);
               }
            }
         }
      }

   }

   @SubscribeEvent
   public void bowNocked(ArrowNockEvent event) {
      if (event.getEntityPlayer().inventory.hasItemStack(new ItemStack(ConfigItems.itemPrimalArrow))) {
         event.getEntityPlayer().setActiveHand(event.getHand()); event.setAction(new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, event.getBow()));
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public void bowShot(ArrowLooseEvent event) {
      if (event.getEntityPlayer().inventory.hasItemStack(new ItemStack(ConfigItems.itemPrimalArrow))) {
         float f = 0.0F;
         float dam = 2.0F;
         if (event.getBow().getItem() instanceof ItemBowBone) {
            f = (float)event.getCharge() / 10.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            if ((double)f < 0.1) {
               return;
            }

            dam = 2.5F;
         } else {
            f = (float)event.getCharge() / 20.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            if ((double)f < 0.1) {
               return;
            }
         }

         if (f > 1.0F) {
            f = 1.0F;
         }

         int type = 0;

         for(int j = 0; j < event.getEntityPlayer().inventory.mainInventory.size(); ++j) {
            if (event.getEntityPlayer().inventory.mainInventory.get(j) != null && event.getEntityPlayer().inventory.mainInventory.get(j).getItem() == ConfigItems.itemPrimalArrow) {
               type = event.getEntityPlayer().inventory.mainInventory.get(j).getItemDamage();
               break;
            }
         }

         EntityPrimalArrow entityarrow = new EntityPrimalArrow(event.getEntityPlayer().world, event.getEntityPlayer(), f * dam, type);
         if (event.getBow().getItem() instanceof ItemBowBone) {
            entityarrow.setDamage(entityarrow.getDamage() + (double)0.5F);
         } else if (f == 1.0F) {
            entityarrow.setIsCritical(true);
         }

         int k = EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.POWER, event.getBow());
         if (k > 0) {
            entityarrow.setDamage(entityarrow.getDamage() + (double)k * (double)0.5F + (double)0.5F);
         }

         int l = EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.PUNCH, event.getBow());
         if (type == 3) {
            ++l;
         }

         if (l > 0) {
            entityarrow.setKnockbackStrength(l);
         }

         if (EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.FLAME, event.getBow()) > 0) {
            entityarrow.setFire(100);
         }

         event.getBow().damageItem(1, event.getEntityPlayer());
         event.getEntityPlayer().world.playSound(null, event.getEntityPlayer().posX, event.getEntityPlayer().posY, event.getEntityPlayer().posZ, net.minecraft.init.SoundEvents.ENTITY_ARROW_SHOOT, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F / (event.getEntityPlayer().world.rand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
         boolean flag = EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.INFINITY, event.getBow()) > 0 && event.getEntityPlayer().world.rand.nextFloat() < 0.33F;

          if (!event.getEntityPlayer().capabilities.isCreativeMode || !flag) {
            InventoryUtils.consumeInventoryItem(event.getEntityPlayer(), ConfigItems.itemPrimalArrow, type);
         }

         if (!event.getEntityPlayer().world.isRemote) {
            event.getEntityPlayer().world.spawnEntity(entityarrow);
         }

         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public void finishedUsingItem(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Finish event) {
      if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
      EntityPlayer player = (EntityPlayer) event.getEntityLiving();
      if (!player.world.isRemote && player.isPotionActive(Potion.getPotionById(Config.potionUnHungerID))) {
         if (!event.getItem().isItemEqual(new ItemStack(Items.ROTTEN_FLESH)) && !event.getItem().isItemEqual(new ItemStack(ConfigItems.itemZombieBrain))) {
            if (event.getItem().getItem() instanceof ItemFood) {
               player.sendMessage(new TextComponentString("§4§o" + I18n.translateToLocal("warp.text.hunger.1")));
            }
         } else {
            PotionEffect pe = player.getActivePotionEffect(Potion.getPotionById(Config.potionUnHungerID));
            int amp = pe.getAmplifier() - 1;
            int duration = pe.getDuration() - 600;
            player.removePotionEffect(net.minecraft.potion.Potion.getPotionById(Config.potionUnHungerID));
            if (duration > 0 && amp >= 0) {
               pe = new PotionEffect(Potion.getPotionById(Config.potionUnHungerID), duration, amp, true, true);
               pe.getCurativeItems().clear();
               pe.addCurativeItem(new ItemStack(Items.ROTTEN_FLESH));
               player.addPotionEffect(pe);
            }

            player.sendMessage(new TextComponentString("§2§o" + I18n.translateToLocal("warp.text.hunger.2")));
         }
      }

   }

   @SubscribeEvent
   public void itemExpire(ItemExpireEvent event) {
      if (!event.getEntityItem().getItem().isEmpty() && event.getEntityItem().getItem().getItem() instanceof ItemBathSalts) {
         int x = MathHelper.floor(event.getEntityItem().posX);
         int y = MathHelper.floor(event.getEntityItem().posY);
         int z = MathHelper.floor(event.getEntityItem().posZ);
         net.minecraft.block.state.IBlockState waterState = event.getEntityItem().world.getBlockState(new BlockPos(x, y, z));
         if (waterState.getBlock() == net.minecraft.init.Blocks.WATER && waterState.getBlock().getMetaFromState(waterState) == 0) {
            event.getEntityItem().world.setBlockState(new BlockPos(x, y, z), ConfigBlocks.blockFluidPure.getDefaultState(), 3);
         }
      }

   }

   @SubscribeEvent
   public void breakSpeedEvent(PlayerEvent.BreakSpeed event) {
      if (!event.getEntityPlayer().onGround && Hover.getHover(event.getEntityPlayer().getEntityId())) {
         event.setNewSpeed(event.getOriginalSpeed() * 5.0F);
      }

   }
}
