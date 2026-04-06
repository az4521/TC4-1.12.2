package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.HashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.blocks.BlockCosmeticOpaque;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.foci.ItemFocusWarding;
import thaumcraft.common.tiles.TileWarded;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class TileWardedRenderer extends TileEntitySpecialRenderer<TileWarded> {
   static HashMap<WorldCoordinates,TextureAtlasSprite> iconCache = new HashMap<>();

   @Override
   public void render(TileWarded tile, double x, double y, double z, float f, int destroyStage, float alpha) {
      Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
      if (!(viewEntity instanceof EntityPlayer)) {
         return;
      }
      EntityPlayer player = (EntityPlayer) viewEntity;
      ItemStack heldStack = player.getHeldItemMainhand();
      if (!heldStack.isEmpty() && heldStack.getItem() instanceof ItemWandCasting) {
         ItemWandCasting wand = (ItemWandCasting) heldStack.getItem();
         if (wand.getFocus(heldStack) instanceof ItemFocusWarding) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 1);
            GlStateManager.alphaFunc(516, 0.003921569F);
            GlStateManager.translate((float)x + 0.5F, (float)y, (float)z + 0.5F);
            World world = tile.getWorld();
            RenderBlocks renderBlocks = new RenderBlocks();
            GlStateManager.disableLighting();
            Tessellator t = Tessellator.getInstance();
            BufferBuilder buf = t.getBuffer();
            renderBlocks.setRenderBounds(-0.001F, -0.001F, -0.001F, 1.001F, 1.001F, 1.001F);
            int tileX = tile.getPos().getX();
            int tileY = tile.getPos().getY();
            int tileZ = tile.getPos().getZ();
            if (tile.owner == player.getName().hashCode()) {
               float r = MathHelper.sin((float)player.ticksExisted / 2.0F + (float)tileX) * 0.2F + 0.8F;
               float g = MathHelper.sin((float)player.ticksExisted / 3.0F + (float)tileY) * 0.2F + 0.7F;
               float b = MathHelper.sin((float)player.ticksExisted / 4.0F + (float)tileZ) * 0.2F + 0.28F;
               GlStateManager.color(r, g, b, 0.5F);
            } else {
               float r = MathHelper.sin((float)player.ticksExisted / 2.0F + (float)tileX) * 0.2F + 0.8F;
               float g = MathHelper.sin((float)player.ticksExisted / 3.0F + (float)tileY) * 0.2F + 0.28F;
               float b = MathHelper.sin((float)player.ticksExisted / 4.0F + (float)tileZ) * 0.2F + 0.28F;
               GlStateManager.color(r, g, b, 0.25F);
            }

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
           
            this.rendererDispatcher.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GL11.glTexEnvi(8960, 8704, 260);
            if (this.shouldSideBeRendered(world, tileX, tileY, tileZ, 1)) {
               renderBlocks.renderFaceYNeg(ConfigBlocks.blockJar, -0.5001, 0.0F, -0.5001, this.getIconOnSide(world, tileX, tileY, tileZ, 0, tile.owner, player.ticksExisted));
            }

            if (this.shouldSideBeRendered(world, tileX, tileY, tileZ, 0)) {
               renderBlocks.renderFaceYPos(ConfigBlocks.blockJar, -0.5001, 0.0F, -0.5001, this.getIconOnSide(world, tileX, tileY, tileZ, 1, tile.owner, player.ticksExisted));
            }

            if (this.shouldSideBeRendered(world, tileX, tileY, tileZ, 3)) {
               renderBlocks.renderFaceZNeg(ConfigBlocks.blockJar, -0.5001, 0.0F, -0.5001, this.getIconOnSide(world, tileX, tileY, tileZ, 2, tile.owner, player.ticksExisted));
            }

            if (this.shouldSideBeRendered(world, tileX, tileY, tileZ, 2)) {
               renderBlocks.renderFaceZPos(ConfigBlocks.blockJar, -0.5001, 0.0F, -0.5001, this.getIconOnSide(world, tileX, tileY, tileZ, 3, tile.owner, player.ticksExisted));
            }

            if (this.shouldSideBeRendered(world, tileX, tileY, tileZ, 5)) {
               renderBlocks.renderFaceXNeg(ConfigBlocks.blockJar, -0.5001, 0.0F, -0.5001, this.getIconOnSide(world, tileX, tileY, tileZ, 4, tile.owner, player.ticksExisted));
            }

            if (this.shouldSideBeRendered(world, tileX, tileY, tileZ, 4)) {
               renderBlocks.renderFaceXPos(ConfigBlocks.blockJar, -0.5001, 0.0F, -0.5001, this.getIconOnSide(world, tileX, tileY, tileZ, 5, tile.owner, player.ticksExisted));
            }

            t.draw();
            GL11.glTexEnvi(8960, 8704, 8448);
            GlStateManager.enableLighting();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
         }
      }
   }

   private boolean shouldSideBeRendered(World world, int x, int y, int z, int side) {
      EnumFacing facing = EnumFacing.byIndex(side);
      int nx = x + facing.getXOffset();
      int ny = y + facing.getYOffset();
      int nz = z + facing.getZOffset();
      IBlockState stateHere = world.getBlockState(new BlockPos(x, y, z));
      IBlockState stateNeighbor = world.getBlockState(new BlockPos(nx, ny, nz));
      int metaHere = stateHere.getBlock().getMetaFromState(stateHere);
      int metaNeighbor = stateNeighbor.getBlock().getMetaFromState(stateNeighbor);
      if (metaHere != metaNeighbor) {
         return true;
      } else {
         return stateNeighbor.getBlock() != ConfigBlocks.blockWarded;
      }
   }

   private boolean isConnectedBlock(World world, int x, int y, int z, int owner) {
      if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == ConfigBlocks.blockWarded) {
         TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
         if (tile instanceof TileWarded) {
            return ((TileWarded)tile).owner == owner;
         }
      }

      return false;
   }

   private TextureAtlasSprite getIconOnSide(World world, int x, int y, int z, int side, int owner, int ticks) {
      WorldCoordinates wc = new WorldCoordinates(x, y, z, side);
      TextureAtlasSprite out = iconCache.get(wc);
      if ((ticks + side) % 10 == 0 || out == null) {
         boolean[] bitMatrix = new boolean[8];
         if (side == 0 || side == 1) {
            bitMatrix[0] = this.isConnectedBlock(world, x - 1, y, z - 1, owner);
            bitMatrix[1] = this.isConnectedBlock(world, x, y, z - 1, owner);
            bitMatrix[2] = this.isConnectedBlock(world, x + 1, y, z - 1, owner);
            bitMatrix[3] = this.isConnectedBlock(world, x - 1, y, z, owner);
            bitMatrix[4] = this.isConnectedBlock(world, x + 1, y, z, owner);
            bitMatrix[5] = this.isConnectedBlock(world, x - 1, y, z + 1, owner);
            bitMatrix[6] = this.isConnectedBlock(world, x, y, z + 1, owner);
            bitMatrix[7] = this.isConnectedBlock(world, x + 1, y, z + 1, owner);
         }

         if (side == 2 || side == 3) {
            bitMatrix[0] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y + 1, z, owner);
            bitMatrix[1] = this.isConnectedBlock(world, x, y + 1, z, owner);
            bitMatrix[2] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y + 1, z, owner);
            bitMatrix[3] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y, z, owner);
            bitMatrix[4] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y, z, owner);
            bitMatrix[5] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y - 1, z, owner);
            bitMatrix[6] = this.isConnectedBlock(world, x, y - 1, z, owner);
            bitMatrix[7] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y - 1, z, owner);
         }

         if (side == 4 || side == 5) {
            bitMatrix[0] = this.isConnectedBlock(world, x, y + 1, z + (side == 5 ? 1 : -1), owner);
            bitMatrix[1] = this.isConnectedBlock(world, x, y + 1, z, owner);
            bitMatrix[2] = this.isConnectedBlock(world, x, y + 1, z + (side == 4 ? 1 : -1), owner);
            bitMatrix[3] = this.isConnectedBlock(world, x, y, z + (side == 5 ? 1 : -1), owner);
            bitMatrix[4] = this.isConnectedBlock(world, x, y, z + (side == 4 ? 1 : -1), owner);
            bitMatrix[5] = this.isConnectedBlock(world, x, y - 1, z + (side == 5 ? 1 : -1), owner);
            bitMatrix[6] = this.isConnectedBlock(world, x, y - 1, z, owner);
            bitMatrix[7] = this.isConnectedBlock(world, x, y - 1, z + (side == 4 ? 1 : -1), owner);
         }

         int idBuilder = 0;

         for(int i = 0; i <= 7; ++i) {
            idBuilder += bitMatrix[i] ? (i == 0 ? 1 : (i == 1 ? 2 : (i == 2 ? 4 : (i == 3 ? 8 : (i == 4 ? 16 : (i == 5 ? 32 : (i == 6 ? 64 : 128))))))) : 0;
         }

         TextureAtlasSprite var13;
         if (idBuilder <= 255 && idBuilder >= 0) {
            BlockCosmeticOpaque var14 = (BlockCosmeticOpaque)ConfigBlocks.blockCosmeticOpaque;
            var13 = BlockCosmeticOpaque.wardedGlassIcon[UtilsFX.connectedTextureRefByID[idBuilder]];
         } else {
            BlockCosmeticOpaque var10000 = (BlockCosmeticOpaque)ConfigBlocks.blockCosmeticOpaque;
            var13 = BlockCosmeticOpaque.wardedGlassIcon[0];
         }

         out = var13;
         iconCache.put(wc, out);
      }

      return out;
   }

}
