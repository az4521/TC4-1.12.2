package thaumcraft.client.renderers.compat;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Adapter that bridges 1.7.10's IItemRenderer to 1.12.2's TileEntityItemStackRenderer.
 * Detects render context via a thread-local set by a ForgeHooksClient transform handler.
 */
public class IItemRendererTEISR extends TileEntityItemStackRenderer {
    private final IItemRenderer renderer;

    public IItemRendererTEISR(IItemRenderer renderer) {
        this.renderer = renderer;
    }

    public static void register(Item item, IItemRenderer renderer) {
        item.setTileEntityItemStackRenderer(new IItemRendererTEISR(renderer));
    }

    @Override
    public void renderByItem(ItemStack stack) {
        resetGlStateForLegacyItemRender();
        GlStateManager.pushMatrix();
        IItemRenderer.ItemRenderType type = mapTransformType(ItemRenderContext.get());
        if (type == IItemRenderer.ItemRenderType.INVENTORY) {
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
        }
        try {
            if (type == IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON || type == IItemRenderer.ItemRenderType.EQUIPPED) {
                net.minecraft.entity.player.EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;
                renderer.renderItem(type, stack, null, player);
            } else {
                renderer.renderItem(type, stack);
            }
        } catch (Exception e) {
            // Silently handle render errors
        } finally {
            ItemRenderContext.clear();
        }
        GlStateManager.popMatrix();
        resetGlStateAfterLegacyItemRender();
    }

    private static void resetGlStateForLegacyItemRender() {
        org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_ALPHA_TEST);
        org.lwjgl.opengl.GL11.glAlphaFunc(org.lwjgl.opengl.GL11.GL_GREATER, 0.1F);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        org.lwjgl.opengl.GL11.glDepthMask(true);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_LIGHTING);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
    }

    /**
     * Legacy renderers use raw GL state mutation and glPushAttrib/glPopAttrib, but
     * GlStateManager caches do not get synchronized by those calls. Force a sane
     * post-render baseline so later GUI/world draws do not inherit stale cached state.
     */
    private static void resetGlStateAfterLegacyItemRender() {
        RenderHelper.disableStandardItemLighting();
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
        GlStateManager.disableTexture2D();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND);
        org.lwjgl.opengl.GL11.glBlendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_ALPHA_TEST);
        org.lwjgl.opengl.GL11.glAlphaFunc(org.lwjgl.opengl.GL11.GL_GREATER, 0.1F);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        org.lwjgl.opengl.GL11.glDepthMask(true);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_LIGHTING);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.disableRescaleNormal();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
    }

    private IItemRenderer.ItemRenderType mapTransformType(ItemCameraTransforms.TransformType transformType) {
        if (transformType == null) {
            java.nio.FloatBuffer projBuf = org.lwjgl.BufferUtils.createFloatBuffer(16);
            org.lwjgl.opengl.GL11.glGetFloat(org.lwjgl.opengl.GL11.GL_PROJECTION_MATRIX, projBuf);
            boolean isFirstPerson = Math.abs(projBuf.get(15) - 1.0F) > 0.01F;
            return isFirstPerson ? IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON : IItemRenderer.ItemRenderType.INVENTORY;
        }

        switch (transformType) {
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
                return IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON;
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
                return IItemRenderer.ItemRenderType.EQUIPPED;
            case GUI:
                return IItemRenderer.ItemRenderType.INVENTORY;
            case GROUND:
            case FIXED:
            case HEAD:
            case NONE:
            default:
                return IItemRenderer.ItemRenderType.ENTITY;
        }
    }
}
