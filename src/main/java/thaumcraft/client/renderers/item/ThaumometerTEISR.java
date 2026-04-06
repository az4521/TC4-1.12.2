package thaumcraft.client.renderers.item;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

public class ThaumometerTEISR extends TileEntityItemStackRenderer {
    private IModelCustom model;
    private static final ResourceLocation SCANNER_MODEL = new ResourceLocation("thaumcraft", "textures/models/scanner.obj");

    private void ensureModel() {
        if (model == null) {
            model = AdvancedModelLoader.loadModel(SCANNER_MODEL);
        }
    }

    @Override
    public void renderByItem(ItemStack stack) {
        ensureModel();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.translate(1.0F, 0.5F, 1.0F);
        GlStateManager.rotate(60.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 0.0F, -1.0F);
        GlStateManager.rotate(248.0F, 0.0F, -1.0F, 0.0F);
        UtilsFX.bindTexture("textures/models/scanner.png");
        model.renderAll();
        GlStateManager.popMatrix();
    }
}
