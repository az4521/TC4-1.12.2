package thaumcraft.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Arrays;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.container.ContainerDeconstructionTable;
import thaumcraft.common.tiles.TileDeconstructionTable;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class GuiDeconstructionTable extends GuiContainer {
   private TileDeconstructionTable tableInventory;

   public GuiDeconstructionTable(InventoryPlayer par1InventoryPlayer, TileDeconstructionTable par2TileEntityFurnace) {
      super(new ContainerDeconstructionTable(par1InventoryPlayer, par2TileEntityFurnace));
      this.tableInventory = par2TileEntityFurnace;
   }

   protected void drawGuiContainerForegroundLayer(int par1, int par2) {
   }

   protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.bindTexture("textures/gui/gui_decontable.png");
      int k = (this.width - this.xSize) / 2;
      int l = (this.height - this.ySize) / 2;
      GlStateManager.enableBlend();
      this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
      if (this.tableInventory.breaktime > 0) {
         int i1 = this.tableInventory.getBreakTimeScaled(46);
         this.drawTexturedModalRect(k + 93, l + 15 + 46 - i1, 176, 46 - i1, 9, i1);
      }

      if (this.tableInventory.aspect != null) {
         UtilsFX.drawTag(k + 64, l + 48, this.tableInventory.aspect, 0.0F, 0, this.zLevel);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         RenderHelper.disableStandardItemLighting();
         int var7 = par2 - (k + 64);
         int var8 = par3 - (l + 48);
         if (var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
            UtilsFX.drawCustomTooltip(this, itemRender, this.fontRenderer, Arrays.asList(this.tableInventory.aspect.getName(), this.tableInventory.aspect.getLocalizedDescription()), par2, par3 - 8, 11);
            return;
         }
      }

      GlStateManager.disableBlend();
   }

   protected void mouseClicked(int mx, int my, int par3) throws java.io.IOException {
      super.mouseClicked(mx, my, par3);
      int gx = (this.width - this.xSize) / 2;
      int gy = (this.height - this.ySize) / 2;
      int var7 = mx - (gx + 64);
      int var8 = my - (gy + 48);
      if (var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16 && this.tableInventory.aspect != null) {
         this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 1);
         this.playButtonAspect();
      }
   }

   private void playButtonAspect() {
      { net.minecraft.entity.Entity _rve = this.mc.getRenderViewEntity(); if (_rve != null) { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:hhoff")); if (_snd != null) _rve.world.playSound(null, new net.minecraft.util.math.BlockPos(_rve.posX, _rve.posY, _rve.posZ), _snd, net.minecraft.util.SoundCategory.PLAYERS, 0.2F, 1.0F + _rve.world.rand.nextFloat() * 0.1F); } }
   }
}
