package thaumcraft.common;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import java.io.File;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tc4tweak.network.MyNet;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.expands.wandconsumption.ConsumptionModifierCalculator;
import thaumcraft.api.expands.warp.WarpEventManager;
import thaumcraft.api.expands.worldgen.node.NodeGenerationManager;
import thaumcraft.client.lib.RenderEventHandler;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigAspects;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigEntities;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.config.ConfigRecipes;
import thaumcraft.common.config.ConfigResearch;
import thaumcraft.common.items.BehaviorDispenseAlumetum;
import thaumcraft.common.lib.CreativeTabThaumcraft;
import thaumcraft.common.lib.FakeThaumcraftPlayer;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.lib.events.CommandThaumcraft;
import thaumcraft.common.lib.events.EventHandlerEntity;
import thaumcraft.common.lib.events.EventHandlerRunic;
import thaumcraft.common.lib.events.EventHandlerWorld;
import thaumcraft.common.lib.events.ServerTickEventsFML;
import thaumcraft.common.lib.network.EventHandlerNetwork;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketSyncWarp;
import thaumcraft.common.lib.network.playerdata.PacketWarpMessage;
import thaumcraft.common.lib.research.PlayerKnowledge;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.CropUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ComponentBankerHome;
import thaumcraft.common.lib.world.ComponentWizardTower;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import thaumcraft.common.lib.world.VillageBankerManager;
import thaumcraft.common.lib.world.VillageWizardManager;
import thaumcraft.common.lib.world.dim.WorldProviderOuter;

import static thaumcraft.common.Thaumcraft.VERSION;

@Mod(
   modid = Thaumcraft.MOD_ID,
   name = Thaumcraft.MOD_NAME,
   version = VERSION,
   guiFactory = "thaumcraft.client.ThaumcraftGuiFactory",
   dependencies = "required-after:forge@[14.23.5,);required-after:baubles@[1.0.1.10,)"
)
public class Thaumcraft {
   public static final String MOD_ID = "thaumcraft";
   public static final String MOD_NAME = "Thaumcraft";
   public static final String VERSION = "4.2.3.5";
   @SidedProxy(
      clientSide = "thaumcraft.client.ClientProxy",
      serverSide = "thaumcraft.common.CommonProxy"
   )
   public static CommonProxy proxy;

   @Instance("thaumcraft")
   public static Thaumcraft instance;
   ResearchManager researchManager;
   public ThaumcraftWorldGenerator worldGen;
   public EventHandlerWorld worldEventHandler;
   public EventHandlerNetwork networkEventHandler;
   public ServerTickEventsFML serverTickEvents;
   public EventHandlerEntity entityEventHandler;
   public EventHandlerRunic runicEventHandler;
   public RenderEventHandler renderEventHandler;
   public File modDir;
   public static final Logger log = LogManager.getLogger("THAUMCRAFT");
   public static boolean isHalloween = false;
   public static CreativeTabs tabTC = new CreativeTabThaumcraft("thaumcraft");
   public boolean aspectShift = false;
   public final MyNet CHANNEL = new MyNet("tc4tweak");

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      event.getModMetadata().version = VERSION;
      this.modDir = event.getModConfigurationDirectory();

      try {
         Config.initialize(event.getSuggestedConfigurationFile());
      } catch (Exception var8) {
         log.error("Thaumcraft has a problem loading it's configuration");
      } finally {
         if (Config.config != null) {
            Config.save();
         }
      }

      MinecraftForge.EVENT_BUS.register(this);

      // Fluids must be registered during preInit — force ConfigBlocks class load and register
      thaumcraft.common.config.ConfigBlocks.registerFluids();

      ThaumcraftApi.internalMethods = new InternalMethodHandler();
      proxy.playerKnowledge = new PlayerKnowledge();
      proxy.researchManager = new ResearchManager();
      this.worldEventHandler = new EventHandlerWorld();
      this.serverTickEvents = new ServerTickEventsFML();
      this.entityEventHandler = new EventHandlerEntity();
      this.runicEventHandler = new EventHandlerRunic();
      this.renderEventHandler = new RenderEventHandler();
      this.networkEventHandler = new EventHandlerNetwork();
      PacketHandler.init();
      MinecraftForge.TERRAIN_GEN_BUS.register(this.worldEventHandler);
      MinecraftForge.EVENT_BUS.register(this.worldEventHandler);
      FMLCommonHandler.instance().bus().register(this.worldEventHandler);
      MinecraftForge.EVENT_BUS.register(this.entityEventHandler);
      MinecraftForge.EVENT_BUS.register(this.runicEventHandler);
      FMLCommonHandler.instance().bus().register(this.networkEventHandler);
      GameRegistry.registerFuelHandler(this.worldEventHandler);
      FMLCommonHandler.instance().bus().register(this.serverTickEvents);
      GameRegistry.registerWorldGenerator(this.worldGen = new ThaumcraftWorldGenerator(), 0);
      ThaumcraftApi.registerScanEventhandler(new ScanManager());
      Config.save();
      ConfigBlocks.init();
      ConfigItems.init();

