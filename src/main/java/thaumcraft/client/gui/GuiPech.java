package thaumcraft.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.ContainerPech;
import thaumcraft.common.entities.monster.EntityPech;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class GuiPech extends GuiContainer {
   EntityPech pech;

   public GuiPech(InventoryPlayer par1InventoryPlayer, World world, EntityPech pech) {
      super(new ContainerPech(par1InventoryPlayer, world, pech));
      this.xSize = 175;
      this.ySize = 232;
      this.pech = pech;
   }

   protected void drawGuiContainerForegroundLayer(int par1, int par2) {
   }

   protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
      UtilsFX.bindTexture("textures/gui/gui_pech.png");
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      int var5 = (this.width - this.xSize) / 2;
      int var6 = (this.height - this.ySize) / 2;
      GlStateManager.enableBlend();
      this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);
      if (this.pech.isValued(this.inventorySlots.getSlot(0).getStack()) && !this.inventorySlots.getSlot(0).getStack().isEmpty() && this.inventorySlots.getSlot(1).getStack().isEmpty() && this.inventorySlots.getSlot(2).getStack().isEmpty() && this.inventorySlots.getSlot(3).getStack().isEmpty() && this.inventorySlots.getSlot(4).getStack().isEmpty()) {
         this.drawTexturedModalRect(var5 + 67, var6 + 24, 176, 0, 25, 25);
      }

      GlStateManager.disableBlend();
   }

   protected void mouseClicked(int mx, int my, int par3) throws java.io.IOException {
      super.mouseClicked(mx, my, par3);
      int gx = (this.width - this.xSize) / 2;
      int gy = (this.height - this.ySize) / 2;
      int var7 = mx - (gx + 67);
      int var8 = my - (gy + 24);
      if (var7 >= 0 && var8 >= 0 && var7 < 25 && var8 < 25 && this.pech.isValued(this.inventorySlots.getSlot(0).getStack()) && !this.inventorySlots.getSlot(0).getStack().isEmpty() && this.inventorySlots.getSlot(1).getStack().isEmpty() && this.inventorySlots.getSlot(2).getStack().isEmpty() && this.inventorySlots.getSlot(3).getStack().isEmpty() && this.inventorySlots.getSlot(4).getStack().isEmpty()) {
         this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 0);
         this.playButton();
      }
   }

   private void playButton() {
      { net.minecraft.entity.Entity _rve = this.mc.getRenderViewEntity(); if (_rve != null) { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:pech_dice")); if (_snd != null) _rve.world.playSound(null, new net.minecraft.util.math.BlockPos(_rve.posX, _rve.posY, _rve.posZ), _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, 0.95F + _rve.world.rand.nextFloat() * 0.1F); } }
   }
}
