package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.potions.PotionInfectiousVisExhaust;

import java.util.Random;

public class BlockFluxGas extends BlockFluidFinite {
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite iconStill;
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite iconFlow;

   public BlockFluxGas() {
      super(ConfigBlocks.FLUXGAS, Config.fluxGoomaterial);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.densityDir = 1;
   }

   public int getDensityDir() {
      return this.densityDir;
   }

   public int getRenderType() {
      return ConfigBlocks.blockFluxGasRI;
   }

   public void setDensityDir(int densityDir) {
      this.densityDir = densityDir;
   }

   public int getQuanta() {
      return this.quantaPerBlock;
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      int md = this.getQuantaValue(world, pos);
      if (world.rand.nextInt(10) == 0 && entity instanceof EntityLivingBase
            && !(entity instanceof ITaintedMob)
            && !((EntityLivingBase)entity).isEntityUndead()
            && !((EntityLivingBase)entity).isPotionActive(PotionInfectiousVisExhaust.instance)
            && !((EntityLivingBase)entity).isPotionActive(MobEffects.NAUSEA)) {
         if (world.rand.nextBoolean()) {
            PotionEffect pe = new PotionEffect(PotionInfectiousVisExhaust.instance, 1200, md / 3, true, false);
            pe.getCurativeItems().clear();
            ((EntityLivingBase)entity).addPotionEffect(pe);
         } else {
            ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80 + md * 20, 0, false, false));
         }

         if (md > 0) {
            world.setBlockToAir(pos); // placeholder -- reduce quanta level when block state is properly wired
         } else {
            world.setBlockToAir(pos);
         }
      }
   }

   @Override
   public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      super.updateTick(worldIn, pos, state, rand);
   }

   public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
      int meta = world.getBlockState(new BlockPos(x, y, z)).getBlock() == this
            ? this.getQuantaValue(world, new BlockPos(x, y, z)) : 0;
      return meta < 2;
   }

   public boolean isInsideOfMaterial(World world, Entity entity) {
      double d0 = entity.posY + (double)entity.getEyeHeight();
      int i = MathHelper.floor(entity.posX);
      int j = MathHelper.floor(d0);
      int k = MathHelper.floor(entity.posZ);
      BlockPos bp = new BlockPos(i, j, k);
      IBlockState bs = world.getBlockState(bp);
      if (bs.getMaterial() == this.getMaterial(this.getDefaultState())) {
         float f = this.getQuantaPercentage(world, bp) - 0.11111111F;
         float f1 = (float)(j + 1) - f;
         return d0 < (double)f1;
      } else {
         return false;
      }
   }
}
