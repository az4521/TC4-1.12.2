package thaumcraft.client.renderers.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileEldritchNothing;


/**
 * Renders the "Portal to Nothing" block with an end-portal-style swirling void effect.
 * Based on the original 1.7.10 TileEldritchNothingRenderer.
 */
public class TileEldritchNothingRenderer extends TileEntitySpecialRenderer<TileEldritchNothing> {
    private static final double MAX_DIST_SQ = 1024.0;   // 32 blocks
    private static final double CLOSE_DIST_SQ = 256.0;  // 16 blocks
    private static final double EPS = 0.002;

    private static final String T1 = "textures/misc/tunnel.png";
    private static final String T2 = "textures/misc/particlefield.png";
    private static final String T3 = "textures/misc/particlefield32.png";

    @Override
    public boolean isGlobalRenderer(TileEldritchNothing te) {
        return false;
    }

    @Override
    public void render(TileEldritchNothing te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) return;
        BlockPos pos = te.getPos();

        net.minecraft.entity.Entity view = Minecraft.getMinecraft().getRenderViewEntity();
        double eye = view != null ? view.getEyeHeight() : 0.0;
        double cx = rendererDispatcher.entityX;
        double cy = rendererDispatcher.entityY + eye;
        double cz = rendererDispatcher.entityZ;

