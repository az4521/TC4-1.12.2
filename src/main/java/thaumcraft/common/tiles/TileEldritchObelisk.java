package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.utils.EntityUtils;

public class TileEldritchObelisk extends TileThaumcraft {
   private int counter = 0;

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 5, this.getPos().getZ() + 1);
   }

   public double getMaxRenderDistanceSquared() {
      return 9216.0F;
   }

   public void updateEntity() {
      if (!this.world.isRemote && this.counter % 20 == 0) {
         ArrayList<Entity> list = EntityUtils.getEntitiesInRange(this.getWorld(), (double)this.getPos().getX() + (double)0.5F, this.getPos().getY(), (double)this.getPos().getZ() + (double)0.5F, null, EntityLivingBase.class, 6.0F);
         if (list != null && !list.isEmpty()) {
            for(Entity e : list) {
               if (e instanceof IEldritchMob && e instanceof EntityLivingBase && !((EntityLivingBase)e).isPotionActive(MobEffects.REGENERATION)) {
                  try {
                     ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 40, 0, true, true));
                     ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 40, 0, true, true));
                  } catch (Exception ignored) {
                  }
               }
            }
         }
      }

      if (this.world.isRemote) {
         ArrayList<Entity> list = EntityUtils.getEntitiesInRange(this.getWorld(), (double)this.getPos().getX() + (double)0.5F, this.getPos().getY(), (double)this.getPos().getZ() + (double)0.5F, null, EntityLivingBase.class, 6.0F);
         if (list != null && !list.isEmpty()) {
            for(Entity e : list) {
               if (e instanceof IEldritchMob && e instanceof EntityLivingBase) {
                  Thaumcraft.proxy.wispFX4(this.getWorld(), (double)this.getPos().getX() + (double)0.5F, (float)(this.getPos().getY() + 1) + this.world.rand.nextFloat() * 3.0F, (double)this.getPos().getZ() + (double)0.5F, e, 5, true, 1.0F);
               }
            }
         }
      }

   }
}
