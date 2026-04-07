package thaumcraft.client.renderers.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * Generic TESR that delegates rendering to an ISimpleBlockRenderingHandler.
 * This bridges the 1.7.10 block rendering system to 1.12.2's TESR system.
 */
public class BlockRendererDispatcherTESR<T extends TileEntity> extends TileEntitySpecialRenderer<T> {
    private final ISimpleBlockRenderingHandler handler;
    private final RenderBlocks renderBlocks = new RenderBlocks();

    public BlockRendererDispatcherTESR(ISimpleBlockRenderingHandler handler) {
        this.handler = handler;
    }

    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

        RenderHelper.disableStandardItemLighting();
        int light = 0;
        for (EnumFacing face : EnumFacing.VALUES) {
            light = Math.max(light, te.getWorld().getCombinedLight(te.getPos().offset(face), 0));
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        renderBlocks.blockAccess = te.getWorld();
        renderBlocks.offsetX = te.getPos().getX();
        renderBlocks.offsetY = te.getPos().getY();
        renderBlocks.offsetZ = te.getPos().getZ();
        handler.renderWorldBlock(te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(),
            te.getWorld().getBlockState(te.getPos()).getBlock(), handler.getRenderId(), renderBlocks);

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
