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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.tiles.TileBellows;
import thaumcraft.common.tiles.TileTube;
import thaumcraft.common.tiles.TileTubeBuffer;
import thaumcraft.common.tiles.TileTubeFilter;

import java.awt.Color;

public class TileTubeRenderer extends TileEntitySpecialRenderer<TileTube> {
    private static final ResourceLocation TEX_PIPE1 = new ResourceLocation("thaumcraft", "blocks/pipe_1");
    private static final ResourceLocation TEX_PIPE2 = new ResourceLocation("thaumcraft", "blocks/pipe_2");
    private static final ResourceLocation TEX_PIPE3 = new ResourceLocation("thaumcraft", "blocks/pipe_3");
    private static final ResourceLocation TEX_FILTER = new ResourceLocation("thaumcraft", "blocks/pipe_filter");
    private static final ResourceLocation TEX_FILTER_CORE = new ResourceLocation("thaumcraft", "blocks/pipe_filter_core");
    private static final ResourceLocation TEX_RESTRICT = new ResourceLocation("thaumcraft", "blocks/pipe_restrict");

    private static final float W4 = 0.25F, W6 = 0.375F, W7 = 0.4375F;
    private static final float W9 = 0.5625F, W10 = 0.625F, W12 = 0.75F;

    @Override
    public void render(TileTube tube, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tube == null || tube.getWorld() == null) return;
        int metadata = tube.getBlockMetadata();
        if (metadata == 2 || metadata == 7) return; // centrifuge and crystallizer have their own full renderers
        renderTubeBase(tube, x, y, z);
    }

    /**
     * Renders the base tube connections. Called by this TESR and also by
     * TileTubeValveRenderer, TileTubeBufferRenderer, TileTubeOnewayRenderer
     * to render the base pipe before their own decorations.
     */
    public static void renderTubeBase(TileEntity tileEntity, double x, double y, double z) {
        if (!(tileEntity instanceof IEssentiaTransport)) return;
        IEssentiaTransport tube = (IEssentiaTransport) tileEntity;
        int metadata = tileEntity.getBlockMetadata();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

        RenderHelper.disableStandardItemLighting();

        int light = 0;
        for (EnumFacing face : EnumFacing.VALUES) {
            light = Math.max(light, tileEntity.getWorld().getCombinedLight(tileEntity.getPos().offset(face), 0));
        }
        int lx = light % 65536;
        int ly = light / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        TextureAtlasSprite spritePipe1 = getSprite(TEX_PIPE1);
        TextureAtlasSprite spritePipe2 = getSprite(TEX_PIPE2);
        TextureAtlasSprite spritePipe3 = getSprite(TEX_PIPE3);

        // Determine which texture to use for the tube arms
        TextureAtlasSprite armSprite = spritePipe1;
        if (metadata == 5) { // restrict
            armSprite = getSprite(TEX_RESTRICT);
        }

        // Calculate connections
        boolean[] connected = new boolean[6];
        boolean[] extended = new boolean[6];
        boolean notConduit = false;
        int connectionCount = 0;

        for (int side = 0; side < 6; side++) {
            EnumFacing face = EnumFacing.byIndex(side);
            if (tube.isConnectable(face)) {
                TileEntity te = getConnectableTile(tileEntity, face);
                if (te != null && (metadata == 4 || !(te instanceof TileBellows))) {
                    connected[side] = true;
                    connectionCount++;
                    if (!(te instanceof TileTube)) {
                        notConduit = true;
                    }
                    if (te instanceof IEssentiaTransport && ((IEssentiaTransport) te).renderExtendedTube()) {
                        extended[side] = true;
                    }
                }
            }
        }

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Draw tube arms for each axis
        // X axis (west=4, east=5)
        if (connected[4] || connected[5]) {
            float minx = connected[4] ? (extended[4] ? -W6 : 0.0F) : W7;
            float maxx = connected[5] ? (extended[5] ? 1.0F + W6 : 1.0F) : W9;
            drawBox(buf, tess, armSprite, minx, W7, W7, maxx, W9, W9);
        }
        // Y axis (down=0, up=1)
        if (connected[0] || connected[1]) {
            float miny = connected[0] ? (extended[0] ? -W6 : 0.0F) : W7;
            float maxy = connected[1] ? (extended[1] ? 1.0F + W6 : 1.0F) : W9;
            drawBox(buf, tess, armSprite, W7, miny, W7, W9, maxy, W9);
        }
        // Z axis (north=2, south=3)
        if (connected[2] || connected[3]) {
            float minz = connected[2] ? (extended[2] ? -W6 : 0.0F) : W7;
            float maxz = connected[3] ? (extended[3] ? 1.0F + W6 : 1.0F) : W9;
            drawBox(buf, tess, armSprite, W7, W7, minz, W9, W9, maxz);
        }

        // Draw center core
        if (metadata == 3) {
            // Filter: outer shell + colored inner core
            float s = 0.03125F;
            drawBox(buf, tess, getSprite(TEX_FILTER), W6 - s, W6 - s, W6 - s, W10 + s, W10 + s, W10 + s);
            // Draw colored core
            float r = 1.0F, g = 1.0F, b = 1.0F;
            if (tube instanceof TileTubeFilter && ((TileTubeFilter) tube).aspectFilter != null) {
                Aspect aspect = ((TileTubeFilter) tube).aspectFilter;
                Color c = new Color(aspect.getColor());
                r = c.getRed() / 255.0F;
                g = c.getGreen() / 255.0F;
                b = c.getBlue() / 255.0F;
            }
            GlStateManager.color(r, g, b, 1.0F);
            drawBox(buf, tess, getSprite(TEX_FILTER_CORE), W6 - s, W6 - s, W6 - s, W10 + s, W10 + s, W10 + s);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } else if (metadata == 4) {
            // Buffer: larger core cube
            drawBox(buf, tess, armSprite, W4, W4, W4, W12, W12, W12);
        } else if (connectionCount > 0 && !notConduit && metadata != 1) {
            // Regular conduit-only core (small inner cube)
            float s = 0.03125F;
            TextureAtlasSprite coreSprite = (metadata == 5) ? armSprite : spritePipe3;
            drawBox(buf, tess, coreSprite, W7 - s, W7 - s, W7 - s, W9 + s, W9 + s, W9 + s);
        } else {
            // Standalone or connected to non-conduit: show larger disconnected core
            drawBox(buf, tess, spritePipe2, W6, W6, W6, W10, W10, W10);
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private static TextureAtlasSprite getSprite(ResourceLocation loc) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString());
    }

    private static TileEntity getConnectableTile(TileEntity source, EnumFacing face) {
        TileEntity te = source.getWorld().getTileEntity(source.getPos().offset(face));
        if (te instanceof IEssentiaTransport && ((IEssentiaTransport) te).isConnectable(face.getOpposite())) {
            return te;
        }
        if (te instanceof TileBellows && ((TileBellows) te).orientation == face.getOpposite().ordinal()) {
            return te;
        }
        return null;
    }

    static void drawBox(BufferBuilder buf, Tessellator tess, TextureAtlasSprite sprite,
                         float x1, float y1, float z1, float x2, float y2, float z2) {
        float yU0 = sprite.getInterpolatedU(x1 * 16.0F);
        float yU1 = sprite.getInterpolatedU(x2 * 16.0F);
        float yV0 = sprite.getInterpolatedV(z1 * 16.0F);
        float yV1 = sprite.getInterpolatedV(z2 * 16.0F);
        float xU0 = sprite.getInterpolatedU(z1 * 16.0F);
        float xU1 = sprite.getInterpolatedU(z2 * 16.0F);
        float xV0 = sprite.getInterpolatedV((1.0F - y2) * 16.0F);
        float xV1 = sprite.getInterpolatedV((1.0F - y1) * 16.0F);
        float zU0 = sprite.getInterpolatedU(x1 * 16.0F);
        float zU1 = sprite.getInterpolatedU(x2 * 16.0F);
        float zV0 = sprite.getInterpolatedV((1.0F - y2) * 16.0F);
        float zV1 = sprite.getInterpolatedV((1.0F - y1) * 16.0F);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        // Bottom
        buf.pos(x1, y1, z2).tex(yU0, yV1).endVertex();
        buf.pos(x1, y1, z1).tex(yU0, yV0).endVertex();
        buf.pos(x2, y1, z1).tex(yU1, yV0).endVertex();
        buf.pos(x2, y1, z2).tex(yU1, yV1).endVertex();
        // Top
        buf.pos(x1, y2, z1).tex(yU0, yV0).endVertex();
        buf.pos(x1, y2, z2).tex(yU0, yV1).endVertex();
        buf.pos(x2, y2, z2).tex(yU1, yV1).endVertex();
        buf.pos(x2, y2, z1).tex(yU1, yV0).endVertex();
        // North
        buf.pos(x2, y2, z1).tex(zU1, zV0).endVertex();
        buf.pos(x2, y1, z1).tex(zU1, zV1).endVertex();
        buf.pos(x1, y1, z1).tex(zU0, zV1).endVertex();
        buf.pos(x1, y2, z1).tex(zU0, zV0).endVertex();
        // South
        buf.pos(x1, y2, z2).tex(zU0, zV0).endVertex();
        buf.pos(x1, y1, z2).tex(zU0, zV1).endVertex();
        buf.pos(x2, y1, z2).tex(zU1, zV1).endVertex();
        buf.pos(x2, y2, z2).tex(zU1, zV0).endVertex();
        // West
        buf.pos(x1, y2, z1).tex(xU0, xV0).endVertex();
        buf.pos(x1, y1, z1).tex(xU0, xV1).endVertex();
        buf.pos(x1, y1, z2).tex(xU1, xV1).endVertex();
        buf.pos(x1, y2, z2).tex(xU1, xV0).endVertex();
        // East
        buf.pos(x2, y2, z2).tex(xU1, xV0).endVertex();
        buf.pos(x2, y1, z2).tex(xU1, xV1).endVertex();
        buf.pos(x2, y1, z1).tex(xU0, xV1).endVertex();
        buf.pos(x2, y2, z1).tex(xU0, xV0).endVertex();
        tess.draw();
    }
}
