package thaumcraft.client.lib;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.IArchitect;
import thaumcraft.api.IGoggles;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.research.ScanResult;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.renderers.tile.TileNodeRenderer;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.ItemGolemBell;
import thaumcraft.common.entities.golems.ItemGolemPlacer;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.items.armor.ItemFortressArmor;
import thaumcraft.common.items.armor.ItemVoidRobeArmor;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.misc.PacketNote;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.tiles.TileSensor;
import thaumcraft.common.tiles.TileWandPedestal;
import truetyper.FontLoader;
import truetyper.TrueTypeFont;

import static thaumcraft.client.renderers.tile.TileNodeRenderer.renderFacingStrip_tweaked;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class RenderEventHandler {
   TrueTypeFont font = null;
   public static List blockTags = new ArrayList<>();
   int q = 0;
   public static float tagscale = 0.0F;
   public long scanCount = 0L;
   public int scanX = 0;
   public int scanY = 0;
   public int scanZ = 0;
   int[][][] scannedBlocks = new int[17][17][17];
   @SideOnly(Side.CLIENT)
   public REHWandHandler wandHandler;
   @SideOnly(Side.CLIENT)
   public REHNotifyHandler notifyHandler;
   public static boolean resetShaders = false;
   private static int oldDisplayWidth = 0;
   private static int oldDisplayHeight = 0;
   public static HashMap<Integer,ShaderGroup> shaderGroups = new HashMap<>();
   public static boolean fogFiddled = false;
   public static float fogTarget = 0.0F;
   public static int fogDuration = 0;
   public static float prevVignetteBrightness = 0.0F;
   public static float targetBrightness = 1.0F;
   protected static final ResourceLocation vignetteTexPath = new ResourceLocation("thaumcraft", "textures/misc/vignette.png");

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void renderOverlay(RenderGameOverlayEvent event) {
      if (this.font == null) {
         this.font = FontLoader.loadSystemFont("Arial", 12.0F, true);
      }

      Minecraft mc = Minecraft.getMinecraft();
      long time = System.nanoTime() / 1000000L;
      if (this.wandHandler == null) {
         this.wandHandler = new REHWandHandler();
      }

      if (this.notifyHandler == null) {
         this.notifyHandler = new REHNotifyHandler();
      }

      if (event.getType() == ElementType.TEXT) {
         this.notifyHandler.handleNotifications(mc, time, event);
         this.wandHandler.handleFociRadial(mc, time, event);
      }

      if (event.getType() == ElementType.PORTAL) {
         this.renderVignette(targetBrightness, event.getResolution().getScaledWidth_double(), event.getResolution().getScaledHeight_double());
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void renderShaders(RenderGameOverlayEvent.Pre event) {
      if (Config.shaders && event.getType() == ElementType.ALL) {
         Minecraft mc = Minecraft.getMinecraft();
         long time = System.nanoTime() / 1000000L;
         if (OpenGlHelper.shadersSupported && !shaderGroups.isEmpty()) {
            this.updateShaderFrameBuffers(mc);
            GL11.glMatrixMode(5890);
            GL11.glLoadIdentity();

            for(ShaderGroup sg : shaderGroups.values()) {
               GlStateManager.pushMatrix();

               try {
                  sg.render(event.getPartialTicks());
               } catch (Exception ignored) {
               }

               GlStateManager.popMatrix();
            }

            mc.getFramebuffer().bindFramebuffer(true);
         }
      }

   }

   private void updateShaderFrameBuffers(Minecraft mc) {
      if (resetShaders || mc.displayWidth != oldDisplayWidth || oldDisplayHeight != mc.displayHeight) {
         for(ShaderGroup sg : shaderGroups.values()) {
            sg.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
         }

         oldDisplayWidth = mc.displayWidth;
         oldDisplayHeight = mc.displayHeight;
         resetShaders = false;
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void blockHighlight(DrawBlockHighlightEvent event) {
      int ticks = event.getPlayer().ticksExisted;
      RayTraceResult target = event.getTarget();
      if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK || target.getBlockPos() == null) {
         return;
      }
      if (!blockTags.isEmpty()) {
         int x = (Integer)blockTags.get(0);
         int y = (Integer)blockTags.get(1);
         int z = (Integer)blockTags.get(2);
         AspectList ot = (AspectList)blockTags.get(3);
         EnumFacing dir = net.minecraft.util.EnumFacing.byIndex((Integer)blockTags.get(4));
         if (x == target.getBlockPos().getX() && y == target.getBlockPos().getY() && z == target.getBlockPos().getZ()) {
            if (tagscale < 0.5F) {
               tagscale += 0.031F - tagscale / 10.0F;
            }

            this.drawTagsOnContainer((float)target.getBlockPos().getX() + (float)dir.getXOffset() / 2.0F, (float)target.getBlockPos().getY() + (float)dir.getYOffset() / 2.0F, (float)target.getBlockPos().getZ() + (float)dir.getZOffset() / 2.0F, ot, 220, dir, event.getPartialTicks());
         }
      }

      if (!event.getPlayer().inventory.armorInventory.get(3).isEmpty() && event.getPlayer().inventory.armorInventory.get(3).getItem() instanceof IGoggles && ((IGoggles)event.getPlayer().inventory.armorInventory.get(3).getItem()).showIngamePopups(event.getPlayer().inventory.armorInventory.get(3), event.getPlayer())) {
         boolean spaceAbove = event.getPlayer().world.isAirBlock(new net.minecraft.util.math.BlockPos(target.getBlockPos().getX(), target.getBlockPos().getY() + 1, target.getBlockPos().getZ()));
         TileEntity te = event.getPlayer().world.getTileEntity(target.getBlockPos());
         if (te != null) {
            int note = -1;
            if (te instanceof TileEntityNote) {
               note = ((TileEntityNote)te).note;
            } else if (te instanceof TileSensor) {
               note = ((TileSensor)te).note;
            } else if (te instanceof IAspectContainer && ((IAspectContainer)te).getAspects() != null && ((IAspectContainer)te).getAspects().size() > 0) {
               float shift = 0.0F;
               if (te instanceof TileWandPedestal) {
                  shift = 0.6F;
               }

               if (tagscale < 0.3F) {
                  tagscale += 0.031F - tagscale / 10.0F;
               }

               this.drawTagsOnContainer(target.getBlockPos().getX(), (float)target.getBlockPos().getY() + (spaceAbove ? 0.4F : 0.0F) + shift, target.getBlockPos().getZ(), ((IAspectContainer)te).getAspects(), 220, spaceAbove ? net.minecraft.util.EnumFacing.UP : event.getTarget().sideHit, event.getPartialTicks());
            }

            if (note >= 0) {
               if (ticks % 5 == 0) {
                  PacketHandler.INSTANCE.sendToServer(new PacketNote(target.getBlockPos().getX(), target.getBlockPos().getY(), target.getBlockPos().getZ(), event.getPlayer().world.provider.getDimension()));
               }

               this.drawTextInAir(target.getBlockPos().getX(), target.getBlockPos().getY() + 1, target.getBlockPos().getZ(), event.getPartialTicks(), "Note: " + note);
            }
         }
      }

      if (this.wandHandler == null) {
         this.wandHandler = new REHWandHandler();
      }

      if (target.typeOfHit == RayTraceResult.Type.BLOCK && !event.getPlayer().getHeldItemMainhand().isEmpty() && event.getPlayer().getHeldItemMainhand().getItem() instanceof IArchitect && !(event.getPlayer().getHeldItemMainhand().getItem() instanceof ItemFocusBasic) && this.wandHandler.handleArchitectOverlay(event.getPlayer().getHeldItemMainhand(), event, ticks, target)) {
         event.setCanceled(true);
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void renderLast(RenderWorldLastEvent event) {
      if (tagscale > 0.0F) {
         tagscale -= 0.005F;
      }

      float partialTicks = event.getPartialTicks();
      Minecraft mc = Minecraft.getMinecraft();
      if (Minecraft.getMinecraft().player instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
         long time = System.currentTimeMillis();
         if (!player.inventory.getCurrentItem().isEmpty() && (player.inventory.getCurrentItem().getItem() instanceof ItemGolemPlacer || player.inventory.getCurrentItem().getItem() instanceof ItemGolemBell)) {
            this.renderMarkedBlocks(event, partialTicks, player, time);
         }

         if (this.scanCount > time) {
            this.showScannedBlocks(partialTicks, player, time);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void fogDensityEvent(EntityViewRenderEvent.RenderFogEvent event) {
      if (fogFiddled && fogTarget > 0.0F) {
         GL11.glFogi(2917, 2048);
         GL11.glFogf(2914, fogTarget);
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void renderPlayerSpecialsEvent(RenderPlayerEvent.Specials.Pre event) {
      if (event.getEntityPlayer() != null && !event.getEntityPlayer().inventory.armorInventory.get(2).isEmpty() && (event.getEntityPlayer().inventory.armorInventory.get(2).getItem() instanceof ItemFortressArmor || event.getEntityPlayer().inventory.armorInventory.get(2).getItem() instanceof ItemVoidRobeArmor)) {
         // Cape hiding not available in 1.12.2 (event.renderCape was removed)
      }

   }

   public void drawTagsOnContainer(double x, double y, double z, AspectList tags, int bright, EnumFacing dir, float partialTicks) {
      if (Minecraft.getMinecraft().player instanceof EntityPlayer && tags != null && tags.size() > 0) {
         EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
         double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
         double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
         double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
         int e = 0;
         int rowsize = 5;
         int current = 0;
         float shifty = 0.0F;
         int left = tags.size();

         for(Aspect tag : tags.getAspects()) {
            int div = Math.min(left, rowsize);
            if (current >= rowsize) {
               current = 0;
               shifty -= tagscale * 1.05F;
               left -= rowsize;
               if (left < rowsize) {
                  div = left % rowsize;
               }
            }

            float shift = ((float)current - (float)div / 2.0F + 0.5F) * tagscale * 4.0F;
            shift *= tagscale;
            Color color = new Color(tag.getColor());
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.translate(-iPX + x + (double)0.5F + (double)(tagscale * 2.0F * (float)dir.getXOffset()), -iPY + y - (double)shifty + (double)0.5F + (double)(tagscale * 2.0F * (float)dir.getYOffset()), -iPZ + z + (double)0.5F + (double)(tagscale * 2.0F * (float)dir.getZOffset()));
            float xd = (float)(iPX - (x + (double)0.5F));
            float zd = (float)(iPZ - (z + (double)0.5F));
            float rotYaw = (float)(Math.atan2(xd, zd) * (double)180.0F / Math.PI);
            GlStateManager.rotate(rotYaw + 180.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(shift, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(tagscale, tagscale, tagscale);
            if (!Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getName(), tag)) {
               UtilsFX.renderQuadCenteredFromTexture("textures/aspects/_unknown.png", 1.0F, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, bright, 771, 0.75F);
               new Color(11184810);
            } else {
               UtilsFX.renderQuadCenteredFromTexture(tag.getImage(), 1.0F, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, bright, 771, 0.75F);
            }

            if (tags.getAmount(tag) >= 0) {
               String am = "" + tags.getAmount(tag);
               GlStateManager.scale(0.04F, 0.04F, 0.04F);
               GlStateManager.translate(0.0F, 6.0F, -0.1);
               int sw = Minecraft.getMinecraft().fontRenderer.getStringWidth(am);
               GlStateManager.enableBlend();
               Minecraft.getMinecraft().fontRenderer.drawString(am, 14 - sw, 1, 1118481);
               GlStateManager.translate(0.0F, 0.0F, -0.1);
               Minecraft.getMinecraft().fontRenderer.drawString(am, 13 - sw, 0, 16777215);
            }

            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
            ++current;
         }
      }

   }

   public void drawTextInAir(double x, double y, double z, float partialTicks, String text) {
      if (Minecraft.getMinecraft().player instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
         double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
         double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
         double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
         GlStateManager.pushMatrix();
         GlStateManager.translate(-iPX + x + (double)0.5F, -iPY + y + (double)0.5F, -iPZ + z + (double)0.5F);
         float xd = (float)(iPX - (x + (double)0.5F));
         float zd = (float)(iPZ - (z + (double)0.5F));
         float rotYaw = (float)(Math.atan2(xd, zd) * (double)180.0F / Math.PI);
         GlStateManager.rotate(rotYaw + 180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.scale(0.02F, 0.02F, 0.02F);
         int sw = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
         GlStateManager.enableBlend();
         Minecraft.getMinecraft().fontRenderer.drawString(text, 1 - sw / 2, 1, 1118481);
         GlStateManager.translate(0.0F, 0.0F, -0.1);
         Minecraft.getMinecraft().fontRenderer.drawString(text, -sw / 2, 0, 16777215);
         GlStateManager.popMatrix();
      }

   }

   public void startScan(Entity player, int x, int y, int z, long time, int range) {
      this.scannedBlocks = new int[17][17][17];
      this.scanX = x;
      this.scanY = y;
      this.scanZ = z;
      this.scanCount = time;

      for(int xx = -range; xx <= range; ++xx) {
         for(int yy = -range; yy <= range; ++yy) {
            for(int zz = -range; zz <= range; ++zz) {
               int value = -1;
               Block bi = player.world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz)).getBlock();
               if (bi != Blocks.AIR && bi != Blocks.BEDROCK) {
                  if (bi.getDefaultState().getMaterial() == Material.LAVA) {
                     value = -10;
                  } else if (bi.getDefaultState().getMaterial() == Material.WATER) {
                     value = -5;
                  } else {
                     int md = (bi).getMetaFromState(player.world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz)));
                     int[] od = OreDictionary.getOreIDs(new ItemStack(bi, 1, md));
                     boolean ore = false;
                     if (od != null) {
                        for(int id : od) {
                           if (OreDictionary.getOreName(id) != null && OreDictionary.getOreName(id).toUpperCase().contains("ORE")) {
                              ore = true;
                              value = 0;
                              break;
                           }
                        }
                     }

                     if (ore) {
                        try {
                           ScanResult scan = new ScanResult((byte)1, Block.getIdFromBlock(bi), md, null, "");
                           value = ScanManager.getScanAspects(scan, player.world).visSize();
                        } catch (Exception var21) {
                           try {
                              ScanResult scan = new ScanResult((byte)1, Item.getIdFromItem(bi.getItem(player.world, new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz), player.world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz))).getItem()), (bi).getMetaFromState(player.world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz))), null, "");
                              value = ScanManager.getScanAspects(scan, player.world).visSize();
                           } catch (Exception ignored) {
                           }
                        }
                     }
                  }
               }

               this.scannedBlocks[xx + 8][yy + 8][zz + 8] = value;
            }
         }
      }

   }

   public void showScannedBlocks(float partialTicks, EntityPlayer player, long time) {
      Minecraft mc = Minecraft.getMinecraft();
      long dif = this.scanCount - time;
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      GlStateManager.disableDepth();

      for(int xx = -8; xx <= 8; ++xx) {
         for(int yy = -8; yy <= 8; ++yy) {
            for(int zz = -8; zz <= 8; ++zz) {
               int value = this.scannedBlocks[xx + 8][yy + 8][zz + 8];
               float alpha = 1.0F;
               if (dif > 4750L) {
                  alpha = 1.0F - (float)(dif - 4750L) / 5.0F;
               }

               if (dif < 1500L) {
                  alpha = (float)dif / 1500.0F;
               }

               float dist = 1.0F - (float)(xx * xx + yy * yy + zz * zz) / 64.0F;
               alpha *= dist;
               if (value == -5) {
                  this.drawSpecialBlockoverlay(this.scanX + xx, this.scanY + yy, this.scanZ + zz, partialTicks, 3986684, alpha);
               } else if (value == -10) {
                  this.drawSpecialBlockoverlay(this.scanX + xx, this.scanY + yy, this.scanZ + zz, partialTicks, 16734721, alpha);
               } else if (value >= 0) {
                  GlStateManager.pushMatrix();
                  GlStateManager.enableBlend();
                  GlStateManager.blendFunc(770, 1);
                  GlStateManager.alphaFunc(516, 0.003921569F);
                  GlStateManager.disableCull();
                  UtilsFX.bindTexture(TileNodeRenderer.nodetex);
                  this.drawPickScannedObject(this.scanX + xx, this.scanY + yy, this.scanZ + zz, partialTicks, alpha, (int)(time / 50L % 32L), (float)value / 7.0F);
                  GlStateManager.alphaFunc(516, 0.1F);
                  GlStateManager.disableBlend();
                  GlStateManager.enableCull();
                  GlStateManager.popMatrix();
               }
            }
         }
      }

      GlStateManager.enableDepth();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }

   @SideOnly(Side.CLIENT)
   public void drawPickScannedObject(double x, double y, double z, float partialTicks, float alpha, int cframe, float size) {
      GlStateManager.pushMatrix();
      //            UtilsFX.renderFacingStrip
      renderFacingStrip_tweaked
              (x + (double)0.5F, y + (double)0.5F, z + (double)0.5F, 0.0F, 0.2F * size, alpha, 32, 0, cframe, partialTicks, 11184657);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      //            UtilsFX.renderFacingStrip
      renderFacingStrip_tweaked
              (x + (double)0.5F, y + (double)0.5F, z + (double)0.5F, 0.0F, 0.5F * size, alpha, 32, 0, cframe, partialTicks, 11145506);
      GlStateManager.popMatrix();
   }

   public void drawSpecialBlockoverlay(double x, double y, double z, float partialTicks, int color, float alpha) {
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
      double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
      double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
      double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
      float time = (float)(System.nanoTime() / 30000000L);
      Color cc = new Color(color);
      r = (float)cc.getRed() / 255.0F;
      g = (float)cc.getGreen() / 255.0F;
      b = (float)cc.getBlue() / 255.0F;

      for(int side = 0; side < 6; ++side) {
         GlStateManager.pushMatrix();
         EnumFacing dir = net.minecraft.util.EnumFacing.byIndex(side);
         GlStateManager.translate(-iPX + x + (double)0.5F, -iPY + y + (double)0.5F, -iPZ + z + (double)0.5F);
         GlStateManager.rotate(90.0F, (float)(-dir.getYOffset()), (float)dir.getXOffset(), (float)(-dir.getZOffset()));
         if (dir.getZOffset() < 0) {
            GlStateManager.translate(0.0F, 0.0F, 0.5F);
         } else {
            GlStateManager.translate(0.0F, 0.0F, -0.5F);
         }

         GlStateManager.rotate(0.0F, 0.0F, 0.0F, 1.0F);
         UtilsFX.renderQuadCenteredFromTexture("textures/blocks/wardedglass.png", 1.0F, r, g, b, 200, 1, alpha);
         GlStateManager.popMatrix();
      }

   }

   @SideOnly(Side.CLIENT)
   public void renderMarkedBlocks(RenderWorldLastEvent event, float partialTicks, EntityPlayer player, long time) {
      Minecraft mc = Minecraft.getMinecraft();
      if (player.inventory.getCurrentItem().hasTagCompound() && player.inventory.getCurrentItem().getTagCompound().hasKey("markers")) {
         Entity golem = null;
         BlockPos cc = null;
         int face = -1;
         if (player.inventory.getCurrentItem().getItem() instanceof ItemGolemBell) {
            cc = ItemGolemBell.getGolemHomeCoords(player.inventory.getCurrentItem());
            face = ItemGolemBell.getGolemHomeFace(player.inventory.getCurrentItem());
            int gid = ItemGolemBell.getGolemId(player.inventory.getCurrentItem());
            if (gid > -1) {
               golem = player.world.getEntityByID(gid);
            }

            if (!(golem instanceof EntityGolemBase)) {
               return;
            }
         }

         GlStateManager.pushMatrix();
         GlStateManager.alphaFunc(516, 0.003921569F);
         if (golem != null && cc != null && face > -1 && player.getDistanceSq(cc.getX(), cc.getY(), cc.getZ()) < (double)4096.0F) {
            GlStateManager.pushMatrix();
            this.drawGolemHomeOverlay(cc.getX(), cc.getY(), cc.getZ(), face, partialTicks);
            GlStateManager.popMatrix();
         }

         NBTTagList tl = player.inventory.getCurrentItem().getTagCompound().getTagList("markers", 10);

         for(int q = 0; q < tl.tagCount(); ++q) {
            NBTTagCompound nbttagcompound1 = tl.getCompoundTagAt(q);
            double x = nbttagcompound1.getInteger("x");
            double y = nbttagcompound1.getInteger("y");
            double z = nbttagcompound1.getInteger("z");
            int ox = nbttagcompound1.getInteger("x");
            int oy = nbttagcompound1.getInteger("y");
            int oz = nbttagcompound1.getInteger("z");
            int dim = nbttagcompound1.getInteger("dim");
            byte s = nbttagcompound1.getByte("side");
            byte c = nbttagcompound1.getByte("color");
            x += net.minecraft.util.EnumFacing.byIndex(s).getXOffset();
            y += net.minecraft.util.EnumFacing.byIndex(s).getYOffset();
            z += net.minecraft.util.EnumFacing.byIndex(s).getZOffset();
            if (dim == player.world.provider.getDimension() && player.getDistanceSq(x, y, z) < (double)4096.0F) {
               GlStateManager.pushMatrix();
               this.drawMarkerOverlay(x, y, z, s, partialTicks, c);
               GlStateManager.popMatrix();
               if (player.world.isAirBlock(new net.minecraft.util.math.BlockPos(new BlockPos(ox, oy, oz)))) {
                  GlStateManager.pushMatrix();

                  for(int a = 0; a < 6; ++a) {
                     this.drawAirBlockoverlay(ox + net.minecraft.util.EnumFacing.byIndex(a).getXOffset(), oy + net.minecraft.util.EnumFacing.byIndex(a).getYOffset(), oz + net.minecraft.util.EnumFacing.byIndex(a).getZOffset(), a, partialTicks, c);
                  }

                  GlStateManager.popMatrix();
               }

               if (golem != null && Config.golemLinkQuality > 3) {
                  x -= (double)net.minecraft.util.EnumFacing.byIndex(s).getXOffset() * (double)0.5F;
                  y -= (double)net.minecraft.util.EnumFacing.byIndex(s).getYOffset() * (double)0.5F;
                  z -= (double)net.minecraft.util.EnumFacing.byIndex(s).getZOffset() * (double)0.5F;
                  GlStateManager.pushMatrix();
                  this.drawMarkerLine(x, y, z, s, partialTicks, c, golem);
                  GlStateManager.popMatrix();
               }
            }
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.popMatrix();
      }

   }

   public void drawAirBlockoverlay(double x, double y, double z, int side, float partialTicks, int color) {
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
      double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
      double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
      double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
      float time = (float)(System.nanoTime() / 30000000L);
      if (color == -1) {
         r = MathHelper.sin(time % 32767.0F / 12.0F + (float)side) * 0.2F + 0.8F;
         g = MathHelper.sin(time % 32767.0F / 14.0F + (float)side) * 0.2F + 0.8F;
         b = MathHelper.sin(time % 32767.0F / 16.0F + (float)side) * 0.2F + 0.8F;
      } else {
         Color cc = new Color(UtilsFX.colors[color]);
         r = (float)cc.getRed() / 255.0F;
         g = (float)cc.getGreen() / 255.0F;
         b = (float)cc.getBlue() / 255.0F;
      }

      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      GlStateManager.disableCull();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      EnumFacing dir = net.minecraft.util.EnumFacing.byIndex(side);
      GlStateManager.translate(-iPX + x + (double)0.5F - (double)((float)dir.getXOffset() * 0.01F), -iPY + y + (double)0.5F - (double)((float)dir.getYOffset() * 0.01F), -iPZ + z + (double)0.5F - (double)((float)dir.getZOffset() * 0.01F));
      GlStateManager.rotate(90.0F, (float)(-dir.getYOffset()), (float)dir.getXOffset(), (float)(-dir.getZOffset()));
      GlStateManager.pushMatrix();
      if (dir.getZOffset() < 0) {
         GlStateManager.translate(0.0F, 0.0F, 0.5F);
      } else {
         GlStateManager.translate(0.0F, 0.0F, -0.5F);
      }

      GlStateManager.rotate(0.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scale(0.98F, 0.98F, 0.98F);
      UtilsFX.renderQuadCenteredFromTexture("textures/blocks/empty.png", 1.0F, r, g, b, 200, 1, 1.0F);
      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }

   public void drawMarkerOverlay(double x, double y, double z, int side, float partialTicks, int color) {
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
      double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
      double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
      double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
      float time = (float)(System.nanoTime() / 30000000L);
      if (color == -1) {
         r = MathHelper.sin(time % 32767.0F / 12.0F + (float)side) * 0.2F + 0.8F;
         g = MathHelper.sin(time % 32767.0F / 14.0F + (float)side) * 0.2F + 0.8F;
         b = MathHelper.sin(time % 32767.0F / 16.0F + (float)side) * 0.2F + 0.8F;
      } else {
         Color cc = new Color(UtilsFX.colors[color]);
         r = (float)cc.getRed() / 255.0F;
         g = (float)cc.getGreen() / 255.0F;
         b = (float)cc.getBlue() / 255.0F;
      }

      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      GlStateManager.disableCull();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      EnumFacing dir = net.minecraft.util.EnumFacing.byIndex(side);
      GlStateManager.translate(-iPX + x + (double)0.5F + (double)((float)dir.getXOffset() * 0.01F), -iPY + y + (double)0.5F + (double)((float)dir.getYOffset() * 0.01F), -iPZ + z + (double)0.5F + (double)((float)dir.getZOffset() * 0.01F));
      GlStateManager.rotate(90.0F, (float)(-dir.getYOffset()), (float)dir.getXOffset(), (float)(-dir.getZOffset()));
      GlStateManager.pushMatrix();
      if (dir.getZOffset() < 0) {
         GlStateManager.translate(0.0F, 0.0F, 0.5F);
      } else {
         GlStateManager.translate(0.0F, 0.0F, -0.5F);
      }

      GlStateManager.rotate(0.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scale(0.4F, 0.4F, 0.4F);
      UtilsFX.renderQuadCenteredFromTexture("textures/misc/mark.png", 1.0F, r, g, b, 200, 1, 1.0F);
      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }

   public void drawGolemHomeOverlay(double x, double y, double z, int side, float partialTicks) {
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
      double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
      double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
      double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
      float time = (float)(System.nanoTime() / 30000000L);
      r = MathHelper.sin(time % 32767.0F / 12.0F + (float)side) * 0.2F + 0.8F;
      g = MathHelper.sin(time % 32767.0F / 14.0F + (float)side) * 0.2F + 0.8F;
      b = MathHelper.sin(time % 32767.0F / 16.0F + (float)side) * 0.2F + 0.8F;
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      GlStateManager.disableCull();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      EnumFacing dir = net.minecraft.util.EnumFacing.byIndex(side);
      GlStateManager.translate(-iPX + x + (double)0.5F + (double)((float)dir.getXOffset() * 0.01F), -iPY + y + (double)0.5F + (double)((float)dir.getYOffset() * 0.01F), -iPZ + z + (double)0.5F + (double)((float)dir.getZOffset() * 0.01F));
      GlStateManager.rotate(90.0F, (float)(-dir.getYOffset()), (float)dir.getXOffset(), (float)(-dir.getZOffset()));
      GlStateManager.pushMatrix();
      if (dir.getZOffset() < 0) {
         GlStateManager.translate(0.0F, 0.0F, 0.5F);
      } else {
         GlStateManager.translate(0.0F, 0.0F, -0.5F);
      }

      GlStateManager.rotate(0.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scale(0.65F, 0.65F, 0.65F);
      UtilsFX.renderQuadCenteredFromTexture("textures/misc/home.png", 1.0F, r, g, b, 200, 1, 1.0F);
      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }

   public void drawMarkerLine(double x, double y, double z, int side, float partialTicks, int color, Entity cc) {
      EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
      double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
      double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
      double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
      double ePX = cc.prevPosX + (cc.posX - cc.prevPosX) * (double)partialTicks;
      double ePY = cc.prevPosY + (cc.posY - cc.prevPosY) * (double)partialTicks;
      double ePZ = cc.prevPosZ + (cc.posZ - cc.prevPosZ) * (double)partialTicks;
      GlStateManager.translate(-iPX + ePX, -iPY + ePY + (double)cc.height, -iPZ + ePZ);
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      float time = (float)(System.nanoTime() / 30000000L);
      if (color > -1) {
         Color co = new Color(UtilsFX.colors[color]);
         r = (float)co.getRed() / 255.0F;
         g = (float)co.getGreen() / 255.0F;
         b = (float)co.getBlue() / 255.0F;
      }

      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      double ds1y = ePY + (double)cc.height;
      double dd1x = x + (double)0.5F + (double)net.minecraft.util.EnumFacing.byIndex(side).getXOffset() * (double)0.5F;
      double dd1y = y + (double)0.5F + (double)net.minecraft.util.EnumFacing.byIndex(side).getYOffset() * (double)0.5F;
      double dd1z = z + (double)0.5F + (double)net.minecraft.util.EnumFacing.byIndex(side).getZOffset() * (double)0.5F;
      double dc1x = (float)(dd1x - ePX);
      double dc1y = (float)(dd1y - ds1y);
      double dc1z = (float)(dd1z - ePZ);
      double ds2x = x + (double)0.5F;
      double ds2y = y + (double)0.5F;
      double ds2z = z + (double)0.5F;
      double dc22x = (float)(ds2x - ePX);
      double dc22y = (float)(ds2y - ds1y);
      double dc22z = (float)(ds2z - ePZ);
      UtilsFX.bindTexture("textures/misc/script.png");
      GlStateManager.disableCull();
      buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX); 
      float f4 = 0.0F;
      double dx2 = 0.0F;
      double dy2 = 0.0F;
      double dz2 = 0.0F;
      double d3 = x - ePX;
      double d4 = y - ePY;
      double d5 = z - ePZ;
      float dist = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
      float blocks = (float)Math.round(dist);
      float length = blocks * (float)Config.golemLinkQuality;
      float f9 = 0.0F;
      float f10 = 1.0F;
      int count = 0;

      for(int i = 0; (float)i <= length; ++i) {
         float f2 = (float)i / length;
         float f2a = (float)i * 1.5F / length;
         f2a = Math.min(0.75F, f2a);
         float f3 = 1.0F - Math.abs((float)i - length / 2.0F) / (length / 2.0F);
         f4 = 0.0F;
         if (color == -1) {
            r = MathHelper.sin(time % 32767.0F / 12.0F + (float)side + (float)i) * 0.2F + 0.8F;
            g = MathHelper.sin(time % 32767.0F / 14.0F + (float)side + (float)i) * 0.2F + 0.8F;
            b = MathHelper.sin(time % 32767.0F / 16.0F + (float)side + (float)i) * 0.2F + 0.8F;
         }

         double dx = dc1x + (double)(MathHelper.sin((float)(((double)(side * 20) + z % (double)16.0F + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality) - (double)(time % 32767.0F / 5.0F)) / (double)4.0F)) * 0.5F * f3);
         double dy = dc1y + (double)(MathHelper.sin((float)(((double)(side * 20) + x % (double)16.0F + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality) - (double)(time % 32767.0F / 5.0F)) / (double)3.0F)) * 0.5F * f3);
         double dz = dc1z + (double)(MathHelper.sin((float)(((double)(side * 20) + y % (double)16.0F + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality) - (double)(time % 32767.0F / 5.0F)) / (double)2.0F)) * 0.5F * f3);
         if ((float)i > length - (float)(Config.golemLinkQuality / 2)) {
            dx2 = dc22x + (double)(MathHelper.sin((float)(((double)(side * 20) + z % (double)16.0F + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality) - (double)(time % 32767.0F / 5.0F)) / (double)4.0F)) * 0.5F * f3);
            dy2 = dc22y + (double)(MathHelper.sin((float)(((double)(side * 20) + x % (double)16.0F + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality) - (double)(time % 32767.0F / 5.0F)) / (double)3.0F)) * 0.5F * f3);
            dz2 = dc22z + (double)(MathHelper.sin((float)(((double)(side * 20) + y % (double)16.0F + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality) - (double)(time % 32767.0F / 5.0F)) / (double)2.0F)) * 0.5F * f3);
            f3 = (length - (float)i) / ((float)Config.golemLinkQuality / 2.0F);
            f4 = 1.0F - f3;
            dx = dx * (double)f3 + dx2 * (double)f4;
            dy = dy * (double)f3 + dy2 * (double)f4;
            dz = dz * (double)f3 + dz2 * (double)f4;
         }

         float f13 = (1.0F - f2) * dist - time * 0.005F;
         buffer.pos(dx * (double)f2, dy * (double)f2 - 0.05, dz * (double)f2).tex(f13, f10).color(r, g, b, f2a * (1.0F - f4))
        .endVertex();
         buffer.pos(dx * (double)f2, dy * (double)f2 + 0.05, dz * (double)f2).tex(f13, f9).color(r, g, b, f2a * (1.0F - f4))
        .endVertex();
      }

      tessellator.draw();
      GlStateManager.enableCull();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
   }

   protected void renderVignette(float brightness, double sw, double sh) {
      int k = (int)sw;
      int l = (int)sh;
      brightness = 1.0F - brightness;
      prevVignetteBrightness = (float)((double)prevVignetteBrightness + (double)(brightness - prevVignetteBrightness) * 0.01);
      if (prevVignetteBrightness > 0.0F) {
         float b = prevVignetteBrightness * (1.0F + MathHelper.sin((float)Minecraft.getMinecraft().player.ticksExisted / 2.0F) * 0.1F);
         GlStateManager.pushMatrix();
         GlStateManager.clear(256);
         GL11.glMatrixMode(5889);
         GL11.glLoadIdentity();
         GL11.glOrtho(0.0F, sw, sh, 0.0F, 1000.0F, 3000.0F);
         Minecraft.getMinecraft().getTextureManager().bindTexture(vignetteTexPath);
         GL11.glMatrixMode(5888);
         GL11.glLoadIdentity();
         GlStateManager.translate(0.0F, 0.0F, -2000.0F);
         GlStateManager.disableDepth();
         GlStateManager.depthMask(false);
         OpenGlHelper.glBlendFunc(0, 769, 1, 0);
         GlStateManager.color(b, b, b, 1.0F);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
         buffer.pos(0.0F, l, -90.0F).tex(0.0F, 1.0F)
        .endVertex();
         buffer.pos(k, l, -90.0F).tex(1.0F, 1.0F)
        .endVertex();
         buffer.pos(k, 0.0F, -90.0F).tex(1.0F, 0.0F)
        .endVertex();
         buffer.pos(0.0F, 0.0F, -90.0F).tex(0.0F, 0.0F)
        .endVertex();
         tessellator.draw();
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         OpenGlHelper.glBlendFunc(770, 771, 1, 0);
         GlStateManager.popMatrix();
      }

   }

   @SubscribeEvent
   public void livingTick(LivingEvent.LivingUpdateEvent event) {
      if (event.getEntity().world.isRemote && event.getEntity() instanceof EntityMob && !event.getEntity().isDead) {
         EntityMob mob = (EntityMob)event.getEntity();
         int t = (int)mob.getEntityAttribute(EntityUtils.CHAMPION_MOD).getAttributeValue();
         if (t >= 0) {
            ChampionModifier.mods[t].effect.showFX(mob);
         }
      }

   }
}
