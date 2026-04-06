package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.entities.ModelPech;
import thaumcraft.common.entities.monster.EntityPech;
import net.minecraft.client.renderer.entity.RenderManager;

@SideOnly(Side.CLIENT)
public class RenderPech extends RenderLiving<EntityPech> {
   protected ModelPech modelMain;
   protected ModelPech modelOverlay;
   private static final ResourceLocation[] skin = new ResourceLocation[]{
      new ResourceLocation("thaumcraft", "textures/models/pech_forage.png"),
      new ResourceLocation("thaumcraft", "textures/models/pech_thaum.png"),
      new ResourceLocation("thaumcraft", "textures/models/pech_stalker.png")
   };

   public RenderPech(RenderManager renderManager, ModelPech par1ModelBiped, float par2) {
      super(renderManager, par1ModelBiped, par2);
      this.modelMain = par1ModelBiped;
      this.modelOverlay = new ModelPech();
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityPech entity) {
      return skin[entity.getPechType()];
   }
}
