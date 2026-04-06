package thaumcraft.client.renderers.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.compat.IItemRenderer;
import thaumcraft.client.renderers.compat.IItemRenderer.ItemRenderType;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class ItemBowBoneRenderer implements IItemRenderer {
   private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
   private RenderManager renderManager;
   private Minecraft mc;
   private TextureManager texturemanager;

   public ItemBowBoneRenderer() {
      this.mc = Minecraft.getMinecraft();
      this.texturemanager = this.mc.getTextureManager();
   }

   public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
      return type == ItemRenderType.EQUIPPED;
   }

   public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
      return false;
   }

   public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data) {
      EntityLivingBase entity = (EntityLivingBase)data[1];
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      float f2 = 2.6666667F;
      GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(-60.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scale(f2, f2, f2);
      GlStateManager.translate(-0.25F, -0.1875F, 0.1875F);
      float f3 = 0.625F;
      GlStateManager.translate(0.0F, 0.125F, 0.3125F);
      GlStateManager.rotate(-20.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.scale(f3, -f3, f3);
      GlStateManager.rotate(-100.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
      this.renderItem(entity, item, 0);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
   }

   private void renderItem(EntityLivingBase par1EntityLiving, ItemStack par2ItemStack, int par3) {
      // Rewrite using ModelResourceLocation + IBakedModel for proper bow rendering
   }
}