        double dx = pos.getX() + 0.5 - cx;
        double dy = pos.getY() + 0.5 - cy;
        double dz = pos.getZ() + 0.5 - cz;
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq > MAX_DIST_SQ) return;

        boolean inrange = distSq < CLOSE_DIST_SQ;
        int maxLayers = inrange ? 16 : 2;

        if (distSq > 64.0 && isOccluded(te.getWorld(), cx, cy, cz, pos)) return;

        GlStateManager.disableFog();
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);

        for (EnumFacing face : EnumFacing.VALUES) {
            if (!isFaceVisibleToCamera(pos, face, cx, cy, cz)) continue;
            if (isFaceOccludedByNeighbor(te.getWorld(), pos, face)) continue;
            float cu = getCameraU(face, cx, cy, cz);
            float cv = getCameraV(face, cx, cy, cz);
            renderFace(x, y, z, face, inrange, maxLayers, cu, cv);
        }

        GlStateManager.enableFog();
    }

    private boolean isFaceOccludedByNeighbor(World world, BlockPos pos, EnumFacing face) {
        BlockPos neighbor = pos.offset(face);
        net.minecraft.block.state.IBlockState state = world.getBlockState(neighbor);
        // Full opaque cubes completely hide the face.
        if (state.isOpaqueCube()) return true;
        // Another Portal-to-Nothing next to this one: skip the shared face so the
        // interior of a multi-block portal isn't cluttered.
        if (state.getBlock() instanceof thaumcraft.common.blocks.BlockEldritchNothing) return true;
        // Any block whose side fully covers ours also hides the face.
        return state.isSideSolid(world, neighbor, face.getOpposite());
    }

    private boolean isOccluded(World world, double cx, double cy, double cz, BlockPos pos) {
        Vec3d start = new Vec3d(cx, cy, cz);
        double bx = pos.getX(), by = pos.getY(), bz = pos.getZ();
        // Sample 8 corners of the block. If ANY corner is reachable without hitting
        // a different block, the block is at least partially visible.
        for (int i = 0; i < 8; i++) {
            double ex = bx + ((i & 1) == 0 ? 0.05 : 0.95);
            double ey = by + ((i & 2) == 0 ? 0.05 : 0.95);
            double ez = bz + ((i & 4) == 0 ? 0.05 : 0.95);
            RayTraceResult hit = world.rayTraceBlocks(start, new Vec3d(ex, ey, ez), false, true, false);
            if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || hit.getBlockPos().equals(pos)) {
                return false;
            }
        }
        return true;
    }

    private boolean isFaceVisibleToCamera(BlockPos pos, EnumFacing face, double cx, double cy, double cz) {
        switch (face) {
            case UP:    return cy > pos.getY() + 1;
            case DOWN:  return cy < pos.getY();
            case NORTH: return cz < pos.getZ();
            case SOUTH: return cz > pos.getZ() + 1;
            case WEST:  return cx < pos.getX();
            case EAST:  return cx > pos.getX() + 1;
        }
        return false;
    }

    private void renderFace(double x, double y, double z, EnumFacing face, boolean inrange, int maxLayers, float cu, float cv) {
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        int layers = inrange ? Math.min(maxLayers, LAYER_COUNT) : 1;
        long time = System.currentTimeMillis();

        for (int i = 0; i < layers; ++i) {
            LayerCfg cfg = LAYERS[i];
            UtilsFX.bindTexture(cfg.texture);
            GlStateManager.blendFunc(cfg.srcBlend, cfg.dstBlend);

            float scroll = (time % cfg.scrollMs) / (float)cfg.scrollMs;
            float rot = ((time % cfg.rotMs) / (float)cfg.rotMs) * 360F * cfg.rotDir;
            float parallaxU = cu * cfg.parallax;
            float parallaxV = cv * cfg.parallax;

            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            // Parallax offset first (leftmost → applied LAST to UV, so it isn't scaled)
            GlStateManager.translate(parallaxU, parallaxV, 0F);
            GlStateManager.translate(0.5F, 0.5F, 0F);
            GlStateManager.rotate(rot, 0F, 0F, 1F);
            GlStateManager.scale(cfg.scale, cfg.scale, 1F);
            GlStateManager.translate(-0.5F + scroll, -0.5F + scroll * 0.7F, 0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            drawFaceQuad(buf, x, y, z, face, cfg.r, cfg.g, cfg.b);
            tess.draw();

            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }

    private static final class LayerCfg {
        final String texture;
        final int srcBlend, dstBlend;
        final long scrollMs, rotMs;
        final float rotDir, scale, parallax, r, g, b;
        LayerCfg(String texture, int srcBlend, int dstBlend, long scrollMs, long rotMs,
                 float rotDir, float scale, float parallax, float r, float g, float b) {
            this.texture = texture; this.srcBlend = srcBlend; this.dstBlend = dstBlend;
            this.scrollMs = scrollMs; this.rotMs = rotMs; this.rotDir = rotDir;
            this.scale = scale; this.parallax = parallax;
            this.r = r; this.g = g; this.b = b;
        }
    }

    // parallax is "texture-tile fraction shifted per 1 block of camera movement" (applied
    // outside of the scale, so 0.50 means half a full texture shifts when you move 1 block).
    // Deeper layers have smaller parallax so they feel distant.
    private static final LayerCfg[] LAYERS = new LayerCfg[]{
        // Layer 0: near-black violet base (alpha blend)
        new LayerCfg(T1, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 60000L, 120000L,  1F, 1.0F, 0.05F, 0.06F, 0.02F, 0.12F),
        // Layer 1: faint distant violet stars
        new LayerCfg(T2, GL11.GL_ONE,       GL11.GL_ONE,                 40000L,  80000L, -1F, 1.5F, 0.12F, 0.30F, 0.12F, 0.50F),
        // Layer 2: magenta-purple mid-stars
        new LayerCfg(T3, GL11.GL_ONE,       GL11.GL_ONE,                 20000L,  50000L,  1F, 2.5F, 0.25F, 0.50F, 0.25F, 0.70F),
        // Layer 3: bright purple near stars
        new LayerCfg(T2, GL11.GL_ONE,       GL11.GL_ONE,                 15000L,  30000L, -1F, 4.0F, 0.45F, 0.65F, 0.40F, 0.90F),
        // Layer 4: lavender foreground sparkle
        new LayerCfg(T3, GL11.GL_ONE,       GL11.GL_ONE,                  9000L,  17000L,  1F, 6.0F, 0.70F, 0.80F, 0.60F, 1.00F),
    };
    private static final int LAYER_COUNT = LAYERS.length;

    private float getCameraU(EnumFacing face, double cx, double cy, double cz) {
        switch (face) {
            case UP:    return (float)cx;
            case DOWN:  return (float)cx;
            case NORTH: return (float)(-cx);
            case SOUTH: return (float)cx;
            case WEST:  return (float)cz;
            case EAST:  return (float)(-cz);
        }
        return 0F;
    }

    private float getCameraV(EnumFacing face, double cx, double cy, double cz) {
        switch (face) {
            case UP:    return (float)cz;
            case DOWN:  return (float)cz;
            default:    return (float)cy;
        }
    }

    // UP = top of block (y+1), DOWN = bottom (y), etc. Each quad is pushed outward by EPS
    // to avoid Z-fighting with the JSON model cube, with winding giving outward normals.
    private void drawFaceQuad(BufferBuilder buf, double x, double y, double z, EnumFacing face, float r, float g, float b) {
        switch (face) {
            case UP: {
                double yy = y + 1 + EPS;
                buf.pos(x, yy, z).tex(0, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(x, yy, z + 1).tex(0, 1).color(r, g, b, 1.0F).endVertex();
                buf.pos(x + 1, yy, z + 1).tex(1, 1).color(r, g, b, 1.0F).endVertex();
                buf.pos(x + 1, yy, z).tex(1, 0).color(r, g, b, 1.0F).endVertex();
                break;
            }
            case DOWN: {
                double yy = y - EPS;
                buf.pos(x, yy, z + 1).tex(0, 1).color(r, g, b, 1.0F).endVertex();
                buf.pos(x, yy, z).tex(0, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(x + 1, yy, z).tex(1, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(x + 1, yy, z + 1).tex(1, 1).color(r, g, b, 1.0F).endVertex();
                break;
            }
            case NORTH: {
                double zz = z - EPS;
                buf.pos(x + 1, y, zz).tex(0, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(x, y, zz).tex(1, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(x, y + 1, zz).tex(1, 1).color(r, g, b, 1.0F).endVertex();
                buf.pos(x + 1, y + 1, zz).tex(0, 1).color(r, g, b, 1.0F).endVertex();
                break;
            }
            case SOUTH: {
                double zz = z + 1 + EPS;
                buf.pos(x, y, zz).tex(0, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(x + 1, y, zz).tex(1, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(x + 1, y + 1, zz).tex(1, 1).color(r, g, b, 1.0F).endVertex();
                buf.pos(x, y + 1, zz).tex(0, 1).color(r, g, b, 1.0F).endVertex();
                break;
            }
            case WEST: {
                double xx = x - EPS;
                buf.pos(xx, y, z).tex(0, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(xx, y, z + 1).tex(1, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(xx, y + 1, z + 1).tex(1, 1).color(r, g, b, 1.0F).endVertex();
                buf.pos(xx, y + 1, z).tex(0, 1).color(r, g, b, 1.0F).endVertex();
                break;
            }
            case EAST: {
                double xx = x + 1 + EPS;
                buf.pos(xx, y, z + 1).tex(0, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(xx, y, z).tex(1, 0).color(r, g, b, 1.0F).endVertex();
                buf.pos(xx, y + 1, z).tex(1, 1).color(r, g, b, 1.0F).endVertex();
                buf.pos(xx, y + 1, z + 1).tex(0, 1).color(r, g, b, 1.0F).endVertex();
                break;
            }
        }
    }

}
