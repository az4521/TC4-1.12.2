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
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.common.tiles.TilePlank;

public class TilePlankRenderer extends TileEntitySpecialRenderer<TilePlank> {
    private static final ResourceLocation TEX_GREATWOOD = new ResourceLocation("thaumcraft", "blocks/planks_greatwood");
    private static final ResourceLocation TEX_SILVERWOOD = new ResourceLocation("thaumcraft", "blocks/planks_silverwood");

    @Override
    public void render(TilePlank te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) return;
        int meta = te.getBlockMetadata();
        if (meta != 6 && meta != 7) return;

        ResourceLocation texLoc = meta == 6 ? TEX_GREATWOOD : TEX_SILVERWOOD;
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texLoc.toString());

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        // Sample light from above since the block itself is opaque and would darken its own reading
        int light = 0;
        for (net.minecraft.util.EnumFacing face : net.minecraft.util.EnumFacing.VALUES) {
            light = Math.max(light, te.getWorld().getCombinedLight(te.getPos().offset(face), 0));
        }
        int lx = light % 65536;
        int ly = light / 65536;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

        RenderHelper.disableStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float u0 = sprite.getMinU(), u1 = sprite.getMaxU();
        float v0 = sprite.getMinV(), v1 = sprite.getMaxV();

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // Bottom (y=0)
        buf.pos(0, 0, 1).tex(u0, v1).endVertex();
        buf.pos(0, 0, 0).tex(u0, v0).endVertex();
        buf.pos(1, 0, 0).tex(u1, v0).endVertex();
        buf.pos(1, 0, 1).tex(u1, v1).endVertex();

        // Top (y=1)
        buf.pos(0, 1, 0).tex(u0, v0).endVertex();
        buf.pos(0, 1, 1).tex(u0, v1).endVertex();
        buf.pos(1, 1, 1).tex(u1, v1).endVertex();
        buf.pos(1, 1, 0).tex(u1, v0).endVertex();

        // North (z=0)
        buf.pos(1, 1, 0).tex(u0, v0).endVertex();
        buf.pos(1, 0, 0).tex(u0, v1).endVertex();
        buf.pos(0, 0, 0).tex(u1, v1).endVertex();
        buf.pos(0, 1, 0).tex(u1, v0).endVertex();

        // South (z=1)
        buf.pos(0, 1, 1).tex(u0, v0).endVertex();
        buf.pos(0, 0, 1).tex(u0, v1).endVertex();
        buf.pos(1, 0, 1).tex(u1, v1).endVertex();
        buf.pos(1, 1, 1).tex(u1, v0).endVertex();

        // West (x=0)
        buf.pos(0, 1, 0).tex(u0, v0).endVertex();
        buf.pos(0, 0, 0).tex(u0, v1).endVertex();
        buf.pos(0, 0, 1).tex(u1, v1).endVertex();
        buf.pos(0, 1, 1).tex(u1, v0).endVertex();

        // East (x=1)
        buf.pos(1, 1, 1).tex(u0, v0).endVertex();
        buf.pos(1, 0, 1).tex(u0, v1).endVertex();
        buf.pos(1, 0, 0).tex(u1, v1).endVertex();
        buf.pos(1, 1, 0).tex(u1, v0).endVertex();

        tess.draw();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
