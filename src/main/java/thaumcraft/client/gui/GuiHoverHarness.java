package thaumcraft.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.container.ContainerHoverHarness;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class GuiHoverHarness extends GuiContainer {
   private int blockSlot;

   public GuiHoverHarness(InventoryPlayer par1InventoryPlayer, World world, int x, int y, int z) {
      super(new ContainerHoverHarness(par1InventoryPlayer, world, x, y, z));
      this.blockSlot = par1InventoryPlayer.currentItem;
   }

   protected void drawGuiContainerForegroundLayer(int par1, int par2) {
      UtilsFX.bindTexture("textures/gui/guihoverharness.png");
      float t = this.zLevel;
      this.zLevel = 200.0F;
      GlStateManager.enableBlend();
      this.drawTexturedModalRect(8 + this.blockSlot * 18, 142, 240, 0, 16, 16);
      GlStateManager.disableBlend();
      this.zLevel = t;
   }

   protected boolean checkHotbarKeys(int par1) {
      return false;
   }

   protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
      if (this.mc.player.inventory.mainInventory.get(this.blockSlot) == null) {
         this.mc.player.closeScreen();
      }

      UtilsFX.bindTexture("textures/gui/guihoverharness.png");
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      int var5 = (this.width - this.xSize) / 2;
      int var6 = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);
   }
}
