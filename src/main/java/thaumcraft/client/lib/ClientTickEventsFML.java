package thaumcraft.client.lib;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.util.JsonException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.common.lib.potions.PotionDeathGaze;
import thaumcraft.common.lib.potions.PotionBlurredVision;
import thaumcraft.common.lib.potions.PotionUnnaturalHunger;
import thaumcraft.common.lib.potions.PotionSunScorned;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.gui.GuiResearchPopup;
import thaumcraft.client.gui.GuiResearchRecipe;
import thaumcraft.client.gui.MappingThread;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.relics.ItemSanityChecker;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.items.wands.foci.ItemFocusTrade;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.events.EssentiaHandler;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.tiles.TileInfusionMatrix;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

public class ClientTickEventsFML {
   public static GuiResearchPopup researchPopup = null;
   public int tickCount = 0;
   int prevWorld;
   boolean checkedDate = false;
   final ResourceLocation HUD = new ResourceLocation("thaumcraft", "textures/gui/hud.png");
   RenderItem ri = null;
   private RenderItem getRi() { if (ri == null) ri = Minecraft.getMinecraft().getRenderItem(); return ri; }
   DecimalFormat myFormatter = new DecimalFormat("#######.##");
   DecimalFormat myFormatter2 = new DecimalFormat("#######.#");
   HashMap<Integer,AspectList> oldvals = new HashMap<>();
   long nextsync = 0L;
   boolean startThread = false;
   public static int warpVignette = 0;
   private static final int SHADER_DESAT = 0;
   private static final int SHADER_BLUR = 1;
   private static final int SHADER_HUNGER = 2;
   private static final int SHADER_SUNSCORNED = 3;
   ResourceLocation[] shader_resources = new ResourceLocation[]{new ResourceLocation("shaders/post/desaturatetc.json"), new ResourceLocation("shaders/post/blurtc.json"), new ResourceLocation("shaders/post/hunger.json"), new ResourceLocation("shaders/post/sunscorned.json")};
   ItemStack lastItem = null;
   int lastCount = 0;

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void playerTick(TickEvent.PlayerTickEvent event) {
      if (event.side != Side.SERVER) {
         if (event.phase == Phase.START) {
            if (!this.startThread && GuiResearchRecipe.cache.isEmpty()) {
               Map<String, Integer> idMappings = Maps.newHashMap();
               // ForgeRegistries.BLOCKS.serializeInto(idMappings);
               // ForgeRegistries.ITEMS.serializeInto(idMappings);
               Thread t = new Thread(new MappingThread(idMappings));
               t.start();
               this.startThread = true;
            }

            Minecraft mc = Minecraft.getMinecraft();

            try {
               if (event.player.getEntityId() == mc.player.getEntityId()) {
                  this.checkShaders(event, mc);
                  if (warpVignette > 0) {
                     --warpVignette;
                     RenderEventHandler.targetBrightness = 0.0F;
                  } else {
                     RenderEventHandler.targetBrightness = 1.0F;
                  }

                  if (RenderEventHandler.fogFiddled) {
                     if (RenderEventHandler.fogDuration < 100) {
                        RenderEventHandler.fogTarget = 0.1F * ((float)RenderEventHandler.fogDuration / 100.0F);
                     } else if (RenderEventHandler.fogTarget < 0.1F) {
                        RenderEventHandler.fogTarget += 0.001F;
                     }

                     --RenderEventHandler.fogDuration;
                     if (RenderEventHandler.fogDuration < 0) {
                        RenderEventHandler.fogFiddled = false;
                     }
                  }
               }
            } catch (Exception ignored) {
            }
         }

      }
   }

