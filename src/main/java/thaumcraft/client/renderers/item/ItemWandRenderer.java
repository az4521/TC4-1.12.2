package thaumcraft.client.renderers.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import thaumcraft.client.renderers.compat.IItemRenderer;
import thaumcraft.client.renderers.compat.IItemRenderer.ItemRenderType;
import thaumcraft.client.renderers.compat.IItemRenderer.ItemRendererHelper;

import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.gear.ModelWand;
import thaumcraft.common.items.wands.ItemWandCasting;
import net.minecraft.client.renderer.GlStateManager;

public class ItemWandRenderer implements IItemRenderer {
   private ModelWand model = new ModelWand();

   public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
      return true;
   }

   public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
      return helper != ItemRendererHelper.BLOCK_3D;
   }

   public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data) {
      Minecraft mc = Minecraft.getMinecraft();
      if (item != null && item.getItem() instanceof ItemWandCasting) {
         ItemWandCasting wand = (ItemWandCasting)item.getItem();
         ItemStack focusStack = wand.getFocusItem(item);
         boolean staff = wand.isStaff(item);
         float pt = UtilsFX.getTimer(mc).renderPartialTicks;
         ItemRenderer ir = null;
         EntityLivingBase wielder = null;
         if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            wielder = (EntityLivingBase)data[1];
         }

         GlStateManager.pushMatrix();
         if (staff) {
            GlStateManager.translate(0.0F, 0.5F, 0.0F);
         }

         if (type != ItemRenderType.INVENTORY) {
            if (type == ItemRenderType.ENTITY) {
               if (staff) {
                  GlStateManager.translate(0.0F, 1.5F, 0.0F);
                  GlStateManager.scale(0.9, 0.9, 0.9);
               } else {
                  GlStateManager.translate(0.0F, 1.0F, 0.0F);
               }
            } else {
               GlStateManager.translate(0.5F, 1.5F, 0.5F);
               if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
                  GlStateManager.scale(1.0F, 1.1, 1.0F);
               }
            }
         } else {
            if (staff) {
               GlStateManager.scale(0.8, 0.8, 0.8);
            }

            GlStateManager.rotate(66.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, 0.6, 0.0F);
            if (staff) {
               GlStateManager.translate(-0.7, 0.6, 0.0F);
            }
         }

         GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
         if (wielder instanceof EntityPlayer && ((EntityPlayer) wielder).getActiveItemStack() != null && !((EntityPlayer) wielder).getActiveItemStack().isEmpty()) {
            float t = (float)((EntityPlayer)wielder).getItemInUseCount() + pt;
            if (t > 3.0F) {
               t = 3.0F;
            }

            GlStateManager.translate(0.0F, 1.0F, 0.0F);
            if (type != ItemRenderType.EQUIPPED_FIRST_PERSON) {
               GlStateManager.rotate(33.0F, 0.0F, 0.0F, 1.0F);
            } else {
               GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
               GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.rotate(60.0F * (t / 3.0F), -1.0F, 0.0F, 0.0F);
            if (wand.animation != ItemFocusBasic.WandFocusAnimation.WAVE && (wand.getFocus(item) == null || wand.getFocus(item).getAnimation(focusStack) != ItemFocusBasic.WandFocusAnimation.WAVE)) {
               if (wand.getFocus(item) != null && wand.getFocus(item).getAnimation(focusStack) == ItemFocusBasic.WandFocusAnimation.CHARGE) {
                  float wave = MathHelper.sin(((float) ((EntityPlayer) wielder).getItemInUseCount() + pt) / 0.8F);
                  GlStateManager.rotate(wave, 0.0F, 0.0F, 1.0F);
                  wave = MathHelper.sin(((float) ((EntityPlayer) wielder).getItemInUseCount() + pt) / 0.7F);
                  GlStateManager.rotate(wave, 1.0F, 0.0F, 0.0F);
               }
            } else {
               float wave = MathHelper.sin(((float)((EntityPlayer)wielder).getItemInUseCount() + pt) / 10.0F) * 10.0F;
               GlStateManager.rotate(wave, 0.0F, 0.0F, 1.0F);
               wave = MathHelper.sin(((float)((EntityPlayer)wielder).getItemInUseCount() + pt) / 15.0F) * 10.0F;
               GlStateManager.rotate(wave, 1.0F, 0.0F, 0.0F);
            }

            GlStateManager.translate(0.0F, -1.0F, 0.0F);
         }

         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 771);
         this.model.render(item);
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }
}
