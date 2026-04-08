package thaumcraft.client;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
// // import net.minecraftforge.fml.common.registry.VillagerRegistry;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelChicken;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleLava;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.IBakedModel;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.lwjgl.input.Mouse;
import tc4tweak.CommonUtils;
import tc4tweak.ConfigurationHandler;
import tc4tweak.modules.hudNotif.HUDNotification;
import tc4tweak.modules.particleEngine.ParticleEngineFix;
import tc4tweak.modules.researchBrowser.BrowserPaging;
import tc4tweak.modules.researchBrowser.DrawResearchCompletionCounter;
import tc4tweak.modules.researchBrowser.ThaumonomiconIndexSearcher;
import tc4tweak.network.NetworkedConfiguration;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.beams.FXArc;
import thaumcraft.client.fx.beams.FXBeam;
import thaumcraft.client.fx.beams.FXBeamBore;
import thaumcraft.client.fx.beams.FXBeamPower;
import thaumcraft.client.fx.beams.FXBeamWand;
import thaumcraft.client.fx.bolt.FXLightningBolt;
import thaumcraft.client.fx.other.FXBlockWard;
import thaumcraft.client.fx.particles.FXBlockRunes;
import thaumcraft.client.fx.particles.FXBoreParticles;
import thaumcraft.client.fx.particles.FXBoreSparkle;
import thaumcraft.client.fx.particles.FXBreaking;
import thaumcraft.client.fx.particles.FXBubble;
import thaumcraft.client.fx.particles.FXBurst;
import thaumcraft.client.fx.particles.FXDrop;
import thaumcraft.client.fx.particles.FXEssentiaTrail;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.client.fx.particles.FXSmokeSpiral;
import thaumcraft.client.fx.particles.FXSmokeTrail;
import thaumcraft.client.fx.particles.FXSpark;
import thaumcraft.client.fx.particles.FXSparkle;
import thaumcraft.client.fx.particles.FXSparkleTrail;
import thaumcraft.client.fx.particles.FXSwarm;
import thaumcraft.client.fx.particles.FXVent;
import thaumcraft.client.fx.particles.FXWisp;
import thaumcraft.client.fx.particles.FXWispArcing;
import thaumcraft.client.fx.particles.FXWispEG;
import thaumcraft.client.gui.*;
import thaumcraft.client.lib.ClientTickEventsFML;
import thaumcraft.client.renderers.block.BlockArcaneFurnaceRenderer;
import thaumcraft.client.renderers.block.BlockCandleRenderer;
import thaumcraft.client.renderers.block.BlockChestHungryRenderer;
import thaumcraft.client.renderers.block.BlockCosmeticOpaqueRenderer;
import thaumcraft.client.renderers.block.BlockCrystalRenderer;
import thaumcraft.client.renderers.block.BlockCustomOreRenderer;
import thaumcraft.client.renderers.block.BlockEldritchRenderer;
import thaumcraft.client.renderers.block.BlockEssentiaReservoirRenderer;
import thaumcraft.client.renderers.block.BlockGasRenderer;
import thaumcraft.client.renderers.block.BlockJarRenderer;
import thaumcraft.client.renderers.block.BlockLifterRenderer;
import thaumcraft.client.renderers.block.BlockLootCrateRenderer;
import thaumcraft.client.renderers.block.BlockLootUrnRenderer;
import thaumcraft.client.renderers.block.BlockMetalDeviceRenderer;
import thaumcraft.client.renderers.block.BlockStoneDeviceRenderer;
import thaumcraft.client.renderers.block.BlockTableRenderer;
import thaumcraft.client.renderers.block.BlockTaintFibreRenderer;
import thaumcraft.client.renderers.block.BlockTaintRenderer;
import thaumcraft.client.renderers.block.BlockTubeRenderer;
import thaumcraft.client.renderers.block.BlockWardedRenderer;
import thaumcraft.client.renderers.block.BlockWoodenDeviceRenderer;
import thaumcraft.client.renderers.entity.RenderAlumentum;
import thaumcraft.client.renderers.entity.RenderAspectOrb;
import thaumcraft.client.renderers.entity.RenderBrainyZombie;
import thaumcraft.client.renderers.entity.RenderCultist;
import thaumcraft.client.renderers.entity.RenderCultistPortal;
import thaumcraft.client.renderers.entity.RenderDart;
import thaumcraft.client.renderers.entity.RenderEldritchCrab;
import thaumcraft.client.renderers.entity.RenderEldritchGolem;
import thaumcraft.client.renderers.entity.RenderEldritchGuardian;
import thaumcraft.client.renderers.entity.RenderEldritchOrb;
import thaumcraft.client.renderers.entity.RenderElectricOrb;
import thaumcraft.client.renderers.entity.RenderEmber;
import thaumcraft.client.renderers.entity.RenderExplosiveOrb;
import thaumcraft.client.renderers.entity.RenderFallingTaint;
import thaumcraft.client.renderers.entity.RenderFireBat;
import thaumcraft.client.renderers.entity.RenderFollowingItem;
import thaumcraft.client.renderers.entity.RenderFrostShard;
import thaumcraft.client.renderers.entity.RenderGolemBase;
import thaumcraft.client.renderers.entity.RenderGolemBobber;
import thaumcraft.client.renderers.entity.RenderInhabitedZombie;
import thaumcraft.client.renderers.entity.RenderMindSpider;
import thaumcraft.client.renderers.entity.RenderPech;
import thaumcraft.client.renderers.entity.RenderPechBlast;
import thaumcraft.client.renderers.entity.RenderPrimalArrow;
import thaumcraft.client.renderers.entity.RenderPrimalOrb;
import thaumcraft.client.renderers.entity.RenderSpecialItem;
import thaumcraft.client.renderers.entity.RenderTaintChicken;
import thaumcraft.client.renderers.entity.RenderTaintCow;
import thaumcraft.client.renderers.entity.RenderTaintCreeper;
import thaumcraft.client.renderers.entity.RenderTaintPig;
import thaumcraft.client.renderers.entity.RenderTaintSheep;
import thaumcraft.client.renderers.entity.RenderTaintSpider;
import thaumcraft.client.renderers.entity.RenderTaintSpore;
import thaumcraft.client.renderers.entity.RenderTaintSporeSwarmer;
import thaumcraft.client.renderers.entity.RenderTaintSwarm;
import thaumcraft.client.renderers.entity.RenderTaintVillager;
import thaumcraft.client.renderers.entity.RenderTaintacle;
import thaumcraft.client.renderers.entity.RenderThaumicSlime;
import thaumcraft.client.renderers.entity.RenderTravelingTrunk;
import thaumcraft.client.renderers.entity.RenderWisp;
import thaumcraft.client.renderers.item.ItemBannerRenderer;
import thaumcraft.client.renderers.item.ItemBowBoneRenderer;
import thaumcraft.client.renderers.item.ItemChestHungryRenderer;
import thaumcraft.client.renderers.item.ItemCrystalRenderer;
import thaumcraft.client.renderers.item.ItemEssentiaReservoirRenderer;
import thaumcraft.client.renderers.item.ItemJarRenderer;
import thaumcraft.client.renderers.item.ItemMetalDeviceRenderer;
import thaumcraft.client.renderers.item.ItemThaumometerRenderer;
import thaumcraft.client.renderers.item.ItemStoneDeviceRenderer;
import thaumcraft.client.renderers.item.ItemTableRenderer;
import thaumcraft.client.renderers.item.ItemTrunkSpawnerRenderer;
import thaumcraft.client.renderers.item.ItemTubeRenderer;
import thaumcraft.client.renderers.item.ItemWandRenderer;
import thaumcraft.client.renderers.item.ItemWoodenDeviceRenderer;
import thaumcraft.client.renderers.compat.TransformTrackingModel;
import thaumcraft.client.renderers.models.entities.ModelEldritchGolem;
import thaumcraft.client.renderers.models.entities.ModelEldritchGuardian;
import thaumcraft.client.renderers.models.entities.ModelGolem;
import thaumcraft.client.renderers.models.entities.ModelPech;
import thaumcraft.client.renderers.models.entities.ModelTaintSheep1;
import thaumcraft.client.renderers.models.entities.ModelTaintSheep2;
import thaumcraft.client.renderers.models.entities.ModelTrunk;
import thaumcraft.client.renderers.tile.ItemJarFilledRenderer;
import thaumcraft.client.renderers.tile.ItemJarNodeRenderer;
import thaumcraft.client.renderers.tile.ItemNodeRenderer;
import thaumcraft.client.renderers.tile.TileAlchemyFurnaceAdvancedRenderer;
import thaumcraft.client.renderers.tile.TileAlembicRenderer;
import thaumcraft.client.renderers.tile.TileArcaneBoreBaseRenderer;
import thaumcraft.client.renderers.tile.TileArcaneBoreRenderer;
import thaumcraft.client.renderers.tile.TileArcaneLampRenderer;
import thaumcraft.client.renderers.tile.TileArcaneWorkbenchRenderer;
import thaumcraft.client.renderers.tile.TileBannerRenderer;
import thaumcraft.client.renderers.tile.TileBellowsRenderer;
import thaumcraft.client.renderers.tile.TileBrainboxRenderer;
import thaumcraft.client.renderers.tile.TileCentrifugeRenderer;
import thaumcraft.client.renderers.tile.TileChestHungryRenderer;
import thaumcraft.client.renderers.tile.TileCrucibleRenderer;
import thaumcraft.client.renderers.tile.TileCrystalRenderer;
import thaumcraft.client.renderers.tile.TileDeconstructionTableRenderer;
import thaumcraft.client.renderers.tile.TileEldritchCapRenderer;
import thaumcraft.client.renderers.tile.TileEldritchCrabSpawnerRenderer;
import thaumcraft.client.renderers.tile.TileEldritchCrystalRenderer;
import thaumcraft.client.renderers.tile.TileEldritchLockCombinedRenderer;
import thaumcraft.client.renderers.tile.TileEldritchLockRenderer;
import thaumcraft.client.renderers.tile.TileEldritchObeliskRenderer;
import thaumcraft.client.renderers.tile.TileEldritchPortalRenderer;
import thaumcraft.client.renderers.tile.TileEssentiaCrystalizerRenderer;
import thaumcraft.client.renderers.tile.TileEssentiaReservoirRenderer;
import thaumcraft.client.renderers.tile.TileEtherealBloomRenderer;
import thaumcraft.client.renderers.tile.TileFluxScrubberRenderer;
import thaumcraft.client.renderers.tile.TileFocalManipulatorRenderer;
import thaumcraft.client.renderers.tile.TileHoleRenderer;
import thaumcraft.client.renderers.tile.TileInfusionPillarRenderer;
import thaumcraft.client.renderers.tile.TileJarRenderer;
import thaumcraft.client.renderers.tile.TileMagicWorkbenchChargerRenderer;
import thaumcraft.client.renderers.tile.TileManaPodRenderer;
import thaumcraft.client.renderers.tile.TileMirrorRenderer;
import thaumcraft.client.renderers.tile.TileNodeConverterRenderer;
import thaumcraft.client.renderers.tile.TileNodeEnergizedRenderer;
import thaumcraft.client.renderers.tile.TileNodeRenderer;
import thaumcraft.client.renderers.tile.TileNodeStabilizerRenderer;
import thaumcraft.client.renderers.tile.TilePedestalRenderer;
import thaumcraft.client.renderers.tile.TileResearchTableRenderer;
import thaumcraft.client.renderers.tile.TileRunicMatrixRenderer;
import thaumcraft.client.renderers.tile.TileTableRenderer;
import thaumcraft.client.renderers.tile.TileThaumatoriumRenderer;
import thaumcraft.client.renderers.tile.TileTubeBufferRenderer;
import thaumcraft.client.renderers.tile.TileTubeOnewayRenderer;
import thaumcraft.client.renderers.tile.TileTubeValveRenderer;
import thaumcraft.client.renderers.tile.TileVisRelayRenderer;
import thaumcraft.client.renderers.tile.TileWandPedestalRenderer;
import thaumcraft.client.renderers.tile.TileWardedRenderer;
import thaumcraft.common.CommonProxy;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigEntities;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntityAspectOrb;
import thaumcraft.common.entities.EntityFallingTaint;
import thaumcraft.common.entities.EntityFollowingItem;
import thaumcraft.common.entities.EntityItemGrate;
import thaumcraft.common.entities.EntityPermanentItem;
import thaumcraft.common.entities.EntitySpecialItem;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.EntityGolemBobber;
import thaumcraft.common.entities.golems.EntityTravelingTrunk;
import thaumcraft.common.entities.monster.EntityBrainyZombie;
import thaumcraft.common.entities.monster.EntityCultistCleric;
import thaumcraft.common.entities.monster.EntityCultistKnight;
import thaumcraft.common.entities.monster.EntityEldritchCrab;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.EntityFireBat;
import thaumcraft.common.entities.monster.EntityGiantBrainyZombie;
import thaumcraft.common.entities.monster.EntityInhabitedZombie;
import thaumcraft.common.entities.monster.EntityMindSpider;
import thaumcraft.common.entities.monster.EntityPech;
import thaumcraft.common.entities.monster.EntityTaintChicken;
import thaumcraft.common.entities.monster.EntityTaintCow;
import thaumcraft.common.entities.monster.EntityTaintCreeper;
import thaumcraft.common.entities.monster.EntityTaintPig;
import thaumcraft.common.entities.monster.EntityTaintSheep;
import thaumcraft.common.entities.monster.EntityTaintSpider;
import thaumcraft.common.entities.monster.EntityTaintSpore;
import thaumcraft.common.entities.monster.EntityTaintSporeSwarmer;
import thaumcraft.common.entities.monster.EntityTaintSwarm;
import thaumcraft.common.entities.monster.EntityTaintVillager;
import thaumcraft.common.entities.monster.EntityTaintacle;
import thaumcraft.common.entities.monster.EntityTaintacleSmall;
import thaumcraft.common.entities.monster.EntityThaumicSlime;
import thaumcraft.common.entities.monster.EntityWisp;
import thaumcraft.common.entities.monster.boss.EntityCultistLeader;
import thaumcraft.common.entities.monster.boss.EntityCultistPortal;
import thaumcraft.common.entities.monster.boss.EntityEldritchGolem;
import thaumcraft.common.entities.monster.boss.EntityEldritchWarden;
import thaumcraft.common.entities.monster.boss.EntityTaintacleGiant;
import thaumcraft.common.entities.projectile.EntityAlumentum;
import thaumcraft.common.entities.projectile.EntityBottleTaint;
import thaumcraft.common.entities.projectile.EntityDart;
import thaumcraft.common.entities.projectile.EntityEldritchOrb;
import thaumcraft.common.entities.projectile.EntityEmber;
import thaumcraft.common.entities.projectile.EntityExplosiveOrb;
import thaumcraft.common.entities.projectile.EntityFrostShard;
import thaumcraft.common.entities.projectile.EntityGolemOrb;
import thaumcraft.common.entities.projectile.EntityPechBlast;
import thaumcraft.common.entities.projectile.EntityPrimalArrow;
import thaumcraft.common.entities.projectile.EntityPrimalOrb;
import thaumcraft.common.entities.projectile.EntityShockOrb;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.lib.events.KeyHandler;
import thaumcraft.common.lib.research.PlayerKnowledge;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.tiles.*;

