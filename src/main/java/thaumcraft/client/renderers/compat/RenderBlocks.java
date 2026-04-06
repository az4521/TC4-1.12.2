package thaumcraft.client.renderers.compat;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/**
 * Reimplementation of 1.7.10's RenderBlocks for 1.12.2.
 * Renders textured quads using the Tessellator/BufferBuilder system.
 */
public class RenderBlocks {
    public IBlockAccess blockAccess;
    public TextureAtlasSprite overrideBlockTexture;
    public boolean flipTexture;
    public boolean renderAllFaces;
    public boolean field_152631_f; // uvRotateTop flag from 1.7.10
    public double renderMinY, renderMaxY;
    public double renderMinX, renderMaxX;
    public double renderMinZ, renderMaxZ;

    public RenderBlocks() {}
    public RenderBlocks(IBlockAccess world) { this.blockAccess = world; }

    public void setRenderBoundsFromBlock(Block block) {
        renderMinX = 0; renderMinY = 0; renderMinZ = 0;
        renderMaxX = 1; renderMaxY = 1; renderMaxZ = 1;
    }

    public void setRenderBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        renderMinX = minX; renderMinY = minY; renderMinZ = minZ;
        renderMaxX = maxX; renderMaxY = maxY; renderMaxZ = maxZ;
    }

    public boolean renderStandardBlock(Block block, int x, int y, int z) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite icon = overrideBlockTexture;
        if (icon == null) {
            icon = Minecraft.getMinecraft().getBlockRendererDispatcher()
                .getBlockModelShapes().getTexture(block.getDefaultState());
        }
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        drawAllFaces(buf, x, y, z, icon);
        tess.draw();
        return true;
    }

    public boolean renderStandardBlockWithColorMultiplier(Block block, int x, int y, int z, float r, float g, float b) {
        GlStateManager.color(r, g, b, 1.0F);
        boolean result = renderStandardBlock(block, x, y, z);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        return result;
    }

    private void drawAllFaces(BufferBuilder buf, double x, double y, double z,TextureAtlasSprite icon) {
        float u0 = icon.getMinU(), u1 = icon.getMaxU();
        float v0 = icon.getMinV(), v1 = icon.getMaxV();
        double x0 = x + renderMinX, x1 = x + renderMaxX;
        double y0 = y + renderMinY, y1 = y + renderMaxY;
        double z0 = z + renderMinZ, z1 = z + renderMaxZ;
        // Bottom (Y-)
        buf.pos(x0, y0, z1).tex(u0, v1).endVertex();
        buf.pos(x0, y0, z0).tex(u0, v0).endVertex();
        buf.pos(x1, y0, z0).tex(u1, v0).endVertex();
        buf.pos(x1, y0, z1).tex(u1, v1).endVertex();
        // Top (Y+)
        buf.pos(x0, y1, z0).tex(u0, v0).endVertex();
        buf.pos(x0, y1, z1).tex(u0, v1).endVertex();
        buf.pos(x1, y1, z1).tex(u1, v1).endVertex();
        buf.pos(x1, y1, z0).tex(u1, v0).endVertex();
        // North (Z-)
        buf.pos(x1, y1, z0).tex(u0, v0).endVertex();
        buf.pos(x1, y0, z0).tex(u0, v1).endVertex();
        buf.pos(x0, y0, z0).tex(u1, v1).endVertex();
        buf.pos(x0, y1, z0).tex(u1, v0).endVertex();
        // South (Z+)
        buf.pos(x0, y1, z1).tex(u0, v0).endVertex();
        buf.pos(x0, y0, z1).tex(u0, v1).endVertex();
        buf.pos(x1, y0, z1).tex(u1, v1).endVertex();
        buf.pos(x1, y1, z1).tex(u1, v0).endVertex();
        // West (X-)
        buf.pos(x0, y1, z0).tex(u0, v0).endVertex();
        buf.pos(x0, y0, z0).tex(u0, v1).endVertex();
        buf.pos(x0, y0, z1).tex(u1, v1).endVertex();
        buf.pos(x0, y1, z1).tex(u1, v0).endVertex();
        // East (X+)
        buf.pos(x1, y1, z1).tex(u0, v0).endVertex();
        buf.pos(x1, y0, z1).tex(u0, v1).endVertex();
        buf.pos(x1, y0, z0).tex(u1, v1).endVertex();
        buf.pos(x1, y1, z0).tex(u1, v0).endVertex();
    }

    public void renderFaceYNeg(Block block, double x, double y, double z, TextureAtlasSprite icon) {
        float u0 = icon.getMinU(), u1 = icon.getMaxU();
        float v0 = icon.getMinV(), v1 = icon.getMaxV();
        double x0 = x + renderMinX, x1 = x + renderMaxX;
        double yy = y + renderMinY;
        double z0 = z + renderMinZ, z1 = z + renderMaxZ;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x0, yy, z1).tex(u0, v1).endVertex();
        buf.pos(x0, yy, z0).tex(u0, v0).endVertex();
        buf.pos(x1, yy, z0).tex(u1, v0).endVertex();
        buf.pos(x1, yy, z1).tex(u1, v1).endVertex();
        tess.draw();
    }

    public void renderFaceYPos(Block block, double x, double y, double z, TextureAtlasSprite icon) {
        float u0 = icon.getMinU(), u1 = icon.getMaxU();
        float v0 = icon.getMinV(), v1 = icon.getMaxV();
        double x0 = x + renderMinX, x1 = x + renderMaxX;
        double yy = y + renderMaxY;
        double z0 = z + renderMinZ, z1 = z + renderMaxZ;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x0, yy, z0).tex(u0, v0).endVertex();
        buf.pos(x0, yy, z1).tex(u0, v1).endVertex();
        buf.pos(x1, yy, z1).tex(u1, v1).endVertex();
        buf.pos(x1, yy, z0).tex(u1, v0).endVertex();
        tess.draw();
    }

    public void renderFaceXNeg(Block block, double x, double y, double z, TextureAtlasSprite icon) {
        float u0 = icon.getMinU(), u1 = icon.getMaxU();
        float v0 = icon.getMinV(), v1 = icon.getMaxV();
        double xx = x + renderMinX;
        double y0 = y + renderMinY, y1 = y + renderMaxY;
        double z0 = z + renderMinZ, z1 = z + renderMaxZ;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(xx, y1, z0).tex(u0, v0).endVertex();
        buf.pos(xx, y0, z0).tex(u0, v1).endVertex();
        buf.pos(xx, y0, z1).tex(u1, v1).endVertex();
        buf.pos(xx, y1, z1).tex(u1, v0).endVertex();
        tess.draw();
    }

    public void renderFaceXPos(Block block, double x, double y, double z, TextureAtlasSprite icon) {
        float u0 = icon.getMinU(), u1 = icon.getMaxU();
        float v0 = icon.getMinV(), v1 = icon.getMaxV();
        double xx = x + renderMaxX;
        double y0 = y + renderMinY, y1 = y + renderMaxY;
        double z0 = z + renderMinZ, z1 = z + renderMaxZ;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(xx, y1, z1).tex(u0, v0).endVertex();
        buf.pos(xx, y0, z1).tex(u0, v1).endVertex();
        buf.pos(xx, y0, z0).tex(u1, v1).endVertex();
        buf.pos(xx, y1, z0).tex(u1, v0).endVertex();
        tess.draw();
    }

    public void renderFaceZNeg(Block block, double x, double y, double z, TextureAtlasSprite icon) {
        float u0 = icon.getMinU(), u1 = icon.getMaxU();
        float v0 = icon.getMinV(), v1 = icon.getMaxV();
        double x0 = x + renderMinX, x1 = x + renderMaxX;
        double y0 = y + renderMinY, y1 = y + renderMaxY;
        double zz = z + renderMinZ;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x1, y1, zz).tex(u0, v0).endVertex();
        buf.pos(x1, y0, zz).tex(u0, v1).endVertex();
        buf.pos(x0, y0, zz).tex(u1, v1).endVertex();
        buf.pos(x0, y1, zz).tex(u1, v0).endVertex();
        tess.draw();
    }

    public void renderFaceZPos(Block block, double x, double y, double z, TextureAtlasSprite icon) {
        float u0 = icon.getMinU(), u1 = icon.getMaxU();
        float v0 = icon.getMinV(), v1 = icon.getMaxV();
        double x0 = x + renderMinX, x1 = x + renderMaxX;
        double y0 = y + renderMinY, y1 = y + renderMaxY;
        double zz = z + renderMaxZ;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x0, y1, zz).tex(u0, v0).endVertex();
        buf.pos(x0, y0, zz).tex(u0, v1).endVertex();
        buf.pos(x1, y0, zz).tex(u1, v1).endVertex();
        buf.pos(x1, y1, zz).tex(u1, v0).endVertex();
        tess.draw();
    }

    public void drawCrossedSquares(TextureAtlasSprite icon, double x, double y, double z, float scale) {
        float u0 = icon.getMinU(), u1 = icon.getMaxU();
        float v0 = icon.getMinV(), v1 = icon.getMaxV();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        // First cross
        buf.pos(x - scale, y + scale, z).tex(u0, v0).endVertex();
        buf.pos(x - scale, y - scale, z).tex(u0, v1).endVertex();
        buf.pos(x + scale, y - scale, z).tex(u1, v1).endVertex();
        buf.pos(x + scale, y + scale, z).tex(u1, v0).endVertex();
        // Second cross (rotated 90)
        buf.pos(x, y + scale, z - scale).tex(u0, v0).endVertex();
        buf.pos(x, y - scale, z - scale).tex(u0, v1).endVertex();
        buf.pos(x, y - scale, z + scale).tex(u1, v1).endVertex();
        buf.pos(x, y + scale, z + scale).tex(u1, v0).endVertex();
        tess.draw();
    }

    public void renderCrossedSquares(Block block, int x, int y, int z) {
        TextureAtlasSprite icon = overrideBlockTexture;
        if (icon == null) {
            icon = Minecraft.getMinecraft().getBlockRendererDispatcher()
                .getBlockModelShapes().getTexture(block.getDefaultState());
        }
        drawCrossedSquares(icon, x + 0.5, y + 0.5, z + 0.5, 0.5F);
    }

    public void renderBlockByRenderType(Block block, int x, int y, int z) {
        renderStandardBlock(block, x, y, z);
    }

    public void clearOverrideBlockTexture() { overrideBlockTexture = null; }
}
