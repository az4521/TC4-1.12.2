package thaumcraft.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class GuiMagicBox extends GuiContainer {
   private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
   private IInventory playerInventory;
   private IInventory lowerChestInventory;
   private int inventoryRows;

   public GuiMagicBox(IInventory par1IInventory, IInventory par2IInventory) {
      super(new ContainerChest(par1IInventory, par2IInventory, net.minecraft.client.Minecraft.getMinecraft().player));
      this.playerInventory = par1IInventory;
      this.lowerChestInventory = par2IInventory;
      this.allowUserInput = false;
      short short1 = 222;
      int i = short1 - 108;
      this.inventoryRows = par2IInventory.getSizeInventory() / 9;
      this.ySize = i + 54;
   }

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      this.fontRenderer.drawString(this.lowerChestInventory.hasCustomName() ? this.lowerChestInventory.getName() : I18n.format(this.lowerChestInventory.getName()), 8, 6, 4210752);
      this.fontRenderer.drawString(this.playerInventory.hasCustomName() ? this.playerInventory.getName() : I18n.format(this.playerInventory.getName()), 8, this.ySize - 96 + 2, 4210752);
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
      int k = (this.width - this.xSize) / 2;
      int l = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(k, l, 0, 0, this.xSize, 71);
      this.drawTexturedModalRect(k, l + 54 + 17, 0, 126, this.xSize, 96);
   }
}