import static tc4tweak.ClientUtils.postponed;
import net.minecraft.util.math.BlockPos;

public class ClientProxy extends CommonProxy {

//   protected PlayerKnowledge playerResearch = new PlayerKnowledge();
//   protected ResearchManager researchManager = new ResearchManager();
//   public WandManager wandManager = new WandManager();
//   private HashMap customIcons = new HashMap<>();

   @Override
   public void openResearchBrowser() {
      net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new thaumcraft.client.gui.GuiResearchBrowser());
   }

   public void registerHandlers() {
      FMLCommonHandler.instance().bus().register(new ClientTickEventsFML());
      MinecraftForge.EVENT_BUS.register(Thaumcraft.instance.renderEventHandler);
      MinecraftForge.EVENT_BUS.register(ConfigBlocks.blockTube);
      MinecraftForge.EVENT_BUS.register(ParticleEngine.instance);
      FMLCommonHandler.instance().bus().register(ParticleEngine.instance);
   }

   public void registerKeyBindings() {
      FMLCommonHandler.instance().bus().register(new KeyHandler());
   }

   public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
      if (world instanceof WorldClient) {
         switch (ID) {
            case 0:
               return new GuiGolem(player, (EntityGolemBase) world.getEntityByID(x));
            case 1:
               return new GuiPech(player.inventory, world, (EntityPech) world.getEntityByID(x));
            case 2:
               return new GuiTravelingTrunk(player, (EntityTravelingTrunk) world.getEntityByID(x));
            case 3:
               return new GuiThaumatorium(player.inventory, (TileThaumatorium)world.getTileEntity(new BlockPos(x, y, z)));
            case 5:
               return new GuiFocusPouch(player.inventory, world, x, y, z);
            case 8:
               return new GuiDeconstructionTable(player.inventory, (TileDeconstructionTable)world.getTileEntity(new BlockPos(x, y, z)));
            case 9:
               return new GuiAlchemyFurnace(player.inventory, (TileAlchemyFurnace)world.getTileEntity(new BlockPos(x, y, z)));
            case 10:
               return new GuiResearchTable(player, (TileResearchTable)world.getTileEntity(new BlockPos(x, y, z)));
            case 12:
               return new GuiResearchBrowser();
            case 13:
               return new GuiArcaneWorkbench(player.inventory, (TileArcaneWorkbench)world.getTileEntity(new BlockPos(x, y, z)));
            case 15:
               return new GuiArcaneBore(player.inventory, (TileArcaneBore)world.getTileEntity(new BlockPos(x, y, z)));
            case 16:
               return new GuiHandMirror(player.inventory, world, x, y, z);
            case 17:
               return new GuiHoverHarness(player.inventory, world, x, y, z);
            case 18:
               return new GuiMagicBox(player.inventory, (TileMagicBox)world.getTileEntity(new BlockPos(x, y, z)));
            case 19:
               return new GuiSpa(player.inventory, (TileSpa)world.getTileEntity(new BlockPos(x, y, z)));
            case 20:
               return new GuiFocalManipulator(player.inventory, (TileFocalManipulator)world.getTileEntity(new BlockPos(x, y, z)));
            default:
               break;
         }
      }

      return null;
   }

   public void registerDisplayInformation() {
      Thaumcraft.instance.aspectShift = FMLClientHandler.instance().hasOptifine();
      if (Loader.isModLoaded("NotEnoughItems")) {
         Thaumcraft.instance.aspectShift = true;
      }

      this.setupItemRenderers();
      this.setupEntityRenderers();
      this.setupBlockRenderers();
      this.setupTileRenderers();
   }

   private void setupItemRenderers() {
      // Bridge 1.7.10 IItemRenderers to 1.12.2 TEISR system
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(ConfigItems.itemThaumometer, new ItemThaumometerRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(ConfigItems.itemWandCasting, new ItemWandRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(ConfigItems.itemTrunkSpawner, new ItemTrunkSpawnerRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(Item.getItemFromBlock(ConfigBlocks.blockChestHungry), new ItemChestHungryRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(Item.getItemFromBlock(ConfigBlocks.blockCrystal), new ItemCrystalRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(Item.getItemFromBlock(ConfigBlocks.blockJar), new ItemJarRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(Item.getItemFromBlock(ConfigBlocks.blockWoodenDevice), new ItemWoodenDeviceRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(Item.getItemFromBlock(ConfigBlocks.blockMetalDevice), new ItemMetalDeviceRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(Item.getItemFromBlock(ConfigBlocks.blockStoneDevice), new ItemStoneDeviceRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(Item.getItemFromBlock(ConfigBlocks.blockTable), new ItemTableRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(Item.getItemFromBlock(ConfigBlocks.blockTube), new ItemTubeRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(Item.getItemFromBlock(ConfigBlocks.blockEssentiaReservoir), new ItemEssentiaReservoirRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(ConfigItems.itemJarFilled, new thaumcraft.client.renderers.tile.ItemJarFilledRenderer());
      thaumcraft.client.renderers.compat.IItemRendererTEISR.register(ConfigItems.itemJarNode, new thaumcraft.client.renderers.tile.ItemJarNodeRenderer());
   }

   @SubscribeEvent
   public void onTextureStitch(net.minecraftforge.client.event.TextureStitchEvent.Pre event) {
      // Register ALL thaumcraft block textures into the atlas.
      // In 1.7.10 this was done via registerBlockIcons/registerIcons calls.
      // In 1.12.2, textures only enter the atlas if referenced by JSON models.
      // TESRs and RenderBlocks need atlas sprites, so register everything.
      try {
         // Scan the JAR/resources for all thaumcraft block textures
         java.io.InputStream is = getClass().getResourceAsStream("/assets/thaumcraft/textures/blocks");
         if (is != null) is.close();
         // Use classloader to find all textures
         java.net.URL url = getClass().getResource("/assets/thaumcraft/textures/blocks");
         if (url != null) {
            String protocol = url.getProtocol();
            if ("jar".equals(protocol)) {
               java.net.JarURLConnection conn = (java.net.JarURLConnection) url.openConnection();
               java.util.jar.JarFile jar = conn.getJarFile();
               java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
               while (entries.hasMoreElements()) {
                  String name = entries.nextElement().getName();
                  if (name.startsWith("assets/thaumcraft/textures/blocks/") && name.endsWith(".png")) {
                     String sprite = name.replace("assets/thaumcraft/textures/", "").replace(".png", "");
                     event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", sprite));
                  }
               }
            } else {
               // Dev environment — scan directory
               java.io.File dir = new java.io.File(url.toURI());
               if (dir.isDirectory()) {
                  for (java.io.File f : dir.listFiles()) {
                     if (f.getName().endsWith(".png")) {
                        String sprite = "blocks/" + f.getName().replace(".png", "");
                        event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", sprite));
                     }
                  }
               }
            }
         }
      } catch (Exception e) {
         // Fallback
         for (int i = 1; i <= 6; i++) {
            event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/crucible" + i));
         }
         event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/metalbase"));
      }
      // Register fluid textures
      event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/fluxgoo"));
      event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/fluxgas"));
      event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/fluidpure"));
      event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/fluiddeath"));
      event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "misc/particlefield"));

      if (ConfigBlocks.blockMetalDevice instanceof thaumcraft.common.blocks.BlockMetalDevice) {
         thaumcraft.common.blocks.BlockMetalDevice bmd = (thaumcraft.common.blocks.BlockMetalDevice) ConfigBlocks.blockMetalDevice;
         bmd.icon[0] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/metalbase"));
         for (int i = 1; i <= 6; i++) {
            bmd.icon[i] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/crucible" + i));
         }
         bmd.icon[7] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/goldbase"));
         bmd.icon[8] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/grate"));
         bmd.icon[9] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/grate_hatch"));
         bmd.icon[10] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_side"));
         bmd.icon[11] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_top"));
         bmd.icon[12] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_grow_side"));
         bmd.icon[13] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_grow_top"));
         bmd.icon[14] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_grow_side_off"));
         bmd.icon[15] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_grow_top_off"));
         bmd.icon[16] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/alchemyblock"));
         bmd.icon[17] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/brainbox"));
         bmd.icon[18] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_fert_side"));
         bmd.icon[19] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_fert_top"));
         bmd.icon[20] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_fert_side_off"));
         bmd.icon[21] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/lamp_fert_top_off"));
         bmd.icon[22] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/alchemyblockadv"));
         bmd.iconGlow = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/animatedglow"));
      }

      if (ConfigBlocks.blockTube instanceof thaumcraft.common.blocks.BlockTube) {
         thaumcraft.common.blocks.BlockTube bt = (thaumcraft.common.blocks.BlockTube) ConfigBlocks.blockTube;
         bt.icon[0] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/pipe_1"));
         bt.icon[1] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/pipe_2"));
         bt.icon[2] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/pipe_3"));
         bt.icon[3] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/pipe_filter"));
         bt.icon[4] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/pipe_filter_core"));
         bt.icon[5] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/pipe_buffer"));
         bt.icon[6] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/pipe_restrict"));
         bt.icon[7] = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/pipe_oneway"));
         bt.iconValve = event.getMap().registerSprite(new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/pipe_valve"));
      }
   }

   @SubscribeEvent
   public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomMeshDefinition(ConfigItems.itemWandCasting, stack -> new ModelResourceLocation("thaumcraft:wandcasting", "inventory"));
        ModelBakery.registerItemVariants(ConfigItems.itemWandCasting, new ModelResourceLocation("thaumcraft:wandcasting", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusPouch, 0, new ModelResourceLocation("thaumcraft:focuspouch", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusFire, 0, new ModelResourceLocation("thaumcraft:focusfire", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusShock, 0, new ModelResourceLocation("thaumcraft:focusshock", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusHellbat, 0, new ModelResourceLocation("thaumcraft:focushellbat", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusFrost, 0, new ModelResourceLocation("thaumcraft:focusfrost", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusTrade, 0, new ModelResourceLocation("thaumcraft:focustrade", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusExcavation, 0, new ModelResourceLocation("thaumcraft:focusexcavation", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusPortableHole, 0, new ModelResourceLocation("thaumcraft:focusportablehole", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusPech, 0, new ModelResourceLocation("thaumcraft:focuspech", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusWarding, 0, new ModelResourceLocation("thaumcraft:focuswarding", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemFocusPrimal, 0, new ModelResourceLocation("thaumcraft:focusprimal", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemEssence, 0, new ModelResourceLocation("thaumcraft:itemessence_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemEssence, 1, new ModelResourceLocation("thaumcraft:itemessence_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemManaBean, 0, new ModelResourceLocation("thaumcraft:itemmanabean", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWispEssence, 0, new ModelResourceLocation("thaumcraft:itemwispessence", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 0, new ModelResourceLocation("thaumcraft:itemresource_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 1, new ModelResourceLocation("thaumcraft:itemresource_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 2, new ModelResourceLocation("thaumcraft:itemresource_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 3, new ModelResourceLocation("thaumcraft:itemresource_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 4, new ModelResourceLocation("thaumcraft:itemresource_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 5, new ModelResourceLocation("thaumcraft:itemresource_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 6, new ModelResourceLocation("thaumcraft:itemresource_6", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 7, new ModelResourceLocation("thaumcraft:itemresource_7", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 8, new ModelResourceLocation("thaumcraft:itemresource_8", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 9, new ModelResourceLocation("thaumcraft:itemresource_9", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 10, new ModelResourceLocation("thaumcraft:itemresource_10", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 11, new ModelResourceLocation("thaumcraft:itemresource_11", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 12, new ModelResourceLocation("thaumcraft:itemresource_12", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 13, new ModelResourceLocation("thaumcraft:itemresource_13", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 14, new ModelResourceLocation("thaumcraft:itemresource_14", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 15, new ModelResourceLocation("thaumcraft:itemresource_15", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 16, new ModelResourceLocation("thaumcraft:itemresource_16", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 17, new ModelResourceLocation("thaumcraft:itemresource_17", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResource, 18, new ModelResourceLocation("thaumcraft:itemresource_18", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShard, 0, new ModelResourceLocation("thaumcraft:itemshard_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShard, 1, new ModelResourceLocation("thaumcraft:itemshard_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShard, 2, new ModelResourceLocation("thaumcraft:itemshard_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShard, 3, new ModelResourceLocation("thaumcraft:itemshard_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShard, 4, new ModelResourceLocation("thaumcraft:itemshard_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShard, 5, new ModelResourceLocation("thaumcraft:itemshard_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShard, 6, new ModelResourceLocation("thaumcraft:itemshard_6", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResearchNotes, 0, new ModelResourceLocation("thaumcraft:itemresearchnotes_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResearchNotes, 24, new ModelResourceLocation("thaumcraft:itemresearchnotes_24", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResearchNotes, 42, new ModelResourceLocation("thaumcraft:itemresearchnotes_42", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResearchNotes, 64, new ModelResourceLocation("thaumcraft:itemresearchnotes_64", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemInkwell, 0, new ModelResourceLocation("thaumcraft:iteminkwell", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemThaumonomicon, 0, new ModelResourceLocation("thaumcraft:itemthaumonomicon_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemThaumonomicon, 42, new ModelResourceLocation("thaumcraft:itemthaumonomicon_42", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemThaumometer, 0, new ModelResourceLocation("thaumcraft:itemthaumometer", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGoggles, 0, new ModelResourceLocation("thaumcraft:itemgoggles", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHelmetThaumium, 0, new ModelResourceLocation("thaumcraft:itemhelmetthaumium", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemChestThaumium, 0, new ModelResourceLocation("thaumcraft:itemchestplatethaumium", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLegsThaumium, 0, new ModelResourceLocation("thaumcraft:itemleggingsthaumium", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBootsThaumium, 0, new ModelResourceLocation("thaumcraft:itembootsthaumium", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShovelThaumium, 0, new ModelResourceLocation("thaumcraft:itemshovelthaumium", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPickThaumium, 0, new ModelResourceLocation("thaumcraft:itempickthaumium", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemAxeThaumium, 0, new ModelResourceLocation("thaumcraft:itemaxethaumium", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemSwordThaumium, 0, new ModelResourceLocation("thaumcraft:itemswordthaumium", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHoeThaumium, 0, new ModelResourceLocation("thaumcraft:itemhoethaumium", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemArcaneDoor, 0, new ModelResourceLocation("thaumcraft:itemarcanedoor", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 0, new ModelResourceLocation("thaumcraft:itemnugget_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 1, new ModelResourceLocation("thaumcraft:itemnugget_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 2, new ModelResourceLocation("thaumcraft:itemnugget_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 3, new ModelResourceLocation("thaumcraft:itemnugget_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 4, new ModelResourceLocation("thaumcraft:itemnugget_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 5, new ModelResourceLocation("thaumcraft:itemnugget_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 6, new ModelResourceLocation("thaumcraft:itemnugget_6", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 7, new ModelResourceLocation("thaumcraft:itemnugget_7", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 16, new ModelResourceLocation("thaumcraft:itemnugget_16", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 17, new ModelResourceLocation("thaumcraft:itemnugget_17", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 18, new ModelResourceLocation("thaumcraft:itemnugget_18", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 19, new ModelResourceLocation("thaumcraft:itemnugget_19", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 20, new ModelResourceLocation("thaumcraft:itemnugget_20", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 21, new ModelResourceLocation("thaumcraft:itemnugget_21", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNugget, 31, new ModelResourceLocation("thaumcraft:itemnugget_31", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBootsTraveller, 0, new ModelResourceLocation("thaumcraft:bootstraveller", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNuggetChicken, 0, new ModelResourceLocation("thaumcraft:itemnuggetchicken", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNuggetBeef, 0, new ModelResourceLocation("thaumcraft:itemnuggetbeef", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNuggetPork, 0, new ModelResourceLocation("thaumcraft:itemnuggetpork", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemNuggetFish, 0, new ModelResourceLocation("thaumcraft:itemnuggetfish", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemTripleMeatTreat, 0, new ModelResourceLocation("thaumcraft:triplemeattreat", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemSwordElemental, 0, new ModelResourceLocation("thaumcraft:itemswordelemental", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShovelElemental, 0, new ModelResourceLocation("thaumcraft:itemshovelelemental", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPickElemental, 0, new ModelResourceLocation("thaumcraft:itempickaxeelemental", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemAxeElemental, 0, new ModelResourceLocation("thaumcraft:itemaxeelemental", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHoeElemental, 0, new ModelResourceLocation("thaumcraft:itemhoeelemental", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemChestRobe, 0, new ModelResourceLocation("thaumcraft:itemchestplaterobe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLegsRobe, 0, new ModelResourceLocation("thaumcraft:itemleggingsrobe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBootsRobe, 0, new ModelResourceLocation("thaumcraft:itembootsrobe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemKey, 0, new ModelResourceLocation("thaumcraft:arcanedoorkey_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemKey, 1, new ModelResourceLocation("thaumcraft:arcanedoorkey_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHandMirror, 0, new ModelResourceLocation("thaumcraft:handmirror", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHoverHarness, 0, new ModelResourceLocation("thaumcraft:hoverharness", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemJarFilled, 0, new ModelResourceLocation("thaumcraft:blockjarfilleditem", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemJarNode, 0, new ModelResourceLocation("thaumcraft:blockjarnodeitem", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemTrunkSpawner, 0, new ModelResourceLocation("thaumcraft:trunkspawner", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemPlacer, 0, new ModelResourceLocation("thaumcraft:itemgolemplacer_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemPlacer, 1, new ModelResourceLocation("thaumcraft:itemgolemplacer_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemPlacer, 2, new ModelResourceLocation("thaumcraft:itemgolemplacer_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemPlacer, 3, new ModelResourceLocation("thaumcraft:itemgolemplacer_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemPlacer, 4, new ModelResourceLocation("thaumcraft:itemgolemplacer_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemPlacer, 5, new ModelResourceLocation("thaumcraft:itemgolemplacer_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemPlacer, 6, new ModelResourceLocation("thaumcraft:itemgolemplacer_6", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemPlacer, 7, new ModelResourceLocation("thaumcraft:itemgolemplacer_7", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 0, new ModelResourceLocation("thaumcraft:itemgolemcore_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 1, new ModelResourceLocation("thaumcraft:itemgolemcore_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 2, new ModelResourceLocation("thaumcraft:itemgolemcore_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 3, new ModelResourceLocation("thaumcraft:itemgolemcore_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 4, new ModelResourceLocation("thaumcraft:itemgolemcore_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 5, new ModelResourceLocation("thaumcraft:itemgolemcore_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 6, new ModelResourceLocation("thaumcraft:itemgolemcore_6", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 7, new ModelResourceLocation("thaumcraft:itemgolemcore_7", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 8, new ModelResourceLocation("thaumcraft:itemgolemcore_8", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 9, new ModelResourceLocation("thaumcraft:itemgolemcore_9", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 10, new ModelResourceLocation("thaumcraft:itemgolemcore_10", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 11, new ModelResourceLocation("thaumcraft:itemgolemcore_11", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemCore, 100, new ModelResourceLocation("thaumcraft:itemgolemcore_100", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemUpgrade, 0, new ModelResourceLocation("thaumcraft:itemgolemupgrade_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemUpgrade, 1, new ModelResourceLocation("thaumcraft:itemgolemupgrade_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemUpgrade, 2, new ModelResourceLocation("thaumcraft:itemgolemupgrade_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemUpgrade, 3, new ModelResourceLocation("thaumcraft:itemgolemupgrade_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemUpgrade, 4, new ModelResourceLocation("thaumcraft:itemgolemupgrade_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemUpgrade, 5, new ModelResourceLocation("thaumcraft:itemgolemupgrade_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemBell, 0, new ModelResourceLocation("thaumcraft:golembell", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemDecoration, 0, new ModelResourceLocation("thaumcraft:itemgolemdecoration_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemDecoration, 1, new ModelResourceLocation("thaumcraft:itemgolemdecoration_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemDecoration, 2, new ModelResourceLocation("thaumcraft:itemgolemdecoration_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemDecoration, 3, new ModelResourceLocation("thaumcraft:itemgolemdecoration_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemDecoration, 4, new ModelResourceLocation("thaumcraft:itemgolemdecoration_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemDecoration, 5, new ModelResourceLocation("thaumcraft:itemgolemdecoration_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemDecoration, 6, new ModelResourceLocation("thaumcraft:itemgolemdecoration_6", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGolemDecoration, 7, new ModelResourceLocation("thaumcraft:itemgolemdecoration_7", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBowBone, 0, new ModelResourceLocation("thaumcraft:itembowbone", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPrimalArrow, 0, new ModelResourceLocation("thaumcraft:primalarrow_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPrimalArrow, 1, new ModelResourceLocation("thaumcraft:primalarrow_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPrimalArrow, 2, new ModelResourceLocation("thaumcraft:primalarrow_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPrimalArrow, 3, new ModelResourceLocation("thaumcraft:primalarrow_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPrimalArrow, 4, new ModelResourceLocation("thaumcraft:primalarrow_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPrimalArrow, 5, new ModelResourceLocation("thaumcraft:primalarrow_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemResonator, 0, new ModelResourceLocation("thaumcraft:itemresonator", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBaubleBlanks, 0, new ModelResourceLocation("thaumcraft:itembaubleblanks_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBaubleBlanks, 1, new ModelResourceLocation("thaumcraft:itembaubleblanks_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBaubleBlanks, 2, new ModelResourceLocation("thaumcraft:itembaubleblanks_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBaubleBlanks, 3, new ModelResourceLocation("thaumcraft:itembaubleblanks_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBaubleBlanks, 4, new ModelResourceLocation("thaumcraft:itembaubleblanks_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBaubleBlanks, 5, new ModelResourceLocation("thaumcraft:itembaubleblanks_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBaubleBlanks, 6, new ModelResourceLocation("thaumcraft:itembaubleblanks_6", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBaubleBlanks, 7, new ModelResourceLocation("thaumcraft:itembaubleblanks_7", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBaubleBlanks, 8, new ModelResourceLocation("thaumcraft:itembaubleblanks_8", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemAmuletRunic, 0, new ModelResourceLocation("thaumcraft:itemamuletrunic_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemAmuletRunic, 1, new ModelResourceLocation("thaumcraft:itemamuletrunic_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemRingRunic, 0, new ModelResourceLocation("thaumcraft:itemringrunic_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemRingRunic, 1, new ModelResourceLocation("thaumcraft:itemringrunic_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemRingRunic, 2, new ModelResourceLocation("thaumcraft:itemringrunic_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemRingRunic, 3, new ModelResourceLocation("thaumcraft:itemringrunic_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGirdleRunic, 0, new ModelResourceLocation("thaumcraft:itemgirdlerunic_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGirdleRunic, 1, new ModelResourceLocation("thaumcraft:itemgirdlerunic_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemAmuletVis, 0, new ModelResourceLocation("thaumcraft:itemamuletvis_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemAmuletVis, 1, new ModelResourceLocation("thaumcraft:itemamuletvis_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemGirdleHover, 0, new ModelResourceLocation("thaumcraft:itemgirdlehover", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemSpawnerEgg, 0, new ModelResourceLocation("thaumcraft:itemspawneregg", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemZombieBrain, 0, new ModelResourceLocation("thaumcraft:itemzombiebrain", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBathSalts, 0, new ModelResourceLocation("thaumcraft:itembathsalts", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemCrystalEssence, 0, new ModelResourceLocation("thaumcraft:itemcrystalessence", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBucketDeath, 0, new ModelResourceLocation("thaumcraft:itembucketdeath", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBucketPure, 0, new ModelResourceLocation("thaumcraft:itembucketpure", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHelmetFortress, 0, new ModelResourceLocation("thaumcraft:itemhelmetfortress", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemChestFortress, 0, new ModelResourceLocation("thaumcraft:itemchestplatefortress", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLegsFortress, 0, new ModelResourceLocation("thaumcraft:itemleggingsfortress", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemEldritchObject, 0, new ModelResourceLocation("thaumcraft:itemeldritchobject_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemEldritchObject, 1, new ModelResourceLocation("thaumcraft:itemeldritchobject_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemEldritchObject, 2, new ModelResourceLocation("thaumcraft:itemeldritchobject_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemEldritchObject, 3, new ModelResourceLocation("thaumcraft:itemeldritchobject_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemEldritchObject, 4, new ModelResourceLocation("thaumcraft:itemeldritchobject_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHelmetVoid, 0, new ModelResourceLocation("thaumcraft:itemhelmetvoid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemChestVoid, 0, new ModelResourceLocation("thaumcraft:itemchestplatevoid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLegsVoid, 0, new ModelResourceLocation("thaumcraft:itemleggingsvoid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBootsVoid, 0, new ModelResourceLocation("thaumcraft:itembootsvoid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemShovelVoid, 0, new ModelResourceLocation("thaumcraft:itemshovelvoid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPickVoid, 0, new ModelResourceLocation("thaumcraft:itempickvoid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemAxeVoid, 0, new ModelResourceLocation("thaumcraft:itemaxevoid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemSwordVoid, 0, new ModelResourceLocation("thaumcraft:itemswordvoid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHoeVoid, 0, new ModelResourceLocation("thaumcraft:itemhoevoid", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHelmetVoidRobe, 0, new ModelResourceLocation("thaumcraft:itemhelmetvoidfortress", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemChestVoidRobe, 0, new ModelResourceLocation("thaumcraft:itemchestplatevoidfortress", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLegsVoidRobe, 0, new ModelResourceLocation("thaumcraft:itemleggingsvoidfortress", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemSanitySoap, 0, new ModelResourceLocation("thaumcraft:itemsanitysoap", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemSanityChecker, 0, new ModelResourceLocation("thaumcraft:itemsanitychecker", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBottleTaint, 0, new ModelResourceLocation("thaumcraft:itembottletaint", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHelmetCultistRobe, 0, new ModelResourceLocation("thaumcraft:itemhelmetcultistrobe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemChestCultistRobe, 0, new ModelResourceLocation("thaumcraft:itemchestplatecultistrobe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLegsCultistRobe, 0, new ModelResourceLocation("thaumcraft:itemleggingscultistrobe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemBootsCultist, 0, new ModelResourceLocation("thaumcraft:itembootscultist", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHelmetCultistPlate, 0, new ModelResourceLocation("thaumcraft:itemhelmetcultistplate", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemChestCultistPlate, 0, new ModelResourceLocation("thaumcraft:itemchestplatecultistplate", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLegsCultistPlate, 0, new ModelResourceLocation("thaumcraft:itemleggingscultistplate", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemHelmetCultistLeaderPlate, 0, new ModelResourceLocation("thaumcraft:itemhelmetcultistleaderplate", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemChestCultistLeaderPlate, 0, new ModelResourceLocation("thaumcraft:itemchestplatecultistleaderplate", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLegsCultistLeaderPlate, 0, new ModelResourceLocation("thaumcraft:itemleggingscultistleaderplate", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemSwordCrimson, 0, new ModelResourceLocation("thaumcraft:itemswordcrimson", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLootbag, 0, new ModelResourceLocation("thaumcraft:itemlootbag_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLootbag, 1, new ModelResourceLocation("thaumcraft:itemlootbag_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemLootbag, 2, new ModelResourceLocation("thaumcraft:itemlootbag_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemCompassStone, 0, new ModelResourceLocation("thaumcraft:itemcompassstone_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemCompassStone, 1, new ModelResourceLocation("thaumcraft:itemcompassstone_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemPrimalCrusher, 0, new ModelResourceLocation("thaumcraft:itemprimalcrusher", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandCap, 0, new ModelResourceLocation("thaumcraft:wandcap_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandCap, 1, new ModelResourceLocation("thaumcraft:wandcap_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandCap, 2, new ModelResourceLocation("thaumcraft:wandcap_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandCap, 3, new ModelResourceLocation("thaumcraft:wandcap_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandCap, 4, new ModelResourceLocation("thaumcraft:wandcap_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandCap, 5, new ModelResourceLocation("thaumcraft:wandcap_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandCap, 6, new ModelResourceLocation("thaumcraft:wandcap_6", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandCap, 7, new ModelResourceLocation("thaumcraft:wandcap_7", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandCap, 8, new ModelResourceLocation("thaumcraft:wandcap_8", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 0, new ModelResourceLocation("thaumcraft:wandrod_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 1, new ModelResourceLocation("thaumcraft:wandrod_1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 2, new ModelResourceLocation("thaumcraft:wandrod_2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 3, new ModelResourceLocation("thaumcraft:wandrod_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 4, new ModelResourceLocation("thaumcraft:wandrod_4", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 5, new ModelResourceLocation("thaumcraft:wandrod_5", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 6, new ModelResourceLocation("thaumcraft:wandrod_6", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 7, new ModelResourceLocation("thaumcraft:wandrod_7", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 50, new ModelResourceLocation("thaumcraft:wandrod_50", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 51, new ModelResourceLocation("thaumcraft:wandrod_51", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 52, new ModelResourceLocation("thaumcraft:wandrod_52", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 53, new ModelResourceLocation("thaumcraft:wandrod_53", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 54, new ModelResourceLocation("thaumcraft:wandrod_54", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 55, new ModelResourceLocation("thaumcraft:wandrod_55", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 56, new ModelResourceLocation("thaumcraft:wandrod_56", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 57, new ModelResourceLocation("thaumcraft:wandrod_57", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ConfigItems.itemWandRod, 100, new ModelResourceLocation("thaumcraft:wandrod_100", "inventory"));
        // Block items
        // Fluid blocks - no custom state mapper needed, blockstate JSON has all level variants
        // BlockCustomOre variants
        String[] oreNames = {"cinnabar","infusedair","infusedfire","infusedwater","infusedearth","infusedorder","infusedentropy","amber"};
        for (int i = 0; i < oreNames.length; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockCustomOre), i, new ModelResourceLocation("thaumcraft:blockcustomore_" + oreNames[i], "inventory"));
        }
        // BlockMagicalLog variants
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockMagicalLog), 0, new ModelResourceLocation("thaumcraft:blockmagicallog", "axis=y,variant=greatwood"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockMagicalLog), 1, new ModelResourceLocation("thaumcraft:blockmagicallog", "axis=y,variant=silverwood"));
        // BlockMagicalLeaves variants
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockMagicalLeaves), 0, new ModelResourceLocation("thaumcraft:blockmagicalleaves", "variant=greatwood"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockMagicalLeaves), 1, new ModelResourceLocation("thaumcraft:blockmagicalleaves", "variant=silverwood"));
        // BlockCustomPlant variants
        String[] plantNames = {"greatwoodsap","silverwoodsap","shimmerleaf","cinderpearl","etherealbloom","manashroom"};
        for (int i = 0; i < plantNames.length; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockCustomPlant), i, new ModelResourceLocation("thaumcraft:blockcustomplant", "variant=" + plantNames[i]));
        }
        // Taint per-meta models
        for (int i = 0; i <= 2; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockTaint), i, new ModelResourceLocation("thaumcraft:blocktaint_" + i, "inventory"));
        }
        for (int i = 0; i <= 4; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockTaintFibres), i, new ModelResourceLocation("thaumcraft:blocktaintfibres_" + i, "inventory"));
        }
        // BlockCosmeticOpaque variants
        String[] opaqueNames = {"amber","amberbrick","wardedglass"};
        for (int i = 0; i < opaqueNames.length; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockCosmeticOpaque), i, new ModelResourceLocation("thaumcraft:blockcosmeticopaque", "variant=" + opaqueNames[i]));
        }
        // BlockCosmeticSolid variants
        int[] solidMetas = {0,1,2,3,4,5,6,7,8,9,11,12,14,15};
        String[] solidNames = {"obsidiantotem","obsidiantile","pavingtravel","pavingwarding","thaumiumblock","tallowblock","arcanestone","arcanestonebrick","obsidiantotemcharged","golemfetter","ancientstone","ancientrock","crustedstone","pedestalstone"};
        for (int i = 0; i < solidMetas.length; i++) {
            String model = solidMetas[i] == 14 ? "thaumcraft:blockcosmeticsolid_crustedstone" : "thaumcraft:blockcosmeticsolid";
            String variant = solidMetas[i] == 14 ? "inventory" : "variant=" + solidNames[i];
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockCosmeticSolid), solidMetas[i], new ModelResourceLocation(model, variant));
        }
        for (int i = 0; i <= 7; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockCrystal), i, new ModelResourceLocation("thaumcraft:blockcrystal_" + i, "inventory"));
        }
        // Tube per-meta models
        for (int i = 0; i <= 7; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockTube), i, new ModelResourceLocation("thaumcraft:blocktube_" + i, "inventory"));
        }
        // MetalDevice per-meta models
        for (int i : new int[]{0,1,2,3,5,7,8,9,12,13,14}) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockMetalDevice), i, new ModelResourceLocation("thaumcraft:blockmetaldevice_" + i, "inventory"));
        }
        // WoodenDevice per-meta models
        for (int i : new int[]{0,1,2,4,5,6,7,8}) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockWoodenDevice), i, new ModelResourceLocation("thaumcraft:blockwoodendevice_" + i, "inventory"));
        }
        // StoneDevice per-meta models
        for (int i : new int[]{0,1,2,5,8,9,10,11,12,13,14}) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockStoneDevice), i, new ModelResourceLocation("thaumcraft:blockstonedevice_" + i, "inventory"));
        }
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockMirror), 0, new ModelResourceLocation("thaumcraft:blockmirror", "normal"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockMirror), 6, new ModelResourceLocation("thaumcraft:blockmirror", "normal"));
        for (int i : new int[]{0, 14, 15}) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockTable), i, new ModelResourceLocation("thaumcraft:blocktable", "normal"));
        }
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockChestHungry), 0, new ModelResourceLocation("thaumcraft:blockchesthungry", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockLifter), 0, new ModelResourceLocation("thaumcraft:blocklifter", "normal"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockMagicBox), 0, new ModelResourceLocation("thaumcraft:blockmagicbox", "normal"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockAlchemyFurnace), 0, new ModelResourceLocation("thaumcraft:blockalchemyfurnace", "normal"));
        for (int i : new int[]{0, 1, 3}) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockJar), i, new ModelResourceLocation("thaumcraft:blockjar_" + i, "inventory"));
        }
        for (int i = 0; i < 16; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockCandle), i, new ModelResourceLocation("thaumcraft:blockcandle", "normal"));
        }
        // Eldritch per-meta models
        for (int i = 0; i <= 10; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockEldritch), i, new ModelResourceLocation("thaumcraft:blockeldritch_" + i, "inventory"));
        }
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockAiry), 0, new ModelResourceLocation("thaumcraft:blockairy_0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockManaPod), 0, new ModelResourceLocation("thaumcraft:blockmanapod", "normal"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockArcaneFurnace), 0, new ModelResourceLocation("thaumcraft:blockarcanefurnace", "normal"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockWarded), 0, new ModelResourceLocation("thaumcraft:blockwarded", "normal"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockEssentiaReservoir), 0, new ModelResourceLocation("thaumcraft:blockessentiareservoir", "normal"));
        // LootUrn/Crate per-meta models
        for (int i = 0; i <= 2; i++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockLootUrn), i, new ModelResourceLocation("thaumcraft:blocklooturn_" + i, "inventory"));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockLootCrate), i, new ModelResourceLocation("thaumcraft:blocklootcrate_" + i, "inventory"));
        }
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockStairsArcaneStone), 0, new ModelResourceLocation("thaumcraft:blockstairsarcanestone", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockStairsGreatwood), 0, new ModelResourceLocation("thaumcraft:blockstairsgreatwood", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockStairsSilverwood), 0, new ModelResourceLocation("thaumcraft:blockstairssilverwood", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockStairsEldritch), 0, new ModelResourceLocation("thaumcraft:blockstairseldritch", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockSlabStone), 0, new ModelResourceLocation("thaumcraft:blockcosmeticslabstone", "half=bottom,variant=arcane"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockSlabStone), 1, new ModelResourceLocation("thaumcraft:blockcosmeticslabstone", "half=bottom,variant=eldritch"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockSlabWood), 0, new ModelResourceLocation("thaumcraft:blockcosmeticslabwood", "half=bottom,variant=greatwood"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ConfigBlocks.blockSlabWood), 1, new ModelResourceLocation("thaumcraft:blockcosmeticslabwood", "half=bottom,variant=silverwood"));
   }

   @SubscribeEvent
   public void onModelBake(net.minecraftforge.client.event.ModelBakeEvent event) {
      ModelResourceLocation thaumometerModel = new ModelResourceLocation("thaumcraft:itemthaumometer", "inventory");
      IBakedModel bakedModel = event.getModelRegistry().getObject(thaumometerModel);
      if (bakedModel != null) {
         event.getModelRegistry().putObject(thaumometerModel, new TransformTrackingModel(bakedModel));
      }
   }

   private void setupEntityRenderers() {
      RenderingRegistry.registerEntityRenderingHandler(EntityItemGrate.class, (IRenderFactory<EntityItemGrate>) manager -> new net.minecraft.client.renderer.entity.RenderEntityItem(manager, net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));
      RenderingRegistry.registerEntityRenderingHandler(EntitySpecialItem.class, (IRenderFactory<EntitySpecialItem>) manager -> new RenderSpecialItem(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityFollowingItem.class, (IRenderFactory<EntityFollowingItem>) manager -> new RenderFollowingItem(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityPermanentItem.class, (IRenderFactory<EntityPermanentItem>) manager -> new RenderSpecialItem(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityAspectOrb.class, (IRenderFactory<EntityAspectOrb>) manager -> new RenderAspectOrb(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityGolemBobber.class, (IRenderFactory<EntityGolemBobber>) manager -> new RenderGolemBobber(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityGolemBase.class, (IRenderFactory<EntityGolemBase>) manager -> new RenderGolemBase(manager, new ModelGolem(false)));
      RenderingRegistry.registerEntityRenderingHandler(EntityWisp.class, (IRenderFactory<EntityWisp>) manager -> new RenderWisp(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityAlumentum.class, (IRenderFactory<EntityAlumentum>) manager -> new RenderAlumentum(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityPrimalOrb.class, (IRenderFactory<EntityPrimalOrb>) manager -> new RenderPrimalOrb(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityEldritchOrb.class, (IRenderFactory<EntityEldritchOrb>) manager -> new RenderEldritchOrb(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityGolemOrb.class, (IRenderFactory<EntityGolemOrb>) manager -> new RenderElectricOrb(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityEmber.class, (IRenderFactory<EntityEmber>) manager -> new RenderEmber(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityShockOrb.class, (IRenderFactory<EntityShockOrb>) manager -> new RenderElectricOrb(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityExplosiveOrb.class, (IRenderFactory<EntityExplosiveOrb>) manager -> new RenderExplosiveOrb(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityPechBlast.class, (IRenderFactory<EntityPechBlast>) manager -> new RenderPechBlast(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityBrainyZombie.class, (IRenderFactory<EntityBrainyZombie>) manager -> new RenderBrainyZombie(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityInhabitedZombie.class, (IRenderFactory<EntityInhabitedZombie>) manager -> new RenderInhabitedZombie(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityGiantBrainyZombie.class, (IRenderFactory<EntityGiantBrainyZombie>) manager -> new RenderBrainyZombie(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityPech.class, (IRenderFactory<EntityPech>) manager -> new RenderPech(manager, new ModelPech(), 0.25F));
      RenderingRegistry.registerEntityRenderingHandler(EntityFireBat.class, (IRenderFactory<EntityFireBat>) manager -> new RenderFireBat(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityFrostShard.class, (IRenderFactory<EntityFrostShard>) manager -> new RenderFrostShard(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityDart.class, (IRenderFactory<EntityDart>) manager -> new RenderDart(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityPrimalArrow.class, (IRenderFactory<EntityPrimalArrow>) manager -> new RenderPrimalArrow(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityFallingTaint.class, (IRenderFactory<EntityFallingTaint>) manager -> new RenderFallingTaint(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityThaumicSlime.class, (IRenderFactory<EntityThaumicSlime>) manager -> new RenderThaumicSlime(manager, new ModelSlime(16), new ModelSlime(0), 0.25F));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintSpider.class, (IRenderFactory<EntityTaintSpider>) manager -> new RenderTaintSpider(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintacle.class, (IRenderFactory<EntityTaintacle>) manager -> new RenderTaintacle(manager, 0.6F, 10));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintacleSmall.class, (IRenderFactory<EntityTaintacleSmall>) manager -> new RenderTaintacle(manager, 0.2F, 6));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintacleGiant.class, (IRenderFactory<EntityTaintacleGiant>) manager -> new RenderTaintacle(manager, 1.0F, 14));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintSpore.class, (IRenderFactory<EntityTaintSpore>) manager -> new RenderTaintSpore(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintSporeSwarmer.class, (IRenderFactory<EntityTaintSporeSwarmer>) manager -> new RenderTaintSporeSwarmer(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintSwarm.class, (IRenderFactory<EntityTaintSwarm>) manager -> new RenderTaintSwarm(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintChicken.class, (IRenderFactory<EntityTaintChicken>) manager -> new RenderTaintChicken(manager, new ModelChicken(), 0.3F));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintCow.class, (IRenderFactory<EntityTaintCow>) manager -> new RenderTaintCow(manager, new ModelCow(), 0.7F));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintCreeper.class, (IRenderFactory<EntityTaintCreeper>) manager -> new RenderTaintCreeper(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintPig.class, (IRenderFactory<EntityTaintPig>) manager -> new RenderTaintPig(manager, new ModelPig(), 0.7F));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintSheep.class, (IRenderFactory<EntityTaintSheep>) manager -> new RenderTaintSheep(manager, new ModelTaintSheep2(), new ModelTaintSheep1(), 0.7F));
      RenderingRegistry.registerEntityRenderingHandler(EntityTaintVillager.class, (IRenderFactory<EntityTaintVillager>) manager -> new RenderTaintVillager(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityTravelingTrunk.class, (IRenderFactory<EntityTravelingTrunk>) manager -> new RenderTravelingTrunk(manager, new ModelTrunk(), 0.5F));
      RenderingRegistry.registerEntityRenderingHandler(EntityMindSpider.class, (IRenderFactory<EntityMindSpider>) manager -> new RenderMindSpider(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityEldritchGuardian.class, (IRenderFactory<EntityEldritchGuardian>) manager -> new RenderEldritchGuardian(manager, new ModelEldritchGuardian(), 0.5F));
      RenderingRegistry.registerEntityRenderingHandler(EntityEldritchWarden.class, (IRenderFactory<EntityEldritchWarden>) manager -> new RenderEldritchGuardian(manager, new ModelEldritchGuardian(), 0.6F));
      RenderingRegistry.registerEntityRenderingHandler(EntityCultistPortal.class, (IRenderFactory<EntityCultistPortal>) manager -> new RenderCultistPortal(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityCultistKnight.class, (IRenderFactory<EntityCultistKnight>) manager -> new RenderCultist(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityCultistLeader.class, (IRenderFactory<EntityCultistLeader>) manager -> new RenderCultist(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityCultistCleric.class, (IRenderFactory<EntityCultistCleric>) manager -> new RenderCultist(manager));
      RenderingRegistry.registerEntityRenderingHandler(EntityEldritchGolem.class, (IRenderFactory<EntityEldritchGolem>) manager -> new RenderEldritchGolem(manager, new ModelEldritchGolem(), 0.5F));
      RenderingRegistry.registerEntityRenderingHandler(EntityBottleTaint.class, (IRenderFactory<EntityBottleTaint>) manager -> new RenderSnowball(manager, ConfigItems.itemBottleTaint, net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));
      RenderingRegistry.registerEntityRenderingHandler(EntityEldritchCrab.class, (IRenderFactory<EntityEldritchCrab>) manager -> new RenderEldritchCrab(manager));
      // VillagerRegistry.instance().registerVillagerSkin(ConfigEntities.entWizardId, new ResourceLocation("thaumcraft", "textures/models/wizard.png"));
      // VillagerRegistry.instance().registerVillagerSkin(ConfigEntities.entBankerId, new ResourceLocation("thaumcraft", "textures/models/moneychanger.png"));
   }

   void setupTileRenderers() {
      this.registerTileEntitySpecialRenderer(TileAlembic.class, new TileAlembicRenderer());
      this.registerTileEntitySpecialRenderer(TileArcaneBore.class, new TileArcaneBoreRenderer());
      this.registerTileEntitySpecialRenderer(TileArcaneBoreBase.class, new TileArcaneBoreBaseRenderer());
      this.registerTileEntitySpecialRenderer(TileArcaneLamp.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockMetalDeviceRenderer()));
      this.registerTileEntitySpecialRenderer(TileArcaneLampGrowth.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockMetalDeviceRenderer()));
      this.registerTileEntitySpecialRenderer(TileArcaneLampFertility.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockMetalDeviceRenderer()));
      this.registerTileEntitySpecialRenderer(TileArcaneWorkbench.class, new TileArcaneWorkbenchRenderer());
      this.registerTileEntitySpecialRenderer(TileArcaneFurnace.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockArcaneFurnaceRenderer()));
      this.registerTileEntitySpecialRenderer(TileArcaneFurnacePart.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockArcaneFurnaceRenderer()));
      this.registerTileEntitySpecialRenderer(TileArcaneFurnaceNozzle.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockArcaneFurnaceRenderer()));
      this.registerTileEntitySpecialRenderer(TileBanner.class, new TileBannerRenderer());
      this.registerTileEntitySpecialRenderer(TileBrainbox.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockMetalDeviceRenderer()));
      this.registerTileEntitySpecialRenderer(TileMetalDevice.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockMetalDeviceRenderer()));
      this.registerTileEntitySpecialRenderer(TileBellows.class, new TileBellowsRenderer());
      this.registerTileEntitySpecialRenderer(TileSensor.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockWoodenDeviceRenderer()));
      this.registerTileEntitySpecialRenderer(TileArcanePressurePlate.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockWoodenDeviceRenderer()));
      this.registerTileEntitySpecialRenderer(thaumcraft.common.tiles.TilePlank.class, new thaumcraft.client.renderers.tile.TilePlankRenderer());
      this.registerTileEntitySpecialRenderer(TileCentrifuge.class, new TileCentrifugeRenderer());
      this.registerTileEntitySpecialRenderer(TileChestHungry.class, new TileChestHungryRenderer());
      this.registerTileEntitySpecialRenderer(TileCrucible.class, new TileCrucibleRenderer());
      this.registerTileEntitySpecialRenderer(TileCrystal.class, new TileCrystalRenderer());
      this.registerTileEntitySpecialRenderer(TileEldritchCrystal.class, new TileEldritchCrystalRenderer());
      this.registerTileEntitySpecialRenderer(TileDeconstructionTable.class, new TileDeconstructionTableRenderer());
      this.registerTileEntitySpecialRenderer(TileEldritchAltar.class, new TileEldritchCapRenderer("textures/models/obelisk_cap_altar.png"));
      this.registerTileEntitySpecialRenderer(TileEldritchCap.class, new TileEldritchCapRenderer("textures/models/obelisk_cap.png"));
      this.registerTileEntitySpecialRenderer(TileEldritchStone.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockEldritchRenderer()));
      this.registerTileEntitySpecialRenderer(TileEldritchDoorway.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockEldritchRenderer()));
      this.registerTileEntitySpecialRenderer(TileEldritchCrabSpawner.class, new TileEldritchCrabSpawnerRenderer());
      this.registerTileEntitySpecialRenderer(TileEldritchObelisk.class, new TileEldritchObeliskRenderer());
      this.registerTileEntitySpecialRenderer(TileEldritchPortal.class, new TileEldritchPortalRenderer());
      this.registerTileEntitySpecialRenderer(TileEldritchLock.class, new TileEldritchLockCombinedRenderer());
      this.registerTileEntitySpecialRenderer(TileEldritchTrap.class, new thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR<>(new BlockEldritchRenderer()));
      this.registerTileEntitySpecialRenderer(TileEssentiaCrystalizer.class, new TileEssentiaCrystalizerRenderer());
      this.registerTileEntitySpecialRenderer(TileEssentiaReservoir.class, new TileEssentiaReservoirRenderer());
      this.registerTileEntitySpecialRenderer(TileEtherealBloom.class, new TileEtherealBloomRenderer());
      this.registerTileEntitySpecialRenderer(TileHole.class, new TileHoleRenderer());
      this.registerTileEntitySpecialRenderer(TileInfusionMatrix.class, new TileRunicMatrixRenderer(0));
      this.registerTileEntitySpecialRenderer(TileInfusionPillar.class, new TileInfusionPillarRenderer());
      this.registerTileEntitySpecialRenderer(TileJar.class, new TileJarRenderer());
      this.registerTileEntitySpecialRenderer(TileMagicWorkbenchCharger.class, new TileMagicWorkbenchChargerRenderer());
      this.registerTileEntitySpecialRenderer(TileManaPod.class, new TileManaPodRenderer());
      TileMirrorRenderer tmr = new TileMirrorRenderer();
      this.registerTileEntitySpecialRenderer(TileMirror.class, tmr);
      this.registerTileEntitySpecialRenderer(TileMirrorEssentia.class, tmr);
      this.registerTileEntitySpecialRenderer(TileNode.class, new TileNodeRenderer());
      this.registerTileEntitySpecialRenderer(TileNodeEnergized.class, new TileNodeEnergizedRenderer());
      this.registerTileEntitySpecialRenderer(TileNodeConverter.class, new TileNodeConverterRenderer());
      this.registerTileEntitySpecialRenderer(TileNodeStabilizer.class, new TileNodeStabilizerRenderer());
      this.registerTileEntitySpecialRenderer(TilePedestal.class, new TilePedestalRenderer());
      this.registerTileEntitySpecialRenderer(TileResearchTable.class, new TileResearchTableRenderer());
      this.registerTileEntitySpecialRenderer(TileTable.class, new TileTableRenderer());
      this.registerTileEntitySpecialRenderer(TileThaumatorium.class, new TileThaumatoriumRenderer());
      this.registerTileEntitySpecialRenderer(TileTube.class, new thaumcraft.client.renderers.tile.TileTubeRenderer());
      this.registerTileEntitySpecialRenderer(TileTubeBuffer.class, new TileTubeBufferRenderer());
      this.registerTileEntitySpecialRenderer(TileTubeOneway.class, new TileTubeOnewayRenderer());
      this.registerTileEntitySpecialRenderer(TileTubeValve.class, new TileTubeValveRenderer());
      this.registerTileEntitySpecialRenderer(TileVisRelay.class, new TileVisRelayRenderer());
      this.registerTileEntitySpecialRenderer(TileWandPedestal.class, new TileWandPedestalRenderer());
      this.registerTileEntitySpecialRenderer(TileWarded.class, new TileWardedRenderer());
      this.registerTileEntitySpecialRenderer(TileFocalManipulator.class, new TileFocalManipulatorRenderer());
      this.registerTileEntitySpecialRenderer(TileAlchemyFurnaceAdvanced.class, new thaumcraft.client.renderers.tile.TileAlchemyFurnaceAdvancedRenderer());
      this.registerTileEntitySpecialRenderer(TileFluxScrubber.class, new TileFluxScrubberRenderer());
   }

   void setupBlockRenderers() {
      // Block rendering is JSON-based in 1.12.2 (see M4 rendering phase)
   }

   public void registerTileEntitySpecialRenderer(Class tile, TileEntitySpecialRenderer renderer) {
      ClientRegistry.bindTileEntitySpecialRenderer(tile, renderer);
   }

   public void registerBlockRenderer(Object renderer) {
   }

   public World getClientWorld() {
      return FMLClientHandler.instance().getClient().world;
   }

   public void blockSparkle(World world, int x, int y, int z, int c, int count) {
      Color color = new Color(c);
      float r = (float)color.getRed() / 255.0F;
      float g = (float)color.getGreen() / 255.0F;
      float b = (float)color.getBlue() / 255.0F;

      for(int a = 0; a < Thaumcraft.proxy.particleCount(count); ++a) {
         if (c == -9999) {
            r = 0.33F + world.rand.nextFloat() * 0.67F;
            g = 0.33F + world.rand.nextFloat() * 0.67F;
            b = 0.33F + world.rand.nextFloat() * 0.67F;
         }

         Thaumcraft.proxy.drawGenericParticles(world, (float)x - 0.1F + world.rand.nextFloat() * 1.2F, (float)y - 0.1F + world.rand.nextFloat() * 1.2F, (float)z - 0.1F + world.rand.nextFloat() * 1.2F, 0.0F, (double)world.rand.nextFloat() * 0.02, 0.0F, r - 0.2F + world.rand.nextFloat() * 0.4F, g - 0.2F + world.rand.nextFloat() * 0.4F, b - 0.2F + world.rand.nextFloat() * 0.4F, 0.9F, false, 112, 9, 1, 5 + world.rand.nextInt(8), world.rand.nextInt(10), 0.7F + world.rand.nextFloat() * 0.4F);
      }

   }

   public void sparkle(float x, float y, float z, float size, int color, float gravity) {
      if (this.getClientWorld() != null && this.getClientWorld().rand.nextInt(6) < this.particleCount(2)) {
         FXSparkle fx = new FXSparkle(this.getClientWorld(), x, y, z, size, color, 6);
         fx.setNoClip(true);
         fx.setGravity(gravity);
         ParticleEngine.instance.addEffect(this.getClientWorld(), fx);
      }

   }

   public void sparkle(float x, float y, float z, int color) {
      if (this.getClientWorld() != null && this.getClientWorld().rand.nextInt(6) < this.particleCount(2)) {
         FXSparkle fx = new FXSparkle(this.getClientWorld(), x, y, z, 1.5F, color, 6);
         fx.setNoClip(true);
         ParticleEngine.instance.addEffect(this.getClientWorld(), fx);
      }

   }

   public void spark(float x, float y, float z, float size, float r, float g, float b, float a) {
      if (this.getClientWorld() != null) {
         FXSpark fx = new FXSpark(this.getClientWorld(), x, y, z, size);
         fx.setRBGColorF(r, g, b);
         fx.setAlphaF(a);
         ParticleEngine.instance.addEffect(this.getClientWorld(), fx);
      }

   }

   public void smokeSpiral(World world, double x, double y, double z, float rad, int start, int miny, int color) {
      FXSmokeSpiral fx = new FXSmokeSpiral(this.getClientWorld(), x, y, z, rad, start, miny);
      Color c = new Color(color);
      fx.setRBGColorF((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F);
      ParticleEngine.instance.addEffect(world, fx);
   }

   public void crucibleBoilSound(World world, int xCoord, int yCoord, int zCoord) {
      SoundEvent _sndSpill = SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft", "spill"));
      if (_sndSpill != null) world.playSound(null, new BlockPos(xCoord, yCoord, zCoord), _sndSpill, SoundCategory.BLOCKS, 0.2F, 1.0F);
   }

   public void crucibleBoil(World world, int xCoord, int yCoord, int zCoord, TileCrucible tile, int j) {
      for(int a = 0; a < this.particleCount(1); ++a) {
         FXBubble fb = new FXBubble(world, (float)xCoord + 0.2F + world.rand.nextFloat() * 0.6F, (float)yCoord + 0.1F + tile.getFluidHeight(), (float)zCoord + 0.2F + world.rand.nextFloat() * 0.6F, 0.0F, 0.0F, 0.0F, 3);
         if (tile.aspects.size() == 0) {
            fb.setRBGColorF(1.0F, 1.0F, 1.0F);
         } else {
            Color color = new Color(tile.aspects.getAspects()[world.rand.nextInt(tile.aspects.getAspects().length)].getColor());
            fb.setRBGColorF((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F);
         }

         fb.bubblespeed = 0.003 * (double)j;
         ParticleEngine.instance.addEffect(world, fb);
      }

   }

   public void crucibleBubble(World world, float x, float y, float z, float cr, float cg, float cb) {
      FXBubble fb = new FXBubble(world, x, y, z, 0.0F, 0.0F, 0.0F, 1);
      fb.setRBGColorF(cr, cg, cb);
      ParticleEngine.instance.addEffect(world, fb);
   }

   public void crucibleFroth(World world, float x, float y, float z) {
      FXBubble fb = new FXBubble(world, x, y, z, 0.0F, 0.0F, 0.0F, -4);
      fb.setRBGColorF(0.5F, 0.5F, 0.7F);
      fb.setFroth();
      ParticleEngine.instance.addEffect(world, fb);
   }

   public void crucibleFrothDown(World world, float x, float y, float z) {
      FXBubble fb = new FXBubble(world, x, y, z, 0.0F, 0.0F, 0.0F, -4);
      fb.setRBGColorF(0.5F, 0.5F, 0.7F);
      fb.setFroth2();
      ParticleEngine.instance.addEffect(world, fb);
   }

   public void wispFX(World world, double posX, double posY, double posZ, float f, float g, float h, float i) {
      FXWisp ef = new FXWisp(world, posX, posY, posZ, f, g, h, i);
      ef.setGravity(0.02F);
      ParticleEngine.instance.addEffect(world, ef);
   }

   public void wispFX2(World world, double posX, double posY, double posZ, float size, int type, boolean shrink, boolean clip, float gravity) {
      FXWisp ef = new FXWisp(world, posX, posY, posZ, size, type);
      ef.setGravity(gravity);
      ef.shrink = shrink;
      ef.setNoClip(clip);
      ParticleEngine.instance.addEffect(world, ef);
   }

   public void wispFXEG(World world, double posX, double posY, double posZ, Entity target) {
      for(int a = 0; a < this.particleCount(1); ++a) {
         FXWispEG ef = new FXWispEG(world, posX, posY, posZ, target);
         ParticleEngine.instance.addEffect(world, ef);
      }

   }

   public void wispFX3(World world, double posX, double posY, double posZ, double posX2, double posY2, double posZ2, float size, int type, boolean shrink, float gravity) {
      FXWisp ef = new FXWisp(world, posX, posY, posZ, posX2, posY2, posZ2, size, type);
      ef.setGravity(gravity);
      ef.shrink = shrink;
      ParticleEngine.instance.addEffect(world, ef);
   }

   public void wispFX4(World world, double posX, double posY, double posZ, Entity target, int type, boolean shrink, float gravity) {
      FXWisp ef = new FXWisp(world, posX, posY, posZ, target, type);
      ef.setGravity(gravity);
      ef.shrink = shrink;
      ParticleEngine.instance.addEffect(world, ef);
   }

   public void burst(World world, double sx, double sy, double sz, float size) {
      FXBurst ef = new FXBurst(world, sx, sy, sz, size);
      ParticleEngine.instance.addEffect(world,ef);
   }

   public void sourceStreamFX(World world, double sx, double sy, double sz, float tx, float ty, float tz, int tagColor) {
      Color c = new Color(tagColor);
      FXWispArcing ef = new FXWispArcing(world, tx, ty, tz, sx, sy, sz, 0.1F, (float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F);
      ef.setGravity(0.0F);
      ParticleEngine.instance.addEffect(world, ef);
   }

   public void bolt(World world, Entity sourceEntity, Entity targetedEntity) {
      FXLightningBolt bolt = new FXLightningBolt(world, sourceEntity, targetedEntity, world.rand.nextLong(), 4);
      bolt.defaultFractal();
      bolt.setType(0);
      bolt.finalizeBolt();
   }

   public void nodeBolt(World world, float x, float y, float z, Entity targetedEntity) {
      FXLightningBolt bolt = new FXLightningBolt(world, x, y, z, targetedEntity.posX, targetedEntity.posY, targetedEntity.posZ, world.rand.nextLong(), 10, 4.0F, 5);
      bolt.defaultFractal();
      bolt.setType(3);
      bolt.finalizeBolt();
   }

   public void nodeBolt(World world, float x, float y, float z, float x2, float y2, float z2) {
      FXLightningBolt bolt = new FXLightningBolt(world, x, y, z, x2, y2, z2, world.rand.nextLong(), 10, 4.0F, 5);
      bolt.defaultFractal();
      bolt.setType(0);
      bolt.finalizeBolt();
   }

   public void excavateFX(int x, int y, int z, EntityPlayer p, int bi, int md, int progress) {
      RenderGlobal rg = Minecraft.getMinecraft().renderGlobal;
      rg.sendBlockBreakProgress(p.getEntityId(), new BlockPos(x, y, z), progress);
   }

   public void beam(World world, double sx, double sy, double sz, double tx, double ty, double tz, int type, int color, boolean reverse, float endmod, int age) {
      FXBeam beamcon = null;
      Color c = new Color(color);
      beamcon = new FXBeam(world, sx, sy, sz, tx, ty, tz, (float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, age);
      beamcon.setType(type);
      beamcon.setEndMod(endmod);
      beamcon.setReverse(reverse);
      beamcon.setPulse(false);
      ParticleEngine.instance.addEffect(world,beamcon);
   }

   public Object beamCont(World world, EntityPlayer p, double tx, double ty, double tz, int type, int color, boolean reverse, float endmod, Object input, int impact) {
      FXBeamWand beamcon = null;
      Color c = new Color(color);
      if (input instanceof FXBeamWand) {
         beamcon = (FXBeamWand)input;
      }

      if (beamcon != null && !beamcon.isDead()) {
         beamcon.updateBeam(tx, ty, tz);
         beamcon.setEndMod(endmod);
         beamcon.impact = impact;
      } else {
         beamcon = new FXBeamWand(world, p, tx, ty, tz, (float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, 8);
         beamcon.setType(type);
         beamcon.setEndMod(endmod);
         beamcon.setReverse(reverse);
         ParticleEngine.instance.addEffect(world,beamcon);
      }

      return beamcon;
   }

   public Object beamBore(World world, double px, double py, double pz, double tx, double ty, double tz, int type, int color, boolean reverse, float endmod, Object input, int impact) {
      FXBeamBore beamcon = null;
      Color c = new Color(color);
      if (input instanceof FXBeamBore) {
         beamcon = (FXBeamBore)input;
      }

      if (beamcon != null && !beamcon.isDead()) {
         beamcon.updateBeam(tx, ty, tz);
         beamcon.setEndMod(endmod);
         beamcon.impact = impact;
      } else {
         beamcon = new FXBeamBore(world, px, py, pz, tx, ty, tz, (float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, 8);
         beamcon.setType(type);
         beamcon.setEndMod(endmod);
         beamcon.setReverse(reverse);
         ParticleEngine.instance.addEffect(world,beamcon);
      }

      return beamcon;
   }

   public void boreDigFx(World world, int x, int y, int z, int x2, int y2, int z2, Block bi, int md) {
      if (world.rand.nextInt(10) == 0) {
         FXBoreSparkle fb = new FXBoreSparkle(world, (float)x + world.rand.nextFloat(), (float)y + world.rand.nextFloat(), (float)z + world.rand.nextFloat(), (double)x2 + (double)0.5F, (double)y2 + (double)0.5F, (double)z2 + (double)0.5F);
         ParticleEngine.instance.addEffect(world, fb);
      } else {
         FXBoreParticles fb = (new FXBoreParticles(world, (float)x + world.rand.nextFloat(), (float)y + world.rand.nextFloat(), (float)z + world.rand.nextFloat(), (double)x2 + (double)0.5F, (double)y2 + (double)0.5F, (double)z2 + (double)0.5F, bi, world.rand.nextInt(6), md)).applyColourMultiplier(x, y, z);
         ParticleEngine.instance.addEffect(world,fb);
      }

   }

   public void essentiaTrailFx(World world, int x, int y, int z, int x2, int y2, int z2, int count, int color, float scale) {
      FXEssentiaTrail fb = new FXEssentiaTrail(world, (double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F, (double)x2 + (double)0.5F, (double)y2 + (double)0.5F, (double)z2 + (double)0.5F, count, color, scale);
      ParticleEngine.instance.addEffect(world, fb);
   }

   public void soulTrail(World world, Entity source, Entity target, float r, float g, float b) {
      for(int a = 0; a < this.particleCount(2); ++a) {
         if (world.rand.nextInt(10) == 0) {
            FXSparkleTrail st = new FXSparkleTrail(world, source.posX - (double)(source.width / 2.0F) + (double)(world.rand.nextFloat() * source.width), source.posY + (double)(world.rand.nextFloat() * source.height), source.posZ - (double)(source.width / 2.0F) + (double)(world.rand.nextFloat() * source.width), target, r, g, b);
            st.setNoClip(true);
            ParticleEngine.instance.addEffect(world, st);
         } else {
            FXSmokeTrail st = new FXSmokeTrail(world, source.posX - (double)(source.width / 2.0F) + (double)(world.rand.nextFloat() * source.width), source.posY + (double)(world.rand.nextFloat() * source.height), source.posZ - (double)(source.width / 2.0F) + (double)(world.rand.nextFloat() * source.width), target, r, g, b);
            st.setNoClip(true);
            ParticleEngine.instance.addEffect(world, st);
         }
      }

   }

   public int particleCount(int base) {
      if (FMLClientHandler.instance().getClient().gameSettings.particleSetting == 2) {
         return 0;
      } else {
         return FMLClientHandler.instance().getClient().gameSettings.particleSetting == 1 ? base : base * 2;
      }
   }

   public void furnaceLavaFx(World world, int x, int y, int z, int facingX, int facingZ) {
      double _px = x + 0.5 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3 + facingX;
      double _py = y + 0.3;
      double _pz = z + 0.5 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3 + facingZ;
      FMLClientHandler.instance().getClient().effectRenderer.spawnEffectParticle(
         EnumParticleTypes.LAVA.getParticleID(), _px, _py, _pz, 0, 0, 0);
   }

   public void blockRunes(World world, double x, double y, double z, float r, float g, float b, int dur, float grav) {
      FXBlockRunes fb = new FXBlockRunes(world, x + (double)0.5F, y + (double)0.5F, z + (double)0.5F, r, g, b, dur);
      fb.setGravity(grav);
      ParticleEngine.instance.addEffect(world, fb);
   }

   public void blockWard(World world, double x, double y, double z, EnumFacing side, float f, float f1, float f2) {
      FXBlockWard fb = new FXBlockWard(world, x + (double)0.5F, y + (double)0.5F, z + (double)0.5F, side, f, f1, f2);
      ParticleEngine.instance.addEffect(world,fb);
   }

   public Object swarmParticleFX(World world, Entity targetedEntity, float f1, float f2, float pg) {
      FXSwarm fx = new FXSwarm(world, targetedEntity.posX + (double)((world.rand.nextFloat() - world.rand.nextFloat()) * 2.0F), targetedEntity.posY + (double)((world.rand.nextFloat() - world.rand.nextFloat()) * 2.0F), targetedEntity.posZ + (double)((world.rand.nextFloat() - world.rand.nextFloat()) * 2.0F), targetedEntity, 0.8F + world.rand.nextFloat() * 0.2F, world.rand.nextFloat() * 0.4F, 1.0F - world.rand.nextFloat() * 0.2F, f1, f2, pg);
      ParticleEngine.instance.addEffect(world, fx);
      return fx;
   }

   public void splooshFX(Entity e) {
      float f = e.world.rand.nextFloat() * (float)Math.PI * 2.0F;
      float f1 = e.world.rand.nextFloat() * 0.5F + 0.5F;
      float f2 = MathHelper.sin(f) * 2.0F * 0.5F * f1;
      float f3 = MathHelper.cos(f) * 2.0F * 0.5F * f1;
      FXBreaking fx = new FXBreaking(e.world, e.posX + (double)f2, e.posY + (double)(e.world.rand.nextFloat() * e.height), e.posZ + (double)f3, Items.SLIME_BALL);
      if (e.world.rand.nextBoolean()) {
         fx.setRBGColorF(0.6F, 0.0F, 0.3F);
         fx.setAlphaF(0.4F);
      } else {
         fx.setRBGColorF(0.3F, 0.0F, 0.3F);
         fx.setAlphaF(0.6F);
      }

      fx.setParticleMaxAge((int)(66.0F / (e.world.rand.nextFloat() * 0.9F + 0.1F)));
      ParticleEngine.instance.addEffect(e.world,fx);
   }

   public void splooshFX(World world, int x, int y, int z) {
      float f = world.rand.nextFloat() * (float)Math.PI * 2.0F;
      float f1 = world.rand.nextFloat() * 0.5F + 0.5F;
      float f2 = MathHelper.sin(f) * 2.0F * 0.5F * f1;
      float f3 = MathHelper.cos(f) * 2.0F * 0.5F * f1;
      FXBreaking fx = new FXBreaking(world, (double)x + (double)f2 + (double)0.5F, (float)y + world.rand.nextFloat(), (double)z + (double)f3 + (double)0.5F, Items.SLIME_BALL);
      if (world.rand.nextBoolean()) {
         fx.setRBGColorF(0.6F, 0.0F, 0.3F);
         fx.setAlphaF(0.4F);
      } else {
         fx.setRBGColorF(0.3F, 0.0F, 0.3F);
         fx.setAlphaF(0.6F);
      }

      fx.setParticleMaxAge((int)(66.0F / (world.rand.nextFloat() * 0.9F + 0.1F)));
      ParticleEngine.instance.addEffect(world,fx);
   }

   public void taintsplosionFX(Entity e) {
      FXBreaking fx = new FXBreaking(e.world, e.posX, e.posY + (double)(e.world.rand.nextFloat() * e.height), e.posZ, Items.SLIME_BALL);
      if (e.world.rand.nextBoolean()) {
         fx.setRBGColorF(0.6F, 0.0F, 0.3F);
         fx.setAlphaF(0.4F);
      } else {
         fx.setRBGColorF(0.3F, 0.0F, 0.3F);
         fx.setAlphaF(0.6F);
      }

      double _fxMx = (Math.random() * 2.0 - 1.0);
      double _fxMy = (Math.random() * 2.0 - 1.0);
      double _fxMz = (Math.random() * 2.0 - 1.0);
      float f = (float)(Math.random() + Math.random() + 1.0) * 0.15F;
      float f1 = MathHelper.sqrt((float)(_fxMx * _fxMx + _fxMy * _fxMy + _fxMz * _fxMz));
      fx.setMotion(_fxMx / f1 * f * 0.9640000000596046,
                   _fxMy / f1 * f * 0.9640000000596046 + 0.1,
                   _fxMz / f1 * f * 0.9640000000596046);
      fx.setParticleMaxAge((int)(66.0F / (e.world.rand.nextFloat() * 0.9F + 0.1F)));
      ParticleEngine.instance.addEffect(e.world,fx);
   }

   public void tentacleAriseFX(Entity e) {
      int xx = MathHelper.floor(e.posX);
      int yy = MathHelper.floor(e.posY) - 1;
      int zz = MathHelper.floor(e.posZ);

      for(int j = 0; (float)j < 2.0F * e.height; ++j) {
         float f = e.world.rand.nextFloat() * (float)Math.PI * e.height;
         float f1 = e.world.rand.nextFloat() * 0.5F + 0.5F;
         float f2 = MathHelper.sin(f) * e.height * 0.25F * f1;
         float f3 = MathHelper.cos(f) * e.height * 0.25F * f1;
         FXBreaking fx = new FXBreaking(e.world, e.posX + (double)f2, e.posY, e.posZ + (double)f3, Items.SLIME_BALL);
         fx.setRBGColorF(0.4F, 0.0F, 0.4F);
         fx.setAlphaF(0.5F);
         fx.setParticleMaxAge((int)(66.0F / (e.world.rand.nextFloat() * 0.9F + 0.1F)));
         ParticleEngine.instance.addEffect(e.world,fx);
         if (!e.world.isAirBlock(new BlockPos(xx, yy, zz))) {
            f = e.world.rand.nextFloat() * (float)Math.PI * e.height;
            f1 = e.world.rand.nextFloat() * 0.5F + 0.5F;
            f2 = MathHelper.sin(f) * e.height * 0.25F * f1;
            f3 = MathHelper.cos(f) * e.height * 0.25F * f1;
            net.minecraft.block.state.IBlockState _pdState = e.world.getBlockState(new BlockPos(xx, yy, zz));
            // ParticleDigging constructor is protected; use spawnEffectParticle with BLOCK_CRACK
            FMLClientHandler.instance().getClient().effectRenderer.spawnEffectParticle(
                net.minecraft.util.EnumParticleTypes.BLOCK_CRACK.getParticleID(),
                e.posX + (double)f2, e.posY, e.posZ + (double)f3,
                0.0F, 0.0F, 0.0F,
                net.minecraft.block.Block.getStateId(_pdState));
         }
      }

   }

   public void slimeJumpFX(Entity e, int i) {
      float f = e.world.rand.nextFloat() * (float)Math.PI * 2.0F;
      float f1 = e.world.rand.nextFloat() * 0.5F + 0.5F;
      float f2 = MathHelper.sin(f) * (float)i * 0.5F * f1;
      float f3 = MathHelper.cos(f) * (float)i * 0.5F * f1;
      FXBreaking fx = new FXBreaking(e.world, e.posX + (double)f2, (e.getEntityBoundingBox().minY + e.getEntityBoundingBox().maxY) / (double)2.0F, e.posZ + (double)f3, Items.SLIME_BALL);
      fx.setRBGColorF(0.7F, 0.0F, 1.0F);
      fx.setAlphaF(0.4F);
      fx.setParticleMaxAge((int)(66.0F / (e.world.rand.nextFloat() * 0.9F + 0.1F)));
      ParticleEngine.instance.addEffect(e.world,fx);
   }

   public void dropletFX(World world, float i, float j, float k, float r, float g, float b) {
      FXDrop obj = new FXDrop(world, i, j, k, r, g, b);
      ParticleEngine.instance.addEffect(world,obj);
   }

   public void taintLandFX(Entity e) {
      float f = e.world.rand.nextFloat() * (float)Math.PI * 2.0F;
      float f1 = e.world.rand.nextFloat() * 0.5F + 0.5F;
      float f2 = MathHelper.sin(f) * 2.0F * 0.5F * f1;
      float f3 = MathHelper.cos(f) * 2.0F * 0.5F * f1;
      if (e.world.isRemote) {
         FXBreaking fx = new FXBreaking(e.world, e.posX + (double)f2, (e.getEntityBoundingBox().minY + e.getEntityBoundingBox().maxY) / (double)2.0F, e.posZ + (double)f3, Items.SLIME_BALL);
         fx.setRBGColorF(0.1F, 0.0F, 0.1F);
         fx.setAlphaF(0.4F);
         fx.setParticleMaxAge((int)(66.0F / (e.world.rand.nextFloat() * 0.9F + 0.1F)));
         ParticleEngine.instance.addEffect(e.world,fx);
      }

   }

   public void hungryNodeFX(World world, int sourceX, int sourceY, int sourceZ, int targetX, int targetY, int targetZ, Block block, int md) {
      FXBoreParticles fb = (new FXBoreParticles(world, (float)sourceX + world.rand.nextFloat(), (float)sourceY + world.rand.nextFloat(), (float)sourceZ + world.rand.nextFloat(), (double)targetX + (double)0.5F, (double)targetY + (double)0.5F, (double)targetZ + (double)0.5F, block, world.rand.nextInt(6), md)).applyColourMultiplier(sourceX, sourceY, sourceZ);
      ParticleEngine.instance.addEffect(world,fb);
   }

   public void drawInfusionParticles1(World world, double x, double y, double z, int x2, int y2, int z2, Item id, int md) {
      FXBoreParticles fb = (new FXBoreParticles(world, x, y, z, (double)x2 + (double)0.5F, (double)y2 - (double)0.5F, (double)z2 + (double)0.5F, id, world.rand.nextInt(6), md)).applyColourMultiplier(x2, y2, z2);
      fb.setAlphaF(0.3F);
      fb.setMotion((float)world.rand.nextGaussian() * 0.03F,
                   (float)world.rand.nextGaussian() * 0.03F,
                   (float)world.rand.nextGaussian() * 0.03F);
      ParticleEngine.instance.addEffect(world,fb);
   }

   public void drawInfusionParticles2(World world, double x, double y, double z, int x2, int y2, int z2, Block id, int md) {
      FXBoreParticles fb = (new FXBoreParticles(world, x, y, z, (double)x2 + (double)0.5F, (double)y2 - (double)0.5F, (double)z2 + (double)0.5F, id, world.rand.nextInt(6), md)).applyColourMultiplier(x2, y2, z2);
      fb.setAlphaF(0.3F);
      ParticleEngine.instance.addEffect(world,fb);
   }

   public void drawInfusionParticles3(World world, double x, double y, double z, int x2, int y2, int z2) {
      FXBoreSparkle fb = new FXBoreSparkle(world, x, y, z, (double)x2 + (double)0.5F, (double)y2 - (double)0.5F, (double)z2 + (double)0.5F);
      fb.setRBGColorF(0.4F + world.rand.nextFloat() * 0.2F, 0.2F, 0.6F + world.rand.nextFloat() * 0.3F);
      ParticleEngine.instance.addEffect(world, fb);
   }

   public void drawInfusionParticles4(World world, double x, double y, double z, int x2, int y2, int z2) {
      FXBoreSparkle fb = new FXBoreSparkle(world, x, y, z, (double)x2 + (double)0.5F, (double)y2 - (double)0.5F, (double)z2 + (double)0.5F);
      fb.setRBGColorF(0.2F, 0.6F + world.rand.nextFloat() * 0.3F, 0.3F);
      ParticleEngine.instance.addEffect(world, fb);
   }

   public void drawVentParticles(World world, double x, double y, double z, double x2, double y2, double z2, int color) {
      FXVent fb = new FXVent(world, x, y, z, x2, y2, z2, color);
      fb.setAlphaF(0.4F);
      ParticleEngine.instance.addEffect(world, fb);
   }

   public void drawGenericParticles(World world, double x, double y, double z, double x2, double y2, double z2, float r, float g, float b, float alpha, boolean loop, int start, int num, int inc, int age, int delay, float scale) {
      FXGeneric fb = new FXGeneric(world, x, y, z, x2, y2, z2);
      fb.setMaxAge(age, delay);
      fb.setRBGColorF(r, g, b);
      fb.setAlphaF(alpha);
      fb.setLoop(loop);
      fb.setParticles(start, num, inc);
      fb.setScale(scale);
      ParticleEngine.instance.addEffect(world, fb);
   }

   public void drawVentParticles(World world, double x, double y, double z, double x2, double y2, double z2, int color, float scale) {
      FXVent fb = new FXVent(world, x, y, z, x2, y2, z2, color);
      fb.setAlphaF(0.4F);
      fb.setScale(scale);
      ParticleEngine.instance.addEffect(world, fb);
   }

   public Object beamPower(World world, double px, double py, double pz, double tx, double ty, double tz, float r, float g, float b, boolean pulse, Object input) {
      FXBeamPower beamcon = null;
      if (input instanceof FXBeamPower) {
         beamcon = (FXBeamPower)input;
      }

      if (beamcon != null && !beamcon.isDead()) {
         beamcon.updateBeam(px, py, pz, tx, ty, tz);
         beamcon.setPulse(pulse, r, g, b);
      } else {
         beamcon = new FXBeamPower(world, px, py, pz, tx, ty, tz, r, g, b, 8);
         ParticleEngine.instance.addEffect(world,beamcon);
      }

      return beamcon;
   }

   public boolean isShiftKeyDown() {
      return GuiScreen.isShiftKeyDown();
   }

   public void bottleTaintBreak(World world, double x, double y, double z) {
      for(int k1 = 0; k1 < 8; ++k1) {
         Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(
             net.minecraft.util.EnumParticleTypes.ITEM_CRACK.getParticleID(),
             x, y, z,
             world.rand.nextGaussian() * 0.15, world.rand.nextDouble() * 0.2, world.rand.nextGaussian() * 0.15,
             Item.getIdFromItem(ConfigItems.itemBottleTaint), 0);
      }
      SoundEvent _sndSmash = SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft", "entity.splash_potion.break"));
      if (_sndSmash != null) world.playSound(null, new BlockPos((int)x, (int)y, (int)z), _sndSmash, SoundCategory.NEUTRAL, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
   }

   public void arcLightning(World world, double x, double y, double z, double tx, double ty, double tz, float r, float g, float b, float h) {
      FXSparkle ef2 = new FXSparkle(world, tx, ty, tz, tx, ty, tz, 3.0F, 6, 2);
      ef2.setGravity(0.0F);
      ef2.setNoClip(true);
      ef2.setRBGColorF(r, g, b);
      ParticleEngine.instance.addEffect(world, ef2);
      FXArc efa = new FXArc(world, x, y, z, tx, ty, tz, r, g, b, h);
      ParticleEngine.instance.addEffect(world,efa);
   }


   static long lastScroll = 0;
   static Field fieldPage = null;
   static Field fieldLastPage = null;
   static Method methodPlayScroll = null;
   static Method GuiResearchRecipeMouseClicked = null;
   private static final int mPrevX = 261, mPrevY = 189, mNextX = -17, mNextY = 189;
   private static final int paneWidth = 256, paneHeight = 181;

   private long updateCounter = 0;

   public static void handleMouseInput(GuiResearchTable screen) {
      if (fieldLastPage == null || fieldPage == null || methodPlayScroll == null) return;
      final int dwheel = Mouse.getEventDWheel();
      if (dwheel == 0)
         return;
      final long currentTimeMillis = System.currentTimeMillis();
      if (currentTimeMillis - lastScroll > 50) {
         lastScroll = currentTimeMillis;
         try {
            int page = fieldPage.getInt(screen);
            if ((dwheel < 0) != ConfigurationHandler.INSTANCE.isInverted()) {
               int lastPage = fieldLastPage.getInt(screen);
               if (page < lastPage) {
                  fieldPage.setInt(screen, page + 1);
                  methodPlayScroll.invoke(screen);
               }
            } else {
               if (page > 0) {
                  fieldPage.setInt(screen, page - 1);
                  methodPlayScroll.invoke(screen);
               }
            }
         } catch (ReflectiveOperationException err) {
            System.err.println("Error scrolling through aspect list!");
            err.printStackTrace();
         }
      }
   }

   public static void handleMouseInput(GuiResearchRecipe screen) {
      if (GuiResearchRecipeMouseClicked == null) return;
      final int dwheel = Mouse.getEventDWheel();
      if (dwheel == 0)
         return;
      final long currentTimeMillis = System.currentTimeMillis();
      if (currentTimeMillis - lastScroll > 50) {
         lastScroll = currentTimeMillis;
         // emulate a click into respective buttons
         int mX, mY;
         if ((dwheel < 0) != ConfigurationHandler.INSTANCE.isInverted()) {
            mX = mPrevX;
            mY= mPrevY;
         } else {
            mX = mNextX;
            mY = mNextY;
         }
         mX += (screen.width - paneWidth) / 2;
         mY += (screen.height - paneHeight) / 2;
         try {
            GuiResearchRecipeMouseClicked.invoke(screen, mX, mY, 0);
         } catch (ReflectiveOperationException err) {
            System.err.println("Error scrolling through research page!");
            err.printStackTrace();
         }
      }
   }

   public ClientProxy() {
      super();
      MinecraftForge.EVENT_BUS.register(this);
   }

   public static boolean dev = false; // mirrors DepLoader.dev, set during preInit

   @Override
   public void preInit(FMLPreInitializationEvent e) {
      dev = thaumcraft.codechicken.core.launch.DepLoader.dev;
      super.preInit(e);
      this.setupEntityRenderers();
      ConfigurationHandler.INSTANCE.setGUISettings();
      try {
         Class<GuiResearchTable> guiResearchTableClass = GuiResearchTable.class;
         fieldPage = guiResearchTableClass.getDeclaredField("page");
         fieldPage.setAccessible(true);
         fieldLastPage = guiResearchTableClass.getDeclaredField("lastPage");
         fieldLastPage.setAccessible(true);
         methodPlayScroll = guiResearchTableClass.getDeclaredMethod("playButtonScroll");
         methodPlayScroll.setAccessible(true);
      } catch (Exception err) {
         System.err.println("Cannot find thaumcraft fields. Aspect list scrolling will not properly function!");
         err.printStackTrace();
      }
      String mouseClicked = dev ? "mouseClicked" : "func_73864_a";
      try {
         GuiResearchRecipeMouseClicked = GuiResearchRecipe.class.getDeclaredMethod(mouseClicked, int.class, int.class, int.class);
         GuiResearchRecipeMouseClicked.setAccessible(true);
      } catch (Exception err) {
         System.err.println("Cannot find thaumcraft fields. Research page scrolling will not properly function!");
         err.printStackTrace();
      }
//      final IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
//      if (resourceManager instanceof IReloadableResourceManager) {
//         //noinspection Convert2Lambda
//         ((IReloadableResourceManager) resourceManager).registerReloadListener(new IResourceManagerReloadListener() {
//            @Override
//            public void onResourceManagerReload(IResourceManager ignored) {
//               reflectiveReloadModel(FXSonic.class, "MODEL");
//               reflectiveReloadModel(TileAlchemyFurnaceAdvancedRenderer.class, "FURNACE");
//            }
//         });
//      }
      ParticleEngineFix.init();
   }

   @Override
   public void init(FMLInitializationEvent e) {
      super.init(e);
      ThaumonomiconIndexSearcher.init();
      DrawResearchCompletionCounter.init();
      HUDNotification.init();
      BrowserPaging.init();
      registerColorHandlers();
   }

   private void registerColorHandlers() {
      net.minecraft.client.renderer.color.BlockColors bc = net.minecraft.client.Minecraft.getMinecraft().getBlockColors();
      net.minecraft.client.renderer.color.ItemColors ic = net.minecraft.client.Minecraft.getMinecraft().getItemColors();

      // Candle colors (16 dye colors)
      int[] CANDLE_COLORS = {0xFFFFFF, 0xD87F33, 0xB24CD8, 0x6699D8, 0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C,
                             0x999999, 0x4C7F99, 0x7F3FB2, 0x334CB2, 0x664C33, 0x667F33, 0x993333, 0x191919};
      bc.registerBlockColorHandler((state, world, pos, ti) -> {
         int m = state.getBlock().getMetaFromState(state);
         return m >= 0 && m < CANDLE_COLORS.length ? CANDLE_COLORS[m] : 0xFFFFFF;
      }, ConfigBlocks.blockCandle);
      ic.registerItemColorHandler((stack, ti) -> {
         int m = stack.getMetadata();
         return m >= 0 && m < CANDLE_COLORS.length ? CANDLE_COLORS[m] : 0xFFFFFF;
      }, Item.getItemFromBlock(ConfigBlocks.blockCandle));

      // Crystal colors (6 primal aspects + balanced)
      thaumcraft.api.aspects.Aspect[] CRYSTAL_ASPECTS = {
            thaumcraft.api.aspects.Aspect.AIR, thaumcraft.api.aspects.Aspect.FIRE,
            thaumcraft.api.aspects.Aspect.WATER, thaumcraft.api.aspects.Aspect.EARTH,
            thaumcraft.api.aspects.Aspect.ORDER, thaumcraft.api.aspects.Aspect.ENTROPY,
            thaumcraft.api.aspects.Aspect.ORDER};
      bc.registerBlockColorHandler((state, world, pos, ti) -> {
         int m = state.getBlock().getMetaFromState(state);
         return m >= 0 && m < CRYSTAL_ASPECTS.length ? CRYSTAL_ASPECTS[m].getColor() : 0xFFFFFF;
      }, ConfigBlocks.blockCrystal);
      ic.registerItemColorHandler((stack, ti) -> {
         int m = stack.getMetadata();
         return m >= 0 && m < CRYSTAL_ASPECTS.length ? CRYSTAL_ASPECTS[m].getColor() : 0xFFFFFF;
      }, Item.getItemFromBlock(ConfigBlocks.blockCrystal));

      // Infused ore colors (meta 1-6 = primal aspects)
      int[] ORE_COLORS = thaumcraft.common.blocks.BlockCustomOreItem.colors;
      bc.registerBlockColorHandler((state, world, pos, ti) -> {
         int m = state.getBlock().getMetaFromState(state);
         return m >= 1 && m <= 6 && (m) < ORE_COLORS.length ? ORE_COLORS[m] : 0xFFFFFF;
      }, ConfigBlocks.blockCustomOre);
      ic.registerItemColorHandler((stack, ti) -> {
         int m = stack.getMetadata();
         return m >= 1 && m <= 6 && m < ORE_COLORS.length ? ORE_COLORS[m] : 0xFFFFFF;
      }, Item.getItemFromBlock(ConfigBlocks.blockCustomOre));

      // Items that delegate to getColorFromItemStack
      ic.registerItemColorHandler((stack, ti) -> ((thaumcraft.common.items.ItemEssence)stack.getItem()).getColorFromItemStack(stack, ti), ConfigItems.itemEssence);
      ic.registerItemColorHandler((stack, ti) -> ((thaumcraft.common.items.ItemManaBean)stack.getItem()).getColorFromItemStack(stack, ti), ConfigItems.itemManaBean);
      ic.registerItemColorHandler((stack, ti) -> ((thaumcraft.common.items.ItemWispEssence)stack.getItem()).getColorFromItemStack(stack, ti), ConfigItems.itemWispEssence);
      ic.registerItemColorHandler((stack, ti) -> ((thaumcraft.common.items.ItemCrystalEssence)stack.getItem()).getColorFromItemStack(stack, ti), ConfigItems.itemCrystalEssence);
      ic.registerItemColorHandler((stack, ti) -> ((thaumcraft.common.items.ItemShard)stack.getItem()).getColorFromItemStack(stack, ti), ConfigItems.itemShard);
      ic.registerItemColorHandler((stack, ti) -> ((thaumcraft.common.items.ItemResource)stack.getItem()).getColorFromItemStack(stack, ti), ConfigItems.itemResource);
      ic.registerItemColorHandler((stack, ti) -> ((thaumcraft.common.items.ItemResearchNotes)stack.getItem()).getColorFromItemStack(stack, ti), ConfigItems.itemResearchNotes);
      ic.registerItemColorHandler((stack, ti) -> ((thaumcraft.common.items.baubles.ItemBaubleBlanks)stack.getItem()).getColorFromItemStack(stack, ti), ConfigItems.itemBaubleBlanks);
      ic.registerItemColorHandler((stack, ti) -> ((thaumcraft.common.entities.ItemSpawnerEgg)stack.getItem()).getColorFromItemStack(stack, ti), ConfigItems.itemSpawnerEgg);

      // Greatwood leaves: biome foliage tint; silverwood: no tint
      bc.registerBlockColorHandler((state, world, pos, ti) -> {
         int meta = state.getBlock().getMetaFromState(state) & 1;
         if (meta == 0) { // greatwood
            if (world != null && pos != null) {
               return net.minecraft.world.biome.BiomeColorHelper.getFoliageColorAtPos(world, pos);
            }
            return net.minecraft.world.ColorizerFoliage.getFoliageColorBasic();
         }
         return 0xFFFFFF; // silverwood - no tint
      }, ConfigBlocks.blockMagicalLeaves);
      ic.registerItemColorHandler((stack, ti) -> {
         int meta = stack.getMetadata() & 1;
         return meta == 0 ? net.minecraft.world.ColorizerFoliage.getFoliageColorBasic() : 0xFFFFFF;
      }, Item.getItemFromBlock(ConfigBlocks.blockMagicalLeaves));
   }

   @Override
   public void postInit(FMLPostInitializationEvent e) {
      super.postInit(e);
      try {
         Object wgSearcher = Class.forName("witchinggadgets.client.ThaumonomiconIndexSearcher").getField("instance").get(null);
         MinecraftForge.EVENT_BUS.unregister(wgSearcher);
         FMLCommonHandler.instance().bus().unregister(wgSearcher);
      } catch (ReflectiveOperationException ignored) {
         // WG is probably not installed, ignoring
      }
   }

   @SubscribeEvent
   public void onWorldLoad(WorldEvent.Load e) {
      if (e.getWorld().isRemote)
         CommonUtils.sortResearchCategories(false);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onTooltip(ItemTooltipEvent e) {
      if (ConfigurationHandler.INSTANCE.isAddTooltip() && e.getItemStack() != null) {
         if (e.getItemStack().getItem() == ConfigItems.itemWandCasting) {
            if (!ConfigurationHandler.INSTANCE.isCheckWorkbenchRecipes() || !NetworkedConfiguration.isCheckWorkbenchRecipes()) {
               e.getToolTip().add(TextFormatting.RED + I18n.translateToLocal("tc4tweaks.disable_vanilla"));
            }
         }
      }
   }

   @SubscribeEvent
   public void onTickEnd(TickEvent.ClientTickEvent e) {
      if (e.phase == TickEvent.Phase.END) {
         if (++updateCounter > ConfigurationHandler.INSTANCE.getUpdateInterval()) {
            updateCounter = 0;
            synchronized (postponed) {
               for (Map.Entry<TileMagicWorkbench, Void> workbench : postponed.entrySet()) {
                  TileMagicWorkbench tile = workbench.getKey();
                  if (tile != null && tile.eventHandler != null && !tile.isInvalid() && tile.hasWorld()) {
                     // best effort guess on whether tile is valid
                     tile.eventHandler.onCraftMatrixChanged(tile);
                  }
               }
               postponed.clear();
            }
         }
      }
   }

   @SubscribeEvent
   public void onServerConnected(FMLNetworkEvent.ClientConnectedToServerEvent e) {
      if (!e.isLocal()) {
         NetworkedConfiguration.resetClient();
      }
   }

   private static void reflectiveReloadModel(Class<?> cls, String resLocationField) {
      try {
         FieldUtils.writeDeclaredStaticField(cls,
                 "model",
                 AdvancedModelLoader.loadModel(
                         (ResourceLocation)
                                 FieldUtils.readDeclaredStaticField(
                                         cls,
                                         resLocationField,
                                         true)
                 )
                 , true);
      } catch (ReflectiveOperationException e) {
         // ignore
      }
   }
}