   private void checkShaders(TickEvent.PlayerTickEvent event, Minecraft mc) {
      if (event.player.isPotionActive(PotionDeathGaze.instance)) {
         warpVignette = 10;
         if (!RenderEventHandler.shaderGroups.containsKey(0)) {
            try {
               this.setShader(new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), this.shader_resources[0]), 0);
            } catch (Exception ignored) {
            }
         }
      } else if (RenderEventHandler.shaderGroups.containsKey(0)) {
         this.deactivateShader(0);
      }

      if (event.player.isPotionActive(PotionBlurredVision.instance)) {
         if (!RenderEventHandler.shaderGroups.containsKey(1)) {
            try {
               this.setShader(new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), this.shader_resources[1]), 1);
            } catch (Exception ignored) {
            }
         }
      } else if (RenderEventHandler.shaderGroups.containsKey(1)) {
         this.deactivateShader(1);
      }

      if (event.player.isPotionActive(PotionUnnaturalHunger.instance)) {
         if (!RenderEventHandler.shaderGroups.containsKey(2)) {
            try {
               this.setShader(new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), this.shader_resources[2]), 2);
            } catch (Exception ignored) {
            }
         }
      } else if (RenderEventHandler.shaderGroups.containsKey(2)) {
         this.deactivateShader(2);
      }

      if (event.player.isPotionActive(PotionSunScorned.instance)) {
         if (!RenderEventHandler.shaderGroups.containsKey(3)) {
            try {
               this.setShader(new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), this.shader_resources[3]), 3);
            } catch (Exception ignored) {
            }
         }
      } else if (RenderEventHandler.shaderGroups.containsKey(3)) {
         this.deactivateShader(3);
      }

   }

   void setShader(ShaderGroup target, int shaderId) {
      if (OpenGlHelper.shadersSupported) {
         Minecraft mc = Minecraft.getMinecraft();
         if (RenderEventHandler.shaderGroups.containsKey(shaderId)) {
            RenderEventHandler.shaderGroups.get(shaderId).deleteShaderGroup();
            RenderEventHandler.shaderGroups.remove(shaderId);
         }

         try {
            if (target == null) {
               this.deactivateShader(shaderId);
            } else {
               RenderEventHandler.resetShaders = true;
               RenderEventHandler.shaderGroups.put(shaderId, target);
            }
         } catch (Exception var5) {
            RenderEventHandler.shaderGroups.remove(shaderId);
         }
      }

   }

   public void deactivateShader(int shaderId) {
      if (RenderEventHandler.shaderGroups.containsKey(shaderId)) {
         RenderEventHandler.shaderGroups.get(shaderId).deleteShaderGroup();
      }

      RenderEventHandler.shaderGroups.remove(shaderId);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void clientWorldTick(TickEvent.ClientTickEvent event) {
      if (event.side != Side.SERVER) {
         Minecraft mc = FMLClientHandler.instance().getClient();
         World world = mc.world;
         if (event.phase == Phase.START) {
            ++this.tickCount;

            for(String fxk : EssentiaHandler.sourceFX.keySet().toArray(new String[0])) {
               EssentiaHandler.EssentiaSourceFX fx = EssentiaHandler.sourceFX.get(fxk);
               if (fx.ticks <= 0) {
                  EssentiaHandler.sourceFX.remove(fxk);
               } else if (world != null) {
                  int mod = 0;
                  TileEntity tile = world.getTileEntity(new BlockPos(fx.start.getX(), fx.start.getY(), fx.start.getZ()));
                  if (tile instanceof TileInfusionMatrix) {
                     mod = -1;
                  }

                  if (fx.ticks > 5) {
                     Thaumcraft.proxy.essentiaTrailFx(world, fx.end.getX(), fx.end.getY(), fx.end.getZ(), fx.start.getX(), fx.start.getY() + mod, fx.start.getZ(), this.tickCount, fx.color, 1.0F);
                  } else {
                     float scale = (float)(fx.ticks * fx.ticks) / 25.0F;
                     Thaumcraft.proxy.essentiaTrailFx(world, fx.end.getX(), fx.end.getY(), fx.end.getZ(), fx.start.getX(), fx.start.getY() + mod, fx.start.getZ(), this.tickCount - (5 - fx.ticks), fx.color, scale);
                  }

                  --fx.ticks;
                  EssentiaHandler.sourceFX.put(fxk, fx);
               }
            }
         } else if (mc.world != null && !this.checkedDate) {
            this.checkedDate = true;
            Calendar calendar = mc.world.getCurrentDate();
            if (calendar.get(Calendar.MONTH) + 1 == 10 && calendar.get(Calendar.DATE) == 31) {
               Thaumcraft.isHalloween = true;
            }
         }

      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void renderTick(TickEvent.RenderTickEvent event) {
      Minecraft mc = FMLClientHandler.instance().getClient();
      World world = mc.world;
      if (event.phase != Phase.START && Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
         long time = System.currentTimeMillis();
         if (researchPopup == null) {
            researchPopup = new GuiResearchPopup(mc);
         }

         researchPopup.updateResearchWindow();
         GuiScreen gui = mc.currentScreen;
         if (gui instanceof GuiContainer && (GuiScreen.isShiftKeyDown() && !Config.showTags || !GuiScreen.isShiftKeyDown() && Config.showTags) && !Mouse.isGrabbed()) {
            this.renderAspectsInGui((GuiContainer)gui, player);
         }

         if (player != null && mc.inGameHasFocus && Minecraft.isGuiEnabled()) {
            if (player.inventory.armorItemInSlot(2) != null && player.inventory.armorItemInSlot(2).getItem() == ConfigItems.itemHoverHarness) {
               this.renderHoverHUD(event.renderTickTime, player, time, player.inventory.armorItemInSlot(2));
            }

            if (!player.capabilities.isCreativeMode && Thaumcraft.instance.runicEventHandler.runicCharge.containsKey(player.getEntityId()) && Thaumcraft.instance.runicEventHandler.runicCharge.get(player.getEntityId()) > 0 && Thaumcraft.instance.runicEventHandler.runicInfo.containsKey(player.getEntityId())) {
               this.renderRunicArmorBar(event.renderTickTime, player, time);
            }

            if (player.inventory.getCurrentItem() != null) {
               if (player.inventory.getCurrentItem().getItem() instanceof ItemWandCasting) {
                  this.renderCastingWandHud(event.renderTickTime, player, time, player.inventory.getCurrentItem());
               } else if (player.inventory.getCurrentItem().getItem() instanceof ItemSanityChecker) {
                  this.renderSanityHud(event.renderTickTime, player, time);
               }
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   private void renderSanityHud(Float partialTicks, EntityPlayer player, long time) {
      Minecraft mc = Minecraft.getMinecraft();
      GlStateManager.pushMatrix();
      ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
      GlStateManager.clear(256);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glOrtho(0.0F, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0F, 1000.0F, 3000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      int k = sr.getScaledWidth();
      int l = sr.getScaledHeight();
      GlStateManager.translate(0.0F, 0.0F, -2000.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      mc.renderEngine.bindTexture(this.HUD);
      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.drawTexturedQuad(1, 1, 152, 0, 20, 76, -90.0F);
      GlStateManager.popMatrix();
      float tw = (float)Thaumcraft.proxy.getPlayerKnowledge().getWarpTotal(player.getName());
      int p = Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(player.getName());
      int s = Thaumcraft.proxy.getPlayerKnowledge().getWarpSticky(player.getName());
      int t = Thaumcraft.proxy.getPlayerKnowledge().getWarpTemp(player.getName());
      float mod = 1.0F;
      if (tw > 100.0F) {
         mod = 100.0F / tw;
         tw = 100.0F;
      }

      int gap = (int)((100.0F - tw) / 100.0F * 48.0F);
      int wt = (int)((float)t / 100.0F * 48.0F * mod);
      int ws = (int)((float)s / 100.0F * 48.0F * mod);
      if (t > 0) {
         GlStateManager.pushMatrix();
         GlStateManager.color(1.0F, 0.5F, 1.0F, 1.0F);
         UtilsFX.drawTexturedQuad(7, 21 + gap, 200, gap, 8, wt + gap, -90.0F);
         GlStateManager.popMatrix();
      }

      if (s > 0) {
         GlStateManager.pushMatrix();
         GlStateManager.color(0.75F, 0.0F, 0.75F, 1.0F);
         UtilsFX.drawTexturedQuad(7, 21 + wt + gap, 200, wt + gap, 8, wt + ws + gap, -90.0F);
         GlStateManager.popMatrix();
      }

      if (p > 0) {
         GlStateManager.pushMatrix();
         GlStateManager.color(0.5F, 0.0F, 0.5F, 1.0F);
         UtilsFX.drawTexturedQuad(7, 21 + wt + ws + gap, 200, wt + ws + gap, 8, 48, -90.0F);
         GlStateManager.popMatrix();
      }

      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.drawTexturedQuad(1, 1, 176, 0, 20, 76, -90.0F);
      GlStateManager.popMatrix();
      if (tw >= 100.0F) {
         GlStateManager.pushMatrix();
         UtilsFX.drawTexturedQuad(1, 1, 216, 0, 20, 16, -90.0F);
         GlStateManager.popMatrix();
      }

      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   @SideOnly(Side.CLIENT)
   private void renderCastingWandHud(Float partialTicks, EntityPlayer player, long time, ItemStack wandstack) {
      Minecraft mc = Minecraft.getMinecraft();
      ItemWandCasting wand = (ItemWandCasting)wandstack.getItem();
      if (this.oldvals.get(player.inventory.currentItem) == null) {
         this.oldvals.put(player.inventory.currentItem, wand.getAllVis(wandstack));
      } else if (this.nextsync <= time) {
         this.oldvals.put(player.inventory.currentItem, wand.getAllVis(wandstack));
         this.nextsync = time + 1000L;
      }

      GlStateManager.pushMatrix();
      ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
      GlStateManager.clear(256);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glOrtho(0.0F, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0F, 1000.0F, 3000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      int k = sr.getScaledWidth();
      int l = sr.getScaledHeight();
      int dailLocation = Config.dialBottom ? l - 32 : 0;
      GlStateManager.translate(0.0F, (float)dailLocation, -2000.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      mc.renderEngine.bindTexture(this.HUD);
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5F, 0.5F, 0.5F);
      UtilsFX.drawTexturedQuad(0, 0, 0, 0, 64, 64, -90.0F);
      GlStateManager.popMatrix();
      GlStateManager.translate(16.0F, 16.0F, 0.0F);
      int max = wand.getMaxVis(wandstack);
      ItemFocusBasic focus = wand.getFocus(wandstack);
      ItemStack focusStack = wand.getFocusItem(wandstack);
      int count = 0;
      AspectList aspects = wand.getAllVis(wandstack);

      for(Aspect aspect : aspects.getAspects()) {
         int amt = aspects.getAmount(aspect);
         GlStateManager.pushMatrix();
         if (!Config.dialBottom) {
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
         }

         GlStateManager.rotate((float)(-15 + count * 24), 0.0F, 0.0F, 1.0F);
         GlStateManager.translate(0.0F, -32.0F, 0.0F);
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         int loc = (int)(30.0F * (float)amt / (float)max);
         GlStateManager.pushMatrix();
         Color ac = new Color(aspect.getColor());
         GlStateManager.color((float)ac.getRed() / 255.0F, (float)ac.getGreen() / 255.0F, (float)ac.getBlue() / 255.0F, 0.8F);
         UtilsFX.drawTexturedQuad(-4, 35 - loc, 104, 0, 8, loc, -90.0F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         UtilsFX.drawTexturedQuad(-8, -3, 72, 0, 16, 42, -90.0F);
         GlStateManager.popMatrix();
         int sh = 0;
         if (focus != null && focus.getVisCost(focusStack).getAmount(aspect) > 0) {
            GlStateManager.pushMatrix();
            UtilsFX.drawTexturedQuad(-4, -8, 136, 0, 8, 8, -90.0F);
            sh = 8;
            GlStateManager.popMatrix();
         }

         if (this.oldvals.get(player.inventory.currentItem).getAmount(aspect) > amt) {
            GlStateManager.pushMatrix();
            UtilsFX.drawTexturedQuad(-4, -8 - sh, 128, 0, 8, 8, -90.0F);
            GlStateManager.popMatrix();
         } else if (this.oldvals.get(player.inventory.currentItem).getAmount(aspect) < amt) {
            GlStateManager.pushMatrix();
            UtilsFX.drawTexturedQuad(-4, -8 - sh, 120, 0, 8, 8, -90.0F);
            GlStateManager.popMatrix();
         }

         if (player.isSneaking()) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            String msg = amt / 100 + "";
            mc.ingameGUI.drawString(mc.fontRenderer, msg, -32, -4, 16777215);
            GlStateManager.popMatrix();
            if (focus != null && focus.getVisCost(focusStack).getAmount(aspect) > 0) {
               float mod = wand.getConsumptionModifier(wandstack, player, aspect, false);
               GlStateManager.pushMatrix();
               GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
               msg = this.myFormatter.format((float)focus.getVisCost(focusStack).getAmount(aspect) * mod / 100.0F);
               mc.ingameGUI.drawString(mc.fontRenderer, msg, 8, -4, 16777215);
               GlStateManager.popMatrix();
            }

            mc.renderEngine.bindTexture(this.HUD);
         }

         GlStateManager.popMatrix();
         ++count;
      }

      if (focus != null) {
         ItemStack picked = null;
         if (focus instanceof ItemFocusTrade) {
            ItemFocusTrade wt = (ItemFocusTrade)focus;
            picked = wt.getPickedBlock(player.inventory.getCurrentItem());
            if (picked != null) {
               this.renderWandTradeHud(partialTicks, player, time, picked);
            }
         }

         if (picked == null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-24.0F, -24.0F, 90.0F);
            GlStateManager.enableLighting();
            getRi().renderItemAndEffectIntoGUI(wand.getFocusItem(wandstack), 16, 16);
            GlStateManager.disableLighting();
            GlStateManager.popMatrix();
            float f = WandManager.getCooldown(player);
            if (f > 0.0F) {
               GlStateManager.pushMatrix();
               GlStateManager.translate(0.0F, 0.0F, 150.0F);
               GlStateManager.scale(0.5F, 0.5F, 0.5F);
               String secs = this.myFormatter2.format(f) + "s";
               int w = mc.fontRenderer.getStringWidth(secs) / 2;
               mc.ingameGUI.drawString(mc.fontRenderer, secs, -w, -4, 16777215);
               GlStateManager.popMatrix();
            }
         }
      }

      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   @SideOnly(Side.CLIENT)
   public void renderRunicArmorBar(float partialTicks, EntityPlayer player, long time) {
      Minecraft mc = Minecraft.getMinecraft();
      float total = (float)((Integer[])Thaumcraft.instance.runicEventHandler.runicInfo.get(player.getEntityId()))[0];
      float current = (float) Thaumcraft.instance.runicEventHandler.runicCharge.get(player.getEntityId());
      GlStateManager.pushMatrix();
      ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
      GlStateManager.clear(256);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glOrtho(0.0F, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0F, 1000.0F, 3000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      GlStateManager.translate(0.0F, 0.0F, -2000.0F);
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableAlpha();
      int k = sr.getScaledWidth();
      int l = sr.getScaledHeight();
      GlStateManager.translate((float)(k / 2 - 91), (float)(l - 39), 0.0F);
      mc.renderEngine.bindTexture(ParticleEngine.particleTexture);
      float fill = current / total;

      for(int a = 0; (float)a < fill * 10.0F; ++a) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         UtilsFX.drawTexturedQuad(a * 8, 0, 160, 16, 9, 9, -90.0F);
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         GlStateManager.color(1.0F, 0.75F, 0.24F, MathHelper.sin((float)player.ticksExisted / 4.0F + (float)a) * 0.4F + 0.6F);
         UtilsFX.drawTexturedQuad(a * 16, 0, a * 16, 96, 16, 16, -90.0F);
         GlStateManager.popMatrix();
      }

      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }

   @SideOnly(Side.CLIENT)
   public void renderHoverHUD(float partialTicks, EntityPlayer player, long time, ItemStack armor) {
      Minecraft mc = Minecraft.getMinecraft();
      GlStateManager.pushMatrix();
      ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
      GlStateManager.clear(256);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glOrtho(0.0F, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0F, 1000.0F, 3000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      GlStateManager.translate(0.0F, 0.0F, -2000.0F);
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableAlpha();
      int k = sr.getScaledWidth();
      int l = sr.getScaledHeight();
      int fuel = 0;
      if (armor.hasTagCompound() && armor.getTagCompound().hasKey("jar")) {
         ItemStack jar = new ItemStack(armor.getTagCompound().getCompoundTag("jar"));
         if (!jar.isEmpty() && jar.getItem() instanceof ItemJarFilled && jar.hasTagCompound()) {
            AspectList aspects = ((ItemJarFilled)jar.getItem()).getAspects(jar);
            if (aspects != null && aspects.size() > 0) {
               fuel = (short)aspects.getAmount(Aspect.ENERGY);
            }
         }
      }

      int level = Math.round((float)fuel / 64.0F * 48.0F);
      mc.renderEngine.bindTexture(ParticleEngine.particleTexture);
      GlStateManager.color(0.0F, 1.0F, 0.75F, 1.0F);
      UtilsFX.drawTexturedQuad(6, l / 2 + 24 - level, 224, 48 - level, 8, level, -91.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.drawTexturedQuad(5, l / 2 - 28, 240, 0, 10, 56, -90.0F);
      if (armor.hasTagCompound() && armor.getTagCompound().hasKey("hover") && armor.getTagCompound().getByte("hover") == 1) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 0.66F);
         UtilsFX.drawTexturedQuad(2, l / 2 - 43, 16 * ((int)(Minecraft.getSystemTime() % 700L) / 50), 32, 16, 16, -90.0F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }

      try {
         getRi().renderItemAndEffectIntoGUI(armor, 2, l / 2 - 43);
      } catch (Exception ignored) {
      }

      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }

   @SideOnly(Side.CLIENT)
   public void renderWandTradeHud(float partialTicks, EntityPlayer player, long time, ItemStack picked) {
      Minecraft mc = Minecraft.getMinecraft();
      int amount = this.lastCount;
      if (!picked.isItemEqual(this.lastItem)) {
         amount = 0;

         for(ItemStack is : player.inventory.mainInventory) {
            if (is != null && is.isItemEqual(picked)) {
               amount += is.getCount();
            }
         }

         this.lastItem = picked;
      }

      this.lastCount = amount;
      GlStateManager.pushMatrix();
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.disableLighting();
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableColorMaterial();
      GlStateManager.enableLighting();

      try {
         getRi().renderItemAndEffectIntoGUI(picked, -8, -8);
      } catch (Exception ignored) {
      }

      GlStateManager.disableLighting();
      GlStateManager.pushMatrix();
      String am = "" + amount;
      int sw = mc.fontRenderer.getStringWidth(am);
      GlStateManager.translate(0.0F, (float)(-mc.fontRenderer.FONT_HEIGHT), 500.0F);
      GlStateManager.scale(0.5F, 0.5F, 0.5F);

      for(int a = -1; a <= 1; ++a) {
         for(int b = -1; b <= 1; ++b) {
            if ((a == 0 || b == 0) && (a != 0 || b != 0)) {
               mc.fontRenderer.drawString(am, a + 16 - sw, b + 24, 0);
            }
         }
      }

      mc.fontRenderer.drawString(am, 16 - sw, 24, 16777215);
      GlStateManager.popMatrix();
      GlStateManager.popMatrix();
   }

   public void renderAspectsInGui(GuiContainer gui, EntityPlayer player) {
      Minecraft mc = FMLClientHandler.instance().getClient();
      ScaledResolution var13 = new ScaledResolution(Minecraft.getMinecraft());
      int var14 = var13.getScaledWidth();
      int var15 = var13.getScaledHeight();
      int var16 = Mouse.getX() * var14 / mc.displayWidth;
      int var17 = var15 - Mouse.getY() * var15 / mc.displayHeight - 1;
      GlStateManager.pushMatrix();
      GL11.glPushAttrib(1048575);
      GlStateManager.disableLighting();

      for(int var20 = 0; var20 < gui.inventorySlots.inventorySlots.size(); ++var20) {
         int xs = UtilsFX.getGuiXSize(gui);
         int ys = UtilsFX.getGuiYSize(gui);
         int shift = 0;
         int shift2 = 0;
         int shiftx = -8;
         int shifty = -8;
         if (Thaumcraft.instance.aspectShift) {
            shiftx -= 8;
            shifty -= 8;
         }

         Slot var23 = (Slot)gui.inventorySlots.inventorySlots.get(var20);
         int guiLeft = shift + (gui.width - xs - shift2) / 2;
         int guiTop = (gui.height - ys) / 2;
         if (this.isMouseOverSlot(var23, var16, var17, guiLeft, guiTop) && var23.getStack() != null) {
            int h = ScanManager.generateItemHash(var23.getStack().getItem(), var23.getStack().getItemDamage());
            List<String> list = Thaumcraft.proxy.getScannedObjects().get(player.getName());
            if (list != null && (list.contains("@" + h) || list.contains("#" + h))) {
               AspectList tags = ThaumcraftCraftingManager.getObjectTags(var23.getStack());
               tags = ThaumcraftCraftingManager.getBonusTags(var23.getStack(), tags);
               if (tags != null) {
                  int x = var16 + 17;
                  int y = var17 + 7 - 33;
                  GlStateManager.disableDepth();
                  int index = 0;
                  if (tags.size() > 0) {
                     for(Aspect tag : tags.getAspectsSortedAmount()) {
                        if (tag != null) {
                           x = var16 + 17 + index * 18;
                           y = var17 + 7 - 33;
                           UtilsFX.bindTexture("textures/aspects/_back.png");
                           GlStateManager.pushMatrix();
                           GlStateManager.enableBlend();
                           GlStateManager.blendFunc(770, 771);
                           GlStateManager.translate(x + shiftx - 2, y + shifty - 2, 0.0F);
                           GlStateManager.scale(1.25F, 1.25F, 0.0F);
                           UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                           GlStateManager.disableBlend();
                           GlStateManager.popMatrix();
                           if (Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getName(), tag)) {
                              UtilsFX.drawTag(x + shiftx, y + shifty, tag, (float)tags.getAmount(tag), 0, UtilsFX.getGuiZLevel(gui));
                           } else {
                              UtilsFX.bindTexture("textures/aspects/_unknown.png");
                              GlStateManager.pushMatrix();
                              GlStateManager.enableBlend();
                              GlStateManager.blendFunc(770, 771);
                              GlStateManager.translate(x + shiftx, y + shifty, 0.0F);
                              UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                              GlStateManager.disableBlend();
                              GlStateManager.popMatrix();
                           }

                           ++index;
                        }
                     }
                  }

                  GlStateManager.enableDepth();
               }
            }
         }
      }

      GL11.glPopAttrib();
      GlStateManager.popMatrix();
   }

   private boolean isMouseOverSlot(Slot par1Slot, int par2, int par3, int par4, int par5) {
      par2 -= par4;
      par3 -= par5;
      return par2 >= par1Slot.xPos - 1 && par2 < par1Slot.xPos + 16 + 1 && par3 >= par1Slot.yPos - 1 && par3 < par1Slot.yPos + 16 + 1;
   }
}
