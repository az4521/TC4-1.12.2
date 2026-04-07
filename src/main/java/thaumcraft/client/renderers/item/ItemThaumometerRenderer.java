package thaumcraft.client.renderers.item;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.math.RayTraceResult;
import thaumcraft.client.renderers.compat.IItemRenderer;
import thaumcraft.client.renderers.compat.IItemRenderer.ItemRenderType;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.research.IScanEventHandler;
import thaumcraft.api.research.ScanResult;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.EntityUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

public class ItemThaumometerRenderer implements IItemRenderer {
   private IModelCustom model;
   private static final ResourceLocation SCANNER = new ResourceLocation("thaumcraft", "textures/models/scanner.obj");

   public ItemThaumometerRenderer() {
      this.model = AdvancedModelLoader.loadModel(SCANNER);
   }

   public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
      return true;
   }

   public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
      return true;
   }

   public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data) {
      Minecraft mc = Minecraft.getMinecraft();
      int rve_id = 0;
      int player_id = 0;
      if (type == ItemRenderType.EQUIPPED) {
         rve_id = mc.getRenderViewEntity().getEntityId();
         player_id = ((EntityLivingBase)data[1]).getEntityId();
      }

      EntityPlayerSP playermp = mc.player;
      ItemRenderType renderType = type;
      if (type == ItemRenderType.EQUIPPED_FIRST_PERSON && playermp != null) {
         ItemStack held = playermp.getHeldItemMainhand();
         boolean matchesHeld = !held.isEmpty() && ItemStack.areItemsEqual(held, item) && ItemStack.areItemStackTagsEqual(held, item);
         if (!(mc.gameSettings.thirdPersonView == 0 && matchesHeld)) {
            renderType = ItemRenderType.ENTITY;
         }
      }

      float par1 = UtilsFX.getTimer(mc).renderPartialTicks;
      float var7 = 0.8F;
      EntityPlayerSP playersp = playermp;
      GlStateManager.pushMatrix();
      if (renderType == ItemRenderType.EQUIPPED_FIRST_PERSON && player_id == rve_id && mc.gameSettings.thirdPersonView == 0) {
         float f1 = UtilsFX.getPrevEquippedProgress(mc.entityRenderer.itemRenderer) + (UtilsFX.getEquippedProgress(mc.entityRenderer.itemRenderer) - UtilsFX.getPrevEquippedProgress(mc.entityRenderer.itemRenderer)) * par1;
         GlStateManager.translate(0.0F, 0.8F + (1.0F - f1) * 0.2F, -1F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F); 
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(1.0F, 1.0F, 1.0F);
      } else {
         if (renderType == ItemRenderType.ENTITY) {
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            GlStateManager.scale(0.12F, 0.12F, 0.12F);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.1F, 0.0F);
         } else {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
         }

         if (renderType == ItemRenderType.EQUIPPED) {
            GlStateManager.translate(1.6F, 0.3F, 2.0F);
            GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(30.0F, 0.0F, 0.0F, -1.0F);
         } else if (renderType == ItemRenderType.INVENTORY) {
            GlStateManager.rotate(60.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(30.0F, 0.0F, 0.0F, -1.0F);
            GlStateManager.rotate(248.0F, 0.0F, -1.0F, 0.0F);
         }
      }

      UtilsFX.bindTexture("textures/models/scanner.png");
      this.model.renderAll();
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, 0.11F, 0.0F);
      GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
      UtilsFX.renderQuadCenteredFromTexture("textures/models/scanscreen.png", 2.5F, 1.0F, 1.0F, 1.0F, (int)(190.0F + MathHelper.sin((float)(playermp.ticksExisted - playermp.world.rand.nextInt(2))) * 10.0F + 10.0F), 771, 1.0F);
      if (playermp instanceof EntityPlayer && renderType == ItemRenderType.EQUIPPED_FIRST_PERSON && player_id == rve_id && mc.gameSettings.thirdPersonView == 0) {
         RenderHelper.disableStandardItemLighting();
         int j = (int)(190.0F + MathHelper.sin((float)(playermp.ticksExisted - playermp.world.rand.nextInt(2))) * 10.0F + 10.0F);
         int k = j % 65536;
         int l = j / 65536;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
         ScanResult scan = this.doScan(playermp.inventory.getCurrentItem(), playermp);
         if (scan != null) {
            AspectList aspects = null;
            GlStateManager.translate(0.0F, 0.0F, -0.01F);
            String text = "?";
            ItemStack stack = null;
            if (scan.id > 0) {
               stack = new ItemStack(Item.getItemById(scan.id), 1, scan.meta);
               if (ScanManager.hasBeenScanned(playermp, scan)) {
                  aspects = ScanManager.getScanAspects(scan, playermp.world);
               }
            }

            if (scan.type == 2) {
               if (scan.entity instanceof EntityItem) {
                  stack = ((EntityItem)scan.entity).getItem();
               } else {
                  text = scan.entity.getName();
               }

               if (ScanManager.hasBeenScanned(playermp, scan)) {
                  aspects = ScanManager.getScanAspects(scan, playermp.world);
               }
            }

            if (scan.type == 3 && scan.phenomena.startsWith("NODE") && ScanManager.hasBeenScanned(playermp, scan)) {
               RayTraceResult mop = null;
               if (stack != null && stack.getItem() != null) {
                  mop = EntityUtils.getMovingObjectPositionFromPlayer(playermp.world, playermp, true);
               }

               if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                  TileEntity tile = playermp.world.getTileEntity(mop.getBlockPos());
                  if (tile instanceof INode) {
                     aspects = ((INode)tile).getAspects();
                     GlStateManager.pushMatrix();
                     GlStateManager.enableBlend();
                     GlStateManager.blendFunc(770, 1);
                     String t = I18n.translateToLocal("nodetype." + ((INode)tile).getNodeType() + ".name");
                     if (((INode)tile).getNodeModifier() != null) {
                        t = t + ", " + I18n.translateToLocal("nodemod." + ((INode)tile).getNodeModifier() + ".name");
                     }

                     int sw = mc.fontRenderer.getStringWidth(t);
                     float scale = 0.004F;
                     GlStateManager.scale(scale, scale, scale);
                     mc.fontRenderer.drawString(t, -sw / 2, -40, 15642134);
                     GlStateManager.disableBlend();
                     GlStateManager.popMatrix();
                  }
               }
            }

            if (stack != null) {
               if (stack.getItem() != null) {
                  try {
                     text = stack.getDisplayName();
                  } catch (Exception ignored) {
                  }
               } else if (stack.getItem() != null) {
                  try {
                     text = stack.getItem().getItemStackDisplayName(stack);
                  } catch (Exception ignored) {
                  }
               }
            }

            if (aspects != null) {
               int posX = 0;
               int posY = 0;
               int aa = aspects.size();
               int baseX = Math.min(5, aa) * 8;

               for(Aspect aspect : aspects.getAspectsSorted()) {
                  GlStateManager.pushMatrix();
                  GlStateManager.scale(0.0075F, 0.0075F, 0.0075F);
                  j = (int)(190.0F + MathHelper.sin((float)(posX + playermp.ticksExisted - playermp.world.rand.nextInt(2))) * 10.0F + 10.0F);
                  k = j % 65536;
                  l = j / 65536;
                  OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
                  UtilsFX.drawTag(-baseX + posX * 16, -8 + posY * 16, aspect, (float)aspects.getAmount(aspect), 0, 0.01, 1, 1.0F, false);
                  GlStateManager.popMatrix();
                  ++posX;
                  if (posX >= 5 - posY) {
                     posX = 0;
                     ++posY;
                     aa -= 5 - posY;
                     baseX = Math.min(5 - posY, aa) * 8;
                  }
               }
            }

            if (text == null) {
               text = "?";
            }

            if (!text.isEmpty()) {
               RenderHelper.disableStandardItemLighting();
               GlStateManager.pushMatrix();
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 1);
               GlStateManager.translate(0.0F, -0.25F, 0.0F);
               int sw = mc.fontRenderer.getStringWidth(text);
               float scale = 0.005F;
               if (sw > 90) {
                  scale -= 2.5E-5F * (float)(sw - 90);
               }

               GlStateManager.scale(scale, scale, scale);
               mc.fontRenderer.drawString(text, -sw / 2, 0, 16777215);
               GlStateManager.disableBlend();
               GlStateManager.popMatrix();
            }
         }

         RenderHelper.enableGUIStandardItemLighting();
      }

      GlStateManager.popMatrix();
      GlStateManager.popMatrix();
   }

   private ScanResult doScan(ItemStack stack, EntityPlayer p) {
      if (stack != null && p != null) {
         Entity pointedEntity = EntityUtils.getPointedEntity(p.world, p, 0.5F, 10.0F, 0.0F, true);
         if (pointedEntity != null) {
            ScanResult sr = new ScanResult((byte)2, 0, 0, pointedEntity, "");
            return sr;
         } else {
            RayTraceResult mop = EntityUtils.getMovingObjectPositionFromPlayer(p.world, p, true);
            if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
               BlockPos bpos = mop.getBlockPos();
               Block bi = p.world.getBlockState(bpos).getBlock();
               TileEntity tile = p.world.getTileEntity(bpos);
               if (tile instanceof INode) {
                  int md = 0;
                  ScanResult sr = new ScanResult((byte)3, Block.getIdFromBlock(bi), md, null, "NODE" + ((INode)tile).getId());
                  return sr;
               }

               if (bi != Blocks.AIR) {
                  ItemStack is = bi.getPickBlock(p.world.getBlockState(bpos), mop, p.world, bpos, p);
                  ScanResult sr = null;
                  int md = p.world.getBlockState(bpos).getBlock().getMetaFromState(p.world.getBlockState(bpos));

                  try {
                     if (is == null) {
                        is = BlockUtils.createStackedBlock(bi, md);
                     }
                  } catch (Exception ignored) {
                  }

                  try {
                     if (is == null) {
                        sr = new ScanResult((byte)1, Block.getIdFromBlock(bi), md, null, "");
                     } else {
                        sr = new ScanResult((byte)1, Item.getIdFromItem(is.getItem()), is.getItemDamage(), null, "");
                     }
                  } catch (Exception ignored) {
                  }

                  return sr;
               }
            }

            for(IScanEventHandler seh : ThaumcraftApi.scanEventhandlers) {
               ScanResult scan = seh.scanPhenomena(stack, p.world, p);
               if (scan != null) {
                  return scan;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }
}
