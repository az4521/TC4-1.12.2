package thaumcraft.client.renderers.models.entities;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.golems.EntityTravelingTrunk;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class ModelTrunk extends ModelBase {
   public ModelRenderer chestLid = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
   public ModelRenderer chestBelow;
   public ModelRenderer chestKnob;

   public ModelTrunk() {
      this.textureWidth = 64;
      this.textureHeight = 64;
      this.chestLid.addBox(0.0F, -5.0F, -14.0F, 14, 5, 14, 0.0F);
      this.chestLid.rotationPointX = 1.0F;
      this.chestLid.rotationPointY = 7.0F;
      this.chestLid.rotationPointZ = 15.0F;
      this.chestKnob = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
      this.chestKnob.addBox(-1.0F, -2.0F, -15.0F, 2, 4, 1, 0.0F);
      this.chestKnob.rotationPointX = 8.0F;
      this.chestKnob.rotationPointY = 7.0F;
      this.chestKnob.rotationPointZ = 15.0F;
      this.chestBelow = (new ModelRenderer(this, 0, 19)).setTextureSize(64, 64);
      this.chestBelow.addBox(0.0F, 0.0F, 0.0F, 14, 10, 14, 0.0F);
      this.chestBelow.rotationPointX = 1.0F;
      this.chestBelow.rotationPointY = 6.0F;
      this.chestBelow.rotationPointZ = 1.0F;
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.chestKnob.rotateAngleX = this.chestLid.rotateAngleX;
      this.chestLid.render(f5);
      this.chestBelow.render(f5);
      this.chestKnob.render(f5);
   }
}
