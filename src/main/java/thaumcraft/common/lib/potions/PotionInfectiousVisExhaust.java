package thaumcraft.common.lib.potions;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import thaumcraft.common.config.Config;

public class PotionInfectiousVisExhaust extends Potion {
   public static PotionInfectiousVisExhaust instance = null;
   private int statusIconIndex = -1;
   static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/misc/potions.png");

   public PotionInfectiousVisExhaust(boolean par2, int par3) {
      super(par2, par3);
      this.setIconIndex(0, 0);
   }

   public static void init() {
      instance.setPotionName("potion.infvisexhaust");
      instance.setIconIndex(6, 1);
      instance.setEffectiveness(0.25F);
   }

   public boolean isBadEffect() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public int getStatusIconIndex() {
      Minecraft.getMinecraft().renderEngine.bindTexture(rl);
      return super.getStatusIconIndex();
   }

   public void performEffect(EntityLivingBase target, int par2) {
      List<EntityLivingBase> targets = target.world.getEntitiesWithinAABB(EntityLivingBase.class, target.getEntityBoundingBox().expand(4.0F, 4.0F, 4.0F));
      if (!targets.isEmpty()) {
         for(EntityLivingBase e : targets) {
            if (!e.isPotionActive(Potion.getPotionById(Config.potionInfVisExhaustID))) {
               if (par2 > 0) {
                  e.addPotionEffect(new PotionEffect(Potion.getPotionById(Config.potionInfVisExhaustID), 6000, par2 - 1, false, false));
               } else {
                  e.addPotionEffect(new PotionEffect(Potion.getPotionById(Config.potionVisExhaustID), 6000, 0, false, false));
               }
            }
         }
      }

   }

   public boolean isReady(int par1, int par2) {
      return par1 % 40 == 0;
   }
}