      try {
         MapGenStructureIO.registerStructureComponent(ComponentWizardTower.class, "TCVillageTower");
         MapGenStructureIO.registerStructureComponent(ComponentBankerHome.class, "TCVillageBanker");
      } catch (Throwable var7) {
         log.error("[Thaumcraft] Village tower could not be registered.");
         var7.printStackTrace();
      }

      proxy.registerHandlers();
      this.worldGen.initialize();
      FMLCommonHandler.instance().bus().register(instance);
      Config.registerBiomes();
      proxy.preInit(event);


      WarpEventManager.init();
      NodeGenerationManager.init();
      ConsumptionModifierCalculator.init();
   }

   @EventHandler
   public void init(FMLInitializationEvent evt) {
      proxy.registerDisplayInformation();
      ConfigEntities.init();
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ConfigItems.itemResource, new BehaviorDispenseAlumetum());
      NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
      VillageWizardManager villageManagerWizard = new VillageWizardManager();
      VillageBankerManager villageManagerBanker = new VillageBankerManager();
      // registerVillagerId removed in 1.12.2; profession-based system used instead
      VillagerRegistry.instance().registerVillageCreationHandler(villageManagerWizard);
      VillagerRegistry.instance().registerVillageCreationHandler(villageManagerBanker);
      // registerVillageTradeHandler removed in 1.12.2; trades attached to VillagerProfession/Career objects
      proxy.registerKeyBindings();
      DimensionType outerDimType = DimensionType.register("outer_lands", "_outer", Config.dimensionOuterId, WorldProviderOuter.class, false);
      DimensionManager.registerDimension(Config.dimensionOuterId, outerDimType);
      proxy.init(evt);
   }

   @EventHandler
   public void postInit(FMLPostInitializationEvent evt) {
      Config.initPotions();
      ConfigEntities.initEntitySpawns();
      Config.initModCompatibility();
      ConfigItems.postInit();
      ConfigRecipes.init();
      ConfigAspects.init();
      ConfigResearch.init();
      Config.initLoot();
      Config.initMisc();

      for(FMLInterModComms.IMCMessage message : FMLInterModComms.fetchRuntimeMessages(this)) {
         if (message.key.equals("harvestStandardCrop") && message.isItemStackMessage()) {
            ItemStack crop = message.getItemStackValue();
            CropUtils.addStandardCrop(crop, crop.getItemDamage());
             log.warn("Adding standard crop support for [{}]", crop.getDisplayName());
         }

         if (message.key.equals("harvestClickableCrop") && message.isItemStackMessage()) {
            ItemStack crop = message.getItemStackValue();
            CropUtils.addClickableCrop(crop, crop.getItemDamage());
             log.warn("Adding clickable crop support for [{}]", crop.getDisplayName());
         }

         if (message.key.equals("harvestStackedCrop") && message.isItemStackMessage()) {
            ItemStack crop = message.getItemStackValue();
            CropUtils.addStackedCrop(crop, crop.getItemDamage());
             log.warn("Adding stacked crop support for [{}]", crop.getDisplayName());
         }

         if (message.key.equals("nativeCluster") && message.isStringMessage()) {
            String[] t = message.getStringValue().split(",");
            if (t != null && t.length == 5) {
               try {
                  ItemStack ore = new ItemStack(Item.getItemById(Integer.parseInt(t[0])), 1, Integer.parseInt(t[1]));
                  ItemStack cluster = new ItemStack(Item.getItemById(Integer.parseInt(t[2])), 1, Integer.parseInt(t[3]));
                  Utils.addSpecialMiningResult(ore, cluster, Float.parseFloat(t[4]));
                   log.warn("Adding [{}] to special result list for [{}]", cluster.getDisplayName(), ore.getDisplayName());
               } catch (Exception ignored) {
               }
            }
         }

         if (message.key.equals("lampBlacklist") && message.isItemStackMessage()) {
            ItemStack crop = message.getItemStackValue();
            CropUtils.blacklistLamp(crop, crop.getItemDamage());
             log.warn("[Thaumcraft] Blacklisting [{}] for lamp of growth", crop.getDisplayName());
         }

         if (message.key.equals("dimensionBlacklist") && message.isStringMessage()) {
            String[] t = message.getStringValue().split(":");
            if (t != null && t.length == 2) {
               try {
                  ThaumcraftWorldGenerator var10000 = this.worldGen;
                  ThaumcraftWorldGenerator.addDimBlacklist(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
                   log.warn("Blacklisting dimension [{}] to only spawn TC content at level [{}]", Integer.parseInt(t[0]), Integer.parseInt(t[1]));
               } catch (Exception ignored) {
               }
            }
         }

         if (message.key.equals("biomeBlacklist") && message.isStringMessage()) {
            String[] t = message.getStringValue().split(":");
            if (t != null && t.length == 2 && Biome.getBiome(Integer.parseInt(t[0])) != null) {
               try {
                  ThaumcraftWorldGenerator var20 = this.worldGen;
                  ThaumcraftWorldGenerator.addBiomeBlacklist(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
                   log.warn("Blacklisting [{}] to only spawn TC content at level [{}]", Biome.getBiome(Integer.parseInt(t[0])).getBiomeName(), Integer.parseInt(t[1]));
               } catch (Exception ignored) {
               }
            }
         }

         if (message.key.equals("championWhiteList") && message.isStringMessage()) {
            try {
               String[] t = message.getStringValue().split(":");
               Class oclass = EntityList.getClassFromName(t[0]);
               if (oclass != null) {
                  ConfigEntities.championModWhitelist.put(oclass, Integer.parseInt(t[1]));
                   log.warn("Whitelisting [{}] to spawn champion mobs at level [{}]", t[0], Integer.parseInt(t[1]));
               }
            } catch (Exception var11) {
                log.error("Failed to Whitelist [{}] with [ championWhiteList ] message.", message.getStringValue());
            }
         }
      }
      proxy.postInit(evt);

   }

   @EventHandler
   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandThaumcraft());

   }

   @Mod.EventHandler
   public void serverStarted(FMLServerStartedEvent e) {
      proxy.serverStarted(e);
   }

   @SubscribeEvent
   public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
      if (eventArgs.getModID().equals("Thaumcraft")) {
         Config.syncConfigurable();
         if (Config.config != null && Config.config.hasChanged()) {
            Config.save();
         }
      }

   }

   public static void addWarpToPlayer(EntityPlayer player, int amount, boolean temporary) {
      if (player instanceof FakeThaumcraftPlayer || player instanceof FakePlayer) {
         return;
      }
      if (!player.world.isRemote) {
         if (proxy.getPlayerKnowledge() != null) {
            if (temporary || amount >= 0) {
               if (amount != 0) {
                  if (temporary) {
                     if (amount < 0 && proxy.getPlayerKnowledge().getWarpTemp(player.getName()) <= 0) {
                        return;
                     }

                     proxy.getPlayerKnowledge().addWarpTemp(player.getName(), amount);
                     PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)2), (EntityPlayerMP)player);
                     PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)2, amount), (EntityPlayerMP)player);
                  } else {
                     proxy.getPlayerKnowledge().addWarpPerm(player.getName(), amount);
                     PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)0), (EntityPlayerMP)player);
                     PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)0, amount), (EntityPlayerMP)player);
                  }

                  proxy.getPlayerKnowledge().setWarpCounter(player.getName(), proxy.getPlayerKnowledge().getWarpTotal(player.getName()));
               }
            }
         }
      }
   }

   public static void addStickyWarpToPlayer(EntityPlayer player, int amount) {
      if (player instanceof FakeThaumcraftPlayer || player instanceof FakePlayer) {
         return;
      }
      if (!player.world.isRemote) {
         if (proxy.getPlayerKnowledge() != null) {
            if (amount != 0) {
               if (amount >= 0 || proxy.getPlayerKnowledge().getWarpSticky(player.getName()) > 0) {
                  proxy.getPlayerKnowledge().addWarpSticky(player.getName(), amount);
                  PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)1), (EntityPlayerMP)player);
                  PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)1, amount), (EntityPlayerMP)player);
                  proxy.getPlayerKnowledge().setWarpCounter(player.getName(), proxy.getPlayerKnowledge().getWarpTotal(player.getName()));
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void registerBiomes(net.minecraftforge.event.RegistryEvent.Register<Biome> event) {
      Config.registerBiomes(event.getRegistry());
   }

   @SubscribeEvent
   public void registerEnchantments(net.minecraftforge.event.RegistryEvent.Register<net.minecraft.enchantment.Enchantment> event) {
      event.getRegistry().register(Config.enchHaste.setRegistryName("thaumcraft:haste"));
      event.getRegistry().register(Config.enchRepair.setRegistryName("thaumcraft:repair"));
   }
}
