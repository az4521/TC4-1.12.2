package thaumcraft.client.renderers.compat;

import net.minecraft.client.renderer.GlStateManager;
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
        org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.pushMatrix();
        // Detect if we're rendering in first-person hand vs GUI/hotbar
        // In first-person, the projection matrix is perspective (element [3][3] != 1)
        // In GUI/hotbar, it's orthographic (element [3][3] == 1)
        java.nio.FloatBuffer projBuf = org.lwjgl.BufferUtils.createFloatBuffer(16);
        org.lwjgl.opengl.GL11.glGetFloat(org.lwjgl.opengl.GL11.GL_PROJECTION_MATRIX, projBuf);
        boolean isFirstPerson = Math.abs(projBuf.get(15) - 1.0F) > 0.01F;
        IItemRenderer.ItemRenderType type = isFirstPerson
            ? IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON
            : IItemRenderer.ItemRenderType.INVENTORY;
        if (type == IItemRenderer.ItemRenderType.INVENTORY) {
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
        }
        try {
            if (type == IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON) {
                net.minecraft.entity.player.EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;
                renderer.renderItem(type, stack, null, player);
            } else {
                renderer.renderItem(type, stack);
            }
        } catch (Exception e) {
            // Silently handle render errors
        }
        GlStateManager.popMatrix();
        org.lwjgl.opengl.GL11.glPopAttrib();
    }
}
