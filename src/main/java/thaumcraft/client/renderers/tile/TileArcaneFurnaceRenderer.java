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
import org.lwjgl.opengl.GL11;
import thaumcraft.common.tiles.TileArcaneFurnace;

public class TileArcaneFurnaceRenderer extends TileEntitySpecialRenderer<TileArcaneFurnace> {

    @Override
    public void render(TileArcaneFurnace te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
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

        TextureAtlasSprite topBot = getSprite("thaumcraft:blocks/furnace0");
        TextureAtlasSprite front = getSprite("thaumcraft:blocks/furnace1");
        TextureAtlasSprite side = getSprite("thaumcraft:blocks/furnace2");

        float tu0 = topBot.getMinU(), tu1 = topBot.getMaxU(), tv0 = topBot.getMinV(), tv1 = topBot.getMaxV();
        float fu0 = front.getMinU(), fu1 = front.getMaxU(), fv0 = front.getMinV(), fv1 = front.getMaxV();
        float su0 = side.getMinU(), su1 = side.getMaxU(), sv0 = side.getMinV(), sv1 = side.getMaxV();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // Bottom (furnace0)
        buf.pos(0, 0, 1).tex(tu0, tv1).endVertex();
        buf.pos(0, 0, 0).tex(tu0, tv0).endVertex();
        buf.pos(1, 0, 0).tex(tu1, tv0).endVertex();
        buf.pos(1, 0, 1).tex(tu1, tv1).endVertex();
        // Top (furnace0)
        buf.pos(0, 1, 0).tex(tu0, tv0).endVertex();
        buf.pos(0, 1, 1).tex(tu0, tv1).endVertex();
        buf.pos(1, 1, 1).tex(tu1, tv1).endVertex();
        buf.pos(1, 1, 0).tex(tu1, tv0).endVertex();
        // North - front (furnace1)
        buf.pos(1, 1, 0).tex(fu0, fv0).endVertex();
        buf.pos(1, 0, 0).tex(fu0, fv1).endVertex();
        buf.pos(0, 0, 0).tex(fu1, fv1).endVertex();
        buf.pos(0, 1, 0).tex(fu1, fv0).endVertex();
        // South (furnace2)
        buf.pos(0, 1, 1).tex(su0, sv0).endVertex();
        buf.pos(0, 0, 1).tex(su0, sv1).endVertex();
        buf.pos(1, 0, 1).tex(su1, sv1).endVertex();
        buf.pos(1, 1, 1).tex(su1, sv0).endVertex();
        // West (furnace2)
        buf.pos(0, 1, 0).tex(su0, sv0).endVertex();
        buf.pos(0, 0, 0).tex(su0, sv1).endVertex();
        buf.pos(0, 0, 1).tex(su1, sv1).endVertex();
        buf.pos(0, 1, 1).tex(su1, sv0).endVertex();
        // East (furnace2)
        buf.pos(1, 1, 1).tex(su0, sv0).endVertex();
        buf.pos(1, 0, 1).tex(su0, sv1).endVertex();
        buf.pos(1, 0, 0).tex(su1, sv1).endVertex();
        buf.pos(1, 1, 0).tex(su1, sv0).endVertex();

        tess.draw();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private TextureAtlasSprite getSprite(String name) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
    }
}
