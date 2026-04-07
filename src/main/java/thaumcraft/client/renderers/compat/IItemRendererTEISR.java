package thaumcraft.client.renderers.compat;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
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
        org.lwjgl.opengl.GL11.glPopAttrib();
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
