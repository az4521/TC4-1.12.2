package thaumcraft.client.renderers.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.common.tiles.TileAlchemyFurnaceAdvanced;

public class TileAlchemyFurnaceRenderer extends TileEntitySpecialRenderer<TileAlchemyFurnaceAdvanced> {
    private static final ResourceLocation TEX = new ResourceLocation("thaumcraft", "blocks/metalbase");

    @Override
    public void render(TileAlchemyFurnaceAdvanced te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
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
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEX.toString());

        float u0 = sprite.getMinU(), u1 = sprite.getMaxU();
        float v0 = sprite.getMinV(), v1 = sprite.getMaxV();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        // Bottom
        buf.pos(0, 0, 1).tex(u0, v1).endVertex();
        buf.pos(0, 0, 0).tex(u0, v0).endVertex();
        buf.pos(1, 0, 0).tex(u1, v0).endVertex();
        buf.pos(1, 0, 1).tex(u1, v1).endVertex();
        // Top
        buf.pos(0, 1, 0).tex(u0, v0).endVertex();
        buf.pos(0, 1, 1).tex(u0, v1).endVertex();
        buf.pos(1, 1, 1).tex(u1, v1).endVertex();
        buf.pos(1, 1, 0).tex(u1, v0).endVertex();
        // North
        buf.pos(1, 1, 0).tex(u0, v0).endVertex();
        buf.pos(1, 0, 0).tex(u0, v1).endVertex();
        buf.pos(0, 0, 0).tex(u1, v1).endVertex();
        buf.pos(0, 1, 0).tex(u1, v0).endVertex();
        // South
        buf.pos(0, 1, 1).tex(u0, v0).endVertex();
        buf.pos(0, 0, 1).tex(u0, v1).endVertex();
        buf.pos(1, 0, 1).tex(u1, v1).endVertex();
        buf.pos(1, 1, 1).tex(u1, v0).endVertex();
        // West
        buf.pos(0, 1, 0).tex(u0, v0).endVertex();
        buf.pos(0, 0, 0).tex(u0, v1).endVertex();
        buf.pos(0, 0, 1).tex(u1, v1).endVertex();
        buf.pos(0, 1, 1).tex(u1, v0).endVertex();
        // East
        buf.pos(1, 1, 1).tex(u0, v0).endVertex();
        buf.pos(1, 0, 1).tex(u0, v1).endVertex();
        buf.pos(1, 0, 0).tex(u1, v1).endVertex();
        buf.pos(1, 1, 0).tex(u1, v0).endVertex();
        tess.draw();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
