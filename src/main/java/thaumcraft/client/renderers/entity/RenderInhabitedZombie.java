package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderInhabitedZombie extends RenderZombie {

   public RenderInhabitedZombie(RenderManager renderManager) {
      super(renderManager);
   }
   private static final ResourceLocation t1 = new ResourceLocation("thaumcraft", "textures/models/czombie.png");
   private ModelBiped defaultModel;
   private ModelZombieVillager zombieVillagerModel;
   private int modelNumber = 1;

   protected ResourceLocation getEntityTexture(EntityZombie par1EntityZombie) {
      return t1;
   }

   // swapArmor removed — RenderZombie fields it used no longer exist in 1.12.2
}
